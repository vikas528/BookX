/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.common;

/**
 * Application-wide constants for the BookXAdmin service.
 *
 * <p>Centralises OAuth2 scope authority names, error messages,
 * and other magic strings.</p>
 *
 * @since 1.0.0
 */
public final class Constants {

    private Constants() {}

    // ── OAuth2 Scope Authorities ───────────────────────────────────────
    /** Spring Security authority name for the read scope. */
    public static final String SCOPE_READ  = "SCOPE_bookxshow.read";

    /** Spring Security authority name for the write scope. */
    public static final String SCOPE_WRITE = "SCOPE_bookxshow.write";

    /** Spring Security authority name for the admin scope. */
    public static final String SCOPE_ADMIN = "SCOPE_bookxshow.admin";

    // ── Error Messages ─────────────────────────────────────────────────
    public static final String ERR_UNEXPECTED        = "An unexpected error occurred";
    public static final String ERR_VALIDATION_FAILED = "Validation failed";

    /** Prefix — append the show ID: {@code ERR_SHOW_NOT_FOUND + showId}. */
    public static final String ERR_SHOW_NOT_FOUND    = "Show not found with ID: ";

    /** Prefix — append the external show ID. */
    public static final String ERR_SHOW_NOT_FOUND_EXT = "Show not found with external ID: ";

    /** Prefix — append the show name. */
    public static final String ERR_SHOW_ALREADY_EXISTS = "Show already exists with name: ";

    /** Prefix — append the current status. */
    public static final String ERR_INVALID_SHOW_STATE  = "Invalid show state transition from: ";

    // ── M2M Client Error Messages ──────────────────────────────────────
    /** Prefix — append the client ID. */
    public static final String ERR_CLIENT_NOT_FOUND     = "M2M client not found with ID: ";

    /** Prefix — append the client ID. */
    public static final String ERR_CLIENT_ALREADY_EXISTS = "M2M client already exists with ID: ";

    /** Seat number format pattern. */
    public static final String SEAT_NUMBER_FORMAT = "%s%02d";

    /** Row labels used for seat generation (A-Z). */
    public static final String ROW_LABELS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
}
