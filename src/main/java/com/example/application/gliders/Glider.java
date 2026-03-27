package com.example.application.gliders;

import com.vaadin.copilot.shaded.checkerframework.checker.units.qual.C;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import javax.xml.crypto.Data;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "gliders")
public class Glider {

    public static final int DESCRIPTION_MAX_LENGTH = 300;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private static int tempNextID;
    private int id;
    public boolean isFlying;

    @Column(name = "registrationNumber", nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String registrationNumber = "";

    @Column(name = "totalFlightTime", nullable = false)
    private Duration totalFlightTime;

    @Column(name = "numberOfFlights", nullable = false)
    private int flightNum;

    @Column(name = "type")
    private String type;

    @Column(name = "nextCheckupHrs")
    private Duration nextCheckupHrs;

    @Column(name = "nextCheckupFlights")
    private int nextCheckupFlights;

    @Column(name = "nextCheckupDate")
    private Date nextCheckupDate;

//    @Column(name = "creation_date", nullable = false)
//    private Instant creationDate;
//
//    @Column(name = "due_date")
//    @Nullable
//    private LocalDate dueDate;

    protected Glider() { // To keep Hibernate happy
    }

    public Glider(String registrationNumber/*, Instant creationDate*/) {
        setRegistrationNumber(registrationNumber);
//        this.creationDate = creationDate;
        setId(tempNextID);
        setTempNextID(tempNextID + 1);

    }

    public void setId(int id) {
        this.id = id;
    }

    public static int getTempNextID() {
        return tempNextID;
    }

    public static void setTempNextID(int tempNextID) {
        Glider.tempNextID = tempNextID;
    }

    public @Nullable int getId() {
        return id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        if (registrationNumber.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("Description length exceeds " + DESCRIPTION_MAX_LENGTH);
        }
        this.registrationNumber = registrationNumber;
    }

//    public Instant getCreationDate() {
//        return creationDate;
//    }

//    public @Nullable LocalDate getDueDate() {
//        return dueDate;
//    }

//    public void setDueDate(@Nullable LocalDate dueDate) {
//        this.dueDate = dueDate;
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Glider other = (Glider) obj;
        return getId() != other.getId();
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
