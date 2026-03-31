/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Unit tests for M2mClientServiceImpl.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import com.bookxshow.admin.dto.CreateM2mClientRequest;
import com.bookxshow.admin.dto.M2mClientResponse;
import com.bookxshow.admin.dto.UpdateM2mClientRequest;
import com.bookxshow.admin.exception.ClientAlreadyExistsException;
import com.bookxshow.admin.exception.ClientNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link M2mClientServiceImpl}. All repository and JDBC
 * interactions are mocked via Mockito.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class M2mClientServiceImplTest {

    @Mock
    private RegisteredClientRepository registeredClientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Captor
    private ArgumentCaptor<RegisteredClient> clientCaptor;

    private M2mClientServiceImpl m2mClientService;

    @BeforeEach
    void setUp() {
        m2mClientService = new M2mClientServiceImpl(
                registeredClientRepository, passwordEncoder, jdbcTemplate);
    }

    // ── Helper ─────────────────────────────────────────────────────────

    private RegisteredClient buildTestClient(String clientId, String clientName) {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret("{bcrypt}encoded-secret")
                .clientName(clientName)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("bookxshow.read")
                .scope("bookxshow.write")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(60))
                        .build())
                .build();
    }

    // ════════════════════════════════════════════════════════════════════
    //  Create Client
    // ════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createClient()")
    class CreateClient {

        @Test
        @DisplayName("should create client with correct defaults")
        void shouldCreateClient() {
            CreateM2mClientRequest request = CreateM2mClientRequest.builder()
                    .clientId("test-client")
                    .clientSecret("super-secret-123")
                    .scopes(Set.of("bookxshow.read", "bookxshow.write"))
                    .accessTokenTtlMinutes(30)
                    .build();

            when(registeredClientRepository.findByClientId("test-client")).thenReturn(null);
            when(passwordEncoder.encode("super-secret-123")).thenReturn("{bcrypt}encoded");

            M2mClientResponse response = m2mClientService.createClient(request);

            verify(registeredClientRepository).save(clientCaptor.capture());
            RegisteredClient saved = clientCaptor.getValue();

            assertNotNull(response);
            assertEquals("test-client", response.getClientId());
            assertEquals("test-client", response.getClientName()); // defaults to clientId
            assertTrue(response.getScopes().containsAll(List.of("bookxshow.read", "bookxshow.write")));
            assertEquals(List.of("client_credentials"), response.getGrantTypes());
            assertEquals(30, response.getAccessTokenTtlMinutes());

            // Verify saved client
            assertEquals("test-client", saved.getClientId());
            assertEquals("{bcrypt}encoded", saved.getClientSecret());
            assertTrue(saved.getClientAuthenticationMethods()
                    .contains(ClientAuthenticationMethod.CLIENT_SECRET_BASIC));
            assertTrue(saved.getClientAuthenticationMethods()
                    .contains(ClientAuthenticationMethod.CLIENT_SECRET_POST));
        }

        @Test
        @DisplayName("should use custom client name when provided")
        void shouldUseCustomClientName() {
            CreateM2mClientRequest request = CreateM2mClientRequest.builder()
                    .clientId("payment-svc")
                    .clientSecret("payment-secret-123")
                    .clientName("Payment Service M2M")
                    .scopes(Set.of("bookxshow.read"))
                    .accessTokenTtlMinutes(15)
                    .build();

            when(registeredClientRepository.findByClientId("payment-svc")).thenReturn(null);
            when(passwordEncoder.encode("payment-secret-123")).thenReturn("{bcrypt}enc");

            M2mClientResponse response = m2mClientService.createClient(request);

            assertEquals("Payment Service M2M", response.getClientName());
        }

        @Test
        @DisplayName("should throw ClientAlreadyExistsException for duplicate")
        void shouldThrowForDuplicate() {
            CreateM2mClientRequest request = CreateM2mClientRequest.builder()
                    .clientId("existing-client")
                    .clientSecret("some-secret-123")
                    .scopes(Set.of("bookxshow.read"))
                    .build();

            RegisteredClient existing = buildTestClient("existing-client", "Existing");
            when(registeredClientRepository.findByClientId("existing-client")).thenReturn(existing);

            assertThrows(ClientAlreadyExistsException.class,
                    () -> m2mClientService.createClient(request));

            verify(registeredClientRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Get All Clients
    // ════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAllClients()")
    class GetAllClients {

        @Test
        @DisplayName("should return all clients ordered by name")
        void shouldReturnAllClients() {
            when(jdbcTemplate.queryForList(
                    "SELECT client_id FROM oauth2_registered_client ORDER BY client_name",
                    String.class))
                    .thenReturn(Arrays.asList("alpha-svc", "beta-svc"));

            RegisteredClient alpha = buildTestClient("alpha-svc", "Alpha Service");
            RegisteredClient beta = buildTestClient("beta-svc", "Beta Service");

            when(registeredClientRepository.findByClientId("alpha-svc")).thenReturn(alpha);
            when(registeredClientRepository.findByClientId("beta-svc")).thenReturn(beta);

            List<M2mClientResponse> result = m2mClientService.getAllClients();

            assertEquals(2, result.size());
            assertEquals("alpha-svc", result.get(0).getClientId());
            assertEquals("beta-svc", result.get(1).getClientId());
        }

        @Test
        @DisplayName("should return empty list when no clients")
        void shouldReturnEmptyList() {
            when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                    .thenReturn(Collections.emptyList());

            List<M2mClientResponse> result = m2mClientService.getAllClients();

            assertTrue(result.isEmpty());
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Get Client by ID
    // ════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getClientByClientId()")
    class GetClientByClientId {

        @Test
        @DisplayName("should return client when found")
        void shouldReturnClient() {
            RegisteredClient client = buildTestClient("my-svc", "My Service");
            when(registeredClientRepository.findByClientId("my-svc")).thenReturn(client);

            M2mClientResponse response = m2mClientService.getClientByClientId("my-svc");

            assertEquals("my-svc", response.getClientId());
            assertEquals("My Service", response.getClientName());
            assertEquals(60, response.getAccessTokenTtlMinutes());
        }

        @Test
        @DisplayName("should throw ClientNotFoundException when not found")
        void shouldThrowNotFound() {
            when(registeredClientRepository.findByClientId("unknown")).thenReturn(null);

            assertThrows(ClientNotFoundException.class,
                    () -> m2mClientService.getClientByClientId("unknown"));
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Update Client
    // ════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateClient()")
    class UpdateClient {

        @Test
        @DisplayName("should update client name only")
        void shouldUpdateClientName() {
            RegisteredClient existing = buildTestClient("update-svc", "Old Name");
            when(registeredClientRepository.findByClientId("update-svc")).thenReturn(existing);

            UpdateM2mClientRequest request = UpdateM2mClientRequest.builder()
                    .clientName("New Name")
                    .build();

            M2mClientResponse response = m2mClientService.updateClient("update-svc", request);

            verify(registeredClientRepository).save(clientCaptor.capture());
            RegisteredClient saved = clientCaptor.getValue();

            assertEquals("New Name", response.getClientName());
            assertEquals("New Name", saved.getClientName());
            // Secret should remain unchanged
            assertEquals("{bcrypt}encoded-secret", saved.getClientSecret());
            // Scopes should remain unchanged
            assertTrue(saved.getScopes().contains("bookxshow.read"));
            assertTrue(saved.getScopes().contains("bookxshow.write"));
        }

        @Test
        @DisplayName("should rotate client secret")
        void shouldRotateSecret() {
            RegisteredClient existing = buildTestClient("rotate-svc", "Rotate Service");
            when(registeredClientRepository.findByClientId("rotate-svc")).thenReturn(existing);
            when(passwordEncoder.encode("new-secret-456")).thenReturn("{bcrypt}new-encoded");

            UpdateM2mClientRequest request = UpdateM2mClientRequest.builder()
                    .clientSecret("new-secret-456")
                    .build();

            m2mClientService.updateClient("rotate-svc", request);

            verify(registeredClientRepository).save(clientCaptor.capture());
            assertEquals("{bcrypt}new-encoded", clientCaptor.getValue().getClientSecret());
        }

        @Test
        @DisplayName("should update scopes")
        void shouldUpdateScopes() {
            RegisteredClient existing = buildTestClient("scope-svc", "Scope Service");
            when(registeredClientRepository.findByClientId("scope-svc")).thenReturn(existing);

            UpdateM2mClientRequest request = UpdateM2mClientRequest.builder()
                    .scopes(Set.of("bookxshow.read", "bookxshow.admin"))
                    .build();

            M2mClientResponse response = m2mClientService.updateClient("scope-svc", request);

            assertTrue(response.getScopes().contains("bookxshow.read"));
            assertTrue(response.getScopes().contains("bookxshow.admin"));
            assertFalse(response.getScopes().contains("bookxshow.write"));
        }

        @Test
        @DisplayName("should update token TTL")
        void shouldUpdateTokenTtl() {
            RegisteredClient existing = buildTestClient("ttl-svc", "TTL Service");
            when(registeredClientRepository.findByClientId("ttl-svc")).thenReturn(existing);

            UpdateM2mClientRequest request = UpdateM2mClientRequest.builder()
                    .accessTokenTtlMinutes(120)
                    .build();

            M2mClientResponse response = m2mClientService.updateClient("ttl-svc", request);

            assertEquals(120, response.getAccessTokenTtlMinutes());
        }

        @Test
        @DisplayName("should preserve all fields when request has only nulls")
        void shouldPreserveFieldsOnNullRequest() {
            RegisteredClient existing = buildTestClient("preserve-svc", "Preserve Service");
            when(registeredClientRepository.findByClientId("preserve-svc")).thenReturn(existing);

            UpdateM2mClientRequest request = UpdateM2mClientRequest.builder().build();

            M2mClientResponse response = m2mClientService.updateClient("preserve-svc", request);

            assertEquals("Preserve Service", response.getClientName());
            assertEquals(60, response.getAccessTokenTtlMinutes());
            assertTrue(response.getScopes().containsAll(List.of("bookxshow.read", "bookxshow.write")));
        }

        @Test
        @DisplayName("should throw ClientNotFoundException when client does not exist")
        void shouldThrowNotFound() {
            when(registeredClientRepository.findByClientId("missing")).thenReturn(null);

            UpdateM2mClientRequest request = UpdateM2mClientRequest.builder()
                    .clientName("Doesn't Matter")
                    .build();

            assertThrows(ClientNotFoundException.class,
                    () -> m2mClientService.updateClient("missing", request));

            verify(registeredClientRepository, never()).save(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Delete Client
    // ════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteClient()")
    class DeleteClient {

        @Test
        @DisplayName("should delete existing client via JDBC")
        void shouldDeleteClient() {
            RegisteredClient client = buildTestClient("delete-svc", "Delete Service");
            when(registeredClientRepository.findByClientId("delete-svc")).thenReturn(client);

            m2mClientService.deleteClient("delete-svc");

            verify(jdbcTemplate).update(
                    eq("DELETE FROM oauth2_registered_client WHERE id = ?"),
                    eq(client.getId()));
        }

        @Test
        @DisplayName("should throw ClientNotFoundException when not found")
        void shouldThrowNotFound() {
            when(registeredClientRepository.findByClientId("ghost")).thenReturn(null);

            assertThrows(ClientNotFoundException.class,
                    () -> m2mClientService.deleteClient("ghost"));

            verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  Response Mapping
    // ════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("mapToResponse()")
    class ResponseMapping {

        @Test
        @DisplayName("should map all RegisteredClient fields to response")
        void shouldMapAllFields() {
            RegisteredClient client = buildTestClient("map-svc", "Map Service");
            when(registeredClientRepository.findByClientId("map-svc")).thenReturn(client);

            M2mClientResponse response = m2mClientService.getClientByClientId("map-svc");

            assertNotNull(response.getId());
            assertEquals("map-svc", response.getClientId());
            assertEquals("Map Service", response.getClientName());
            assertNotNull(response.getScopes());
            assertEquals(2, response.getScopes().size());
            assertEquals(List.of("client_credentials"), response.getGrantTypes());
            assertTrue(response.getAuthenticationMethods().contains("client_secret_basic"));
            assertTrue(response.getAuthenticationMethods().contains("client_secret_post"));
            assertEquals(60, response.getAccessTokenTtlMinutes());
            // clientIdIssuedAt is set by JdbcRegisteredClientRepository on persist,
            // so it is null in unit tests where the repository is mocked
        }
    }
}
