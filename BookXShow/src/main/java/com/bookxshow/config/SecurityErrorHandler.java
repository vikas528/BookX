/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Custom security error handlers for authentication and access-denied failures.
 *
 * @since 1.0.0
 */
package com.bookxshow.config;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Produces a consistent JSON {@code ErrorResponseDto}-shaped body for
 * authentication (401) and authorisation (403) failures.
 *
 * <p>Spring Security intercepts requests in filters <em>before</em> any
 * controller is reached, so the {@code GlobalExceptionHandler} never sees
 * these failures.  Implementing {@link AuthenticationEntryPoint} and
 * {@link AccessDeniedHandler} here lets us return the same JSON contract
 * for security errors as for application errors.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final ObjectMapper MAPPER = buildMapper();

    // ── AuthenticationEntryPoint (401 Unauthorized) ─────────────────────

    /**
     * Invoked when a request arrives without a valid bearer token (or with
     * an expired / malformed one).  Writes a 401 JSON error response.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        writeError(response, HttpStatus.UNAUTHORIZED, "Unauthorized: " + authException.getMessage(), request.getRequestURI());
    }

    // ── AccessDeniedHandler (403 Forbidden) ─────────────────────────────

    /**
     * Invoked when an authenticated principal lacks the required scope /
     * authority.  Writes a 403 JSON error response.
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        writeError(response, HttpStatus.FORBIDDEN, "Forbidden: " + accessDeniedException.getMessage(), request.getRequestURI());
    }

    // ── Helper ───────────────────────────────────────────────────────────

    private static void writeError(HttpServletResponse response,
                                   HttpStatus status,
                                   String message,
                                   String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorBody body = new ErrorBody(Instant.now().toString(), String.valueOf(status.value()), message, path);
        MAPPER.writeValue(response.getOutputStream(), body);
    }

    private static ObjectMapper buildMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /** Inline record mirrors the shape of {@code ErrorResponseDto}. */
    private record ErrorBody(String timestamp, String errorStatus, String message, String path) { }
}
