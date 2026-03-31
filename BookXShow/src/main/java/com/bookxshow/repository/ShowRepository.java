/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Spring Data JPA repository for Show entities.
 *
 * @since 1.0.0
 */
package com.bookxshow.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.bookxshow.entity.Show;

/**
 * Data-access layer for {@link Show} entities.
 *
 * <p>Provides standard CRUD operations via Spring Data JPA and custom
 * finder methods for looking up shows by their external identifier.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface ShowRepository extends JpaRepository<Show, Long>, JpaSpecificationExecutor<Show> {

    /**
     * Finds a show by the external identifier assigned by the Admin Service.
     *
     * @param externalShowId the Admin-Service-assigned show ID
     * @return an {@link Optional} containing the show if found
     */
    Optional<Show> findByExternalShowId(String externalShowId);

    /**
     * Checks whether a show with the given external ID already exists.
     *
     * @param externalShowId the Admin-Service-assigned show ID
     * @return {@code true} if a matching show exists
     */
    boolean existsByExternalShowId(String externalShowId);

    /**
     * Returns shows matching optional filters, ordered by show date/time ascending.
     *
     * <p>Use {@link ShowSpecification#withFilters} to build the predicate and pass it
     * to {@link #findAll(org.springframework.data.jpa.domain.Specification, org.springframework.data.domain.Sort)}.</p>
     */
}
