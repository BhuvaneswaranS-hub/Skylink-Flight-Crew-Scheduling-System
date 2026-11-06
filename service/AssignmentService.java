package service;

import dao.AssignmentDAO;
import model.Assignment;
import java.sql.SQLException;
import java.util.List;

public class AssignmentService {
    private final AssignmentDAO dao = new AssignmentDAO();

    public boolean assignCrew(Assignment a) throws SQLException {
        if (a == null || a.getCrewId() < 1 || a.getFlightId() < 1) {
            throw new IllegalArgumentException("Crew ID and flight ID must be positive.");
        }
        if (a.getAssignmentDate() == null) {
            throw new IllegalArgumentException("Assignment date is required.");
        }
        if (!dao.crewExists(a.getCrewId())) {
            throw new IllegalArgumentException("Crew Member Not Found: Please enter an existing Crew ID.");
        }
        if (!dao.crewAvailable(a.getCrewId())) {
            throw new IllegalArgumentException("Crew member is not available.");
        }
        if (!dao.flightExists(a.getFlightId())) {
            throw new IllegalArgumentException("Flight not found.");
        }
        if (dao.duplicate(a.getCrewId(), a.getFlightId())) {
            throw new IllegalArgumentException("This crew member is already assigned to that flight.");
        }
        validateRole(dao.crewRole(a.getCrewId()));
        if (dao.onApprovedLeaveDuringFlight(a.getCrewId(), a.getFlightId())) {
            throw new IllegalArgumentException("Cannot assign crew member because they are on approved leave.");
        }
        if (dao.hasScheduleConflict(a.getCrewId(), a.getFlightId())) {
            throw new IllegalArgumentException("Schedule Conflict: This crew member is already assigned to an overlapping flight.");
        }
        dao.validateMandatoryRest(a.getCrewId(), a.getFlightId());
        return dao.addAssignment(a);
    }

    public boolean addAssignment(Assignment a) throws SQLException {
        return assignCrew(a);
    }

    public List<Assignment> viewAssignments() throws SQLException {
        return dao.getAllAssignments();
    }

    public List<Assignment> getAllAssignments() throws SQLException {
        return viewAssignments();
    }

    public Assignment assignmentById(int id) throws SQLException {
        if (id < 1) {
            throw new IllegalArgumentException("Assignment ID must be positive.");
        }
        return dao.getAssignmentById(id);
    }

    public Assignment getAssignmentById(int id) throws SQLException {
        return assignmentById(id);
    }

    public boolean updateAssignment(Assignment a) throws SQLException {
        if (a == null) {
            throw new IllegalArgumentException("Assignment details are required.");
        }
        if (a.getAssignmentId() < 1 || a.getCrewId() < 1 || a.getFlightId() < 1) {
            throw new IllegalArgumentException("Assignment ID, crew ID, and flight ID must be positive.");
        }
        if (a.getAssignmentDate() == null) {
            throw new IllegalArgumentException("Assignment date is required.");
        }
        Assignment existing = dao.getAssignmentById(a.getAssignmentId());
        if (existing == null) {
            return false;
        }
        if (!dao.crewExists(a.getCrewId())) {
            throw new IllegalArgumentException("Crew member not found.");
        }
        validateRole(dao.crewRole(a.getCrewId()));
        if (!dao.crewAvailable(a.getCrewId())) {
            throw new IllegalArgumentException("Crew member is not available.");
        }
        if (!dao.flightExists(a.getFlightId())) {
            throw new IllegalArgumentException("Flight not found.");
        }
        if (dao.duplicateExcluding(a.getCrewId(), a.getFlightId(), a.getAssignmentId())) {
            throw new IllegalArgumentException("This crew member is already assigned to that flight.");
        }
        if (dao.onApprovedLeaveDuringFlight(a.getCrewId(), a.getFlightId())) {
            throw new IllegalArgumentException("Cannot assign crew member because they are on approved leave.");
        }
        if (dao.hasScheduleConflictExcluding(a.getCrewId(), a.getFlightId(), a.getAssignmentId())) {
            throw new IllegalArgumentException("Cannot assign crew member because of a schedule conflict.");
        }
        dao.validateMandatoryRest(a.getCrewId(), a.getFlightId());
        return dao.updateAssignment(a);
    }

    public boolean removeAssignment(int id) throws SQLException {
        if (id < 1) {
            throw new IllegalArgumentException("Assignment ID must be positive.");
        }
        return dao.deleteAssignment(id);
    }

    public boolean deleteAssignment(int id) throws SQLException {
        return removeAssignment(id);
    }

    public List<String> joinedAssignments() throws SQLException {
        return dao.getJoinedAssignments();
    }

    public List<Assignment> crewForFlight(int flightId) throws SQLException {
        if (flightId < 1) {
            throw new IllegalArgumentException("Flight ID must be positive.");
        }
        return dao.getAssignmentsByFlight(flightId);
    }

    public List<Assignment> getAssignmentsByFlight(int flightId) throws SQLException {
        return crewForFlight(flightId);
    }

    public List<String[]> flightWiseCrew() throws SQLException {
        return dao.getFlightWiseCrew();
    }

    public List<String[]> crewDetailsForFlight(int flightId) throws SQLException {
        if (flightId < 1) {
            throw new IllegalArgumentException("Flight ID must be positive.");
        }
        return dao.getCrewForFlight(flightId);
    }

    private void validateRole(String role) {
        if (!"Pilot".equalsIgnoreCase(role) && !"Co-Pilot".equalsIgnoreCase(role)
                && !"Cabin Crew".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("Crew role is not eligible for flight assignment.");
        }
    }
}
