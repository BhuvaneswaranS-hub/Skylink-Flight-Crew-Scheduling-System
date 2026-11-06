package model;

import java.time.LocalDate;

public class Assignment {
    private int assignmentId;
    private int crewId;
    private int flightId;
    private LocalDate assignmentDate;
    public Assignment() { }
    public Assignment(int assignmentId, int crewId, int flightId, LocalDate assignmentDate) {
        this.assignmentId = assignmentId; this.crewId = crewId; this.flightId = flightId; this.assignmentDate = assignmentDate;
    }
    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int v) { assignmentId = v; }
    public int getCrewId() { return crewId; }
    public void setCrewId(int v) { crewId = v; }
    public int getFlightId() { return flightId; }
    public void setFlightId(int v) { flightId = v; }
    public LocalDate getAssignmentDate() { return assignmentDate; }
    public void setAssignmentDate(LocalDate v) { assignmentDate = v; }
    @Override public String toString() { return "Assignment{id=" + assignmentId + ", crewId=" + crewId + ", flightId=" + flightId + ", date=" + assignmentDate + "}"; }
}
