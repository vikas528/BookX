/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookxshow.admin.entity.Show;
import com.bookxshow.admin.enums.ShowStatus;

/**
 * JPA repository for {@link Show} entities.
 *
 * @since 1.0.0
 */
@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    /**
     * Finds a show by its external identifier.
     *
     * @param externalShowId the admin-assigned show ID
     * @return the show, if found
     */
    Optional<Show> findByExternalShowId(String externalShowId);

    /**
     * Checks if a show with the given external ID exists.
     *
     * @param externalShowId the admin-assigned show ID
     * @return true if exists
     */
    boolean existsByExternalShowId(String externalShowId);

    /**
     * Finds all shows with the given status.
     *
     * @param status the show lifecycle status
     * @return list of matching shows
     */
    List<Show> findByStatus(ShowStatus status);

    /**
     * Finds all shows ordered by show date/time descending.
     *
     * @return all shows, newest first
     */
    List<Show> findAllByOrderByShowDateTimeDesc();
}
