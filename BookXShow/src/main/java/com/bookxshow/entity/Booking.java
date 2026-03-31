/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * JPA entity representing a confirmed booking record.
 *
 * @since 1.0.0
 */
package com.bookxshow.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.bookxshow.enums.BookingStatus;

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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persistent entity representing a completed seat booking.
 *
 * <p>A {@code Booking} is created only after the seat has been successfully
 * locked <em>and</em> payment has been confirmed. It serves as the
 * <strong>source of truth</strong> for financial reconciliation and
 * ticket issuance.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 * @see Seat
 * @see Show
 */
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
public class Booking {

    /** Auto-generated surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique, user-facing booking reference (e.g. "BXS-a3f8…").
     * Used in confirmation emails and customer support lookups.
     */
    @Column(name = "booking_reference", nullable = false, unique = true)
    private String bookingReference;

    /** The seat that was booked. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    /** The show for which this booking was made. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    /** Identifier of the user who made the booking. */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /** Amount charged to the user. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** Current status of this booking record. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    /**
     * Transaction identifier returned by the payment gateway.
     * Nullable when the booking was created before payment confirmation
     * is fully reconciled.
     */
    @Column(name = "payment_transaction_id")
    private String paymentTransactionId;

    /** Timestamp when this booking was created. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp of the most recent update. */
    @Column(name = "updated_at")
    private Instant updatedAt;

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
}
