package com.example.application.pilots;

import com.example.application.gliders.Glider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PilotService {
    private final DataSource dataSource;

    @Autowired
    public PilotService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    //returns pilot with specified ID
    public Pilot findPilot(Long id){
        Pilot pilot = new Pilot();
        try (Connection conn = dataSource.getConnection()){
            String sql = "SELECT * FROM pilots WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                pilot.setId(id);
                pilot.setName(rs.getString("name"));
                pilot.setLicenseNumber(rs.getString("license_number"));
                pilot.isFlying = rs.getBoolean("is_flying");
                return pilot;
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    //returns list of all pilots in DB
    public List<Pilot> getAllPilots(){
        List<Pilot> pilots = new ArrayList<>();
        try (
                Connection conn = dataSource.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM pilots");
        ) {
            while (rs.next()) {
                Pilot pilot = new Pilot();
                pilot.setId(rs.getLong("id"));
                pilot.setName(rs.getString("name"));
                pilot.setLicenseNumber(rs.getString("license_number"));
                pilot.isFlying = rs.getBoolean("is_flying");
                pilots.add(pilot);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return  pilots;
    }
    public void addPilot(String name, String licenseNumber, boolean isFlying){
        String sql = "INSERT INTO pilots (name, license_number, is_flying) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, licenseNumber);
            ps.setBoolean(3, isFlying);
            ps.execute();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
    public void deletePilot(Long id){
        String sql = "DELETE FROM pilots WHERE id=?";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement st = conn.prepareStatement(sql);
            st.setLong(1, id);
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void editPilot(Pilot pilot){
        String sql = "UPDATE pilots  SET name = ?, license_number = ?, is_flying = ? WHERE id=?";
        try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, pilot.getName());
                ps.setString(2, pilot.getLicenseNumber());
                ps.setBoolean(3, pilot.isFlying());
                ps.setLong(4, pilot.getId());
                ps.execute();
        }
        catch (SQLException e) {
                throw new RuntimeException(e);
        }
    }
}
