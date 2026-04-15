package com.example.application.gliders;

import org.postgresql.util.PGInterval;

import java.sql.Date;

public class Glider {

    private Long id;

    private String registrationNumber;

    private PGInterval totalFlightTime;

    private int flightCount;

    private String type;

    private PGInterval nextCheckupHrs;

    private int nextCheckupFlights;

    private Date nextCheckupDate;

    public boolean isFlying;

    public Glider() {
    }

    public Long getId() {
        return id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public PGInterval getTotalFlightTime() {
        return totalFlightTime;
    }

    public void setTotalFlightTime(PGInterval totalFlightTime) {
        this.totalFlightTime = totalFlightTime;
    }

    public int getFlightCount() {
        return flightCount;
    }

    public void setFlightCount(int flightCount) {
        this.flightCount = flightCount;
    }

    public PGInterval getNextCheckupHrs() {
        return nextCheckupHrs;
    }

    public void setNextCheckupHrs(PGInterval nextCheckupHrs) {
        this.nextCheckupHrs = nextCheckupHrs;
    }

    public int getNextCheckupFlights() {
        return nextCheckupFlights;
    }

    public void setNextCheckupFlights(int nextCheckupFlights) {
        this.nextCheckupFlights = nextCheckupFlights;
    }

    public Date getNextCheckupDate() {
        return nextCheckupDate;
    }

    public void setNextCheckupDate(Date nextCheckupDate) {
        this.nextCheckupDate = nextCheckupDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Glider other = (Glider) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        // Hashcode should never change during the lifetime of an object. Because of
        // this we can't use getId() to calculate the hashcode. Unless you have sets
        // with lots of entities in them, returning the same hashcode should not be a
        // problem.
        return getClass().hashCode();
    }
}
