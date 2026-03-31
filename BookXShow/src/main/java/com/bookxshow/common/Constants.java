/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.common;

/**
 * Application-wide constants for the BookXShow Seat Booking Service.
 *
 * <p>Centralises OAuth2 scope authority names and user-facing error messages
 * so that all magic strings are defined in one place.</p>
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

    // ── Generic Error Messages ─────────────────────────────────────────
    public static final String ERR_UNEXPECTED        = "An unexpected error occurred";
    public static final String ERR_VALIDATION_FAILED = "Validation failed";

    // ── Domain Error Message Prefixes ──────────────────────────────────
    /** Prefix — append the seat ID: {@code ERR_SEAT_NOT_FOUND + seatId}. */
    public static final String ERR_SEAT_NOT_FOUND    = "Seat not found with ID: ";

    /** Prefix — append the external show ID: {@code ERR_SHOW_NOT_FOUND + showId}. */
    public static final String ERR_SHOW_NOT_FOUND    = "Show not found: ";

    /** Prefix — append the show PK: {@code ERR_SHOW_NOT_FOUND_ID + showId}. */
    public static final String ERR_SHOW_NOT_FOUND_ID = "Show not found with ID: ";

    /** Prefix — append the booking reference: {@code ERR_BOOKING_NOT_FOUND + ref}. */
    public static final String ERR_BOOKING_NOT_FOUND = "Booking not found: ";

    /** Prefix — append the seat ID: {@code ERR_SEAT_UNAVAILABLE + seatId}. */
    public static final String ERR_SEAT_UNAVAILABLE  = "Seat is not available for booking: ";

    // ── Admin ──────────────────────────────────────────────────────────
    /** printf-style template; use {@code String.format(SYNC_SUCCESS, externalShowId)}. */
    public static final String SYNC_SUCCESS = "Show %s synced successfully";
}
