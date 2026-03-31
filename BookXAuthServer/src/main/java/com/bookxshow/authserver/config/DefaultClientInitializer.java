/*
 * BookXShow - Authorization Server
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Seeds the default OAuth2 clients into the JDBC-backed client repository
 * on application startup. Clients are only created if they do not already
 * exist, making this initializer idempotent across restarts.
 *
 * @since 1.0.0
 */
package com.bookxshow.authserver.config;

import java.time.Duration;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import com.bookxshow.authserver.common.Constants;

/**
 * Seeds the four default OAuth2 registered clients on startup:
 * <ol>
 *   <li><strong>Gateway Client</strong> — Authorization Code + Refresh Token</li>
 *   <li><strong>M2M Client</strong> — Client Credentials for external API access</li>
 *   <li><strong>Service Client</strong> — Client Credentials for internal S2S</li>
 *   <li><strong>Admin Client</strong> — Client Credentials for Admin Service</li>
 * </ol>
 *
 * <p>Each client is only created if it does not already exist in the
 * repository, making restarts safe.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class DefaultClientInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultClientInitializer.class);

    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${bookxshow.auth.access-token-ttl-minutes:60}")
    private long accessTokenTtlMinutes;

    @Value("${bookxshow.auth.refresh-token-ttl-hours:8}")
    private long refreshTokenTtlHours;

    @Value("${bookxshow.auth.gateway-client-id:bookxshow-gateway}")
    private String gatewayClientId;

    @Value("${bookxshow.auth.m2m-client-id:bookxshow-m2m}")
    private String m2mClientId;

    @Value("${bookxshow.auth.service-client-id:bookxshow-service}")
    private String serviceClientId;

    @Value("${bookxshow.auth.admin-client-id:bookxshow-admin}")
    private String adminClientId;

    @Value("${bookxshow.auth.gateway-client-secret:gateway-secret}")
    private String gatewayClientSecret;

    @Value("${bookxshow.auth.service-client-secret:service-secret}")
    private String serviceClientSecret;

    @Value("${bookxshow.auth.m2m-client-secret:m2m-secret}")
    private String m2mClientSecret;

    @Value("${bookxshow.auth.admin-client-secret:admin-secret}")
    private String adminClientSecret;

    @Value("${bookxshow.auth.gateway-redirect-uri:http://localhost:8443/login/oauth2/code/bookxshow-auth-server}")
    private String gatewayRedirectUri;

    @Value("${bookxshow.auth.gateway-post-logout-uri:http://localhost:8443/}")
    private String gatewayPostLogoutUri;

    public DefaultClientInitializer(RegisteredClientRepository registeredClientRepository,
                                    PasswordEncoder passwordEncoder) {
        this.registeredClientRepository = registeredClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedIfAbsent(buildGatewayClient());
        seedIfAbsent(buildM2mClient());
        seedIfAbsent(buildServiceClient());
        seedIfAbsent(buildAdminClient());
        log.info("Default OAuth2 client initialization complete");
    }

    private void seedIfAbsent(RegisteredClient client) {
        if (registeredClientRepository.findByClientId(client.getClientId()) == null) {
            registeredClientRepository.save(client);
            log.info("Seeded default client: {}", client.getClientId());
        } else {
            log.debug("Client already exists, skipping seed: {}", client.getClientId());
        }
    }

    // ── Gateway Client (Authorization Code + Refresh) ──────────────────

    private RegisteredClient buildGatewayClient() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(gatewayClientId)
                .clientSecret(passwordEncoder.encode(gatewayClientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(gatewayRedirectUri)
                .postLogoutRedirectUri(gatewayPostLogoutUri)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(Constants.SCOPE_READ)
                .scope(Constants.SCOPE_WRITE)
                .scope(Constants.SCOPE_ADMIN)
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(accessTokenTtlMinutes))
                        .refreshTokenTimeToLive(Duration.ofHours(refreshTokenTtlHours))
                        .build())
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .build())
                .build();
    }

    // ── M2M Client (Client Credentials — external API access / Postman) ─

    private RegisteredClient buildM2mClient() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(m2mClientId)
                .clientSecret(passwordEncoder.encode(m2mClientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope(Constants.SCOPE_READ)
                .scope(Constants.SCOPE_WRITE)
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(accessTokenTtlMinutes))
                        .build())
                .build();
    }

    // ── Service Client (Client Credentials — internal S2S) ─────────────

    private RegisteredClient buildServiceClient() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(serviceClientId)
                .clientSecret(passwordEncoder.encode(serviceClientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope(Constants.SCOPE_READ)
                .scope(Constants.SCOPE_WRITE)
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .build())
                .build();
    }

    // ── Admin Client (Client Credentials — Admin Service M2M) ──────────

    private RegisteredClient buildAdminClient() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(adminClientId)
                .clientSecret(passwordEncoder.encode(adminClientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope(Constants.SCOPE_READ)
                .scope(Constants.SCOPE_WRITE)
                .scope(Constants.SCOPE_ADMIN)
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(accessTokenTtlMinutes))
                        .build())
                .build();
    }
}
