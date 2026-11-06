package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;
import util.DatabaseConnection;

public class CrewDAO {
    public boolean contactExists(String email, String phone, int excludingCrewId) throws SQLException {
        String sql = "SELECT 1 FROM crew WHERE (LOWER(email)=LOWER(?) OR phone=?) AND crew_id<>?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, email); p.setString(2, phone); p.setInt(3, excludingCrewId);
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        }
    }

    private CrewMember map(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        CrewMember crew;
        if ("Pilot".equalsIgnoreCase(role)) {
            crew = new Pilot();
        } else if ("Co-Pilot".equalsIgnoreCase(role) || "Copilot".equalsIgnoreCase(role)) {
            crew = new CoPilot();
        } else if ("Cabin Crew".equalsIgnoreCase(role)) {
            crew = new CabinCrew();
        } else {
            crew = new CrewMember();
        }
        crew.setCrewId(rs.getInt("crew_id"));
        crew.setName(rs.getString("name"));
        crew.setGender(rs.getString("gender"));
        crew.setRole(role);
        crew.setPhone(rs.getString("phone"));
        crew.setEmail(rs.getString("email"));
        crew.setAvailability(rs.getString("availability"));
        return crew;
    }

    public boolean addCrew(CrewMember c) throws SQLException {
        String sql = "INSERT INTO crew(name, gender, role, phone, email, availability) VALUES(?,?,?,?,?,?)";
        try (Connection cn = DatabaseConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getGender());
            ps.setString(3, c.getCrewType());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getAvailability());
            return ps.executeUpdate() == 1;
        }
    }

    public List<CrewMember> getAllCrew() throws SQLException {
        List<CrewMember> list = new ArrayList<>();
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM crew ORDER BY crew_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public CrewMember getCrewById(int id) throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM crew WHERE crew_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean updateCrew(CrewMember c) throws SQLException {
        String sql = "UPDATE crew SET name=?, gender=?, role=?, phone=?, email=?, availability=? WHERE crew_id=?";
        try (Connection cn = DatabaseConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getGender());
            ps.setString(3, c.getCrewType());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getAvailability());
            ps.setInt(7, c.getCrewId());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean deleteCrew(int id) throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM crew WHERE crew_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    public List<CrewMember> getAvailableCrew() throws SQLException {
        return queryCrew("SELECT c.* FROM crew c WHERE c.availability='Available' " +
                "AND NOT EXISTS (SELECT 1 FROM leave_record l WHERE l.crew_id=c.crew_id AND l.status='Approved' AND CURDATE() BETWEEN l.from_date AND l.to_date) " +
                "ORDER BY c.name");
    }

    public List<CrewMember> getCrewOnApprovedLeave() throws SQLException {
        return queryCrew("SELECT DISTINCT c.* FROM crew c JOIN leave_record l ON c.crew_id=l.crew_id " +
                "WHERE l.status='Approved' AND CURDATE() BETWEEN l.from_date AND l.to_date ORDER BY c.name");
    }

    private List<CrewMember> queryCrew(String sql) throws SQLException {
        List<CrewMember> list = new ArrayList<>();
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }
}
