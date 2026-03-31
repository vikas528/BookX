/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Typesafe configuration properties bound from application.yaml.
 *
 * @since 1.0.0
 */
package com.bookxshow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds the {@code bookxshow.*} configuration tree from
 * {@code application.yaml} into a strongly-typed POJO.
 *
 * <p>This class follows the <em>Externalized Configuration</em> pattern,
 * avoiding scattered {@code @Value} annotations throughout the codebase.
 * Every configuration group (booking, admin-service, payment) is represented
 * by an inner class for clear structure.</p>
 *
 * <p>All secrets default to placeholder values; production deployments
 * must supply real values via environment variables.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "bookxshow")
public class BookingProperties {

    /** Booking-related settings.
     * -- GETTER --
     * Returns the booking settings.
     * -- SETTER --
     * Sets the booking settings.

     */
    private Booking booking = new Booking();

    /** OAuth2 client credentials for external service calls.
     * -- GETTER --
     * Returns the OAuth2 auth settings.
     * -- SETTER --
     * Sets the OAuth2 auth settings.

     */
    private Auth auth = new Auth();

    /** Admin Service integration settings.
     * -- GETTER --
     * Returns the admin-service settings.
     * -- SETTER --
     * Sets the admin-service settings.

     */
    private AdminService adminService = new AdminService();

    /** Payment gateway integration settings.
     * -- GETTER --
     * Returns the payment settings.
     * -- SETTER --
     * Sets the payment settings.

     */
    private Payment payment = new Payment();

  // ── Inner configuration classes ────────────────────────────────────

    /**
     * Settings related to the booking and seat-lock mechanism.
     */
    @Setter
    @Getter
    public static class Booking {

        /** How long (ms) a seat stays locked while awaiting payment.
         * -- GETTER --
         * Returns the seat lock TTL in milliseconds.
         * -- SETTER --
         * Sets the seat lock TTL in milliseconds.

         */
        private long seatLockTtlMs = 30_000;

        /** Interval (ms) between sweeps for expired seat locks.
         * -- GETTER --
         * Returns the lock sweep interval in milliseconds.
         * -- SETTER --
         * Sets the lock sweep interval in milliseconds.

         */
        private long lockSweepIntervalMs = 5_000;

    }

    /**
     * OAuth2 client credentials used to obtain access tokens when calling
     * external services (Admin Service, Payment Gateway).
     *
     * <p>The service acquires a bearer token from the configured token URL
     * using the <em>client_credentials</em> grant type before each outbound
     * HTTP call.</p>
     */
    @Setter
    @Getter
    public static class Auth {

        /** OAuth2 token endpoint URL.
         * -- GETTER --
         * Returns the token endpoint URL.
         * -- SETTER --
         * Sets the token endpoint URL.

         */
        private String tokenUrl = "https://auth.example.com/oauth2/token";

        /** OAuth2 client identifier.
         * -- GETTER --
         * Returns the client identifier.
         * -- SETTER --
         * Sets the client identifier.

         */
        private String clientId = "changeme";

        /** OAuth2 client secret.
         * -- GETTER --
         * Returns the client secret.
         * -- SETTER --
         * Sets the client secret.

         */
        private String clientSecret = "changeme";

        /** Space-delimited OAuth2 scopes requested during token acquisition.
         * -- GETTER --
         * Returns the OAuth2 scopes.
         * -- SETTER --
         * Sets the OAuth2 scopes.

         */
        private String scope = "bookxshow.read bookxshow.write";

    }

    /**
     * Settings for connecting to the external Admin Service.
     */
    @Setter
    @Getter
    public static class AdminService {

        /** Base URL of the Admin Service REST API.
         * -- GETTER --
         * Returns the base URL.
         * -- SETTER --
         * Sets the base URL.

         */
        private String baseUrl = "http://localhost:8081";

        /** URI path for fetching shows from the Admin Service.
         * -- GETTER --
         * Returns the shows URI path.
         * -- SETTER --
         * Sets the shows URI path.

         */
        private String showsUri = "/bookxshow/v1/shows";

        /** Connection timeout in milliseconds.
         * -- GETTER --
         * Returns the timeout in milliseconds.
         * -- SETTER --
         * Sets the timeout in milliseconds.

         */
        private long timeoutMs = 10_000;

    }

    /**
     * Settings for connecting to the external Payment Gateway.
     */
    @Setter
    @Getter
    public static class Payment {

        /** Whether to use a stub payment gateway that always succeeds.
         * -- GETTER --
         * Returns whether stub mode is enabled.
         * -- SETTER --
         * Sets whether stub mode is enabled.

         */
        private boolean stubEnabled = true;

        /** Base URL of the Payment Gateway REST API.
         * -- GETTER --
         * Returns the gateway URL.
         * -- SETTER --
         * Sets the gateway URL.

         */
        private String gatewayUrl = "http://localhost:8082";

        /** URI path for the charge endpoint on the Payment Gateway.
         * -- GETTER --
         * Returns the charge URI path.
         * -- SETTER --
         * Sets the charge URI path.

         */
        private String chargeUri = "/api/v1/payments/charge";

        /** Request timeout in milliseconds.
         * -- GETTER --
         * Returns the timeout in milliseconds.
         * -- SETTER --
         * Sets the timeout in milliseconds.

         */
        private long timeoutMs = 30_000;

    }
}
