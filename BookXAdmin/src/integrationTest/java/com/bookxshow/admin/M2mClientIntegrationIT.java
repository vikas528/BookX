/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Integration tests for the M2M Client Management API with a full
 * Spring Boot context.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.bookxshow.admin.config.TestSecurityConfig;
import com.bookxshow.admin.dto.CreateM2mClientRequest;
import com.bookxshow.admin.dto.M2mClientResponse;
import com.bookxshow.admin.dto.UpdateM2mClientRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that boot the full Spring context with a random port,
 * using H2 in-memory database and the shared {@code oauth2_registered_client}
 * table (created by schema.sql).
 *
 * <p>Verifies the full M2M client management lifecycle:
 * <ul>
 *   <li>IT01 — Create an M2M client</li>
 *   <li>IT02 — List all clients</li>
 *   <li>IT03 — Get client by ID</li>
 *   <li>IT04 — Update client metadata</li>
 *   <li>IT05 — Rotate client secret</li>
 *   <li>IT06 — Delete a client</li>
 *   <li>IT07 — Get non-existent client returns 404</li>
 *   <li>IT08 — Create duplicate client returns 409</li>
 *   <li>IT09 — Validation failures return 400</li>
 *   <li>IT10 — Update scopes and token TTL</li>
 * </ul>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
