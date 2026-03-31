/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.exception;

/**
 * Thrown when a requested show cannot be found.
 *
 * @since 1.0.0
 */
public class ShowNotFoundException extends RuntimeException {

    public ShowNotFoundException(String message) {
        super(message);
    }

    public ShowNotFoundException(Long showId) {
        super("Show not found with ID: " + showId);
    }
}
