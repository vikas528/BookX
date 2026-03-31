/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for M2M client information.
 *
 * <p>Never contains the client secret — secrets are write-only.</p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class M2mClientResponse {

    /** Internal UUID (primary key in oauth2_registered_client). */
    private String id;

    /** Client identifier used for authentication. */
    private String clientId;

    /** Human-readable client name. */
    private String clientName;

    /** OAuth2 scopes granted to this client. */
    private List<String> scopes;

    /** Grant types this client supports (e.g., "client_credentials"). */
    private List<String> grantTypes;

    /** Authentication methods (e.g., "client_secret_basic"). */
    private List<String> authenticationMethods;

    /** Access token time-to-live in minutes. */
    private long accessTokenTtlMinutes;

    /** Timestamp when the client ID was issued. */
    private Instant clientIdIssuedAt;
}
