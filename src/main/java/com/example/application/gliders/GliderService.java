package com.example.application.gliders;

import org.postgresql.util.PGInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class GliderService {
    private final DataSource dataSource;
    @Autowired
    public GliderService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
//
//    private final GliderRepository gliderRepository;
//
//    GliderService(GliderRepository gliderRepository) {
//        this.gliderRepository = gliderRepository;
//    }
//
//    @Transactional
//    public void createTask(String description, @Nullable LocalDate dueDate) {
//        var task = new Glider(description, Instant.now());
//        task.setDueDate(dueDate);
//        gliderRepository.saveAndFlush(task);
//    }
//
//    @Transactional(readOnly = true)
//    public List<Glider> list(Pageable pageable) {
//        return gliderRepository.findAllBy(pageable).toList();
//    }
    public List<Glider> getAllGliders(){
        List<Glider> gliders = new ArrayList<>();
        try(
                Connection conn = dataSource.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM gliders");
        ){
            while(rs.next()){
                Glider glider = new Glider();
                glider.setId(rs.getLong("id"));
                glider.setRegistrationNumber(rs.getString("reg_number"));
                glider.setTotalFlightTime((org.postgresql.util.PGInterval)rs.getObject("total_flight_time"));
                glider.setFlightCount(rs.getInt("flight_count"));
                glider.setType(rs.getString("type"));
                glider.setNextCheckupHrs((org.postgresql.util.PGInterval)rs.getObject("next_checkup_hrs") );
                glider.setNextCheckupFlights((Integer)rs.getObject("next_checkup_flights"));
                glider.setNextCheckupDate(rs.getDate("next_checkup_deadline"));
                gliders.add(glider);
            }
        }
        catch(Exception e){
            System.out.println("Exception: " + e.getMessage());
        }
        return gliders;
    }

    public void addGlider(String regNum, PGInterval totalFlightTime, int flightCount, String type, PGInterval nextCheckupHrs, Integer nextCheckupFlights, Date nextCheckupDate){
        String sql = "INSERT INTO gliders (reg_number, total_flight_time, flight_count, type, next_checkup_hrs, next_checkup_flights, next_checkup_deadline) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(Connection conn = dataSource.getConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, regNum);
            ps.setObject(2, totalFlightTime);
            ps.setInt(3, flightCount);
            ps.setString(4, type);
            ps.setObject(5, nextCheckupHrs);
            ps.setObject(6, nextCheckupFlights, java.sql.Types.INTEGER);
            ps.setDate(7, nextCheckupDate);
            ps.execute();
        }
        catch (Exception e){
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
