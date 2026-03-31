/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Exception thrown when a requested show is not found.
 *
 * @since 1.0.0
 */
package com.bookxshow.exception;

import com.bookxshow.common.Constants;

/**
 * Thrown when a show lookup fails because no show exists with the
 * given identifier.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class ShowNotFoundException extends RuntimeException {

    /**
     * Constructs the exception with the missing show ID.
     *
     * @param showId the identifier that was not found
     */
    public ShowNotFoundException(String showId) {
        super(Constants.ERR_SHOW_NOT_FOUND + showId);
    }

    /**
     * Constructs the exception with the missing show primary key.
     *
     * @param showId the primary key that was not found
     */
    public ShowNotFoundException(Long showId) {
        super(Constants.ERR_SHOW_NOT_FOUND_ID + showId);
    }
}
