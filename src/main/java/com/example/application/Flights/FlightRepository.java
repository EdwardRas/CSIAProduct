package com.example.application.Flights;

import com.example.application.gliders.Glider;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface FlightRepository extends JpaRepository<com.example.application.gliders.Glider, Long>, JpaSpecificationExecutor<com.example.application.gliders.Glider> {

    // If you don't need a total row count, Slice is better than Page as it only performs a select query.
    // Page performs both a select and a count query.
    Slice<Glider> findAllBy(Pageable pageable);
}
