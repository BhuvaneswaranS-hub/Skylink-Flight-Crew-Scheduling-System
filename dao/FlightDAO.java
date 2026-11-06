package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Flight;
import util.DatabaseConnection;

public class FlightDAO {
    private Flight map(ResultSet r) throws SQLException {
        Flight f = new Flight(
                r.getInt("flight_id"),
                r.getString("flight_number"),
                r.getString("source"),
                r.getString("destination"),
                r.getTimestamp("departure_time").toLocalDateTime(),
                r.getTimestamp("arrival_time").toLocalDateTime()
        );
        f.setSourceZoneId(r.getString("source_zone_id"));
        f.setDestinationZoneId(r.getString("destination_zone_id"));
        return f;
    }

    public boolean addFlight(Flight f) throws SQLException {
        String s = "INSERT INTO flight(flight_number, source, destination, departure_time, arrival_time, source_zone_id, destination_zone_id) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(s)) {
            p.setString(1, f.getFlightNumber());
            p.setString(2, f.getSource());
            p.setString(3, f.getDestination());
            p.setTimestamp(4, Timestamp.valueOf(f.getDepartureTime()));
            p.setTimestamp(5, Timestamp.valueOf(f.getArrivalTime()));
            p.setString(6, f.getSourceZoneId()); p.setString(7, f.getDestinationZoneId());
            return p.executeUpdate() == 1;
        }
    }

    public List<Flight> getAllFlights() throws SQLException {
        List<Flight> a = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM flight ORDER BY departure_time");
             ResultSet r = p.executeQuery()) {
            while (r.next()) a.add(map(r));
        }
        return a;
    }

    public Flight getFlightById(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM flight WHERE flight_id=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public boolean updateFlight(Flight f) throws SQLException {
        String s = "UPDATE flight SET flight_number=?, source=?, destination=?, departure_time=?, arrival_time=?, source_zone_id=?, destination_zone_id=? WHERE flight_id=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(s)) {
            p.setString(1, f.getFlightNumber());
            p.setString(2, f.getSource());
            p.setString(3, f.getDestination());
            p.setTimestamp(4, Timestamp.valueOf(f.getDepartureTime()));
            p.setTimestamp(5, Timestamp.valueOf(f.getArrivalTime()));
            p.setString(6, f.getSourceZoneId()); p.setString(7, f.getDestinationZoneId());
            p.setInt(8, f.getFlightId());
            return p.executeUpdate() == 1;
        }
    }

    public boolean deleteFlight(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM flight WHERE flight_id=?")) {
            p.setInt(1, id);
            return p.executeUpdate() == 1;
        }
    }

    public List<Flight> getUnassignedFlights() throws SQLException {
        List<Flight> a = new ArrayList<>();
        String s = "SELECT f.* FROM flight f LEFT JOIN assignment a ON f.flight_id=a.flight_id WHERE a.assignment_id IS NULL ORDER BY f.departure_time";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(s);
             ResultSet r = p.executeQuery()) {
            while (r.next()) a.add(map(r));
        }
        return a;
    }
}
