/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Exception thrown when a seat is not available for booking.
 *
 * @since 1.0.0
 */
package com.bookxshow.exception;

import com.bookxshow.common.Constants;

/**
 * Thrown when a booking attempt targets a seat that is currently
 * locked or already booked.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class SeatUnavailableException extends RuntimeException {

    /**
     * Constructs the exception indicating which seat is unavailable.
     *
     * @param seatId the primary key of the unavailable seat
     */
    public SeatUnavailableException(Long seatId) {
        super(Constants.ERR_SEAT_UNAVAILABLE + seatId);
    }
}
