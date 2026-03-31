/*
 * BookXShow - API Gateway
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.gateway.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Uniform error response body returned for all gateway-level error conditions.
 *
 * <h3>JSON Shape</h3>
 * <pre>{@code
 * {
 *   "timestamp":   "2026-03-28T10:15:30.123Z",
 *   "errorStatus": "401",
 *   "message":     "Full authentication is required to access this resource",
 *   "path":        "/bookxshow/v1/bookings"
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Getter
@Builder
public class ErrorResponse {

    /** UTC timestamp when the error occurred. */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * HTTP status category indicating the class of the error
     * (e.g. {@code "4xx"} for client errors, {@code "5xx"} for server errors).
     */
    private String errorStatus;

    /** Human-readable description of what went wrong. */
    private String message;

    /** The request path that triggered the error. */
    private String path;
}
