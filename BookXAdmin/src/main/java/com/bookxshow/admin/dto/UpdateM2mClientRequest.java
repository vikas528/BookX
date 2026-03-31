/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.dto;

import java.util.Set;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for updating an existing M2M OAuth2 client.
 * All fields are optional — only provided fields trigger an update.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateM2mClientRequest {

    /** Updated human-readable client name. */
    private String clientName;

    /** New client secret for rotation (plaintext — will be BCrypt-hashed). */
    @Size(min = 8, message = "Client secret must be at least 8 characters")
    private String clientSecret;

    /** Updated OAuth2 scopes. Replaces existing scopes entirely. */
    private Set<String> scopes;

    /** Updated access token time-to-live in minutes. */
    @Min(value = 1, message = "Token TTL must be at least 1 minute")
    @Max(value = 1440, message = "Token TTL must not exceed 1440 minutes (24 hours)")
    private Integer accessTokenTtlMinutes;
}
