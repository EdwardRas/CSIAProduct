package com.example.application.flights;

import com.example.application.gliders.Glider;
import com.example.application.pilots.Pilot;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "task")
public class Flight {

    public static final int DESCRIPTION_MAX_LENGTH = 300;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)

    private int id;
    private Glider glider;

    @Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String description = "";

    @Column(name = "departureTime", nullable = false)
    private Instant departureTime;

    @Column(name = "pilot1", nullable = false)
    private Pilot pilot1;

    @Column(name = "due_date")
    @Nullable
    private LocalDate dueDate;

    protected Flight() { // To keep Hibernate happy
    }

    public Flight(String description, Instant departureTime) {
        setDescription(description);
        this.departureTime = departureTime;
    }

    public Flight(int id, Glider glider, Pilot pilot1) {
        this.id = id;
        this.glider = glider;
        this.pilot1 = pilot1;
    }

    public @Nullable int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("Description length exceeds " + DESCRIPTION_MAX_LENGTH);
        }
        this.description = description;
    }

    public Instant getDepartureTime() {
        return departureTime;
    }

    public @Nullable LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(@Nullable LocalDate dueDate) {
        this.dueDate = dueDate;
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
        return getId() == other.getId();
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
