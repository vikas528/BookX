/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookxshow.admin.entity.Seat;

/**
 * JPA repository for {@link Seat} entities.
 *
 * @since 1.0.0
 */
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    /**
     * Finds all seats for a show, ordered by seat number.
     *
     * @param showId the show primary key
     * @return seats in seat-number order
     */
    List<Seat> findByShowIdOrderBySeatNumber(Long showId);

    /**
     * Finds a seat by its number and show.
     *
     * @param seatNumber the seat label
     * @param showId     the show primary key
     * @return the seat, if found
     */
    Optional<Seat> findBySeatNumberAndShowId(String seatNumber, Long showId);

    /**
     * Deletes all seats for a given show.
     *
     * @param showId the show primary key
     */
    void deleteByShowId(Long showId);

    /**
     * Counts seats belonging to a show.
     *
     * @param showId the show primary key
     * @return number of seats
     */
    long countByShowId(Long showId);
}
