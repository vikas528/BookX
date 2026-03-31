/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Stub payment gateway for local development and testing when no
 * external payment service is available.
 *
 * @since 1.0.0
 */
package com.bookxshow.service.impl;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.bookxshow.enums.PaymentResult;
import com.bookxshow.service.PaymentGateway;

/**
 * A stub {@link PaymentGateway} that always returns {@link PaymentResult#SUCCESS}.
 *
 * <p>Activated when {@code bookxshow.payment.stub-enabled=true} (the default).
 * This allows the full booking flow to complete in local development and
 * integration tests without requiring a real payment service.</p>
 *
 * <p>To switch to the real gateway, set
 * {@code bookxshow.payment.stub-enabled=false} or the environment variable
 * {@code PAYMENT_STUB_ENABLED=false}.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(name = "bookxshow.payment.stub-enabled", havingValue = "true", matchIfMissing = true)
public class StubPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StubPaymentGateway.class);

    /**
     * Always returns {@link PaymentResult#SUCCESS} without making any
     * external HTTP call.
     *
     * @param userId the user to "charge"
     * @param amount the amount to "charge"
     * @return {@link PaymentResult#SUCCESS}
     */
    @Override
    public PaymentResult processPayment(String userId, BigDecimal amount) {
        log.info("[STUB] Payment SUCCESS for user {} amount {} (no real charge)", userId, amount);
        return PaymentResult.SUCCESS;
    }
}
