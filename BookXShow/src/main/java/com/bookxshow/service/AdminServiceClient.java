/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Client interface for fetching show and seat configuration from the
 * external Admin Service.
 *
 * @since 1.0.0
 */
package com.bookxshow.service;

import java.util.List;

import com.bookxshow.dto.AdminSeatDto;
import com.bookxshow.dto.ShowDto;
import com.bookxshow.exception.ShowNotFoundException;

/**
 * Adapter interface for the external <em>Admin Service</em> that owns
 * show and seat master data.
 *
 * <p>Follows the <em>Adapter / Anti-Corruption Layer</em> pattern — the
 * booking domain never depends on the Admin Service's internal model.
 * All data is mapped into BookXShow DTOs before being consumed.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface AdminServiceClient {

    /**
     * Fetches the details of a show from the Admin Service.
     *
     * @param externalShowId the Admin-Service-assigned show identifier
     * @return show details mapped into a BookXShow DTO
     * @throws ShowNotFoundException
     *         if the admin service does not know this show
     */
    ShowDto fetchShowDetails(String externalShowId);

    /**
     * Fetches the seat layout for a show from the Admin Service.
     *
     * @param externalShowId the Admin-Service-assigned show identifier
     * @return list of seat DTOs with their labels and metadata
     */
    List<AdminSeatDto> fetchSeatsForShow(String externalShowId);

    /**
     * Synchronises a show and its seats from the Admin Service into
     * the local database.
     *
     * <p>If the show already exists locally it is updated; otherwise a new
     * record (and its seats) is created.</p>
     *
     * @param externalShowId the Admin-Service-assigned show identifier
     */
    void syncShow(String externalShowId);
}
