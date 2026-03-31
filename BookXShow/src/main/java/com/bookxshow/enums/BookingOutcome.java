/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Enumerates all possible outcomes of a seat booking attempt.
 *
 * @since 1.0.0
 */
package com.bookxshow.enums;

/**
 * Describes the result of a single booking attempt returned to the caller.
 *
 * <p>Each outcome maps to a specific point in the lock → pay → confirm flow:</p>
 * <ul>
 *   <li>{@link #BOOKED} — seat confirmed, payment succeeded.</li>
 *   <li>{@link #SEAT_UNAVAILABLE} — seat was already locked or booked by another user.</li>
 *   <li>{@link #PAYMENT_FAILED} — payment gateway declined/errored; seat released.</li>
 *   <li>{@link #LOCK_EXPIRED} — payment took longer than the lock TTL; seat reclaimed.</li>
 *   <li>{@link #ALREADY_BOOKED} — the seat has already been confirmed by another booking.</li>
 *   <li>{@link #CANCELLED} — the booking was successfully cancelled.</li>
 * </ul>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public enum BookingOutcome {

    /** Seat confirmed — payment succeeded and booking is persisted. */
    BOOKED,

    /** Seat was already locked or booked by another user. */
    SEAT_UNAVAILABLE,

    /** Payment gateway returned a failure; the seat has been released. */
    PAYMENT_FAILED,

    /** Payment processing exceeded the lock TTL; the seat was reclaimed. */
    LOCK_EXPIRED,

    /** The seat has already been permanently booked by another user. */
    ALREADY_BOOKED,

    /** The booking was successfully cancelled and the seat released. */
    CANCELLED
}
