package model;

public class Pilot extends CrewMember {
    public Pilot() { }
    public Pilot(int crewId, String name, String gender, String phone, String email, String availability) {
        super(crewId, name, gender, phone, email, availability);
    }
    public Pilot(int id, String name, String gender, String phone, String email, String availability, String role) {
        super(id, name, gender, phone, email, availability, role);
    }
    @Override public String getCrewType() { return "Pilot"; }
}
