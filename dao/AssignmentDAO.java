package dao;

import java.sql.*;
import java.util.*;
import model.Assignment;
import util.DatabaseConnection;
import java.time.*;

public class AssignmentDAO {
    public void validateMandatoryRest(int crewId, int nextFlightId) throws SQLException {
        LocalDateTime nextDeparture, nextArrival;
        String nextSourceZone, nextDestinationZone;
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(
                "SELECT departure_time, arrival_time, source_zone_id, destination_zone_id FROM flight WHERE flight_id=?")) {
            p.setInt(1, nextFlightId);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Flight not found.");
                nextDeparture = r.getTimestamp(1).toLocalDateTime();
                nextArrival = r.getTimestamp(2).toLocalDateTime();
                nextSourceZone = r.getString(3); nextDestinationZone = r.getString(4);
            }
        }
        if (nextSourceZone == null || nextDestinationZone == null)
            throw new IllegalArgumentException("Rest compliance cannot be verified: flight time-zone information is missing.");
        ZoneId nextZone;
        try { nextZone = ZoneId.of(nextSourceZone); ZoneId.of(nextDestinationZone); }
        catch (DateTimeException e) { throw new IllegalArgumentException("Rest compliance cannot be verified: flight has an invalid time-zone ID."); }
        Instant nextStart = nextDeparture.atZone(nextZone).toInstant();
        Instant nextEnd = nextArrival.atZone(ZoneId.of(nextDestinationZone)).toInstant();
        Instant latestArrival = null;
        Duration previousDuty = null;
        boolean crossedZones = !nextSourceZone.equals(nextDestinationZone);
        String sql = "SELECT f.departure_time, f.arrival_time, f.source_zone_id, f.destination_zone_id FROM assignment a JOIN flight f ON f.flight_id=a.flight_id WHERE a.crew_id=? AND a.flight_id<>?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, crewId); p.setInt(2, nextFlightId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    String source = r.getString(3), destination = r.getString(4);
                    if (source == null || destination == null)
                        throw new IllegalArgumentException("Rest compliance cannot be verified: a previous assigned flight has missing time-zone information.");
                    try {
                        ZoneId from = ZoneId.of(source), to = ZoneId.of(destination);
                        Instant start = r.getTimestamp(1).toLocalDateTime().atZone(from).toInstant();
                        Instant end = r.getTimestamp(2).toLocalDateTime().atZone(to).toInstant();
                        if (!end.isAfter(start)) throw new IllegalArgumentException("Rest compliance cannot be verified: flight timestamps produce an invalid elapsed duty period.");
                        if (start.isBefore(nextEnd) && end.isAfter(nextStart))
                            throw new IllegalArgumentException("Schedule Conflict: This crew member is already assigned to a flight overlapping this flight.");
                        if (!end.isAfter(nextStart) && (latestArrival == null || end.isAfter(latestArrival))) {
                            latestArrival = end; previousDuty = Duration.between(start, end);
                            crossedZones = crossedZones || !source.equals(destination);
                        }
                    } catch (DateTimeException e) {
                        throw new IllegalArgumentException("Rest compliance cannot be verified: a flight has an invalid time-zone ID.");
                    }
                }
            }
        }
        if (latestArrival == null) return;
        Duration available = Duration.between(latestArrival, nextStart);
        long dutyHours = (previousDuty.toSeconds() + 3599) / 3600;
        long requiredHours = Math.max(12, dutyHours);
        if (crossedZones) {
            int configured = Integer.getInteger("crew.rest.multipleZoneMinimumHours", -1);
            if (configured < 12 || configured > 36)
                throw new IllegalArgumentException("Rest compliance cannot be verified for a multi-time-zone duty: configure crew.rest.multipleZoneMinimumHours to the applicable DGCA policy value (12–36 hours).");
            requiredHours = Math.max(requiredHours, configured);
        }
        Duration required = Duration.ofHours(requiredHours);
        if (available.compareTo(required) < 0) {
            long availableHours = available.toSeconds() / 3600;
            throw new IllegalArgumentException("Assignment Rejected! Previous duty period was " + dutyHours +
                    " hours. Minimum required rest is " + requiredHours + " hours, but only " + availableHours + " hours are available.");
        }
    }

    private Assignment map(ResultSet r) throws SQLException {
        return new Assignment(
                r.getInt("assignment_id"),
                r.getInt("crew_id"),
                r.getInt("flight_id"),
                r.getDate("assignment_date").toLocalDate()
        );
    }

    public boolean crewExists(int id) throws SQLException {
        return exists("SELECT 1 FROM crew WHERE crew_id=?", id);
    }

    public boolean crewAvailable(int id) throws SQLException {
        return exists("SELECT 1 FROM crew WHERE crew_id=? AND availability='Available'", id);
    }

    public String crewRole(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT role FROM crew WHERE crew_id=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next() ? r.getString(1) : null; }
        }
    }

    public boolean flightExists(int id) throws SQLException {
        return exists("SELECT 1 FROM flight WHERE flight_id=?", id);
    }

    private boolean exists(String sql, int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public boolean duplicate(int crew, int flight) throws SQLException {
        String s = "SELECT 1 FROM assignment WHERE crew_id=? AND flight_id=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(s)) {
            p.setInt(1, crew);
            p.setInt(2, flight);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public boolean duplicateExcluding(int crew, int flight, int assignmentId) throws SQLException {
        String s = "SELECT 1 FROM assignment WHERE crew_id=? AND flight_id=? AND assignment_id <> ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(s)) {
            p.setInt(1, crew);
            p.setInt(2, flight);
            p.setInt(3, assignmentId);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public Assignment getAssignmentById(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM assignment WHERE assignment_id=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public boolean onApprovedLeaveDuringFlight(int crew, int flight) throws SQLException {
        String s = "SELECT 1 FROM leave_record l JOIN flight f ON f.flight_id=? " +
                "WHERE l.crew_id=? AND l.status='Approved' " +
                "AND l.from_date <= DATE(f.arrival_time) AND l.to_date >= DATE(f.departure_time)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(s)) {
            p.setInt(1, flight);
            p.setInt(2, crew);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public boolean hasScheduleConflict(int crew, int flight) throws SQLException {
        String s = "SELECT 1 FROM assignment a JOIN flight oldf ON a.flight_id=oldf.flight_id " +
                "JOIN flight newf ON newf.flight_id=? " +
                "WHERE a.crew_id=? AND oldf.flight_id <> newf.flight_id " +
                "AND oldf.departure_time < newf.arrival_time AND oldf.arrival_time > newf.departure_time";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(s)) {
            p.setInt(1, flight);
            p.setInt(2, crew);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public boolean hasScheduleConflictExcluding(int crew, int flight, int assignmentId) throws SQLException {
        String s = "SELECT 1 FROM assignment a JOIN flight oldf ON a.flight_id=oldf.flight_id " +
                "JOIN flight newf ON newf.flight_id=? " +
                "WHERE a.crew_id=? AND a.assignment_id <> ? AND oldf.flight_id <> newf.flight_id " +
                "AND oldf.departure_time < newf.arrival_time AND oldf.arrival_time > newf.departure_time";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(s)) {
            p.setInt(1, flight);
            p.setInt(2, crew);
            p.setInt(3, assignmentId);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public boolean addAssignment(Assignment a) throws SQLException {
        String s = "INSERT INTO assignment(crew_id, flight_id, assignment_date) VALUES(?,?,?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(s)) {
            p.setInt(1, a.getCrewId());
            p.setInt(2, a.getFlightId());
            p.setDate(3, java.sql.Date.valueOf(a.getAssignmentDate()));
            return p.executeUpdate() == 1;
        }
    }

    public boolean updateAssignment(Assignment a) throws SQLException {
        String s = "UPDATE assignment SET crew_id=?, flight_id=?, assignment_date=? WHERE assignment_id=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(s)) {
            p.setInt(1, a.getCrewId());
            p.setInt(2, a.getFlightId());
            p.setDate(3, java.sql.Date.valueOf(a.getAssignmentDate()));
            p.setInt(4, a.getAssignmentId());
            return p.executeUpdate() == 1;
        }
    }

    public List<Assignment> getAllAssignments() throws SQLException {
        return query("SELECT * FROM assignment ORDER BY assignment_date, assignment_id", 0);
    }

    public List<Assignment> getAssignmentsByFlight(int id) throws SQLException {
        return query("SELECT * FROM assignment WHERE flight_id=? ORDER BY assignment_id", id);
    }

    private List<Assignment> query(String s, int id) throws SQLException {
        List<Assignment> a = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(s)) {
            if (id != 0) p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) a.add(map(r));
            }
        }
        return a;
    }

    public boolean deleteAssignment(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM assignment WHERE assignment_id=?")) {
            p.setInt(1, id);
            return p.executeUpdate() == 1;
        }
    }

    public List<String[]> getFlightWiseCrew() throws SQLException {
        List<String[]> a = new ArrayList<>();
        String s = "SELECT f.flight_number, c.name, c.role FROM crew c " +
                "JOIN assignment x ON c.crew_id=x.crew_id " +
                "JOIN flight f ON x.flight_id=f.flight_id " +
                "ORDER BY f.flight_number, c.name";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(s);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                a.add(new String[]{r.getString(1), r.getString(2), r.getString(3)});
            }
        }
        return a;
    }

    public List<String> getJoinedAssignments() throws SQLException {
        List<String> a = new ArrayList<>();
        String s = "SELECT x.assignment_id, c.name, c.role, f.flight_number, f.source, f.destination, x.assignment_date " +
                "FROM assignment x JOIN crew c ON x.crew_id=c.crew_id JOIN flight f ON x.flight_id=f.flight_id " +
                "ORDER BY x.assignment_date, x.assignment_id";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(s);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                a.add("Assignment " + r.getInt(1) + " | " + r.getString(2) + " (" + r.getString(3) + ") | " +
                        r.getString(4) + " | " + r.getString(5) + " -> " + r.getString(6) + " | " + r.getDate(7));
            }
        }
        return a;
    }

    public List<String[]> getCrewForFlight(int flightId) throws SQLException {
        List<String[]> a = new ArrayList<>();
        String s = "SELECT c.crew_id, c.name, c.role, x.assignment_date FROM crew c " +
                "JOIN assignment x ON c.crew_id=x.crew_id WHERE x.flight_id=? ORDER BY c.name";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(s)) {
            p.setInt(1, flightId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    a.add(new String[]{String.valueOf(r.getInt(1)), r.getString(2), r.getString(3), r.getDate(4).toString()});
                }
            }
        }
        return a;
    }
}
