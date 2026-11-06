package model;

import java.time.LocalDate;

public class Leave {
    private int leaveId;
    private int crewId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;
    private String status;
    public Leave() { }
    public Leave(int leaveId, int crewId, LocalDate fromDate, LocalDate toDate, String reason, String status) {
        this.leaveId = leaveId; this.crewId = crewId; this.fromDate = fromDate; this.toDate = toDate; this.reason = reason; this.status = status;
    }
    public int getLeaveId() { return leaveId; }
    public void setLeaveId(int v) { leaveId = v; }
    public int getCrewId() { return crewId; }
    public void setCrewId(int v) { crewId = v; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate v) { fromDate = v; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate v) { toDate = v; }
    public String getReason() { return reason; }
    public void setReason(String v) { reason = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    @Override public String toString() { return "Leave{id=" + leaveId + ", crewId=" + crewId + ", from=" + fromDate + ", to=" + toDate + ", reason='" + reason + "', status='" + status + "'}"; }
}
