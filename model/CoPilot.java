package model;

public class CoPilot extends CrewMember {
    public CoPilot() { }
    public CoPilot(int crewId, String name, String gender, String phone, String email, String availability) {
        super(crewId, name, gender, phone, email, availability);
    }
    public CoPilot(int id, String name, String gender, String phone, String email, String availability, String role) {
        super(id, name, gender, phone, email, availability, role);
    }
    @Override public String getCrewType() { return "Co-Pilot"; }
}
