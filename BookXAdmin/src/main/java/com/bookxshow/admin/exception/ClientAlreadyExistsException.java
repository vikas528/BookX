/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.exception;

/**
 * Thrown when attempting to create an M2M client with a client ID
 * that already exists.
 *
 * @since 1.0.0
 */
public class ClientAlreadyExistsException extends RuntimeException {

    public ClientAlreadyExistsException(String message) {
        super(message);
    }
}
