/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Defines the lifecycle states of a seat within a show.
 *
 * @since 1.0.0
 */
package com.bookxshow.enums;

/**
 * Represents the lifecycle states of a single bookable seat.
 *
 * <p>State transitions:</p>
 * <pre>
 *   AVAILABLE ──→ LOCKED   (user initiates booking, payment pending)
 *   LOCKED    ──→ BOOKED   (payment succeeded, booking confirmed)
 *   LOCKED    ──→ AVAILABLE (payment failed OR lock TTL expired)
 * </pre>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public enum SeatStatus {

    /** Seat is open and available for booking. */
    AVAILABLE,

    /** Seat is temporarily held by a user while payment is being processed. */
    LOCKED,

    /** Seat has been permanently booked — payment was confirmed. */
    BOOKED
}
