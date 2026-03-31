/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * REST controller for managing OAuth2 M2M client credentials.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookxshow.admin.common.Routes;
import com.bookxshow.admin.dto.CreateM2mClientRequest;
import com.bookxshow.admin.dto.M2mClientResponse;
import com.bookxshow.admin.dto.UpdateM2mClientRequest;
import com.bookxshow.admin.service.M2mClientService;

import jakarta.validation.Valid;

/**
 * Provides CRUD endpoints for managing OAuth2 M2M (client_credentials)
 * client registrations.
 *
 * <h3>Endpoints</h3>
 * <pre>
 *   POST   /bookxshow/v1/admin/clients              — register a new M2M client
 *   GET    /bookxshow/v1/admin/clients              — list all registered clients
 *   GET    /bookxshow/v1/admin/clients/{clientId}   — get client by ID
 *   PUT    /bookxshow/v1/admin/clients/{clientId}   — update client
 *   DELETE /bookxshow/v1/admin/clients/{clientId}   — delete client
 * </pre>
 *
 * <p>All endpoints require {@code SCOPE_bookxshow.admin}.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping(Routes.ADMIN_CLIENTS_V1)
public class M2mClientController {

    private final M2mClientService m2mClientService;

    public M2mClientController(M2mClientService m2mClientService) {
        this.m2mClientService = m2mClientService;
    }

    @PostMapping
    public ResponseEntity<M2mClientResponse> createClient(
            @Valid @RequestBody CreateM2mClientRequest request) {
        M2mClientResponse response = m2mClientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<M2mClientResponse>> getAllClients() {
        return ResponseEntity.ok(m2mClientService.getAllClients());
    }

    @GetMapping(Routes.CLIENT_ID)
    public ResponseEntity<M2mClientResponse> getClient(
            @PathVariable String clientId) {
        return ResponseEntity.ok(m2mClientService.getClientByClientId(clientId));
    }

    @PutMapping(Routes.CLIENT_ID)
    public ResponseEntity<M2mClientResponse> updateClient(
            @PathVariable String clientId,
            @Valid @RequestBody UpdateM2mClientRequest request) {
        return ResponseEntity.ok(m2mClientService.updateClient(clientId, request));
    }

    @DeleteMapping(Routes.CLIENT_ID)
    public ResponseEntity<Void> deleteClient(@PathVariable String clientId) {
        m2mClientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }
}
