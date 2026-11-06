package service;

import dao.FlightDAO;
import model.Flight;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import util.TimeZoneCatalog;
import java.util.List;

public class FlightService {
    private final FlightDAO dao = new FlightDAO();

    public boolean addFlight(Flight f) throws SQLException {
        validate(f);
        return dao.addFlight(f);
    }

    public List<Flight> getAllFlights() throws SQLException {
        return dao.getAllFlights();
    }

    public List<Flight> listFlights() throws SQLException {
        return getAllFlights();
    }

    public Flight getFlightById(int id) throws SQLException {
        if (id < 1) {
            throw new IllegalArgumentException("Flight ID must be positive.");
        }
        return dao.getFlightById(id);
    }

    public Flight searchFlight(int id) throws SQLException {
        return getFlightById(id);
    }

    public boolean updateFlight(Flight f) throws SQLException {
        if (f == null) {
            throw new IllegalArgumentException("Flight details are required.");
        }
        if (f.getFlightId() < 1) {
            throw new IllegalArgumentException("Flight ID must be positive.");
        }
        validate(f);
        return dao.updateFlight(f);
    }

    public boolean deleteFlight(int id) throws SQLException {
        if (id < 1) {
            throw new IllegalArgumentException("Flight ID must be positive.");
        }
        return dao.deleteFlight(id);
    }

    public List<Flight> unassignedFlights() throws SQLException {
        return dao.getUnassignedFlights();
    }

    public List<Flight> getUnassignedFlights() throws SQLException {
        return unassignedFlights();
    }

    private void validate(Flight f) {
        if (f == null) {
            throw new IllegalArgumentException("Flight details are required.");
        }
        if (blank(f.getFlightNumber()) || blank(f.getSource()) || blank(f.getDestination())) {
            throw new IllegalArgumentException("Flight number, source, and destination cannot be empty.");
        }
        if (f.getFlightNumber().trim().length() > 20 || f.getSource().trim().length() > 50 || f.getDestination().trim().length() > 50) {
            throw new IllegalArgumentException("Flight number or airport name exceeds the database field limit.");
        }
        if (f.getSource().trim().equalsIgnoreCase(f.getDestination().trim())) {
            throw new IllegalArgumentException("Source and destination cannot be identical.");
        }
        if (f.getDepartureTime() == null || f.getArrivalTime() == null) {
            throw new IllegalArgumentException("Departure and arrival time cannot be empty.");
        }
        try { f.setSourceZoneId(TimeZoneCatalog.canonicalize(f.getSourceZoneId())); ZoneId.of(f.getSourceZoneId()); }
        catch (Exception e) { throw new IllegalArgumentException("Source time zone must be India or a valid IANA ZoneId, such as Europe/London."); }
        try { f.setDestinationZoneId(TimeZoneCatalog.canonicalize(f.getDestinationZoneId())); ZoneId.of(f.getDestinationZoneId()); }
        catch (Exception e) { throw new IllegalArgumentException("Destination time zone must be India or a valid IANA ZoneId, such as Europe/London."); }
        if (f.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Departure time cannot be in the past. Please enter a current or future date and time.");
        }
        if (!f.getArrivalTime().isAfter(f.getDepartureTime())) {
            throw new IllegalArgumentException("Arrival time must be later than departure time.");
        }
        f.setFlightNumber(f.getFlightNumber().trim());
        f.setSource(f.getSource().trim());
        f.setDestination(f.getDestination().trim());
    }

    private boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
