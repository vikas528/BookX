/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.dto;

import java.util.Set;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating a new M2M (client_credentials) OAuth2 client.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateM2mClientRequest {

    /** Unique client identifier used for authentication. */
    @NotBlank(message = "Client ID is required")
    private String clientId;

    /** Client secret (plaintext — will be BCrypt-hashed before storage). */
    @NotBlank(message = "Client secret is required")
    @Size(min = 8, message = "Client secret must be at least 8 characters")
    private String clientSecret;

    /** Human-readable client name. Defaults to clientId if omitted. */
    private String clientName;

    /** OAuth2 scopes this client is allowed to request. */
    @NotEmpty(message = "At least one scope is required")
    private Set<String> scopes;

    /** Access token time-to-live in minutes (1–1440, default 60). */
    @Min(value = 1, message = "Token TTL must be at least 1 minute")
    @Max(value = 1440, message = "Token TTL must not exceed 1440 minutes (24 hours)")
    @Builder.Default
    private int accessTokenTtlMinutes = 60;
}
