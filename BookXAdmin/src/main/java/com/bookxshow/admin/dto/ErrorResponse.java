/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Uniform error response DTO returned by the global exception handler.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    /** Timestamp when the error occurred. */
    private Instant timestamp;

    /** HTTP status code. */
    private int status;

    /** Short label (e.g. "Not Found"). */
    private String error;

    /** Detailed human-readable message. */
    private String message;

    /** Request path that triggered the error. */
    private String path;
}
