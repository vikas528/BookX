/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Strategy interface for payment processing.
 * Implementations may call external payment gateways (Stripe, Razorpay, etc.).
 *
 * @since 1.0.0
 */
package com.bookxshow.service;

import java.math.BigDecimal;

import com.bookxshow.enums.PaymentResult;

/**
 * Abstraction for payment processing — <em>Strategy Pattern</em>.
 *
 * <p>Different payment providers can be integrated by supplying a new
 * implementation of this interface and registering it as a Spring bean.
 * The booking service is decoupled from any specific payment vendor,
 * satisfying the <em>Open/Closed Principle</em>.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 * @see PaymentResult
 */
public interface PaymentGateway {

    /**
     * Processes a payment charge for the given user.
     *
     * <p>Implementations should handle retries, timeouts, and error mapping
     * internally and return a normalised {@link PaymentResult}.</p>
     *
     * @param userId the identifier of the user being charged
     * @param amount the amount to charge (currency assumed from service config)
     * @return the outcome of the payment attempt
     */
    PaymentResult processPayment(String userId, BigDecimal amount);
}
