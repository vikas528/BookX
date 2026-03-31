/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.common;

/**
 * Central routing constants for the BookXShow Seat Booking Service.
 *
 * <p>All path strings used in {@code @RequestMapping} / {@code @GetMapping} /
 * {@code @PostMapping} annotations and in the security filter-chain configuration
 * must reference these constants so that route changes propagate automatically.</p>
 *
 * <h3>Full API surface</h3>
 * <pre>
 *   GET  /bookxshow/v1/shows                                 — list shows (filterable)
 *   GET  /bookxshow/v1/shows/{showId}/seats                  — list seats for a show
 *   GET  /bookxshow/v1/shows/{showId}/seats/{seatId}         — get seat status
 *   POST /bookxshow/v1/bookings                              — book a seat
 *   GET  /bookxshow/v1/bookings/{bookingReference}           — get booking by reference
 *   GET  /bookxshow/v1/users/{userId}/bookings               — get all bookings for a user
 *   POST /bookxshow/v1/admin/shows/{externalShowId}/sync     — sync show from Admin Service
 * </pre>
 *
 * @since 1.0.0
 */
public final class Routes {

    private Routes() {}

    // ── Root segments ──────────────────────────────────────────────────
    public static final String ROOT    = "/bookxshow";
    public static final String ROOT_V1 = ROOT + "/v1";

    // ── Bookings ───────────────────────────────────────────────────────
    public static final String BOOKINGS           = "/bookings";
    public static final String BOOKINGS_REFERENCE = BOOKINGS + "/{bookingReference}";

    // ── Shows & Seats ──────────────────────────────────────────────────
    public static final String SHOWS       = "/shows";
    public static final String SHOWS_SEATS = SHOWS + "/{showId}/seats";
    public static final String SHOWS_SEAT  = SHOWS + "/{showId}/seats/{seatId}";

    // ── Users ──────────────────────────────────────────────────────────
    public static final String USERS          = "/users";
    public static final String USER_BOOKINGS  = USERS + "/{userId}/bookings";

    // ── Admin ──────────────────────────────────────────────────────────
    public static final String ADMIN            = "/admin";
    public static final String ADMIN_SHOWS_SYNC = ADMIN + "/shows/{externalShowId}/sync";

    // ── Security matcher patterns (Ant-style wildcards) ────────────────
    public static final String BOOKINGS_MATCHER = ROOT_V1 + BOOKINGS + "/**";
    public static final String SHOWS_MATCHER    = ROOT_V1 + SHOWS + "/**";
    public static final String USERS_MATCHER    = ROOT_V1 + USERS + "/**";
    public static final String ADMIN_MATCHER    = ROOT_V1 + ADMIN + "/**";

    // ── Actuator ───────────────────────────────────────────────────────
    public static final String ACTUATOR_HEALTH = "/actuator/health";
    public static final String ACTUATOR_INFO   = "/actuator/info";
}
