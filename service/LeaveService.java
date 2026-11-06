package service;

import dao.LeaveDAO;
import model.Leave;
import java.sql.SQLException;
import java.util.List;

public class LeaveService {
    private final LeaveDAO dao = new LeaveDAO();

    public boolean applyLeave(Leave leave) throws SQLException {
        validateLeave(leave);
        if (dao.hasLeaveConflict(leave)) throw new IllegalArgumentException("Leave request conflicts with an existing leave request for this crew member.");
        return dao.addLeave(leave);
    }

    public boolean addLeave(Leave leave) throws SQLException {
        return applyLeave(leave);
    }

    public List<Leave> viewLeaves() throws SQLException {
        return dao.getAllLeaves();
    }

    public List<Leave> getAllLeaves() throws SQLException {
        return viewLeaves();
    }

    public Leave leaveById(int id) throws SQLException {
        if (id < 1) {
            throw new IllegalArgumentException("Leave ID must be positive.");
        }
        return dao.getLeaveById(id);
    }

    public Leave getLeaveById(int id) throws SQLException {
        return leaveById(id);
    }

    public List<String> approvedLeaves() throws SQLException {
        return dao.getApprovedLeaveReport();
    }

    public List<Leave> getApprovedLeaves() throws SQLException {
        return dao.getApprovedLeaves();
    }

    public List<Leave> leavesByCrew(int crewId) throws SQLException {
        if (crewId < 1) {
            throw new IllegalArgumentException("Crew ID must be positive.");
        }
        return dao.getLeavesByCrew(crewId);
    }

    public List<Leave> getLeavesByCrew(int crewId) throws SQLException {
        return leavesByCrew(crewId);
    }

    public boolean updateLeave(Leave leave) throws SQLException {
        if (leave == null) {
            throw new IllegalArgumentException("Leave details are required.");
        }
        if (leave.getLeaveId() < 1) {
            throw new IllegalArgumentException("Leave ID must be positive.");
        }
        validateLeave(leave);
        if (dao.hasLeaveConflict(leave)) throw new IllegalArgumentException("Leave request conflicts with an existing leave request for this crew member.");
        if ("Approved".equals(leave.getStatus()) && dao.hasAssignmentDuringLeave(leave.getCrewId(), leave.getFromDate(), leave.getToDate())) {
            throw new IllegalArgumentException("Leave approval rejected: crew member has an assignment during the requested leave dates.");
        }
        return dao.updateLeave(leave);
    }

    public boolean updateLeaveStatus(int leaveId, String status) throws SQLException {
        if (leaveId < 1) {
            throw new IllegalArgumentException("Leave ID must be positive.");
        }
        String normalizedStatus = status == null ? "" : status.trim();
        if (!"Pending".equalsIgnoreCase(normalizedStatus)
                && !"Approved".equalsIgnoreCase(normalizedStatus)
                && !"Rejected".equalsIgnoreCase(normalizedStatus)) {
            throw new IllegalArgumentException("Status must be Pending, Approved, or Rejected.");
        }
        String normalized = normalizedStatus.substring(0, 1).toUpperCase() + normalizedStatus.substring(1).toLowerCase();
        Leave existing = dao.getLeaveById(leaveId);
        if (existing == null) return false;
        if ("Approved".equals(normalized) && dao.hasAssignmentDuringLeave(existing.getCrewId(), existing.getFromDate(), existing.getToDate())) {
            throw new IllegalArgumentException("Leave approval rejected: crew member has an assignment during the requested leave dates.");
        }
        return dao.updateLeaveStatus(leaveId, normalized);
    }

    public boolean deleteLeave(int leaveId) throws SQLException {
        if (leaveId < 1) {
            throw new IllegalArgumentException("Leave ID must be positive.");
        }
        return dao.deleteLeave(leaveId);
    }

    private void validateLeave(Leave leave) throws SQLException {
        if (leave == null) {
            throw new IllegalArgumentException("Leave details are required.");
        }
        if (leave.getCrewId() < 1) {
            throw new IllegalArgumentException("Crew ID must be positive.");
        }
        if (leave.getFromDate() == null || leave.getToDate() == null
                || leave.getFromDate().isAfter(leave.getToDate())) {
            throw new IllegalArgumentException("Leave start date must not be after its end date.");
        }
        if (leave.getReason() == null || leave.getReason().trim().isEmpty() || leave.getReason().trim().length() > 255) {
            throw new IllegalArgumentException("Reason is required and must be 255 characters or fewer.");
        }
        leave.setReason(leave.getReason().trim());
        if (!dao.crewExists(leave.getCrewId())) {
            throw new IllegalArgumentException("Crew member not found.");
        }
        if (leave.getStatus() == null || leave.getStatus().trim().isEmpty()) {
            leave.setStatus("Pending");
        } else {
            String s = leave.getStatus().trim();
            if (!"Pending".equalsIgnoreCase(s) && !"Approved".equalsIgnoreCase(s) && !"Rejected".equalsIgnoreCase(s)) {
                throw new IllegalArgumentException("Status must be Pending, Approved, or Rejected.");
            }
            leave.setStatus(s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase());
        }
    }
}
