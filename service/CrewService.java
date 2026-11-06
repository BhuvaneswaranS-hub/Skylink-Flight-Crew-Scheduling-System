package service;

import dao.CrewDAO;
import model.CrewMember;
import java.sql.SQLException;
import java.util.List;

public class CrewService {
    private final CrewDAO dao = new CrewDAO();

    public boolean addCrew(CrewMember crew) throws SQLException {
        validateCrew(crew);
        if (dao.contactExists(crew.getEmail(), crew.getPhone(), 0))
            throw new IllegalArgumentException("Duplicate crew record: email address or phone number is already registered.");
        return dao.addCrew(crew);
    }

    public List<CrewMember> getAllCrew() throws SQLException {
        return dao.getAllCrew();
    }

    public List<CrewMember> listCrew() throws SQLException {
        return getAllCrew();
    }

    public CrewMember getCrewById(int crewId) throws SQLException {
        validateCrewId(crewId);
        return dao.getCrewById(crewId);
    }

    public CrewMember searchCrew(int crewId) throws SQLException {
        return getCrewById(crewId);
    }

    public boolean updateCrew(CrewMember crew) throws SQLException {
        if (crew == null) {
            throw new IllegalArgumentException("Crew details are required.");
        }
        validateCrewId(crew.getCrewId());
        validateCrew(crew);
        if (dao.contactExists(crew.getEmail(), crew.getPhone(), crew.getCrewId()))
            throw new IllegalArgumentException("Duplicate crew record: email address or phone number is already registered to another crew member.");
        return dao.updateCrew(crew);
    }

    public boolean deleteCrew(int crewId) throws SQLException {
        validateCrewId(crewId);
        return dao.deleteCrew(crewId);
    }

    public List<CrewMember> availableCrew() throws SQLException {
        return dao.getAvailableCrew();
    }

    public List<CrewMember> getAvailableCrew() throws SQLException {
        return availableCrew();
    }

    public List<CrewMember> crewOnApprovedLeave() throws SQLException {
        return dao.getCrewOnApprovedLeave();
    }

    public List<CrewMember> getCrewOnApprovedLeave() throws SQLException {
        return crewOnApprovedLeave();
    }

    private void validateCrew(CrewMember crew) {
        if (crew == null) {
            throw new IllegalArgumentException("Crew details are required.");
        }
        if (crew.getName() == null || crew.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (crew.getName().trim().length() > 100) {
            throw new IllegalArgumentException("Name must be 100 characters or fewer.");
        }
        crew.setName(crew.getName().trim());

        if (crew.getGender() != null) {
            String g = crew.getGender().trim();
            if (g.isEmpty()) {
                crew.setGender(null);
            } else if (!g.equalsIgnoreCase("M") && !g.equalsIgnoreCase("F") && !g.equalsIgnoreCase("Other")) {
                throw new IllegalArgumentException("Gender must be M, F, Other, or blank.");
            } else {
                crew.setGender(g.equalsIgnoreCase("other") ? "Other" : g.toUpperCase());
            }
        }

        String role = crew.getCrewType();
        if (role == null) {
            role = crew.getRole();
        }
        if (role != null) {
            role = role.trim();
        }
        if ("pilot".equalsIgnoreCase(role)) {
            crew.setRole("Pilot");
        } else if ("co-pilot".equalsIgnoreCase(role) || "copilot".equalsIgnoreCase(role)) {
            crew.setRole("Co-Pilot");
        } else if ("cabin crew".equalsIgnoreCase(role)) {
            crew.setRole("Cabin Crew");
        } else {
            throw new IllegalArgumentException("Invalid role. Choose Pilot, Co-Pilot, or Cabin Crew.");
        }

        if (crew.getPhone() == null || crew.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone cannot be empty.");
        }
        String phone = crew.getPhone().trim();
        String digits = phone.replaceAll("\\D", "");
        if (phone.length() > 15 || !phone.matches("[+0-9() -]+") || digits.length() < 7 || digits.length() > 15) {
            throw new IllegalArgumentException("Phone must contain 7 to 15 digits and only standard phone characters.");
        }
        crew.setPhone(phone);

        if (crew.getEmail() == null || crew.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        String email = crew.getEmail().trim();
        if (email.length() > 100 || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Email address is invalid.");
        }
        crew.setEmail(email);

        if (crew.getAvailability() == null || crew.getAvailability().trim().isEmpty()) {
            crew.setAvailability("Available");
        } else {
            String av = crew.getAvailability().trim();
            if ("available".equalsIgnoreCase(av)) {
                crew.setAvailability("Available");
            } else if ("unavailable".equalsIgnoreCase(av)) {
                crew.setAvailability("Unavailable");
            } else if ("on leave".equalsIgnoreCase(av)) {
                crew.setAvailability("On Leave");
            } else {
                throw new IllegalArgumentException("Availability must be Available, Unavailable, or On Leave.");
            }
        }
    }

    private void validateCrewId(int crewId) {
        if (crewId <= 0) {
            throw new IllegalArgumentException("Invalid crew ID. ID must be a positive number.");
        }
    }
}
