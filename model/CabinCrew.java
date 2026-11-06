package model;

public class CabinCrew extends CrewMember {
    public CabinCrew() { }
    public CabinCrew(int crewId, String name, String gender, String phone, String email, String availability) {
        super(crewId, name, gender, phone, email, availability);
    }
    public CabinCrew(int id, String name, String gender, String phone, String email, String availability, String role) {
        super(id, name, gender, phone, email, availability, role);
    }
    @Override public String getCrewType() { return "Cabin Crew"; }
}
