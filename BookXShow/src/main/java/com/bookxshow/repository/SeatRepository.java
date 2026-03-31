/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Spring Data JPA repository for Seat entities with pessimistic-locking
 * query methods used during the booking flow.
 *
 * @since 1.0.0
 */
package com.bookxshow.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bookxshow.entity.Seat;

import jakarta.persistence.LockModeType;

/**
 * Data-access layer for {@link Seat} entities.
 *
 * <p>Includes <strong>pessimistic-write</strong> query methods that issue
 * {@code SELECT … FOR UPDATE} statements, ensuring that concurrent
 * transactions serialize access to the same seat row and preventing
 * double-bookings at the database level.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    /**
     * Retrieves a seat by its primary key while acquiring a database-level
     * exclusive row lock ({@code SELECT … FOR UPDATE}).
     *
     * <p>This method <b>must</b> be called within a transaction. The lock is
     * held until the enclosing transaction commits or rolls back.</p>
     *
     * @param id the seat primary key
     * @return an {@link Optional} containing the locked seat
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Optional<Seat> findByIdWithLock(@Param("id") Long id);

    /**
     * Retrieves all seats for a given show, ordered by seat number.
     *
     * @param showId the show primary key
     * @return ordered list of seats
     */
    @Query("SELECT s FROM Seat s WHERE s.show.id = :showId ORDER BY s.seatNumber")
    List<Seat> findByShowIdOrderBySeatNumber(@Param("showId") Long showId);

    /**
     * Finds a seat by its seat number within a specific show.
     *
     * @param seatNumber the human-readable seat label (e.g. "A01")
     * @param showId     the show primary key
     * @return an {@link Optional} containing the seat if found
     */
    Optional<Seat> findBySeatNumberAndShowId(String seatNumber, Long showId);

    /**
     * Bulk-releases all seats whose lock has expired.
     *
     * <p>This is invoked by the scheduled lock-expiry sweeper. It updates
     * seats in a single statement, avoiding row-by-row processing.</p>
     *
     * @param now the current instant — locks with expiry before this are released
     * @return the number of seats that were released
     */
    @Modifying
    @Query("UPDATE Seat s SET s.status = 'AVAILABLE', s.lockedBy = null, s.lockExpiry = null "
            + "WHERE s.status = 'LOCKED' AND s.lockExpiry < :now")
    int releaseExpiredLocks(@Param("now") Instant now);
}
