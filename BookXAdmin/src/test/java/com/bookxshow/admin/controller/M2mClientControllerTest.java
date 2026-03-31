/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Unit tests for M2mClientController using MockMvc.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.controller;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bookxshow.admin.config.TestSecurityConfig;
import com.bookxshow.admin.dto.M2mClientResponse;
import com.bookxshow.admin.exception.ClientAlreadyExistsException;
import com.bookxshow.admin.exception.ClientNotFoundException;
import com.bookxshow.admin.service.M2mClientService;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvcTest for {@link M2mClientController}. Tests HTTP request/response
 * mapping, validation, and status codes.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@WebMvcTest(M2mClientController.class)
@Import(TestSecurityConfig.class)
class M2mClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private M2mClientService m2mClientService;

    private static final String BASE_PATH = "/bookxshow/v1/admin/clients";

    // ── Helpers ────────────────────────────────────────────────────────

    private M2mClientResponse buildResponse(String clientId, String clientName) {
        return M2mClientResponse.builder()
                .id("uuid-123")
                .clientId(clientId)
                .clientName(clientName)
                .scopes(List.of("bookxshow.read", "bookxshow.write"))
                .grantTypes(List.of("client_credentials"))
                .authenticationMethods(List.of("client_secret_basic", "client_secret_post"))
                .accessTokenTtlMinutes(60)
                .clientIdIssuedAt(Instant.now())
                .build();
    }

    // ════════════════════════════════════════════════════════════════════
    //  POST /bookxshow/v1/admin/clients
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("POST /clients — 201 Created with valid request")
    void createClient_shouldReturn201() throws Exception {
        M2mClientResponse response = buildResponse("new-svc", "New Service");
        when(m2mClientService.createClient(any())).thenReturn(response);

        String body = """
                {
                    "clientId": "new-svc",
                    "clientSecret": "my-secret-123",
                    "clientName": "New Service",
                    "scopes": ["bookxshow.read", "bookxshow.write"],
                    "accessTokenTtlMinutes": 60
                }
                """;

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId", is("new-svc")))
                .andExpect(jsonPath("$.clientName", is("New Service")))
                .andExpect(jsonPath("$.scopes", hasSize(2)))
                .andExpect(jsonPath("$.grantTypes[0]", is("client_credentials")))
                .andExpect(jsonPath("$.accessTokenTtlMinutes", is(60)));

        verify(m2mClientService).createClient(any());
    }

    @Test
    @DisplayName("POST /clients — 400 when clientId is missing")
    void createClient_shouldReturn400WhenNoClientId() throws Exception {
        String body = """
                {
                    "clientSecret": "my-secret-123",
                    "scopes": ["bookxshow.read"]
                }
                """;

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(m2mClientService, never()).createClient(any());
    }

    @Test
    @DisplayName("POST /clients — 400 when secret is too short")
    void createClient_shouldReturn400WhenShortSecret() throws Exception {
        String body = """
                {
                    "clientId": "short-svc",
                    "clientSecret": "abc",
                    "scopes": ["bookxshow.read"]
                }
                """;

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /clients — 400 when scopes are empty")
    void createClient_shouldReturn400WhenNoScopes() throws Exception {
        String body = """
                {
                    "clientId": "no-scopes",
                    "clientSecret": "my-secret-123",
                    "scopes": []
                }
                """;

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /clients — 409 when client already exists")
    void createClient_shouldReturn409ForDuplicate() throws Exception {
        when(m2mClientService.createClient(any()))
                .thenThrow(new ClientAlreadyExistsException("Client already exists"));

        String body = """
                {
                    "clientId": "existing-svc",
                    "clientSecret": "my-secret-123",
                    "scopes": ["bookxshow.read"]
                }
                """;

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    // ════════════════════════════════════════════════════════════════════
    //  GET /bookxshow/v1/admin/clients
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /clients — 200 with list of clients")
    void getAllClients_shouldReturn200() throws Exception {
        List<M2mClientResponse> clients = Arrays.asList(
                buildResponse("alpha", "Alpha"), buildResponse("beta", "Beta"));
        when(m2mClientService.getAllClients()).thenReturn(clients);

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].clientId", is("alpha")))
                .andExpect(jsonPath("$[1].clientId", is("beta")));
    }

    @Test
    @DisplayName("GET /clients — 200 with empty list")
    void getAllClients_shouldReturn200Empty() throws Exception {
        when(m2mClientService.getAllClients()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ════════════════════════════════════════════════════════════════════
    //  GET /bookxshow/v1/admin/clients/{clientId}
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /clients/{id} — 200 when found")
    void getClient_shouldReturn200() throws Exception {
        M2mClientResponse response = buildResponse("found-svc", "Found Service");
        when(m2mClientService.getClientByClientId("found-svc")).thenReturn(response);

        mockMvc.perform(get(BASE_PATH + "/found-svc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId", is("found-svc")))
                .andExpect(jsonPath("$.clientName", is("Found Service")));
    }

    @Test
    @DisplayName("GET /clients/{id} — 404 when not found")
    void getClient_shouldReturn404() throws Exception {
        when(m2mClientService.getClientByClientId("missing"))
                .thenThrow(new ClientNotFoundException("Not found"));

        mockMvc.perform(get(BASE_PATH + "/missing"))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════════════════════════
    //  PUT /bookxshow/v1/admin/clients/{clientId}
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("PUT /clients/{id} — 200 with valid update")
    void updateClient_shouldReturn200() throws Exception {
        M2mClientResponse response = M2mClientResponse.builder()
                .id("uuid-123")
                .clientId("upd-svc")
                .clientName("Updated Name")
                .scopes(List.of("bookxshow.read", "bookxshow.admin"))
                .grantTypes(List.of("client_credentials"))
                .authenticationMethods(List.of("client_secret_basic"))
                .accessTokenTtlMinutes(120)
                .clientIdIssuedAt(Instant.now())
                .build();

        when(m2mClientService.updateClient(eq("upd-svc"), any())).thenReturn(response);

        String body = """
                {
                    "clientName": "Updated Name",
                    "scopes": ["bookxshow.read", "bookxshow.admin"],
                    "accessTokenTtlMinutes": 120
                }
                """;

        mockMvc.perform(put(BASE_PATH + "/upd-svc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName", is("Updated Name")))
                .andExpect(jsonPath("$.accessTokenTtlMinutes", is(120)));
    }

    @Test
    @DisplayName("PUT /clients/{id} — 404 when not found")
    void updateClient_shouldReturn404() throws Exception {
        when(m2mClientService.updateClient(eq("missing"), any()))
                .thenThrow(new ClientNotFoundException("Not found"));

        String body = """
                { "clientName": "New Name" }
                """;

        mockMvc.perform(put(BASE_PATH + "/missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /clients/{id} — 400 when secret too short")
    void updateClient_shouldReturn400WhenShortSecret() throws Exception {
        String body = """
                { "clientSecret": "abc" }
                """;

        mockMvc.perform(put(BASE_PATH + "/some-svc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════════════
    //  DELETE /bookxshow/v1/admin/clients/{clientId}
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("DELETE /clients/{id} — 204 No Content")
    void deleteClient_shouldReturn204() throws Exception {
        doNothing().when(m2mClientService).deleteClient("delete-svc");

        mockMvc.perform(delete(BASE_PATH + "/delete-svc"))
                .andExpect(status().isNoContent());

        verify(m2mClientService).deleteClient("delete-svc");
    }

    @Test
    @DisplayName("DELETE /clients/{id} — 404 when not found")
    void deleteClient_shouldReturn404() throws Exception {
        doThrow(new ClientNotFoundException("Not found"))
                .when(m2mClientService).deleteClient("missing");

        mockMvc.perform(delete(BASE_PATH + "/missing"))
                .andExpect(status().isNotFound());
    }
}
