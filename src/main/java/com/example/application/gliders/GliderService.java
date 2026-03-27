package com.example.application.gliders;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class GliderService {

    private final GliderRepository gliderRepository;

    GliderService(GliderRepository gliderRepository) {
        this.gliderRepository = gliderRepository;
    }

//    @Transactional
//    public void createTask(String description, @Nullable LocalDate dueDate) {
//        var task = new Glider(description, Instant.now());
//        task.setDueDate(dueDate);
//        gliderRepository.saveAndFlush(task);
//    }

//    @Transactional(readOnly = true)
//    public List<Glider> list(Pageable pageable) {
//        return gliderRepository.findAllBy(pageable).toList();
//    }

}
