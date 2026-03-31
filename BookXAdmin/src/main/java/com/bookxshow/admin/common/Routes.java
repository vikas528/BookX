/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.common;

/**
 * Central routing constants for the BookXAdmin service.
 *
 * <p>All path strings used in {@code @RequestMapping} annotations and
 * security filter-chain matchers must reference these constants.</p>
 *
 * <h3>Full API surface</h3>
 * <pre>
 *   POST   /bookxshow/v1/admin/shows                          — create a show
 *   GET    /bookxshow/v1/admin/shows                          — list all shows
 *   GET    /bookxshow/v1/admin/shows/{showId}                 — get show by ID
 *   PUT    /bookxshow/v1/admin/shows/{showId}                 — update a show
 *   DELETE /bookxshow/v1/admin/shows/{showId}                 — cancel a show
 *   POST   /bookxshow/v1/admin/shows/{showId}/publish         — publish a show
 *   GET    /bookxshow/v1/admin/shows/{showId}/seats            — list seats for a show
 *
 *   POST   /bookxshow/v1/admin/clients                        — register an M2M client
 *   GET    /bookxshow/v1/admin/clients                        — list all M2M clients
 *   GET    /bookxshow/v1/admin/clients/{clientId}              — get client by ID
 *   PUT    /bookxshow/v1/admin/clients/{clientId}              — update an M2M client
 *   DELETE /bookxshow/v1/admin/clients/{clientId}              — delete an M2M client
 *
 *   GET    /bookxshow/v1/shows/{externalShowId}               — read show (service-to-service)
 *   GET    /bookxshow/v1/shows/{externalShowId}/seats          — read seats (service-to-service)
 * </pre>
 *
 * @since 1.0.0
 */
public final class Routes {

    private Routes() {}

    // ── Root segments ──────────────────────────────────────────────────
    public static final String ROOT      = "/bookxshow";
    public static final String ROOT_V1   = ROOT + "/v1";

    // ── Admin endpoints ────────────────────────────────────────────────
    public static final String ADMIN          = "/admin";
    public static final String ADMIN_V1       = ROOT_V1 + ADMIN;
    public static final String ADMIN_SHOWS    = "/shows";
    public static final String ADMIN_SHOW_ID  = ADMIN_SHOWS + "/{showId}";
    public static final String ADMIN_SHOW_PUBLISH = ADMIN_SHOW_ID + "/publish";
    public static final String ADMIN_SHOW_SEATS   = ADMIN_SHOW_ID + "/seats";

    // ── M2M Client management endpoints ────────────────────────────────
    public static final String ADMIN_CLIENTS    = "/clients";
    public static final String ADMIN_CLIENTS_V1 = ADMIN_V1 + ADMIN_CLIENTS;
    public static final String CLIENT_ID        = "/{clientId}";

    // ── Service-to-service read endpoints ──────────────────────────────
    public static final String SHOWS_V1          = ROOT_V1 + "/shows";
    public static final String SHOW_BY_EXT_ID    = "/{externalShowId}";
    public static final String SHOW_SEATS_BY_EXT = "/{externalShowId}/seats";

    // ── Security matcher patterns (Ant-style wildcards) ────────────────
    public static final String ADMIN_MATCHER  = ROOT_V1 + ADMIN + "/**";
    public static final String SHOWS_MATCHER  = ROOT_V1 + "/shows/**";
    public static final String CLIENTS_MATCHER = ROOT_V1 + ADMIN + "/clients/**";

    // ── Actuator ───────────────────────────────────────────────────────
    public static final String ACTUATOR_HEALTH = "/actuator/health";
    public static final String ACTUATOR_INFO   = "/actuator/info";
}
