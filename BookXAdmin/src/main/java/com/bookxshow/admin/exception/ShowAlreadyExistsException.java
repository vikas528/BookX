/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.exception;

/**
 * Thrown when attempting to create a show that already exists.
 *
 * @since 1.0.0
 */
public class ShowAlreadyExistsException extends RuntimeException {

    public ShowAlreadyExistsException(String message) {
        super(message);
    }
}
