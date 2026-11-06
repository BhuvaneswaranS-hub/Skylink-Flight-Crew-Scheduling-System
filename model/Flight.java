package model;

import java.time.LocalDateTime;

public class Flight {
    private int flightId;
    private String flightNumber;
    private String source;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String sourceZoneId;
    private String destinationZoneId;
    public Flight() { }
    public Flight(int flightId, String flightNumber, String source, String destination, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.flightId = flightId; this.flightNumber = flightNumber; this.source = source; this.destination = destination;
        this.departureTime = departureTime; this.arrivalTime = arrivalTime;
    }
    public int getFlightId() { return flightId; }
    public void setFlightId(int v) { flightId = v; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String v) { flightNumber = v; }
    public String getSource() { return source; }
    public void setSource(String v) { source = v; }
    public String getDestination() { return destination; }
    public void setDestination(String v) { destination = v; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime v) { departureTime = v; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime v) { arrivalTime = v; }
    public String getSourceZoneId() { return sourceZoneId; }
    public void setSourceZoneId(String v) { sourceZoneId = v; }
    public String getDestinationZoneId() { return destinationZoneId; }
    public void setDestinationZoneId(String v) { destinationZoneId = v; }
    @Override public String toString() { return "Flight{id=" + flightId + ", number='" + flightNumber + "', " + source + " -> " + destination + ", departure=" + departureTime + ", arrival=" + arrivalTime + "}"; }
}
