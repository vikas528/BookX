/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.service;

import java.time.LocalDateTime;
import java.util.List;

import com.bookxshow.dto.ShowDto;

/**
 * Service interface for querying shows in the local catalogue.
 *
 * <p>Shows are synced from the Admin Service and stored locally.
 * This service provides read-only access with optional filtering.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ShowService {

    /**
     * Returns shows matching the optional filters, ordered by show date ascending.
     *
     * <p>All parameters are optional. {@code name} supports SQL LIKE wildcards
     * ({@code %} matches any sequence, {@code _} matches a single character).
     * A plain name without wildcards is automatically treated as a
     * case-insensitive contains search (e.g. {@code "pushpa"} → {@code "%pushpa%"}).</p>
     *
     * @param fromDate lower bound of the show date/time range (inclusive), may be null
     * @param toDate   upper bound of the show date/time range (inclusive), may be null
     * @param name     name pattern for filtering, may be null
     * @return list of matching shows
     */
    List<ShowDto> getShows(LocalDateTime fromDate, LocalDateTime toDate, String name);
}
