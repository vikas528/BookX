/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookxshow.common.Routes;
import com.bookxshow.dto.ShowDto;
import com.bookxshow.service.ShowService;

/**
 * REST controller for browsing the local show catalogue.
 *
 * <h3>Endpoints</h3>
 * <table>
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>GET</td><td>/bookxshow/v1/shows</td><td>List shows with optional filters</td></tr>
 * </table>
 *
 * <h3>Query parameters for GET /shows</h3>
 * <ul>
 *   <li>{@code fromDate} — ISO-8601 date-time lower bound (inclusive), e.g. {@code 2026-04-01T00:00:00}</li>
 *   <li>{@code toDate}   — ISO-8601 date-time upper bound (inclusive)</li>
 *   <li>{@code name}     — name filter; supports SQL LIKE wildcards ({@code %}, {@code _}).
 *       A plain string without wildcards is treated as a case-insensitive contains search.</li>
 * </ul>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping(Routes.ROOT_V1)
@Validated
public class ShowController {

    private static final Logger log = LoggerFactory.getLogger(ShowController.class);

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    /**
     * Returns all locally-synced shows that match the optional filters.
     *
     * <p>All query parameters are optional — omitting them returns the full
     * catalogue ordered by show date ascending.</p>
     *
     * <p><b>Name filtering</b> — supports SQL LIKE wildcards:
     * <ul>
     *   <li>{@code name=Pushpa}        → contains "pushpa" (case-insensitive)</li>
     *   <li>{@code name=Pushpa%}       → starts with "Pushpa"</li>
     *   <li>{@code name=%The%Rule%}    → contains "The" then "Rule"</li>
     * </ul>
     *
     * @param fromDate lower bound of the show date range (inclusive)
     * @param toDate   upper bound of the show date range (inclusive)
     * @param name     name pattern (supports {@code %} and {@code _} wildcards)
     * @return 200 OK with the list of matching shows
     */
    @GetMapping(Routes.SHOWS)
    public ResponseEntity<List<ShowDto>> getShows(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,

            @RequestParam(required = false)
            String name) {

        log.debug("GET /shows fromDate={} toDate={} name={}", fromDate, toDate, name);
        return ResponseEntity.ok(showService.getShows(fromDate, toDate, name));
    }
}
