/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.exception;

/**
 * Thrown when an invalid state transition is attempted on a show.
 *
 * @since 1.0.0
 */
public class InvalidShowStateException extends RuntimeException {

    public InvalidShowStateException(String message) {
        super(message);
    }
}
