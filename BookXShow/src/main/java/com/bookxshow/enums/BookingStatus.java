/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Defines the status of a persisted booking record.
 *
 * @since 1.0.0
 */
package com.bookxshow.enums;

import com.bookxshow.entity.Booking;

/**
 * Represents the status of a {@link Booking} record
 * stored in the database.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public enum BookingStatus {

    /** Booking is confirmed and the seat is reserved for the user. */
    CONFIRMED,

    /** Booking has been canceled by the user or the system. */
    CANCELLED
}
