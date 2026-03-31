/*
 * BookXShow - API Gateway
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.gateway.common;

/**
 * Application-wide constants for the BookXShow API Gateway.
 *
 * @since 1.0.0
 */
public final class Constants {

    private Constants() {}

    // ── Error Messages ─────────────────────────────────────────────────
    public static final String ERR_UNEXPECTED   = "An unexpected error occurred";
    public static final String ERR_SERIALISATION = "Serialisation error";

    // ── Error Response Fallback ────────────────────────────────────────
    /** {@code errorStatus} value used when JSON serialisation itself fails. */
    public static final String ERROR_STATUS_FALLBACK = "500";
}
