/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Exception thrown when the payment gateway reports an error.
 *
 * @since 1.0.0
 */
package com.bookxshow.exception;

/**
 * Thrown when the payment gateway encounters an unrecoverable error
 * during charge processing.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class PaymentProcessingException extends RuntimeException {

    /**
     * Constructs the exception with a descriptive message.
     *
     * @param message description of the payment error
     */
    public PaymentProcessingException(String message) {
        super(message);
    }

    /**
     * Constructs the exception with a message and root cause.
     *
     * @param message description of the payment error
     * @param cause   the underlying exception
     */
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
