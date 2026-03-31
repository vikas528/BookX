/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.service;

import java.util.List;

import com.bookxshow.admin.dto.CreateShowRequest;
import com.bookxshow.admin.dto.SeatResponse;
import com.bookxshow.admin.dto.ShowResponse;
import com.bookxshow.admin.dto.UpdateShowRequest;

/**
 * Service interface for managing shows in the admin catalogue.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ShowService {

    /**
     * Creates a new show with auto-generated seats.
     *
     * @param request the show creation request
     * @return the created show
     */
    ShowResponse createShow(CreateShowRequest request);

    /**
     * Returns all shows ordered by date descending.
     *
     * @return list of all shows
     */
    List<ShowResponse> getAllShows();

    /**
     * Gets a show by its primary key.
     *
     * @param showId the show ID
     * @return the show details
     */
    ShowResponse getShowById(Long showId);

    /**
     * Gets a show by its external identifier (for service-to-service calls).
     *
     * @param externalShowId the admin-assigned show ID
     * @return the show details
     */
    ShowResponse getShowByExternalId(String externalShowId);

    /**
     * Updates an existing show's metadata.
     *
     * @param showId  the show ID
     * @param request the update request (partial updates supported)
     * @return the updated show
     */
    ShowResponse updateShow(Long showId, UpdateShowRequest request);

    /**
     * Publishes a draft show, making it available for booking.
     *
     * @param showId the show ID
     * @return the published show
     */
    ShowResponse publishShow(Long showId);

    /**
     * Cancels a show.
     *
     * @param showId the show ID
     */
    void cancelShow(Long showId);

    /**
     * Returns all seats for a show by its primary key.
     *
     * @param showId the show ID
     * @return list of seats
     */
    List<SeatResponse> getSeatsForShow(Long showId);

    /**
     * Returns all seats for a show by its external identifier
     * (for service-to-service calls).
     *
     * @param externalShowId the admin-assigned show ID
     * @return list of seats
     */
    List<SeatResponse> getSeatsByExternalShowId(String externalShowId);
}
