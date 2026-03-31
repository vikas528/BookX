/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Exception thrown when a booking record is not found.
 *
 * @since 1.0.0
 */
package com.bookxshow.exception;

import com.bookxshow.common.Constants;

/**
 * Thrown when a booking lookup fails because no record exists with the
 * given reference.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class BookingNotFoundException extends RuntimeException {

    /**
     * Constructs the exception with the missing booking reference.
     *
     * @param bookingReference the reference that was not found
     */
    public BookingNotFoundException(String bookingReference) {
        super(Constants.ERR_BOOKING_NOT_FOUND + bookingReference);
    }
}
