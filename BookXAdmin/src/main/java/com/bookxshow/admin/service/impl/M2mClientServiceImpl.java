/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * M2M client management service implementation.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookxshow.admin.common.Constants;
import com.bookxshow.admin.dto.CreateM2mClientRequest;
import com.bookxshow.admin.dto.M2mClientResponse;
import com.bookxshow.admin.dto.UpdateM2mClientRequest;
import com.bookxshow.admin.exception.ClientAlreadyExistsException;
import com.bookxshow.admin.exception.ClientNotFoundException;
import com.bookxshow.admin.service.M2mClientService;

/**
 * Manages OAuth2 M2M client credentials by reading/writing to the
 * {@code oauth2_registered_client} table via {@link RegisteredClientRepository}.
 *
 * <p>For operations not supported by {@link RegisteredClientRepository}
 * (e.g., listing all clients, deleting), raw SQL via {@link JdbcTemplate}
 * is used.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class M2mClientServiceImpl implements M2mClientService {

    private static final Logger log = LoggerFactory.getLogger(M2mClientServiceImpl.class);

    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public M2mClientServiceImpl(RegisteredClientRepository registeredClientRepository,
                                PasswordEncoder passwordEncoder,
                                JdbcTemplate jdbcTemplate) {
        this.registeredClientRepository = registeredClientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    // ── Create ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public M2mClientResponse createClient(CreateM2mClientRequest request) {
        // Guard: check for duplicate client ID
        if (registeredClientRepository.findByClientId(request.getClientId()) != null) {
            throw new ClientAlreadyExistsException(
                    Constants.ERR_CLIENT_ALREADY_EXISTS + request.getClientId());
        }

        String clientName = request.getClientName() != null
                ? request.getClientName()
                : request.getClientId();

        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(request.getClientId())
                .clientSecret(passwordEncoder.encode(request.getClientSecret()))
                .clientName(clientName)
                .clientIdIssuedAt(Instant.now())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scopes(scopes -> request.getScopes().forEach(scopes::add))
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(
                                Duration.ofMinutes(request.getAccessTokenTtlMinutes()))
                        .build())
                .build();

        registeredClientRepository.save(client);
        log.info("Created M2M client: {}", request.getClientId());

        return mapToResponse(client);
    }

    // ── Read ──────────────────────────────────────────────────────────

    @Override
    public List<M2mClientResponse> getAllClients() {
        // RegisteredClientRepository has no findAll — query client_id column directly
        List<String> clientIds = jdbcTemplate.queryForList(
                "SELECT client_id FROM oauth2_registered_client ORDER BY client_name",
                String.class);

        return clientIds.stream()
                .map(registeredClientRepository::findByClientId)
                .filter(Objects::nonNull)
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public M2mClientResponse getClientByClientId(String clientId) {
        RegisteredClient client = registeredClientRepository.findByClientId(clientId);
        if (client == null) {
            throw new ClientNotFoundException(Constants.ERR_CLIENT_NOT_FOUND + clientId);
        }
        return mapToResponse(client);
    }

    // ── Update ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public M2mClientResponse updateClient(String clientId, UpdateM2mClientRequest request) {
        RegisteredClient existing = registeredClientRepository.findByClientId(clientId);
        if (existing == null) {
            throw new ClientNotFoundException(Constants.ERR_CLIENT_NOT_FOUND + clientId);
        }

        // Rebuild the client, preserving existing values for null request fields
        RegisteredClient.Builder builder = RegisteredClient.withId(existing.getId())
                .clientId(existing.getClientId());

        // Client name
        builder.clientName(request.getClientName() != null
                ? request.getClientName()
                : existing.getClientName());

        // Client secret — rotate if provided, otherwise keep existing hash
        builder.clientSecret(request.getClientSecret() != null
                ? passwordEncoder.encode(request.getClientSecret())
                : existing.getClientSecret());

        // Preserve authentication methods
        existing.getClientAuthenticationMethods().forEach(builder::clientAuthenticationMethod);

        // Preserve grant types
        existing.getAuthorizationGrantTypes().forEach(builder::authorizationGrantType);

        // Scopes — replace if provided, otherwise preserve
        if (request.getScopes() != null && !request.getScopes().isEmpty()) {
            builder.scopes(scopes -> request.getScopes().forEach(scopes::add));
        } else {
            builder.scopes(scopes -> existing.getScopes().forEach(scopes::add));
        }

        // Token TTL — update if provided, otherwise preserve
        long ttlMinutes = request.getAccessTokenTtlMinutes() != null
                ? request.getAccessTokenTtlMinutes()
                : existing.getTokenSettings().getAccessTokenTimeToLive().toMinutes();
        builder.tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(ttlMinutes))
                .build());

        // Preserve redirect URIs (relevant for gateway-type clients)
        existing.getRedirectUris().forEach(builder::redirectUri);
        existing.getPostLogoutRedirectUris().forEach(builder::postLogoutRedirectUri);

        RegisteredClient updated = builder.build();
        registeredClientRepository.save(updated);
        log.info("Updated M2M client: {}", clientId);

        return mapToResponse(updated);
    }

    // ── Delete ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteClient(String clientId) {
        RegisteredClient client = registeredClientRepository.findByClientId(clientId);
        if (client == null) {
            throw new ClientNotFoundException(Constants.ERR_CLIENT_NOT_FOUND + clientId);
        }

        jdbcTemplate.update(
                "DELETE FROM oauth2_registered_client WHERE id = ?",
                client.getId());
        log.info("Deleted M2M client: {}", clientId);
    }

    // ── Mapping ────────────────────────────────────────────────────────

    private M2mClientResponse mapToResponse(RegisteredClient client) {
        return M2mClientResponse.builder()
                .id(client.getId())
                .clientId(client.getClientId())
                .clientName(client.getClientName())
                .scopes(new ArrayList<>(client.getScopes()))
                .grantTypes(client.getAuthorizationGrantTypes().stream()
                        .map(AuthorizationGrantType::getValue)
                        .toList())
                .authenticationMethods(client.getClientAuthenticationMethods().stream()
                        .map(ClientAuthenticationMethod::getValue)
                        .toList())
                .accessTokenTtlMinutes(
                        client.getTokenSettings().getAccessTokenTimeToLive().toMinutes())
                .clientIdIssuedAt(client.getClientIdIssuedAt())
                .build();
    }
}
