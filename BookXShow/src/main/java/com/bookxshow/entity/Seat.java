/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * JPA entity representing a single bookable seat within a show.
 *
 * @since 1.0.0
 */
package com.bookxshow.entity;

import java.time.Instant;

import com.bookxshow.enums.SeatStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persistent entity representing a single bookable seat within a
 * {@link Show}.
 *
 * <p>Concurrency is handled at the database level using pessimistic
 * row-level locks ({@code SELECT … FOR UPDATE}) when acquiring or
 * releasing a seat. This ensures that only one transaction can modify
 * a seat's status at a time, preventing double-bookings even under
 * high contention.</p>
 *
 * <h3>Seat lifecycle</h3>
 * <pre>
 *   AVAILABLE ──→ LOCKED    (lockSeat)
 *   LOCKED    ──→ BOOKED    (confirmBooking — payment succeeded)
 *   LOCKED    ──→ AVAILABLE (releaseSeat — payment failed / TTL expired)
 * </pre>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 * @see Show
 * @see SeatStatus
 */
@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"seat_number", "show_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class Seat {

    /** Auto-generated surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Seat label visible to end-users (e.g. "A01", "B12"). */
    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    /** The show this seat belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    /** Current booking lifecycle status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    /** User ID of the person currently holding the lock (null when available). */
    @Column(name = "locked_by")
    private String lockedBy;

    /** Instant at which the current lock automatically expires. */
    @Column(name = "lock_expiry")
    private Instant lockExpiry;

    /** Timestamp when this record was first persisted. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp of the most recent update. */
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── Business state-transition methods ────────────────────────────────

    /**
     * Transitions this seat to {@link SeatStatus#LOCKED}.
     *
     * @param userId the user acquiring the lock
     * @param expiry the instant at which the lock should auto-expire
     */
    public void markLocked(String userId, Instant expiry) {
        this.status = SeatStatus.LOCKED;
        this.lockedBy = userId;
        this.lockExpiry = expiry;
    }

    /**
     * Transitions this seat back to {@link SeatStatus#AVAILABLE},
     * clearing lock metadata.
     */
    public void markAvailable() {
        this.status = SeatStatus.AVAILABLE;
        this.lockedBy = null;
        this.lockExpiry = null;
    }

    /**
     * Transitions this seat to {@link SeatStatus#BOOKED},
     * clearing temporary lock metadata.
     */
    public void markBooked() {
        this.status = SeatStatus.BOOKED;
        this.lockedBy = null;
        this.lockExpiry = null;
    }

    // ── JPA lifecycle callbacks ─────────────────────────────────────────

    /** Sets audit timestamps before initial persist. */
    @PrePersist
    void onPrePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /** Updates the modification timestamp. */
    @PreUpdate
    void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    @Override
    public String toString() {
        return String.format("Seat{id=%d, number='%s', showId=%d, status=%s, lockedBy='%s'}",
                id, seatNumber, show != null ? show.getId() : null, status, lockedBy);
    }
}
