/*
 * BookXShow - Authorization Server
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.authserver.common;

/**
 * Central routing constants for the BookXShow Authorization Server.
 *
 * <p>All path strings used in {@code @RequestMapping} annotations,
 * security filter-chain matchers, and test utilities must reference
 * these constants so that route changes are made in one place only.</p>
 *
 * <h3>Full paths exposed by this service</h3>
 * <pre>
 *   POST /bookxshow/v1/auth/signup   — register a new user
 *   POST /bookxshow/v1/auth/login    — authenticate and obtain a JWT
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
    /** Base path for all login/signup endpoints: {@code /bookxshow/v1/auth}. */
    public static final String AUTH = ROOT_V1 + "/auth";

    /** Ant-pattern wildcard for the auth namespace (used in security matchers). */
    public static final String AUTH_PATTERN = AUTH + "/**";

    /** Signup endpoint segment: {@code /signup}. */
    public static final String SIGNUP = "/signup";

    /** Login endpoint segment: {@code /login}. */
    public static final String LOGIN = "/login";

    // ── Actuator ───────────────────────────────────────────────────────
    public static final String ACTUATOR_HEALTH = "/actuator/health";
    public static final String ACTUATOR_INFO   = "/actuator/info";
}
