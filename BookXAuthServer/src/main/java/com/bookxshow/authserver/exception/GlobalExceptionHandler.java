/*
 * BookXShow - Authorization Server
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.authserver.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.bookxshow.authserver.common.Constants;
import com.bookxshow.authserver.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Centralised exception handler for all REST controllers in the Auth Server.
 *
 * <p>Maps every domain and framework exception to an appropriate HTTP status
 * and returns a uniform {@link ErrorResponse} body. The controller stays
 * completely free of error-response construction.</p>
 *
 * <h3>Exception-to-status mapping</h3>
 * <table>
 *   <tr><th>Exception</th><th>HTTP</th></tr>
 *   <tr><td>{@link UsernameAlreadyExistsException}</td><td>409</td></tr>
 *   <tr><td>{@link EmailAlreadyExistsException}</td><td>409</td></tr>
 *   <tr><td>{@link InvalidCredentialsException}</td><td>401</td></tr>
 *   <tr><td>{@link AccountDisabledException}</td><td>403</td></tr>
 *   <tr><td>{@link MethodArgumentNotValidException}</td><td>400</td></tr>
 *   <tr><td>{@link Exception} (fallback)</td><td>500</td></tr>
 * </table>
 *
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Auth domain exceptions ─────────────────────────────────────────

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameExists(
            UsernameAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Signup rejected — duplicate username at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(
            EmailAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Signup rejected — duplicate email at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {
        log.warn("Login failed — invalid credentials at {}", request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ErrorResponse> handleAccountDisabled(
            AccountDisabledException ex, HttpServletRequest request) {
        log.warn("Login failed — account disabled at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    // ── Validation ─────────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse(Constants.ERR_VALIDATION_FAILED);
        log.warn("Validation error at {}: {}", request.getRequestURI(), message);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // ── Spring MVC 4xx ─────────────────────────────────────────────────

    /** 404 — URL does not match any endpoint. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(
            NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("No resource found at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "No endpoint found for " + ex.getHttpMethod() + " " + ex.getResourcePath(), request);
    }

    /** 405 — HTTP method not allowed on the matched endpoint. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not allowed at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
    }

    /** 415 — request Content-Type is not accepted by the endpoint. */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        log.warn("Unsupported media type at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), request);
    }

    /** 400 — request body is missing, empty, or cannot be parsed. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Unreadable request body at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed or missing request body", request);
    }

    /** 400 — required query parameter is absent. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing request parameter at {}: {}", request.getRequestURI(), ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST, "Missing required parameter: " + ex.getParameterName(), request);
    }

    /** 400 — path or query parameter has the wrong type. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String msg = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'";
        log.warn("Type mismatch at {}: {}", request.getRequestURI(), msg);
        return buildResponse(HttpStatus.BAD_REQUEST, msg, request);
    }

    /** 400 — {@code @Validated} constraint violation on a controller method parameter. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        String msg = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .findFirst()
                .orElse("Constraint violation");
        log.warn("Constraint violation at {}: {}", request.getRequestURI(), msg);
        return buildResponse(HttpStatus.BAD_REQUEST, msg, request);
    }

    // ── Fallback ───────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, Constants.ERR_UNEXPECTED, request);
    }

    // ── Helper ─────────────────────────────────────────────────────────

    /**
     * Builds a standardized error response entity.
     *
     * <p>{@code errorStatus} is the numeric HTTP status code as a string
     * (e.g. {@code "404"}, {@code "409"}, {@code "500"}).</p>
     */
    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        String errorStatus = String.valueOf(status.value());
        ErrorResponse body = ErrorResponse.builder()
                .errorStatus(errorStatus)
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
