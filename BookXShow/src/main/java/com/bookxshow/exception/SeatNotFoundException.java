/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Exception thrown when a requested seat is not found.
 *
 * @since 1.0.0
 */
package com.bookxshow.exception;

import com.bookxshow.common.Constants;

/**
 * Thrown when a seat lookup fails because no seat exists with the
 * given identifier.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class SeatNotFoundException extends RuntimeException {

    /**
     * Constructs the exception with the missing seat ID.
     *
     * @param seatId the primary key that was not found
     */
    public SeatNotFoundException(Long seatId) {
        super(Constants.ERR_SEAT_NOT_FOUND + seatId);
    }

    /**
     * Constructs the exception with a custom message.
     *
     * @param message descriptive error message
     */
    public SeatNotFoundException(String message) {
        super(message);
    }
}
