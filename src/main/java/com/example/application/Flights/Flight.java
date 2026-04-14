package com.example.application.Flights;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;

public class Flight {

    public static final int DESCRIPTION_MAX_LENGTH = 300;

    private Long id;


    public Flight() {
    }



    public Long getId() {
        return id;
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
        // this we can't use getId() to calculate the hashcode. Unless you have sets
        // with lots of entities in them, returning the same hashcode should not be a
        // problem.
        return getClass().hashCode();
    }
}
