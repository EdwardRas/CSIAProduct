package com.example.application.flights;

import com.example.application.gliders.Glider;
import com.example.application.gliders.GliderService;
import com.example.application.pilots.Pilot;
import com.example.application.pilots.PilotService;
import org.postgresql.util.PGInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class FlightService {

    private final DataSource dataSource;
    private GliderService gliderService;
    private PilotService pilotService;

    @Autowired
    public FlightService(DataSource dataSource, GliderService gliderService, PilotService pilotService) {
        this.dataSource = dataSource;
        this.gliderService = gliderService;
        this.pilotService = pilotService;
    }

    public List<Flight> getAllFlights() {
        List<Flight> flights = new ArrayList<>();
        try (
                Connection conn = dataSource.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM flights");
        ) {
            while (rs.next()) {
                Flight flight = new Flight();
                flight.setId(rs.getLong("id"));
                flight.setPilot1(pilotService.findPilot(rs.getLong("pilot_1_id")));
                flight.setPilot2(pilotService.findPilot(rs.getLong("pilot_2_id")));
                flight.setGlider(gliderService.getGliderById(rs.getLong("glider_id")));
                String status = rs.getString("status");
                if (status.equals("premade")) {
                    flight.isActive = false;
                    flight.isArchival = false;
                } else if (status.equals("active")) {
                    flight.isActive = true;
                    flight.isArchival = false;
                } else if (status.equals("archival")) {
                    flight.isArchival = true;
                    flight.isActive = false;
                }
                flight.setDate(rs.getDate("date"));
                flight.setPointOfDeparture(rs.getString("point_of_departure"));
                flight.setPointOfArrival(rs.getString("point_of_arrival"));
                if(!status.equals("premade")){
                    flight.setTimeOfDeparture(rs.getTime("time_of_departure"));
                    if(status.equals("archival")){
                        flight.setTimeOfArrival(rs.getTime("time_of_arrival"));
                    }
                }
                flight.setFlightTime((org.postgresql.util.PGInterval) rs.getObject("flight_duration"));
                flight.setTask(rs.getString("task"));
                flight.setPreFlightCheckup(rs.getString("pre_flight_checkup"));
                flights.add(flight);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return flights;
    }


    public void addFlight(Flight flight) {
        String sql = "INSERT INTO flights (glider_id, pilot_1_id, pilot_2_id, status, date, point_of_departure, point_of_arrival, time_of_departure, time_of_arrival, flight_duration, task, pre_flight_checkup) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, flight.getGlider().getId());
            ps.setLong(2, flight.getPilot1().getId());
            if(flight.getPilot2() != null) {
                ps.setLong(3, flight.getPilot2().getId());
            }
            else {
                ps.setObject(3, null);
            }
            if(flight.isArchival) {
                ps.setString(4, "archival");
            }
            else if (flight.isActive) {
                ps.setString(4, "active");
            }
            else{
                ps.setString(4, "premade");
            }
            ps.setDate(5, flight.getDate());
            ps.setString(6, flight.getPointOfDeparture());
            ps.setString(7, flight.getPointOfArrival());
            ps.setTime(8, flight.getTimeOfDeparture());
            ps.setTime(9, flight.getTimeOfArrival());
            if(flight.getTimeOfArrival() != null && flight.getTimeOfDeparture() != null) {
                String hours;
                String minutes;
                if(flight.getTimeOfArrival().getHours() - flight.getTimeOfDeparture().getHours() > 0 && flight.getTimeOfArrival().getMinutes() < flight.getTimeOfDeparture().getMinutes()){
                    hours = String.valueOf(flight.getTimeOfArrival().getHours() - flight.getTimeOfDeparture().getHours() - 1);
                    minutes = String.valueOf(60 -  flight.getTimeOfArrival().getMinutes() + flight.getTimeOfDeparture().getMinutes());
                }
                else {
                    hours = String.valueOf(flight.getTimeOfArrival().getHours() -  flight.getTimeOfDeparture().getHours());
                    minutes = String.valueOf(flight.getTimeOfArrival().getMinutes() - flight.getTimeOfDeparture().getMinutes());
                }
                ps.setObject(10, new PGInterval(hours + " hours " +  minutes + " minutes"));
            }
            else {
                ps.setObject(10, null);
            }
            ps.setString(11, flight.getTask());
            ps.setString(12, flight.getPreFlightCheckup());
            ps.execute();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    public void deleteFlight(Long id) {
        String sql = "DELETE FROM flights WHERE id=?";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement st = conn.prepareStatement(sql);
            st.setLong(1, id);
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    //TODO doesn't work
    public void editFlight(Flight editedFlight) throws SQLException {
        Glider glider = editedFlight.getGlider();
        Pilot pilot1 = editedFlight.getPilot1();
        Pilot pilot2 = editedFlight.getPilot2();
        Date date = editedFlight.getDate();
        String pointOfDeparture = editedFlight.getPointOfDeparture();
        String pointOfArrival = editedFlight.getPointOfArrival();
        Time timeOfDeparture = editedFlight.getTimeOfDeparture();
        Time timeOfArrival = editedFlight.getTimeOfArrival();
        String task = editedFlight.getTask();
        String preFlightCheckup = editedFlight.getPreFlightCheckup();
        String status = "premade";
        PGInterval flightTime = null;
        if(timeOfArrival == null && timeOfDeparture != null){
            status = "active";
        }
        else if(timeOfArrival != null && timeOfDeparture != null){
            status = "archival";
            String hours;
            String minutes;
            if(timeOfArrival.getHours() - timeOfDeparture.getHours() > 0 && timeOfArrival.getMinutes() < timeOfDeparture.getMinutes()){
                hours = String.valueOf(timeOfArrival.getHours() - timeOfDeparture.getHours() - 1);
                minutes = String.valueOf(60 -  timeOfArrival.getMinutes() + timeOfDeparture.getMinutes());
            }
            else {
                hours = String.valueOf(timeOfArrival.getHours() -  timeOfDeparture.getHours());
                minutes = String.valueOf(timeOfArrival.getMinutes() - timeOfDeparture.getMinutes());
            }
            flightTime = new PGInterval(hours + " hours " +  minutes + " minutes");
        }
        String sql = "UPDATE flights SET glider_id = ?, pilot_1_id = ?, pilot_2_id = ?, status = ?, date = ?, point_of_departure = ?, point_of_arrival = ?, time_of_departure = ?, time_of_arrival = ?, flight_duration = ?, task = ?, pre_flight_checkup = ? WHERE id = " + editedFlight.getId();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, glider.getId());
            ps.setLong(2, pilot1.getId());
            if(pilot2 != null) {
                ps.setLong(3, pilot2.getId());
            }
            else{
                ps.setNull(3, Types.INTEGER);
            }
            ps.setDate(5, date);
            ps.setTime(8, timeOfDeparture);
            ps.setTime(9, timeOfArrival);
            ps.setObject(10, flightTime);
            ps.setString(6, pointOfDeparture);
            ps.setString(7, pointOfArrival);
            ps.setString(11, task);
            ps.setString(12, preFlightCheckup);
            ps.setString(4, status);
            ps.execute();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        /*
        if (flight.getGlider().getId() != glider.getId()) {
            String sql = "UPDATE flights SET glider_id = ? WHERE id = " + flight.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setLong(1, glider.getId());
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (flight.getPilot1().getId() != pilot1.getId()) {
            String sql = "UPDATE flights SET pilot_1_id = ? WHERE id = " + flight.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setLong(1, pilot1.getId());
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if(pilot2 != null) {
            if(flight.getPilot2() == null) {
                String sql = "UPDATE flights SET pilot_2_id = ? WHERE id = " + flight.getId();
                try (Connection conn = dataSource.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setLong(1, pilot2.getId());
                    ps.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if (flight.getPilot2().getId() != pilot2.getId()) {
                String sql = "UPDATE flights SET pilot_2_id = ? WHERE id = " + flight.getId();
                try (Connection conn = dataSource.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setLong(1, pilot2.getId());
                    ps.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else if (flight.getPilot2() != null) {
            String sql = "UPDATE flights SET pilot_2_id = ? WHERE id = " + flight.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setNull(1, Types.INTEGER);
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if(!flight.getDate().equals(date)) {
            String sql = "UPDATE flights SET date = ? WHERE id = " + flight.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setDate(1, date);
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (timeOfDeparture == null && flight.getTimeOfDeparture() != null) {
            String sql = "UPDATE flights SET time_of_departure = ?, status = ? WHERE id = " + flight.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setObject(1, null);
                ps.setObject(2, "premade");
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if(flight.getFlightTime()!=null) {
                sql = "UPDATE flights SET flight_duration = ? WHERE id = " + flight.getId();
                try (Connection conn = dataSource.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setObject(1, null);
                    ps.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (timeOfDeparture != null && timeOfArrival == null && (flight.getTimeOfDeparture() == null || !flight.getTimeOfDeparture().equals(timeOfDeparture) || flight.getTimeOfArrival() != null)) {
            String sql = "UPDATE flights SET time_of_arrival = ?, time_of_departure = ?, status = ? WHERE id = " + flight.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setObject(1, timeOfArrival);
                ps.setObject(2, timeOfDeparture);
                ps.setObject(3, "active");
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if(flight.getFlightTime()!=null) {
                sql = "UPDATE flights SET flight_duration = ? WHERE id = " + flight.getId();
                try (Connection conn = dataSource.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setObject(1, null);
                    ps.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (flight.getTimeOfArrival() == null && timeOfArrival != null) {
            String sql = "UPDATE flights SET status = ?, time_of_arrival = ?, time_of_departure = ? WHERE id = " + flight.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, "archival");
                ps.setObject(2, timeOfArrival);
                ps.setObject(3, timeOfDeparture);
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            sql = "UPDATE flights SET flight_duration = ? WHERE id = " + flight.getId();
            try (Connection conn = dataSource.getConnection()) {
                String hours;
                String minutes;
                if(timeOfArrival.getHours() - timeOfDeparture.getHours() > 0 && timeOfArrival.getMinutes() < timeOfDeparture.getMinutes()){
                    hours = String.valueOf(timeOfArrival.getHours() - timeOfDeparture.getHours() - 1);
                    minutes = String.valueOf(60 -  timeOfArrival.getMinutes() + timeOfDeparture.getMinutes());
                }
                else {
                    hours = String.valueOf(timeOfArrival.getHours() -  timeOfDeparture.getHours());
                    minutes = String.valueOf(timeOfArrival.getMinutes() - timeOfDeparture.getMinutes());
                }
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setObject(1, new PGInterval(hours + " hours " +  minutes + " minutes"));
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if(!flight.getPointOfDeparture().equals(pointOfDeparture)) {
            String sql = "UPDATE flights SET point_of_departure = ? WHERE id = " + flight.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, pointOfDeparture);
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if(!flight.getPointOfArrival().equals(pointOfArrival)) {
            String sql = "UPDATE flights SET point_of_arrival = ? WHERE id = " + flight.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, pointOfArrival);
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if(!flight.getTask().equals(task)) {
            String sql = "UPDATE flights SET task = ? WHERE id = " + flight.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, task);
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if(!flight.getPreFlightCheckup().equals(preFlightCheckup)) {
            String sql = "UPDATE flights SET pre_flight_checkup = ? WHERE id = " + flight.getId();
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, preFlightCheckup);
                ps.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }*/
    }
    public List<Flight> getFlightsByDate(Date date) throws SQLException {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT * FROM flights WHERE date = ?";
        try (Connection conn = dataSource.getConnection()) {
            Flight flight = new Flight();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setObject(1, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                flight.setId(rs.getLong("id"));
                flight.setPilot1(pilotService.findPilot(rs.getLong("pilot_1_id")));
                flight.setPilot2(pilotService.findPilot(rs.getLong("pilot_2_id")));
                flight.setGlider(gliderService.getGliderById(rs.getLong("glider_id")));
                String status = rs.getString("status");
                if (status.equals("premade")) {
                    flight.isActive = false;
                    flight.isArchival = false;
                } else if (status.equals("active")) {
                    flight.isActive = true;
                    flight.isArchival = false;
                } else if (status.equals("archival")) {
                    flight.isArchival = true;
                    flight.isActive = false;
                }
                flight.setDate(rs.getDate("date"));
                flight.setPointOfDeparture(rs.getString("point_of_departure"));
                flight.setPointOfArrival(rs.getString("point_of_arrival"));
                if(!status.equals("premade")){
                    flight.setTimeOfDeparture(rs.getTime("time_of_departure"));
                    if(status.equals("archival")){
                        flight.setTimeOfArrival(rs.getTime("time_of_arrival"));
                    }
                }
                flight.setFlightTime((org.postgresql.util.PGInterval) rs.getObject("flight_duration"));
                flight.setTask(rs.getString("task"));
                flight.setPreFlightCheckup(rs.getString("pre_flight_checkup"));
                flights.add(flight);
            }
        }
        return flights;
    }
    public List<Flight> getFlightsByPilot(Pilot pilot) throws SQLException {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT * FROM flights WHERE pilot_1_id = ? OR pilot_2_id = ?";
        try (Connection conn = dataSource.getConnection()) {
            Flight flight = new Flight();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, pilot.getId());
            ps.setLong(2, pilot.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                flight.setId(rs.getLong("id"));
                flight.setPilot1(pilotService.findPilot(rs.getLong("pilot_1_id")));
                flight.setPilot2(pilotService.findPilot(rs.getLong("pilot_2_id")));
                flight.setGlider(gliderService.getGliderById(rs.getLong("glider_id")));
                String status = rs.getString("status");
                if (status.equals("premade")) {
                    flight.isActive = false;
                    flight.isArchival = false;
                } else if (status.equals("active")) {
                    flight.isActive = true;
                    flight.isArchival = false;
                } else if (status.equals("archival")) {
                    flight.isArchival = true;
                    flight.isActive = false;
                }
                flight.setDate(rs.getDate("date"));
                flight.setPointOfDeparture(rs.getString("point_of_departure"));
                flight.setPointOfArrival(rs.getString("point_of_arrival"));
                if(!status.equals("premade")){
                    flight.setTimeOfDeparture(rs.getTime("time_of_departure"));
                    if(status.equals("archival")){
                        flight.setTimeOfArrival(rs.getTime("time_of_arrival"));
                    }
                }
                flight.setFlightTime((org.postgresql.util.PGInterval) rs.getObject("flight_duration"));
                flight.setTask(rs.getString("task"));
                flight.setPreFlightCheckup(rs.getString("pre_flight_checkup"));
                flights.add(flight);
            }
        }
        return flights;
    }
    public List<Flight> getFlightsByGlider(Glider glider) throws SQLException {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT * FROM flights WHERE glider_id = ?";
        try (Connection conn = dataSource.getConnection()) {
            Flight flight = new Flight();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, glider.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                flight.setId(rs.getLong("id"));
                flight.setPilot1(pilotService.findPilot(rs.getLong("pilot_1_id")));
                flight.setPilot2(pilotService.findPilot(rs.getLong("pilot_2_id")));
                flight.setGlider(gliderService.getGliderById(rs.getLong("glider_id")));
                String status = rs.getString("status");
                if (status.equals("premade")) {
                    flight.isActive = false;
                    flight.isArchival = false;
                } else if (status.equals("active")) {
                    flight.isActive = true;
                    flight.isArchival = false;
                } else if (status.equals("archival")) {
                    flight.isArchival = true;
                    flight.isActive = false;
                }
                flight.setDate(rs.getDate("date"));
                flight.setPointOfDeparture(rs.getString("point_of_departure"));
                flight.setPointOfArrival(rs.getString("point_of_arrival"));
                if(!status.equals("premade")){
                    flight.setTimeOfDeparture(rs.getTime("time_of_departure"));
                    if(status.equals("archival")){
                        flight.setTimeOfArrival(rs.getTime("time_of_arrival"));
                    }
                }
                flight.setFlightTime((org.postgresql.util.PGInterval) rs.getObject("flight_duration"));
                flight.setTask(rs.getString("task"));
                flight.setPreFlightCheckup(rs.getString("pre_flight_checkup"));
                flights.add(flight);
            }
        }
        return flights;
    }
    public List<Flight> getArchivalFlightsByGliderAndDate(Glider glider, Date date) throws SQLException {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT * FROM flights WHERE (glider_id = ? AND date = ? AND status = 'archival')";
        try (Connection conn = dataSource.getConnection()) {
            Flight flight = new Flight();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, glider.getId());
            ps.setDate(2, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                flight.setId(rs.getLong("id"));
                flight.setPilot1(pilotService.findPilot(rs.getLong("pilot_1_id")));
                flight.setPilot2(pilotService.findPilot(rs.getLong("pilot_2_id")));
                flight.setGlider(gliderService.getGliderById(rs.getLong("glider_id")));
                String status = rs.getString("status");
                if (status.equals("premade")) {
                    flight.isActive = false;
                    flight.isArchival = false;
                } else if (status.equals("active")) {
                    flight.isActive = true;
                    flight.isArchival = false;
                } else if (status.equals("archival")) {
                    flight.isArchival = true;
                    flight.isActive = false;
                }
                flight.setDate(rs.getDate("date"));
                flight.setPointOfDeparture(rs.getString("point_of_departure"));
                flight.setPointOfArrival(rs.getString("point_of_arrival"));
                if(!status.equals("premade")){
                    flight.setTimeOfDeparture(rs.getTime("time_of_departure"));
                    if(status.equals("archival")){
                        flight.setTimeOfArrival(rs.getTime("time_of_arrival"));
                    }
                }
                flight.setFlightTime((org.postgresql.util.PGInterval) rs.getObject("flight_duration"));
                flight.setTask(rs.getString("task"));
                flight.setPreFlightCheckup(rs.getString("pre_flight_checkup"));
                flights.add(flight);
            }
        }
        return flights;
    }
    public List<Flight> getArchivalFlightsByPilotAndDate(Pilot pilot, Date date) throws SQLException {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT * FROM flights WHERE ((pilot_1_id = ? OR pilot_2_id = ?) AND date = ? and status = 'archival')";
        try (Connection conn = dataSource.getConnection()) {
            Flight flight = new Flight();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, pilot.getId());
            ps.setLong(2, pilot.getId());
            ps.setDate(3, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                flight.setId(rs.getLong("id"));
                flight.setPilot1(pilotService.findPilot(rs.getLong("pilot_1_id")));
                flight.setPilot2(pilotService.findPilot(rs.getLong("pilot_2_id")));
                flight.setGlider(gliderService.getGliderById(rs.getLong("glider_id")));
                String status = rs.getString("status");
                if (status.equals("premade")) {
                    flight.isActive = false;
                    flight.isArchival = false;
                } else if (status.equals("active")) {
                    flight.isActive = true;
                    flight.isArchival = false;
                } else if (status.equals("archival")) {
                    flight.isArchival = true;
                    flight.isActive = false;
                }
                flight.setDate(rs.getDate("date"));
                flight.setPointOfDeparture(rs.getString("point_of_departure"));
                flight.setPointOfArrival(rs.getString("point_of_arrival"));
                if(!status.equals("premade")){
                    flight.setTimeOfDeparture(rs.getTime("time_of_departure"));
                    if(status.equals("archival")){
                        flight.setTimeOfArrival(rs.getTime("time_of_arrival"));
                    }
                }
                flight.setFlightTime((org.postgresql.util.PGInterval) rs.getObject("flight_duration"));
                flight.setTask(rs.getString("task"));
                flight.setPreFlightCheckup(rs.getString("pre_flight_checkup"));
                flights.add(flight);
            }
        }
        return flights;
    }
    public void deleteFlightByGliderId(long id) throws SQLException {
        String sql = "DELETE FROM flights WHERE glider_id=?";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement st = conn.prepareStatement(sql);
            st.setLong(1, id);
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void modifyFlightByPilotId(long id) throws SQLException {
        String sql = "DELETE FROM flights WHERE pilot_1_id=?";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement st = conn.prepareStatement(sql);
            st.setLong(1, id);
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        sql = "UPDATE flights SET pilot_2_id = null WHERE pilot_2_id=?";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement st = conn.prepareStatement(sql);
            st.setLong(1, id);
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        };
    }
    public Flight getFlightById(long id) throws SQLException {
        String sql = "SELECT * FROM flights WHERE id=?";
        Flight flight = new Flight();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement st = conn.prepareStatement(sql);
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                flight.setId(rs.getLong("id"));
                flight.setPilot1(pilotService.findPilot(rs.getLong("pilot_1_id")));
                flight.setPilot2(pilotService.findPilot(rs.getLong("pilot_2_id")));
                flight.setGlider(gliderService.getGliderById(rs.getLong("glider_id")));
                String status = rs.getString("status");
                if (status.equals("premade")) {
                    flight.isActive = false;
                    flight.isArchival = false;
                } else if (status.equals("active")) {
                    flight.isActive = true;
                    flight.isArchival = false;
                } else if (status.equals("archival")) {
                    flight.isArchival = true;
                    flight.isActive = false;
                }
                flight.setDate(rs.getDate("date"));
                flight.setPointOfDeparture(rs.getString("point_of_departure"));
                flight.setPointOfArrival(rs.getString("point_of_arrival"));
                if(!status.equals("premade")){
                    flight.setTimeOfDeparture(rs.getTime("time_of_departure"));
                    if(status.equals("archival")){
                        flight.setTimeOfArrival(rs.getTime("time_of_arrival"));
                    }
                }
                flight.setFlightTime((org.postgresql.util.PGInterval) rs.getObject("flight_duration"));
                flight.setTask(rs.getString("task"));
                flight.setPreFlightCheckup(rs.getString("pre_flight_checkup"));
            }
        }
        return flight;
    }
}


