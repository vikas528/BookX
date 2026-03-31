/*
 * BookXShow - Authorization Server
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.authserver.service;

import com.bookxshow.authserver.dto.AuthResponse;
import com.bookxshow.authserver.dto.LoginRequest;
import com.bookxshow.authserver.dto.SignupRequest;
import com.bookxshow.authserver.exception.AccountDisabledException;
import com.bookxshow.authserver.exception.EmailAlreadyExistsException;
import com.bookxshow.authserver.exception.InvalidCredentialsException;
import com.bookxshow.authserver.exception.UsernameAlreadyExistsException;

/**
 * Service contract for user authentication operations.
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Validate uniqueness of username / email on signup.</li>
 *   <li>Store a BCrypt-encoded password.</li>
 *   <li>Validate credentials on login.</li>
 *   <li>Issue a signed JWT access token on success.</li>
 * </ul>
 *
 * <p>Domain exceptions declared below are caught by
 * {@link com.bookxshow.authserver.exception.GlobalExceptionHandler} and
 * serialised into a uniform {@link com.bookxshow.authserver.dto.ErrorResponse}.</p>
 *
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * Registers a new user and returns a JWT access token.
     *
     * @param request the validated signup payload
     * @return the JWT token and user details
     * @throws UsernameAlreadyExistsException if the username is taken
     * @throws EmailAlreadyExistsException    if the email is already registered
     */
    AuthResponse signup(SignupRequest request);

    /**
     * Authenticates a user and returns a JWT access token.
     *
     * @param request the validated login credentials
     * @return the JWT token and user details
     * @throws InvalidCredentialsException if username or password is wrong
     * @throws AccountDisabledException    if the account is inactive
     */
    AuthResponse login(LoginRequest request);
}
