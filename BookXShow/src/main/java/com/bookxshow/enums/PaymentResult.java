/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Outcome returned by the payment gateway after processing a charge.
 *
 * @since 1.0.0
 */
package com.bookxshow.enums;

import com.bookxshow.service.PaymentGateway;

/**
 * Represents the outcome of a payment processing request sent
 * to the external payment gateway.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 * @see PaymentGateway
 */
public enum PaymentResult {

    /** Payment was authorized — seat can be confirmed as booked. */
    SUCCESS,

    /** Payment was declined or encountered an error — seat should be released. */
    FAILURE,

    /** Payment gateway did not respond within the configured timeout. */
    TIMEOUT
}
