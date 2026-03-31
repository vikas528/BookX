/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Spring Data JPA repository for Booking entities.
 *
 * @since 1.0.0
 */
package com.bookxshow.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bookxshow.entity.Booking;

/**
 * Data-access layer for {@link Booking} entities.
 *
 * <p>Provides standard CRUD operations and custom finders for looking up
 * bookings by reference code or user.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Finds a booking by its unique, user-facing reference code.
     * Eagerly fetches the associated seat and show to avoid lazy-loading issues.
     *
     * @param bookingReference the reference string (e.g. "BXS-a3f8…")
     * @return an {@link Optional} containing the booking if found
     */
    @Query("SELECT b FROM Booking b JOIN FETCH b.seat JOIN FETCH b.show WHERE b.bookingReference = :ref")
    Optional<Booking> findByBookingReference(@Param("ref") String bookingReference);

    /**
     * Retrieves all bookings made by a specific user, ordered by creation
     * date descending (most recent first).
     * Eagerly fetches the associated seat and show.
     *
     * @param userId the user identifier
     * @return list of bookings
     */
    @Query("SELECT b FROM Booking b JOIN FETCH b.seat JOIN FETCH b.show WHERE b.userId = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);

    /**
     * Retrieves all bookings for a specific show.
     *
     * @param showId the show primary key
     * @return list of bookings for the show
     */
    List<Booking> findByShowId(Long showId);
}
