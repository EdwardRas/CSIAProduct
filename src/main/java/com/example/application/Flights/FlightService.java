package com.example.application.Flights;

import com.example.application.gliders.Glider;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class FlightService {

    private final FlightRepository flightRepository;

    FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional
    public void createTask(String description, @Nullable LocalDate dueDate) {
        var task = new com.example.application.gliders.Glider(description, Instant.now());
        task.setDueDate(dueDate);
        flightRepository.saveAndFlush(task);
    }

    @Transactional(readOnly = true)
    public List<Glider> list(Pageable pageable) {
        return flightRepository.findAllBy(pageable).toList();
    }

}
