package com.example.application.flights;

import com.example.application.gliders.Glider;
import com.example.application.pilots.Pilot;
import jakarta.annotation.Nullable;
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

    //getters and setters
    public Pilot getPilot1() {
        return pilot1;
    }

    public void setPilot1(Pilot pilot1) {
        this.pilot1 = pilot1;
    }
    @Nullable
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    //constructors as needed in the code
    public Flight() {}

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
        // TODO change this to not rely on ID
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Flight other = (Flight) obj;
        return getId() != null && getId().equals(other.getId());
    }

    //TODO probably delete since not used
    @Override
    public int hashCode() {
        // Hashcode should never change during the lifetime of an object. Because of
        // this we can't use getId() to calculate the hashcode. Unless yu have sets
        // with lots of entities in them, returning the same hashcode should not be a
        // problem.
        return getClass().hashCode();
    }

    //validates if the flight conflicts with any other flights
    public boolean validateAddition(List<Flight> filteredByGlider, List<Flight> filteredByPilot1, List<Flight> filteredByPilot2) {
        //if it's pre-made, then it can't conflict with anything
        if (isActive || isArchival) {
            //if it's active, it can't have its pilots or gliders be flying
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
            //if there are flights on this day with this glider, pilot1 or pilot2, this flight cannot have departed during any such flight
            if (!filteredByGlider.isEmpty()) {
                for (int i = 0; i < filteredByGlider.size(); i++) {
                    if (filteredByGlider.get(i).getTimeOfDeparture().before(timeOfDeparture) && filteredByGlider.get(i).getTimeOfArrival().after(timeOfDeparture)) {
                        return false;
                    }
                }
            }
            if (!filteredByPilot1.isEmpty()) {
                for (int i = 0; i < filteredByPilot1.size(); i++) {
                    if (filteredByPilot1.get(i).getTimeOfDeparture().before(timeOfDeparture) && filteredByPilot1.get(i).getTimeOfArrival().after(timeOfDeparture)) {
                        return false;
                    }
                }
            }
            if (filteredByPilot2 !=null) {
                if (!filteredByPilot2.isEmpty()) {
                    for (int i = 0; i < filteredByPilot2.size(); i++) {
                        if (filteredByPilot2.get(i).getTimeOfDeparture().before(timeOfDeparture) && filteredByPilot2.get(i).getTimeOfArrival().after(timeOfDeparture)) {
                            return false;
                        }
                    }
                }
            }

            //it cannot have landed during any such flights either
            if (isArchival) {
                if (!filteredByGlider.isEmpty()) {
                    for (int i = 0; i < filteredByGlider.size(); i++) {
                        if (filteredByGlider.get(i).getTimeOfDeparture().before(timeOfArrival) && filteredByGlider.get(i).getTimeOfArrival().after(timeOfArrival)) {
                            return false;
                        }
                        if(filteredByGlider.get(i).getTimeOfDeparture().after(timeOfDeparture) && filteredByGlider.get(i).getTimeOfArrival().before(timeOfArrival)){
                            return false;
                        }
                    }
                }
                if (!filteredByPilot1.isEmpty()) {
                    for (int i = 0; i < filteredByPilot1.size(); i++) {
                        if (filteredByPilot1.get(i).getTimeOfDeparture().before(timeOfArrival) && filteredByPilot1.get(i).getTimeOfArrival().after(timeOfArrival)) {
                            return false;
                        }
                        if(filteredByPilot1.get(i).getTimeOfDeparture().after(timeOfDeparture) && filteredByPilot1.get(i).getTimeOfArrival().before(timeOfArrival)){
                            return false;
                        }
                    }
                }
                if (filteredByPilot2 !=null) {
                    if (!filteredByPilot2.isEmpty()) {
                        for (int i = 0; i < filteredByPilot2.size(); i++) {
                            if (filteredByPilot2.get(i).getTimeOfDeparture().before(timeOfArrival) && filteredByPilot2.get(i).getTimeOfArrival().after(timeOfArrival)) {
                                return false;
                            }
                            if(filteredByPilot2.get(i).getTimeOfDeparture().after(timeOfDeparture) && filteredByPilot2.get(i).getTimeOfArrival().before(timeOfArrival)){
                                return false;
                            }
                        }
                    }
                }
            }
        }
        //if no conflicts were found, return true
        return true;
    }

    //this method works the same way, except that it handles the fact that the old flight record could conflict with the new one
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
                for (int i = 0; i < filteredByPilot1.size(); i++) {
                    if (filteredByPilot1.get(i).getTimeOfDeparture().before(timeOfDeparture) && filteredByPilot1.get(i).getTimeOfArrival().after(timeOfDeparture) && !filteredByPilot1.get(i).equals(oldFlight)) {
                        return false;
                    }
                }
            }
            if (filteredByPilot2 !=null) {
                if (!filteredByPilot2.isEmpty()) {
                    for (int i = 0; i < filteredByPilot2.size(); i++) {
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
                        if(filteredByGlider.get(i).getTimeOfDeparture().after(timeOfDeparture) && filteredByGlider.get(i).getTimeOfArrival().before(timeOfArrival) && !filteredByGlider.get(i).equals(oldFlight)){
                            return false;
                        }
                    }
                }
                if (!filteredByPilot1.isEmpty()) {
                    for (int i = 0; i < filteredByPilot1.size(); i++) {
                        if (filteredByPilot1.get(i).getTimeOfDeparture().before(timeOfArrival) && filteredByPilot1.get(i).getTimeOfArrival().after(timeOfArrival) && !filteredByPilot1.get(i).equals(oldFlight)) {
                            return false;
                        }
                        if(filteredByPilot1.get(i).getTimeOfDeparture().after(timeOfDeparture) && filteredByPilot1.get(i).getTimeOfArrival().before(timeOfArrival) && !filteredByPilot1.get(i).equals(oldFlight)){
                            return false;
                        }
                    }
                }
                if (filteredByPilot2 !=null) {
                    if (!filteredByPilot2.isEmpty()) {
                        for (int i = 0; i < filteredByPilot2.size(); i++) {
                            if (filteredByPilot2.get(i).getTimeOfDeparture().before(timeOfArrival) && filteredByPilot2.get(i).getTimeOfArrival().after(timeOfArrival) && !filteredByPilot2.get(i).equals(oldFlight)) {
                                return false;
                            }
                            if(filteredByPilot2.get(i).getTimeOfDeparture().after(timeOfDeparture) && filteredByPilot2.get(i).getTimeOfArrival().before(timeOfArrival) && !filteredByPilot2.get(i).equals(oldFlight)){
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    //this method checks whether this flight doesn't violate any checkup deadlines declared for its glider
    public String checkNextCheckup(){
        //it outputs a string which contains info on all deadlines it violates or is close to validation
        String output = null;
        boolean overdueHrs = false;
        //if the glider is already overdue in terms of hours, there is no point in checking if the flight adding flight time would violate this deadline
        if(glider.getNextCheckupHrs() != null){
            if (glider.getNextCheckupHrs().getHours() < glider.getTotalFlightTime().getHours() || (glider.getNextCheckupHrs().getHours() == glider.getTotalFlightTime().getHours()) && glider.getNextCheckupHrs().getMinutes() < glider.getTotalFlightTime().getMinutes()) {
                output = "This glider is overdue for a checkup in terms of flight hours";
                overdueHrs = true;
            }
        }
        if(isArchival) {
            if (glider.getNextCheckupHrs() != null && !overdueHrs) {
                if (glider.getNextCheckupHrs().getHours() < glider.getTotalFlightTime().getHours() + flightTime.getHours()) {
                    if (output != null) {
                        output = output + "\nThis glider is overdue for a checkup in terms of flight hours";
                    } else {
                        output = "This glider is overdue for a checkup in terms of flight hours";
                    }
                } else if (glider.getTotalFlightTime().getMinutes() + flightTime.getMinutes() > 60) {
                    if (glider.getNextCheckupHrs().getHours() < glider.getTotalFlightTime().getHours() + flightTime.getHours() + 1) {
                        if (output != null) {
                            output = output + "\nThis glider is overdue for a checkup in terms of flight hours";
                        } else {
                            output = "This glider is overdue for a checkup in terms of flight hours";
                        }
                    }
                    else if (glider.getNextCheckupHrs().getHours() < glider.getTotalFlightTime().getHours() + flightTime.getHours() + 1 + 10) {
                        if (output != null) {
                            output = output + "\nCheckup in less than 10 hrs, next checkup at: " + glider.getNextCheckupHrs() + ", current flight time: " + glider.getTotalFlightTime() + ", flight duration: " + flightTime;
                        } else {
                            output = "Checkup in less than 10 hrs, next checkup at: " + glider.getNextCheckupHrs() + ", current flight time: " + glider.getTotalFlightTime() + ", flight duration: " + flightTime;
                        }
                    }
                } else if (glider.getNextCheckupHrs().getHours() < glider.getTotalFlightTime().getHours() + flightTime.getHours() + 10) {
                    if (output != null) {
                        output = output + "\nCheckup in less than 10 hrs, next checkup at: " + glider.getNextCheckupHrs() + ", current flight time: " + glider.getTotalFlightTime() + ", flight duration: " + flightTime;
                    } else {
                        output = "Checkup in less than 10 hrs, next checkup at: " + glider.getNextCheckupHrs() + ", current flight time: " + glider.getTotalFlightTime() + ", flight duration: " + flightTime;
                    }
                }
            }
        }
        //checks if flight violates date deadline
        if (glider.getNextCheckupDate() != null) {
            if(glider.getNextCheckupDate().before(date)) {
                if (output != null) {
                    output = output + "\nThis glider is overdue for a checkup in terms of date";
                } else {
                    output = "This glider is overdue for a checkup in terms of date";
                }
            }
        }
        if (glider.getNextCheckupFlights() != null) {
            if (glider.getNextCheckupFlights() < glider.getFlightCount() + 1) {
                if (output != null) {
                    output = output + "\nThis glider is overdue for a checkup in terms of flights";
                }
                else {
                    output = "This glider is overdue for a checkup in terms of flights";
                }
            }
            else if (glider.getFlightCount() + 1 + 30 > glider.getNextCheckupFlights()) {
                    if (output != null) {
                        output = output + "\nCheckup in " + (glider.getFlightCount() + 1 + 30 - glider.getNextCheckupFlights()) + " flights";
                    } else {
                        output = "Checkup in " + (glider.getFlightCount() + 1 + 30 - glider.getNextCheckupFlights()) + " flights";
                    }
            }
        }
        return output;
    }
}