@Import(TestSecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class M2mClientIntegrationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String clientsUrl;

    @BeforeEach
    void setUp() {
        clientsUrl = "http://localhost:" + port + "/bookxshow/v1/admin/clients";
        // Clean up between tests
        jdbcTemplate.update("DELETE FROM oauth2_registered_client");
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ════════════════════════════════════════════════════════════════════
    //  IT01 — Create M2M client
    // ════════════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("IT01 — Create M2M client with auto-generated defaults")
    void shouldCreateM2mClient() {
        CreateM2mClientRequest request = CreateM2mClientRequest.builder()
                .clientId("test-m2m-client")
                .clientSecret("super-secret-123")
                .clientName("Test M2M Client")
                .scopes(Set.of("bookxshow.read", "bookxshow.write"))
                .accessTokenTtlMinutes(30)
                .build();

        ResponseEntity<M2mClientResponse> response = rest.postForEntity(
                clientsUrl,
                new HttpEntity<>(request, jsonHeaders()),
                M2mClientResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        M2mClientResponse body = response.getBody();
        assertNotNull(body.getId());
        assertEquals("test-m2m-client", body.getClientId());
        assertEquals("Test M2M Client", body.getClientName());
        assertTrue(body.getScopes().containsAll(List.of("bookxshow.read", "bookxshow.write")));
        assertEquals(List.of("client_credentials"), body.getGrantTypes());
        assertEquals(30, body.getAccessTokenTtlMinutes());
        assertNotNull(body.getClientIdIssuedAt());
    }

    // ════════════════════════════════════════════════════════════════════
    //  IT02 — List all clients
    // ════════════════════════════════════════════════════════════════════

    @Test
    @Order(2)
    @DisplayName("IT02 — List all registered clients")
    void shouldListAllClients() {
        // Create two clients
        createClient("svc-alpha", "Alpha Service");
        createClient("svc-beta", "Beta Service");

        ResponseEntity<List<M2mClientResponse>> response = rest.exchange(
                clientsUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    // ════════════════════════════════════════════════════════════════════
    //  IT03 — Get client by ID
    // ════════════════════════════════════════════════════════════════════

    @Test
    @Order(3)
    @DisplayName("IT03 — Get client by client ID")
    void shouldGetClientByClientId() {
        createClient("lookup-svc", "Lookup Service");

        ResponseEntity<M2mClientResponse> response = rest.getForEntity(
                clientsUrl + "/lookup-svc", M2mClientResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("lookup-svc", response.getBody().getClientId());
        assertEquals("Lookup Service", response.getBody().getClientName());
    }

    // ════════════════════════════════════════════════════════════════════
    //  IT04 — Update client metadata
    // ════════════════════════════════════════════════════════════════════

    @Test
    @Order(4)
    @DisplayName("IT04 — Update client name and TTL")
    void shouldUpdateClientMetadata() {
        createClient("update-svc", "Old Name");

        UpdateM2mClientRequest updateRequest = UpdateM2mClientRequest.builder()
                .clientName("New Name")
                .accessTokenTtlMinutes(120)
                .build();

        ResponseEntity<M2mClientResponse> response = rest.exchange(
                clientsUrl + "/update-svc",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, jsonHeaders()),
                M2mClientResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Name", response.getBody().getClientName());
        assertEquals(120, response.getBody().getAccessTokenTtlMinutes());
    }

    // ════════════════════════════════════════════════════════════════════
    //  IT05 — Rotate client secret
    // ════════════════════════════════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("IT05 — Rotate client secret via update")
    void shouldRotateClientSecret() {
        createClient("rotate-svc", "Rotate Service");

        UpdateM2mClientRequest updateRequest = UpdateM2mClientRequest.builder()
                .clientSecret("brand-new-secret-456")
                .build();

        ResponseEntity<M2mClientResponse> response = rest.exchange(
                clientsUrl + "/rotate-svc",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, jsonHeaders()),
                M2mClientResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Secret is not returned in response — just verify 200
        assertEquals("rotate-svc", response.getBody().getClientId());
    }

    // ════════════════════════════════════════════════════════════════════
    //  IT06 — Delete a client
    // ════════════════════════════════════════════════════════════════════

    @Test
    @Order(6)
    @DisplayName("IT06 — Delete a client returns 204 and removes from DB")
    void shouldDeleteClient() {
        createClient("delete-svc", "Delete Service");

        ResponseEntity<Void> deleteResponse = rest.exchange(
                clientsUrl + "/delete-svc",
                HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // Verify the client is gone
        ResponseEntity<Map> getResponse = rest.getForEntity(
                clientsUrl + "/delete-svc", Map.class);

        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    // ════════════════════════════════════════════════════════════════════
    //  IT07 — Get non-existent client returns 404
    // ════════════════════════════════════════════════════════════════════

    @Test
    @Order(7)
    @DisplayName("IT07 — Get non-existent client returns 404")
    void shouldReturn404ForNonExistentClient() {
        ResponseEntity<Map> response = rest.getForEntity(
                clientsUrl + "/nonexistent", Map.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ════════════════════════════════════════════════════════════════════
    //  IT08 — Create duplicate client returns 409
    // ════════════════════════════════════════════════════════════════════

    @Test
    @Order(8)
    @DisplayName("IT08 — Create duplicate client returns 409")
    void shouldReturn409ForDuplicateClient() {
        createClient("dup-svc", "Duplicate Service");

        CreateM2mClientRequest request = CreateM2mClientRequest.builder()
                .clientId("dup-svc")
                .clientSecret("another-secret-123")
                .scopes(Set.of("bookxshow.read"))
                .accessTokenTtlMinutes(60)
                .build();

        ResponseEntity<Map> response = rest.postForEntity(
                clientsUrl,
                new HttpEntity<>(request, jsonHeaders()),
                Map.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // ════════════════════════════════════════════════════════════════════
    //  IT09 — Validation failures return 400
    // ════════════════════════════════════════════════════════════════════

    @Test
    @Order(9)
    @DisplayName("IT09 — Validation failures return 400")
    void shouldReturn400ForValidationFailure() {
        // Missing required fields
        Map<String, Object> invalidRequest = Map.of(
                "clientId", "",
                "clientSecret", "short");

        ResponseEntity<Map> response = rest.postForEntity(
                clientsUrl,
                new HttpEntity<>(invalidRequest, jsonHeaders()),
                Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ════════════════════════════════════════════════════════════════════
    //  IT10 — Update scopes and token TTL
    // ════════════════════════════════════════════════════════════════════

    @Test
    @Order(10)
    @DisplayName("IT10 — Update scopes replaces existing scopes entirely")
    void shouldReplaceScopes() {
        createClient("scope-svc", "Scope Service");

        UpdateM2mClientRequest updateRequest = UpdateM2mClientRequest.builder()
                .scopes(Set.of("bookxshow.read", "bookxshow.admin"))
                .build();

        ResponseEntity<M2mClientResponse> response = rest.exchange(
                clientsUrl + "/scope-svc",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, jsonHeaders()),
                M2mClientResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getScopes().contains("bookxshow.read"));
        assertTrue(response.getBody().getScopes().contains("bookxshow.admin"));
        assertFalse(response.getBody().getScopes().contains("bookxshow.write"));
    }

    // ── Helper ─────────────────────────────────────────────────────────

    private void createClient(String clientId, String clientName) {
        CreateM2mClientRequest request = CreateM2mClientRequest.builder()
                .clientId(clientId)
                .clientSecret("test-secret-123456")
                .clientName(clientName)
                .scopes(Set.of("bookxshow.read", "bookxshow.write"))
                .accessTokenTtlMinutes(60)
                .build();

        ResponseEntity<M2mClientResponse> response = rest.postForEntity(
                clientsUrl,
                new HttpEntity<>(request, jsonHeaders()),
                M2mClientResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Failed to create helper client: " + clientId);
    }
}
