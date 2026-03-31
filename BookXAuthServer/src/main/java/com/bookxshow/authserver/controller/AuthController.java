/*
 * BookXShow - Authorization Server
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.authserver.controller;

import com.bookxshow.authserver.common.Routes;
import com.bookxshow.authserver.dto.AuthResponse;
import com.bookxshow.authserver.dto.LoginRequest;
import com.bookxshow.authserver.dto.SignupRequest;
import com.bookxshow.authserver.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user authentication — signup and login.
 *
 * <p>These endpoints are <strong>public</strong> (no JWT required) and return
 * a signed JWT access token on success.</p>
 *
 * <h3>Endpoints</h3>
 * <ul>
 *   <li>{@code POST /api/auth/signup} — Register a new user</li>
 *   <li>{@code POST /api/auth/login}  — Authenticate and get a JWT</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping(Routes.AUTH)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user account.
     *
     * <p>Delegates all business logic to {@link AuthService}. Domain violations
     * (duplicate username/email) surface as typed exceptions and are translated
     * to error responses by the global exception handler.</p>
     *
     * @param request the signup details
     * @return 201 Created with JWT access token + user info
     */
    @PostMapping(Routes.SIGNUP)
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request for username: {}", request.getUsername());
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate a user and return a JWT access token.
     *
     * <p>Delegates all logic to {@link AuthService}. Invalid credentials or a
     * disabled account surface as typed exceptions handled globally.</p>
     *
     * @param request the login credentials
     * @return 200 OK with JWT access token + user info
     */
    @PostMapping(Routes.LOGIN)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
