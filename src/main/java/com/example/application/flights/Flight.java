package com.example.application.flights;

import com.example.application.gliders.Glider;
import com.example.application.pilots.Pilot;
import org.postgresql.util.PGInterval;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

public class Flight {

    private Long id;
    public boolean isActive;
    public boolean isArchival;
    private Pilot pilot1;
    private Pilot pilot2;
    private Date date;
    private String pointOfDeparture;
    private String pointOfArrival;
    private Time timeOfDeparture;
    private Time timeOfArrival;
    private PGInterval flightTime;
    private String task;
    private String preFlightCheckup;
    private Glider glider;

    public Pilot getPilot1() {
        return pilot1;
    }

    public void setPilot1(Pilot pilot1) {
        this.pilot1 = pilot1;
    }

    public Pilot getPilot2() {
        return pilot2;
    }

    public void setPilot2(Pilot pilot2) {
        this.pilot2 = pilot2;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPointOfDeparture() {
        return pointOfDeparture;
    }

    public void setPointOfDeparture(String pointOfDeparture) {
        this.pointOfDeparture = pointOfDeparture;
    }

    public String getPointOfArrival() {
        return pointOfArrival;
    }

    public void setPointOfArrival(String pointOfArrival) {
        this.pointOfArrival = pointOfArrival;
    }

    public Time getTimeOfDeparture() {
        return timeOfDeparture;
    }

    public void setTimeOfDeparture(Time timeOfDeparture) {
        this.timeOfDeparture = timeOfDeparture;
    }

    public Time getTimeOfArrival() {
        return timeOfArrival;
    }

    public void setTimeOfArrival(Time timeOfArrival) {
        this.timeOfArrival = timeOfArrival;
    }

    public PGInterval getFlightTime() {
        return flightTime;
    }

    public void setFlightTime(PGInterval flightTime) {
        this.flightTime = flightTime;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getPreFlightCheckup() {
        return preFlightCheckup;
    }

    public void setPreFlightCheckup(String preFlightCheckup) {
        this.preFlightCheckup = preFlightCheckup;
    }

    public Glider getGlider() {
        return glider;
    }

    public void setGlider(Glider glider) {
        this.glider = glider;
    }

    public Flight() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Flight(Glider glider, Pilot pilot1, Pilot pilot2, Date date, String pointOfDeparture, String pointOfArrival, Time timeOfDeparture, Time timeOfArrival, String task, String preFlightCheckup) {
        this.pilot1 = pilot1;
        this.pilot2 = pilot2;
        this.date = date;
        this.pointOfDeparture = pointOfDeparture;
        this.pointOfArrival = pointOfArrival;
        this.timeOfDeparture = timeOfDeparture;
        this.timeOfArrival = timeOfArrival;
        this.task = task;
        this.preFlightCheckup = preFlightCheckup;
        this.glider = glider;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Flight other = (Flight) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        // Hashcode should never change during the lifetime of an object. Because of
        // this we can't use getId() to calculate the hashcode. Unless yu have sets
        // with lots of entities in them, returning the same hashcode should not be a
        // problem.
        return getClass().hashCode();
    }
    public boolean validateAddition(List<Flight> filteredByGlider, List<Flight> filteredByPilot1, List<Flight> filteredByPilot2) {
        if (isActive || isArchival) {
            if (isActive) {
                if (pilot1.isFlying || glider.isFlying) {
                    return false;
                }
                if (pilot2 != null) {
                    if (pilot2.isFlying) {
                        return false;
                    }
                }
            }
            if (!filteredByGlider.isEmpty()) {
                for (int i = 0; i < filteredByGlider.size(); i++) {
                    if (filteredByGlider.get(i).getTimeOfDeparture().before(timeOfDeparture) && filteredByGlider.get(i).getTimeOfArrival().after(timeOfDeparture)) {
                        return false;
                    }
                }
            }
            if (!filteredByPilot1.isEmpty()) {
                for (int i = 0; i < filteredByGlider.size(); i++) {
                    if (filteredByPilot1.get(i).getTimeOfDeparture().before(timeOfDeparture) && filteredByPilot1.get(i).getTimeOfArrival().after(timeOfDeparture)) {
                        return false;
                    }
                }
            }
            if (filteredByPilot2 !=null) {
                if (!filteredByPilot2.isEmpty()) {
                    for (int i = 0; i < filteredByGlider.size(); i++) {
                        if (filteredByPilot2.get(i).getTimeOfDeparture().before(timeOfDeparture) && filteredByPilot2.get(i).getTimeOfArrival().after(timeOfDeparture)) {
                            return false;
                        }
                    }
                }
            }
            if (isArchival) {
                if (!filteredByGlider.isEmpty()) {
                    for (int i = 0; i < filteredByGlider.size(); i++) {
                        if (filteredByGlider.get(i).getTimeOfDeparture().before(timeOfArrival) && filteredByGlider.get(i).getTimeOfArrival().after(timeOfArrival)) {
                            return false;
                        }
                    }
                }
                if (!filteredByPilot1.isEmpty()) {
                    for (int i = 0; i < filteredByGlider.size(); i++) {
                        if (filteredByPilot1.get(i).getTimeOfDeparture().before(timeOfArrival) && filteredByPilot1.get(i).getTimeOfArrival().after(timeOfArrival)) {
                            return false;
                        }
                    }
                }
                if (filteredByPilot2 !=null) {
                    if (!filteredByPilot2.isEmpty()) {
                        for (int i = 0; i < filteredByGlider.size(); i++) {
                            if (filteredByPilot2.get(i).getTimeOfDeparture().before(timeOfArrival) && filteredByPilot2.get(i).getTimeOfArrival().after(timeOfArrival)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    public boolean validateEdit(Flight oldFlight, List<Flight> filteredByGlider, List<Flight> filteredByPilot1, List<Flight> filteredByPilot2) {
        if (isActive || isArchival) {
            if (isActive && !oldFlight.isActive) {
                if (pilot1.isFlying || glider.isFlying) {
                    return false;
                }
                if (pilot2 != null) {
                    if (pilot2.isFlying) {
                        return false;
                    }
                }
            }
            if (!filteredByGlider.isEmpty()) {
                for (int i = 0; i < filteredByGlider.size(); i++) {
                    if (filteredByGlider.get(i).getTimeOfDeparture().before(timeOfDeparture) && filteredByGlider.get(i).getTimeOfArrival().after(timeOfDeparture) && !filteredByGlider.get(i).equals(oldFlight)) {
                        return false;
                    }
                }
            }
            if (!filteredByPilot1.isEmpty()) {
                for (int i = 0; i < filteredByGlider.size(); i++) {
                    if (filteredByPilot1.get(i).getTimeOfDeparture().before(timeOfDeparture) && filteredByPilot1.get(i).getTimeOfArrival().after(timeOfDeparture) && !filteredByPilot1.get(i).equals(oldFlight)) {
                        return false;
                    }
                }
            }
            if (filteredByPilot2 !=null) {
                if (!filteredByPilot2.isEmpty()) {
                    for (int i = 0; i < filteredByGlider.size(); i++) {
                        if (filteredByPilot2.get(i).getTimeOfDeparture().before(timeOfDeparture) && filteredByPilot2.get(i).getTimeOfArrival().after(timeOfDeparture) && !filteredByPilot2.get(i).equals(oldFlight)) {
                            return false;
                        }
                    }
                }
            }
            if (isArchival) {
                if (!filteredByGlider.isEmpty()) {
                    for (int i = 0; i < filteredByGlider.size(); i++) {
                        if (filteredByGlider.get(i).getTimeOfDeparture().before(timeOfArrival) && filteredByGlider.get(i).getTimeOfArrival().after(timeOfArrival) && !filteredByGlider.get(i).equals(oldFlight)) {
                            return false;
                        }
                    }
                }
                if (!filteredByPilot1.isEmpty()) {
                    for (int i = 0; i < filteredByGlider.size(); i++) {
                        if (filteredByPilot1.get(i).getTimeOfDeparture().before(timeOfArrival) && filteredByPilot1.get(i).getTimeOfArrival().after(timeOfArrival) && !filteredByPilot1.get(i).equals(oldFlight)) {
                            return false;
                        }
                    }
                }
                if (filteredByPilot2 !=null) {
                    if (!filteredByPilot2.isEmpty()) {
                        for (int i = 0; i < filteredByGlider.size(); i++) {
                            if (filteredByPilot2.get(i).getTimeOfDeparture().before(timeOfArrival) && filteredByPilot2.get(i).getTimeOfArrival().after(timeOfArrival) && !filteredByPilot2.get(i).equals(oldFlight)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
