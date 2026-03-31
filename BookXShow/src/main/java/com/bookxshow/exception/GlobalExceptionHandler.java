/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Global exception handler that translates domain exceptions into
 * consistent JSON error responses.
 *
 * @since 1.0.0
 */
package com.bookxshow.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.bookxshow.common.Constants;
import com.bookxshow.dto.ErrorResponseDto;

/**
 * Centralised exception handler for all REST controllers.
 *
 * <p>Maps domain-specific exceptions to appropriate HTTP status codes
 * and returns a uniform {@link ErrorResponseDto} body so that clients
 * can rely on a consistent error contract.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles seat not found exceptions.
     *
     * @param ex      the exception
     * @param request the originating HTTP request
     * @return 404 Not Found response
     */
    @ExceptionHandler(SeatNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleSeatNotFound(
            SeatNotFoundException ex, HttpServletRequest request) {
        log.warn("Seat not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles show not found exceptions.
     *
     * @param ex      the exception
     * @param request the originating HTTP request
     * @return 404 Not Found response
     */
    @ExceptionHandler(ShowNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleShowNotFound(
            ShowNotFoundException ex, HttpServletRequest request) {
        log.warn("Show not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles booking not found exceptions.
     *
     * @param ex      the exception
     * @param request the originating HTTP request
     * @return 404 Not Found response
     */
    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleBookingNotFound(
            BookingNotFoundException ex, HttpServletRequest request) {
        log.warn("Booking not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles seat unavailable exceptions.
     *
     * @param ex      the exception
     * @param request the originating HTTP request
     * @return 409 Conflict response
     */
    @ExceptionHandler(SeatUnavailableException.class)
    public ResponseEntity<ErrorResponseDto> handleSeatUnavailable(
            SeatUnavailableException ex, HttpServletRequest request) {
        log.info("Seat unavailable: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Handles payment processing exceptions.
     *
     * @param ex      the exception
     * @param request the originating HTTP request
     * @return 502 Bad Gateway response
     */
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentError(
            PaymentProcessingException ex, HttpServletRequest request) {
        log.error("Payment processing error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    /**
     * Handles validation errors from {@code @Valid} annotated request bodies.
     *
     * @param ex      the validation exception
     * @param request the originating HTTP request
     * @return 400 Bad Request response with the first validation error
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse(Constants.ERR_VALIDATION_FAILED);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    /** 404 — URL does not match any endpoint. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResource(
            NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("No resource found at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "No endpoint found for " + ex.getHttpMethod() + " " + ex.getResourcePath(), request);
    }

    /** 405 — HTTP method not allowed on the matched endpoint. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not allowed at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
    }

    /** 415 — request Content-Type is not accepted by the endpoint. */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        log.warn("Unsupported media type at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), request);
    }

    /** 400 — request body is missing, empty, or cannot be parsed. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Unreadable request body at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed or missing request body", request);
    }

    /** 400 — required query parameter is absent. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing request parameter at {}: {}", request.getRequestURI(), ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST, "Missing required parameter: " + ex.getParameterName(), request);
    }

    /** 400 — path or query parameter has the wrong type (e.g. string where long expected). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String msg = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'";
        log.warn("Type mismatch at {}: {}", request.getRequestURI(), msg);
        return buildResponse(HttpStatus.BAD_REQUEST, msg, request);
    }

    /** 400 — {@code @Validated} constraint violation on a controller method parameter. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        String msg = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .findFirst()
                .orElse("Constraint violation");
        log.warn("Constraint violation at {}: {}", request.getRequestURI(), msg);
        return buildResponse(HttpStatus.BAD_REQUEST, msg, request);
    }

    /**
     * Handles access denied exceptions thrown by method-level security
     * ({@code @PreAuthorize}).
     *
     * @param ex      the exception
     * @param request the originating HTTP request
     * @return 403 Forbidden response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied: " + ex.getMessage(), request);
    }

    /**
     * Fallback handler for any unexpected exception.
     *
     * @param ex      the exception
     * @param request the originating HTTP request
     * @return 500 Internal Server Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneral(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, Constants.ERR_UNEXPECTED, request);
    }

    // ── Helper ─────────────────────────────────────────────────────────

    /**
     * Builds a standardized error response.
     *
     * <p>{@code errorStatus} is the numeric HTTP status code as a string
     * (e.g. {@code "404"}, {@code "409"}, {@code "500"}).</p>
     */
    private ResponseEntity<ErrorResponseDto> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        String errorStatus = String.valueOf(status.value());
        ErrorResponseDto body = new ErrorResponseDto(errorStatus, message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
