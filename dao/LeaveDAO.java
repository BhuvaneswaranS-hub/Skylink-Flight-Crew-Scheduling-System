package dao;

import java.sql.*;
import java.util.*;
import model.Leave;
import util.DatabaseConnection;

public class LeaveDAO {
    private Leave map(ResultSet r) throws SQLException {
        return new Leave(
                r.getInt("leave_id"),
                r.getInt("crew_id"),
                r.getDate("from_date").toLocalDate(),
                r.getDate("to_date").toLocalDate(),
                r.getString("reason"),
                r.getString("status")
        );
    }

    public boolean crewExists(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT 1 FROM crew WHERE crew_id=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public boolean hasLeaveConflict(Leave l) throws SQLException {
        String sql = "SELECT 1 FROM leave_record WHERE crew_id=? AND leave_id<>? AND status<>'Rejected' AND from_date<=? AND to_date>=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, l.getCrewId()); p.setInt(2, l.getLeaveId());
            p.setDate(3, java.sql.Date.valueOf(l.getToDate())); p.setDate(4, java.sql.Date.valueOf(l.getFromDate()));
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        }
    }

    public boolean hasAssignmentDuringLeave(int crewId, java.time.LocalDate from, java.time.LocalDate to) throws SQLException {
        String sql = "SELECT 1 FROM assignment a JOIN flight f ON f.flight_id=a.flight_id WHERE a.crew_id=? AND DATE(f.departure_time)<=? AND DATE(f.arrival_time)>=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, crewId); p.setDate(2, java.sql.Date.valueOf(to)); p.setDate(3, java.sql.Date.valueOf(from));
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        }
    }

    public boolean addLeave(Leave l) throws SQLException {
        String s = "INSERT INTO leave_record(crew_id, from_date, to_date, reason, status) VALUES(?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(s)) {
            p.setInt(1, l.getCrewId());
            p.setDate(2, java.sql.Date.valueOf(l.getFromDate()));
            p.setDate(3, java.sql.Date.valueOf(l.getToDate()));
            p.setString(4, l.getReason());
            p.setString(5, l.getStatus());
            return p.executeUpdate() == 1;
        }
    }

    public List<Leave> getAllLeaves() throws SQLException {
        return query("SELECT * FROM leave_record ORDER BY from_date, leave_id", 0);
    }

    public Leave getLeaveById(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM leave_record WHERE leave_id=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public List<Leave> getApprovedLeaves() throws SQLException {
        return query("SELECT * FROM leave_record WHERE status='Approved' ORDER BY from_date, leave_id", 0);
    }

    public List<String> getApprovedLeaveReport() throws SQLException {
        List<String> a = new ArrayList<>();
        String s = "SELECT l.leave_id, c.name, c.role, l.from_date, l.to_date, l.reason " +
                "FROM leave_record l JOIN crew c ON c.crew_id=l.crew_id " +
                "WHERE l.status='Approved' ORDER BY l.from_date, l.leave_id";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(s);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                a.add("Leave " + r.getInt(1) + " | " + r.getString(2) + " (" + r.getString(3) + ") | " +
                        r.getDate(4) + " to " + r.getDate(5) + " | " + r.getString(6));
            }
        }
        return a;
    }

    public List<Leave> getLeavesByCrew(int id) throws SQLException {
        return query("SELECT * FROM leave_record WHERE crew_id=? ORDER BY from_date, leave_id", id);
    }

    private List<Leave> query(String s, int id) throws SQLException {
        List<Leave> a = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(s)) {
            if (id > 0) p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) a.add(map(r));
            }
        }
        return a;
    }

    public boolean updateLeave(Leave l) throws SQLException {
        String s = "UPDATE leave_record SET crew_id=?, from_date=?, to_date=?, reason=?, status=? WHERE leave_id=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(s)) {
            p.setInt(1, l.getCrewId());
            p.setDate(2, java.sql.Date.valueOf(l.getFromDate()));
            p.setDate(3, java.sql.Date.valueOf(l.getToDate()));
            p.setString(4, l.getReason());
            p.setString(5, l.getStatus());
            p.setInt(6, l.getLeaveId());
            return p.executeUpdate() == 1;
        }
    }

    public boolean updateLeaveStatus(int id, String status) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE leave_record SET status=? WHERE leave_id=?")) {
            p.setString(1, status);
            p.setInt(2, id);
            return p.executeUpdate() == 1;
        }
    }

    public boolean deleteLeave(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM leave_record WHERE leave_id=?")) {
            p.setInt(1, id);
            return p.executeUpdate() == 1;
        }
    }
}
