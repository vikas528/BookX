/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * REST controller for admin show management endpoints.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookxshow.admin.common.Routes;
import com.bookxshow.admin.dto.CreateShowRequest;
import com.bookxshow.admin.dto.SeatResponse;
import com.bookxshow.admin.dto.ShowResponse;
import com.bookxshow.admin.dto.UpdateShowRequest;
import com.bookxshow.admin.service.ShowService;

import jakarta.validation.Valid;

/**
 * REST controller for admin-facing show management.
 *
 * <h3>Endpoints</h3>
 * <table>
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>POST</td><td>/bookxshow/v1/admin/shows</td><td>Create a show</td></tr>
 *   <tr><td>GET</td><td>/bookxshow/v1/admin/shows</td><td>List all shows</td></tr>
 *   <tr><td>GET</td><td>/bookxshow/v1/admin/shows/{showId}</td><td>Get show by ID</td></tr>
 *   <tr><td>PUT</td><td>/bookxshow/v1/admin/shows/{showId}</td><td>Update a show</td></tr>
 *   <tr><td>DELETE</td><td>/bookxshow/v1/admin/shows/{showId}</td><td>Cancel a show</td></tr>
 *   <tr><td>POST</td><td>/bookxshow/v1/admin/shows/{showId}/publish</td><td>Publish a show</td></tr>
 *   <tr><td>GET</td><td>/bookxshow/v1/admin/shows/{showId}/seats</td><td>List seats</td></tr>
 * </table>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping(Routes.ADMIN_V1)
@Validated
public class AdminShowController {

    private static final Logger log = LoggerFactory.getLogger(AdminShowController.class);

    private final ShowService showService;

    public AdminShowController(ShowService showService) {
        this.showService = showService;
    }

    /**
     * Creates a new show with auto-generated seating layout.
     *
     * @param request the show creation request
     * @return 201 Created with the new show
     */
    @PostMapping(Routes.ADMIN_SHOWS)
    public ResponseEntity<ShowResponse> createShow(
            @Valid @RequestBody CreateShowRequest request) {
        log.info("Creating show: {}", request.getName());
        ShowResponse response = showService.createShow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns all shows ordered by date descending.
     *
     * @return 200 OK with list of shows
     */
    @GetMapping(Routes.ADMIN_SHOWS)
    public ResponseEntity<List<ShowResponse>> getAllShows() {
        return ResponseEntity.ok(showService.getAllShows());
    }

    /**
     * Gets a single show by its primary key.
     *
     * @param showId the show primary key
     * @return 200 OK with the show details
     */
    @GetMapping(Routes.ADMIN_SHOW_ID)
    public ResponseEntity<ShowResponse> getShowById(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getShowById(showId));
    }

    /**
     * Updates an existing show. Only non-null fields are applied.
     *
     * @param showId  the show primary key
     * @param request the update request
     * @return 200 OK with the updated show
     */
    @PutMapping(Routes.ADMIN_SHOW_ID)
    public ResponseEntity<ShowResponse> updateShow(
            @PathVariable Long showId,
            @Valid @RequestBody UpdateShowRequest request) {
        log.info("Updating show {}", showId);
        return ResponseEntity.ok(showService.updateShow(showId, request));
    }

    /**
     * Publishes a draft show, making it available for booking.
     *
     * @param showId the show primary key
     * @return 200 OK with the published show
     */
    @PostMapping(Routes.ADMIN_SHOW_PUBLISH)
    public ResponseEntity<ShowResponse> publishShow(@PathVariable Long showId) {
        log.info("Publishing show {}", showId);
        return ResponseEntity.ok(showService.publishShow(showId));
    }

    /**
     * Cancels a show.
     *
     * @param showId the show primary key
     * @return 204 No Content
     */
    @DeleteMapping(Routes.ADMIN_SHOW_ID)
    public ResponseEntity<Void> cancelShow(@PathVariable Long showId) {
        log.info("Cancelling show {}", showId);
        showService.cancelShow(showId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists all seats for a show.
     *
     * @param showId the show primary key
     * @return 200 OK with seat list
     */
    @GetMapping(Routes.ADMIN_SHOW_SEATS)
    public ResponseEntity<List<SeatResponse>> getSeatsForShow(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getSeatsForShow(showId));
    }
}
