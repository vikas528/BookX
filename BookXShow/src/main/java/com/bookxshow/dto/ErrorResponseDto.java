/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Standard error response DTO returned by the global exception handler.
 *
 * @since 1.0.0
 */
package com.bookxshow.dto;

import java.time.Instant;

/**
 * Uniform error response body returned for all API error conditions.
 *
 * <p>Ensures that clients always receive a consistent JSON structure
 * when an error occurs, regardless of the specific exception type.</p>
 *
 * <h3>JSON Shape</h3>
 * <pre>{@code
 * {
 *   "timestamp":   "2026-03-28T10:15:30.123Z",
 *   "errorStatus": "404",
 *   "message":     "Seat with id 10 not found",
 *   "path":        "/bookxshow/v1/shows/1/seats/10"
 * }
 * }</pre>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class ErrorResponseDto {

    /** UTC timestamp when the error occurred. */
    private Instant timestamp;

    /**
     * HTTP status category indicating the class of the error
     * (e.g. {@code "4xx"} for client errors, {@code "5xx"} for server errors).
     */
    private String errorStatus;

    /** Human-readable description of what went wrong. */
    private String message;

    /** The request path that triggered the error. */
    private String path;

    /** Default constructor — sets timestamp to now (UTC). */
    public ErrorResponseDto() {
        this.timestamp = Instant.now();
    }

    /**
     * Constructs an error response with all fields populated.
     *
     * @param errorStatus the numeric HTTP status code as a string (e.g. {@code "404"})
     * @param message   the detailed message
     * @param path      the request path
     */
    public ErrorResponseDto(String errorStatus, String message, String path) {
        this.timestamp = Instant.now();
        this.errorStatus = errorStatus;
        this.message = message;
        this.path = path;
    }

    /** Returns the UTC timestamp when the error occurred. */
    public Instant getTimestamp() { return timestamp; }

    /** Sets the error timestamp. */
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    /** Returns the HTTP status category (e.g. {@code "4xx"}). */
    public String getErrorStatus() { return errorStatus; }

    /** Sets the HTTP status category. */
    public void setErrorStatus(String errorStatus) { this.errorStatus = errorStatus; }

    /** Returns the human-readable error message. */
    public String getMessage() { return message; }

    /** Sets the human-readable error message. */
    public void setMessage(String message) { this.message = message; }

    /** Returns the request path that triggered the error. */
    public String getPath() { return path; }

    /** Sets the request path. */
    public void setPath(String path) { this.path = path; }
}
