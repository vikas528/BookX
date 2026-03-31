/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Unit tests for GlobalExceptionHandler.
 *
 * @since 1.0.0
 */
package com.bookxshow.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.bookxshow.dto.ErrorResponseDto;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/bookings");
    }

    @Test
    @DisplayName("T01 - SeatNotFoundException returns 404")
    void seatNotFound_returns404() {
        SeatNotFoundException ex = new SeatNotFoundException(10L);

        ResponseEntity<ErrorResponseDto> response = handler.handleSeatNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("404", response.getBody().getErrorStatus());
        assertTrue(response.getBody().getMessage().contains("10"));
    }

    @Test
    @DisplayName("T02 - ShowNotFoundException returns 404")
    void showNotFound_returns404() {
        ShowNotFoundException ex = new ShowNotFoundException("SHOW-999");

        ResponseEntity<ErrorResponseDto> response = handler.handleShowNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("404", response.getBody().getErrorStatus());
        assertTrue(response.getBody().getMessage().contains("SHOW-999"));
    }

    @Test
    @DisplayName("T03 - BookingNotFoundException returns 404")
    void bookingNotFound_returns404() {
        BookingNotFoundException ex = new BookingNotFoundException("BXS-missing");

        ResponseEntity<ErrorResponseDto> response = handler.handleBookingNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("404", response.getBody().getErrorStatus());
        assertTrue(response.getBody().getMessage().contains("BXS-missing"));
    }

    @Test
    @DisplayName("T04 - SeatUnavailableException returns 409")
    void seatUnavailable_returns409() {
        SeatUnavailableException ex = new SeatUnavailableException(10L);

        ResponseEntity<ErrorResponseDto> response = handler.handleSeatUnavailable(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("409", response.getBody().getErrorStatus());
    }

    @Test
    @DisplayName("T05 - PaymentProcessingException returns 502")
    void paymentError_returns502() {
        PaymentProcessingException ex = new PaymentProcessingException("Gateway timeout");

        ResponseEntity<ErrorResponseDto> response = handler.handlePaymentError(ex, request);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("502", response.getBody().getErrorStatus());
        assertTrue(response.getBody().getMessage().contains("Gateway timeout"));
    }

    @Test
    @DisplayName("T06 - MethodArgumentNotValidException returns 400")
    void validationError_returns400() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("bookingRequest", "userId", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponseDto> response = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("400", response.getBody().getErrorStatus());
        assertTrue(response.getBody().getMessage().contains("userId"));
    }

    @Test
    @DisplayName("T07 - MethodArgumentNotValidException with no field errors returns default message")
    void validationError_noFieldErrors_returnsDefault() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponseDto> response = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
    }

    @Test
    @DisplayName("T08 - Generic exception returns 500")
    void genericException_returns500() {
        Exception ex = new RuntimeException("Unexpected failure");

        ResponseEntity<ErrorResponseDto> response = handler.handleGeneral(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("500", response.getBody().getErrorStatus());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    @DisplayName("T09 - Error response includes request path")
    void errorResponse_includesPath() {
        when(request.getRequestURI()).thenReturn("/api/v1/shows/1/seats");
        SeatNotFoundException ex = new SeatNotFoundException(42L);

        ResponseEntity<ErrorResponseDto> response = handler.handleSeatNotFound(ex, request);

        assertEquals("/api/v1/shows/1/seats", response.getBody().getPath());
    }

    @Test
    @DisplayName("T10 - ShowNotFoundException with Long ID returns 404")
    void showNotFoundLongId_returns404() {
        ShowNotFoundException ex = new ShowNotFoundException(42L);

        ResponseEntity<ErrorResponseDto> response = handler.handleShowNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("42"));
    }
}
