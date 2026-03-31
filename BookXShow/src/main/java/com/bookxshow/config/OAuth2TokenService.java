/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Service responsible for acquiring and caching OAuth2 access tokens
 * using the client_credentials grant type.
 *
 * @since 1.0.0
 */
package com.bookxshow.config;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Acquires and caches OAuth2 bearer tokens using the
 * <em>client_credentials</em> grant type.
 *
 * <p>Tokens are cached in memory and automatically refreshed when they
 * expire. A 30-second safety buffer is applied so that a token is
 * considered expired slightly before its actual expiry time.</p>
 *
 * <p>Thread safety is guaranteed via {@code synchronized} access to the
 * cached token state.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class OAuth2TokenService {

    private static final Logger log = LoggerFactory.getLogger(OAuth2TokenService.class);

    /** Safety buffer (seconds) subtracted from token expiry to avoid edge-case staleness. */
    private static final long EXPIRY_BUFFER_SECONDS = 30;

    private final WebClient webClient;
    private final BookingProperties properties;

    /** Currently cached access token (may be {@code null}). */
    private String cachedToken;

    /** Instant at which the cached token is considered expired. */
    private Instant tokenExpiry = Instant.EPOCH;

    /**
     * Constructs the service with required dependencies.
     *
     * @param webClient  shared HTTP client
     * @param properties application configuration containing OAuth2 credentials
     */
    public OAuth2TokenService(WebClient webClient, BookingProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    /**
     * Returns a valid OAuth2 access token, fetching a new one from the
     * authorization server if the cached token has expired.
     *
     * @return a bearer access token string
     * @throws IllegalStateException if the token cannot be obtained
     */
    public synchronized String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }
        return refreshToken();
    }

    /**
     * Requests a fresh access token from the OAuth2 token endpoint.
     *
     * @return the new access token
     * @throws IllegalStateException if the request fails or the response is invalid
     */
    @SuppressWarnings("unchecked")
    private String refreshToken() {
        BookingProperties.Auth auth = properties.getAuth();

        try {
            Map<String, Object> responseBody = webClient.post()
                    .uri(auth.getTokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                            .with("client_id", auth.getClientId())
                            .with("client_secret", auth.getClientSecret())
                            .with("scope", auth.getScope()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseBody == null || !responseBody.containsKey("access_token")) {
                throw new IllegalStateException(
                        "OAuth2 token response missing 'access_token' field");
            }

            cachedToken = (String) responseBody.get("access_token");

            long expiresIn = responseBody.containsKey("expires_in")
                    ? ((Number) responseBody.get("expires_in")).longValue()
                    : 3600L;
            tokenExpiry = Instant.now().plusSeconds(expiresIn - EXPIRY_BUFFER_SECONDS);

            log.debug("OAuth2 access token acquired, expires in {} seconds", expiresIn);
            return cachedToken;

        } catch (WebClientResponseException ex) {
            log.error("Failed to obtain OAuth2 access token: {}", ex.getMessage());
            throw new IllegalStateException(
                    "Unable to obtain OAuth2 access token from " + auth.getTokenUrl(), ex);
        }
    }
}
