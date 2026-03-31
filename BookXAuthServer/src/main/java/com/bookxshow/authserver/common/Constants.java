/*
 * BookXShow - Authorization Server
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.authserver.common;

/**
 * Application-wide constants for the BookXShow Authorization Server.
 *
 * <p>Centralizes every magic string — roles, scopes, JWT claim names, token
 * metadata, and user-facing error messages — so that changes propagate
 * automatically to every place they are used.</p>
 *
 * @since 1.0.0
 */
public final class Constants {

    private Constants() {}

    // ── Token ──────────────────────────────────────────────────────────
    /** HTTP {@code Authorization} token type returned in auth responses. */
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    // ── Roles ──────────────────────────────────────────────────────────
    public static final String ROLE_USER  = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // ── OAuth2 Scopes ──────────────────────────────────────────────────
    public static final String SCOPE_READ  = "bookxshow.read";
    public static final String SCOPE_WRITE = "bookxshow.write";
    public static final String SCOPE_ADMIN = "bookxshow.admin";

    /** Space-separated scope string granted to regular users. */
    public static final String SCOPES_STANDARD = SCOPE_READ + " " + SCOPE_WRITE;

    /** Space-separated scope string granted to admin users. */
    public static final String SCOPES_ADMIN_FULL = SCOPE_READ + " " + SCOPE_WRITE + " " + SCOPE_ADMIN;

    // ── JWT Claim Names ────────────────────────────────────────────────
    public static final String CLAIM_SCOPE   = "scope";
    public static final String CLAIM_ROLES   = "roles";
    public static final String CLAIM_EMAIL   = "email";
    public static final String CLAIM_USER_ID = "userId";

    // ── Error Messages ─────────────────────────────────────────────────
    public static final String ERR_UNEXPECTED         = "An unexpected error occurred";
    public static final String ERR_VALIDATION_FAILED  = "Validation failed";
    public static final String ERR_INVALID_CREDENTIALS = "Username or password is incorrect";
    public static final String ERR_ACCOUNT_DISABLED   = "Your account has been disabled. Contact support.";

    /** Prefix — append the username: {@code ERR_USERNAME_EXISTS + username}. */
    public static final String ERR_USERNAME_EXISTS    = "Username already exists: ";

    /** Prefix — append the email: {@code ERR_EMAIL_EXISTS + email}. */
    public static final String ERR_EMAIL_EXISTS       = "An account with this email already exists: ";
}
