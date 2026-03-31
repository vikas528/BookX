/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Read-only REST controller for service-to-service show data retrieval.
 * BookXShow calls these endpoints to sync show and seat data.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookxshow.admin.common.Routes;
import com.bookxshow.admin.dto.SeatResponse;
import com.bookxshow.admin.dto.ShowResponse;
import com.bookxshow.admin.service.ShowService;

/**
 * Service-to-service (S2S) read-only endpoints.
 *
 * <p>These endpoints are called by the BookXShow booking service
 * (via {@code AdminServiceClient}) to fetch show and seat data for
 * local synchronization. They use the {@code externalShowId} path
 * variable for cross-service correlation.</p>
 *
 * <h3>Endpoints</h3>
 * <table>
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>GET</td><td>/bookxshow/v1/shows/{externalShowId}</td><td>Get show details</td></tr>
 *   <tr><td>GET</td><td>/bookxshow/v1/shows/{externalShowId}/seats</td><td>Get seats</td></tr>
 * </table>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping(Routes.SHOWS_V1)
public class ShowReadController {

    private static final Logger log = LoggerFactory.getLogger(ShowReadController.class);

    private final ShowService showService;

    public ShowReadController(ShowService showService) {
        this.showService = showService;
    }

    /**
     * Returns show details by external show ID.
     * Called by BookXShow's AdminServiceClient.fetchShowDetails().
     *
     * @param externalShowId the admin-assigned show identifier
     * @return 200 OK with show details
     */
    @GetMapping(Routes.SHOW_BY_EXT_ID)
    public ResponseEntity<ShowResponse> getShowByExternalId(
            @PathVariable String externalShowId) {
        log.debug("S2S: fetching show by externalId={}", externalShowId);
        return ResponseEntity.ok(showService.getShowByExternalId(externalShowId));
    }

    /**
     * Returns all seats for a show by external show ID.
     * Called by BookXShow's AdminServiceClient.fetchSeatsForShow().
     *
     * @param externalShowId the admin-assigned show identifier
     * @return 200 OK with seat list
     */
    @GetMapping(Routes.SHOW_SEATS_BY_EXT)
    public ResponseEntity<List<SeatResponse>> getSeatsByExternalShowId(
            @PathVariable String externalShowId) {
        log.debug("S2S: fetching seats for externalId={}", externalShowId);
        return ResponseEntity.ok(showService.getSeatsByExternalShowId(externalShowId));
    }
}
