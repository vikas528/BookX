/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Unit tests for GlobalExceptionHandler.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.exception;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.bookxshow.admin.dto.ErrorResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/bookxshow/v1/admin/shows/1");

    @Test
    @DisplayName("ShowNotFoundException returns 404")
    void handleShowNotFound_returns404() {
        ShowNotFoundException ex = new ShowNotFoundException("Show not found with ID: 1");

        ResponseEntity<ErrorResponse> response = handler.handleShowNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("Show not found"));
        assertEquals("/bookxshow/v1/admin/shows/1", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
        assertTrue(response.getBody().getTimestamp().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("ShowAlreadyExistsException returns 409")
    void handleShowAlreadyExists_returns409() {
        ShowAlreadyExistsException ex = new ShowAlreadyExistsException("Show already exists with name: Test");

        ResponseEntity<ErrorResponse> response = handler.handleShowAlreadyExists(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
    }

    @Test
    @DisplayName("InvalidShowStateException returns 409")
    void handleInvalidShowState_returns409() {
        InvalidShowStateException ex = new InvalidShowStateException("Invalid show state transition from: CANCELLED");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidShowState(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("CANCELLED"));
    }

    @Test
    @DisplayName("Generic Exception returns 500")
    void handleGeneric_returns500() {
        Exception ex = new RuntimeException("Something broke");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}
