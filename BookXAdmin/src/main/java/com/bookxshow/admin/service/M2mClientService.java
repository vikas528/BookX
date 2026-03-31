/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.service;

import java.util.List;

import com.bookxshow.admin.dto.CreateM2mClientRequest;
import com.bookxshow.admin.dto.M2mClientResponse;
import com.bookxshow.admin.dto.UpdateM2mClientRequest;

/**
 * Service interface for managing OAuth2 M2M (client_credentials) clients.
 *
 * <p>Operates on the {@code oauth2_registered_client} table shared with
 * the Authorization Server.</p>
 *
 * @since 1.0.0
 */
public interface M2mClientService {

    /**
     * Creates a new M2M client with {@code client_credentials} grant type.
     *
     * @param request creation parameters
     * @return the created client metadata (secret is not returned)
     */
    M2mClientResponse createClient(CreateM2mClientRequest request);

    /**
     * Lists all registered OAuth2 clients.
     *
     * @return list of client metadata
     */
    List<M2mClientResponse> getAllClients();

    /**
     * Retrieves a single client by its client ID.
     *
     * @param clientId the OAuth2 client identifier
     * @return client metadata
     */
    M2mClientResponse getClientByClientId(String clientId);

    /**
     * Partially updates an existing client.
     *
     * @param clientId the OAuth2 client identifier
     * @param request  fields to update (null fields are ignored)
     * @return the updated client metadata
     */
    M2mClientResponse updateClient(String clientId, UpdateM2mClientRequest request);

    /**
     * Deletes a client by its client ID.
     *
     * @param clientId the OAuth2 client identifier
     */
    void deleteClient(String clientId);
}
