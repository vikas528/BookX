/*
 * BookXShow - API Gateway
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.gateway.common;

/**
 * Central routing constants for the BookXShow API Gateway.
 *
 * <p>All path patterns used in the security filter chain and
 * application configuration must reference these constants.</p>
 *
 * <h3>Gateway route summary</h3>
 * <pre>
 *   /bookxshow/v1/bookings/**  → booking-service (BookXShow)
 *   /bookxshow/v1/shows/**    → booking-service (BookXShow)
 *   /bookxshow/v1/admin/**    → booking-service (admin sync)
 *   /bookxshow/v1/auth/**     → auth-server (login / signup)
 *   /oauth2/token             → auth-server (M2M token)
 *   /actuator/health          → public
 *   /actuator/info            → public
 * </pre>
 *
 * @since 1.0.0
 */
public final class Routes {

    private Routes() {}

    // ── Root segments ──────────────────────────────────────────────────
    public static final String ROOT    = "/bookxshow";
    public static final String ROOT_V1 = ROOT + "/v1";

    // ── Auth namespace ─────────────────────────────────────────────────
    public static final String AUTH         = ROOT_V1 + "/auth";
    public static final String AUTH_PATTERN = AUTH + "/**";

    // ── Downstream service patterns (Ant-style wildcards) ──────────────
    public static final String BOOKINGS_PATTERN = ROOT_V1 + "/bookings/**";
    public static final String SHOWS_PATTERN    = ROOT_V1 + "/shows/**";
    public static final String ADMIN_PATTERN    = ROOT_V1 + "/admin/**";

    // ── OAuth2 protocol ────────────────────────────────────────────────
    public static final String OAUTH2_TOKEN = "/oauth2/token";

    // ── Actuator ───────────────────────────────────────────────────────
    public static final String ACTUATOR_HEALTH = "/actuator/health";
    public static final String ACTUATOR_INFO   = "/actuator/info";
}
