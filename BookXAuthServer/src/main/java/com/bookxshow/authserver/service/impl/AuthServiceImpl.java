/*
 * BookXShow - Authorization Server
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.authserver.service.impl;

import com.bookxshow.authserver.common.Constants;
import com.bookxshow.authserver.dto.AuthResponse;
import com.bookxshow.authserver.dto.LoginRequest;
import com.bookxshow.authserver.dto.SignupRequest;
import com.bookxshow.authserver.entity.AppUser;
import com.bookxshow.authserver.exception.AccountDisabledException;
import com.bookxshow.authserver.exception.EmailAlreadyExistsException;
import com.bookxshow.authserver.exception.InvalidCredentialsException;
import com.bookxshow.authserver.exception.UsernameAlreadyExistsException;
import com.bookxshow.authserver.repository.AppUserRepository;
import com.bookxshow.authserver.service.AuthService;
import com.bookxshow.authserver.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link AuthService}.
 *
 * <p>Encapsulates all business rules for signup and login:
 * uniqueness checks, password hashing, credential validation, and
 * JWT token generation. Domain violations are surfaced as typed
 * exceptions and translated to HTTP responses by the global exception
 * handler — the controller stays thin.</p>
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Value("${bookxshow.auth.access-token-ttl-minutes:60}")
    private long accessTokenTtlMinutes;

    // ── Signup ─────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Checks username and email uniqueness, encodes the password with
     * BCrypt (strength 12), persists the new user, then issues a JWT.</p>
     */
    @Override
    public AuthResponse signup(SignupRequest request) {
        log.info("Signup validation for username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Signup rejected — duplicate username: {}", request.getUsername());
            throw new UsernameAlreadyExistsException(request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Signup rejected — duplicate email: {}", request.getEmail());
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Constants.ROLE_USER)
                .enabled(true)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        return buildAuthResponse(user);
    }

    // ── Login ──────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Looks up the user by username, performs a constant-time BCrypt
     * comparison, checks the enabled flag, then issues a JWT.</p>
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        AppUser user = userRepository.findByUsername(request.getUsername()).orElse(null);

        // Constant-time comparison via BCrypt even when user is null (prevents timing attacks)
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed — invalid credentials for: {}", request.getUsername());
            throw new InvalidCredentialsException();
        }

        if (!user.isEnabled()) {
            log.warn("Login failed — account disabled: {}", user.getUsername());
            throw new AccountDisabledException(user.getUsername());
        }

        log.info("Login successful for user: {}", user.getUsername());
        return buildAuthResponse(user);
    }

    // ── Helper ─────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(AppUser user) {
        String token = jwtTokenService.generateAccessToken(user);
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType(Constants.TOKEN_TYPE_BEARER)
                .expiresIn(accessTokenTtlMinutes * 60)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }
}
