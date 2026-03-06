package com.example.application.gliders;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface GliderRepository extends JpaRepository<Glider, Long>, JpaSpecificationExecutor<Glider> {

    // If you don't need a total row count, Slice is better than Page as it only performs a select query.
    // Page performs both a select and a count query.
    Slice<Glider> findAllBy(Pageable pageable);
}
