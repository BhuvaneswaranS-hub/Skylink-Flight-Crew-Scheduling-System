package model;

public class CrewMember {
    private int crewId;
    private String name;
    private String gender;
    private String phone;
    private String email;
    private String availability;
    private String role;

    public CrewMember() { }

    public CrewMember(int crewId, String name, String gender, String phone, String email, String availability) {
        this.crewId = crewId; this.name = name; this.gender = gender; this.phone = phone;
        this.email = email; this.availability = availability;
    }

    public CrewMember(int crewId, String name, String gender, String phone, String email, String availability, String role) {
        this(crewId, name, gender, phone, email, availability); this.role = role;
    }

    public String getCrewType() { return role == null ? "Crew Member" : role; }
    public String getRole() { return getCrewType(); }
    public void setRole(String role) { this.role = role; }

    public int getCrewId() { return crewId; }
    public void setCrewId(int crewId) { this.crewId = crewId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    @Override public String toString() {
        return "CrewMember{id=" + crewId + ", name='" + name + "', gender='" + gender
                + "', role='" + getCrewType() + "', phone='" + phone + "', email='" + email
                + "', availability='" + availability + "'}";
    }
}
