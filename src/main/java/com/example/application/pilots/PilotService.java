package com.example.application.pilots;

import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PilotService {
    private final DataSource dataSource;

    @Autowired
    public PilotService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    public Pilot findPilot(Long id){
        Pilot pilot = new Pilot();
        try (Connection conn = dataSource.getConnection()){
            String sql = "SELECT * FROM pilots WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                pilot.setId(id);
                pilot.name = rs.getString("name");
                pilot.setLicenseNumber(rs.getString("license_number"));
                pilot.isFlying = rs.getBoolean("isFlying");
                return pilot;
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
