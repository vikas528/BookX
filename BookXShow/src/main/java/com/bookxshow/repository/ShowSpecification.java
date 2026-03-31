/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.bookxshow.entity.Show;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for {@link Specification} predicates used to filter {@link Show} entities.
 *
 * <p>By building the predicate list dynamically (only adding a clause when the
 * corresponding parameter is non-null), no SQL {@code ? IS NULL} pattern is
 * emitted and PostgreSQL's strict parameter-type inference is never triggered.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ShowSpecification {

    private ShowSpecification() { }

    /**
     * Builds a combined specification for the optional filters.
     *
     * @param fromDate lower bound on {@code showDateTime} (inclusive), or null
     * @param toDate   upper bound on {@code showDateTime} (inclusive), or null
     * @param name     already-lowercased SQL LIKE pattern (e.g. {@code %rock%}), or null
     * @return a Specification that constrains results to matching shows
     */
    public static Specification<Show> withFilters(LocalDateTime fromDate,
                                                   LocalDateTime toDate,
                                                   String name) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("showDateTime"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("showDateTime"), toDate));
            }
            if (name != null && !name.isBlank()) {
                // LOWER(s.name) LIKE :name  — name is already lowercased by the service
                predicates.add(cb.like(cb.lower(root.get("name")), name));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
