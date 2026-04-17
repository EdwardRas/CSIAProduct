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
    public List<Glider> getAllGliders() {
        List<Glider> gliders = new ArrayList<>();
        try (
                Connection conn = dataSource.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM gliders");
        ) {
            while (rs.next()) {
                Glider glider = new Glider();
                glider.setId(rs.getLong("id"));
                glider.setRegistrationNumber(rs.getString("reg_number"));
                glider.setTotalFlightTime((org.postgresql.util.PGInterval) rs.getObject("total_flight_time"));
                glider.setFlightCount(rs.getInt("flight_count"));
                glider.setType(rs.getString("type"));
                glider.setNextCheckupHrs((org.postgresql.util.PGInterval) rs.getObject("next_checkup_hrs"));
                glider.setNextCheckupFlights((Integer) rs.getObject("next_checkup_flights"));
                glider.setNextCheckupDate(rs.getDate("next_checkup_deadline"));
                gliders.add(glider);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return gliders;
    }

    public void addGlider(String regNum, PGInterval totalFlightTime, int flightCount, String type, PGInterval nextCheckupHrs, Integer nextCheckupFlights, Date nextCheckupDate) {
        String sql = "INSERT INTO gliders (reg_number, total_flight_time, flight_count, type, next_checkup_hrs, next_checkup_flights, next_checkup_deadline) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, regNum);
            ps.setObject(2, totalFlightTime);
            ps.setInt(3, flightCount);
            ps.setString(4, type);
            ps.setObject(5, nextCheckupHrs);
            ps.setObject(6, nextCheckupFlights, java.sql.Types.INTEGER);
            ps.setDate(7, nextCheckupDate);
            ps.execute();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    public void deleteGlider(Long id) {
        String sql = "DELETE FROM gliders WHERE id=?";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement st = conn.prepareStatement(sql);
            st.setLong(1, id);
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void editGlider(Glider glider, String regNum, PGInterval totalFlightTime, int flightCount, String type, PGInterval nextCheckupHrs, Integer nextCheckupFlights, Date nextCheckupDate) {
        if (!glider.getRegistrationNumber().equals(regNum)) {
            String sql = "UPDATE gliders SET reg_number = ? WHERE id = " + glider.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, regNum);
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (!glider.getTotalFlightTime().equals(totalFlightTime)) {
            String sql = "UPDATE gliders SET total_flight_time = ? WHERE id = " + glider.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setObject(1, totalFlightTime);
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (glider.getFlightCount() != flightCount) {
            String sql = "UPDATE gliders SET flight_count = ? WHERE id = " + glider.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, flightCount);
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (!glider.getType().equals(type)) {
            String sql = "UPDATE gliders SET type = ? WHERE id = " + glider.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, type);
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (glider.getNextCheckupHrs() != null) {
            if (!glider.getNextCheckupHrs().equals(nextCheckupHrs)) {
                String sql = "UPDATE gliders SET next_checkup_hrs = ? WHERE id = " + glider.getId();
                try (Connection conn = dataSource.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setObject(1, nextCheckupHrs);
                    ps.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (glider.getNextCheckupHrs() == null && nextCheckupHrs != null) {
            if (!glider.getNextCheckupHrs().equals(nextCheckupHrs)) {
                String sql = "UPDATE gliders SET next_checkup_hrs = ? WHERE id = " + glider.getId();
                try (Connection conn = dataSource.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setObject(1, nextCheckupHrs);
                    ps.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (glider.getNextCheckupFlights() != null) {
            if (!glider.getNextCheckupFlights().equals(nextCheckupFlights)) {
                String sql = "UPDATE gliders SET next_checkup_flights = ? WHERE id = " + glider.getId();
                try (Connection conn = dataSource.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setObject(1, nextCheckupFlights, java.sql.Types.INTEGER);
                    ps.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if(glider.getNextCheckupFlights() == null && nextCheckupFlights != null){
            String sql = "UPDATE gliders SET next_checkup_flights = ? WHERE id = " + glider.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setObject(1, nextCheckupFlights, java.sql.Types.INTEGER);
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (glider.getNextCheckupDate() != null) {
            if (!glider.getNextCheckupDate().equals(nextCheckupDate)) {
                String sql = "UPDATE gliders SET next_checkup_deadline = ? WHERE id = " + glider.getId();
                 try (Connection conn = dataSource.getConnection()) {
                     PreparedStatement ps = conn.prepareStatement(sql);
                     ps.setDate(1, nextCheckupDate);
                     ps.execute();
                    }
                 catch (SQLException e) {
                     throw new RuntimeException(e);
                 }
            }
        }
        if(glider.getNextCheckupDate() == null && nextCheckupDate != null){
            String sql = "UPDATE gliders SET next_checkup_deadline = ? WHERE id = " + glider.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setDate(1, nextCheckupDate);
                ps.execute();
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
