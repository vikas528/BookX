/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * REST controller exposing the seat booking API.
 *
 * @since 1.0.0
 */
package com.bookxshow.controller;

import com.bookxshow.common.Constants;
import com.bookxshow.common.Routes;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookxshow.dto.BookingRequestDto;
import com.bookxshow.dto.BookingResponseDto;
import com.bookxshow.dto.SeatStatusDto;
import com.bookxshow.enums.BookingOutcome;
import com.bookxshow.service.AdminServiceClient;
import com.bookxshow.service.BookingService;

import jakarta.validation.Valid;

/**
 * REST controller for the BookXShow seat booking API (v1).
 *
 * <h3>Endpoints</h3>
 * <table>
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>POST</td><td>/api/v1/bookings</td><td>Book a seat</td></tr>
 *   <tr><td>GET</td><td>/api/v1/bookings/{ref}</td><td>Get booking by reference</td></tr>
 *   <tr><td>GET</td><td>/api/v1/shows/{showId}/seats</td><td>List seats for a show</td></tr>
 *   <tr><td>GET</td><td>/api/v1/shows/{showId}/seats/{seatId}</td><td>Get seat status</td></tr>
 *   <tr><td>POST</td><td>/api/v1/admin/shows/{externalShowId}/sync</td><td>Sync show from Admin Service</td></tr>
 * </table>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping(Routes.ROOT_V1)
@Validated
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;
    private final AdminServiceClient adminServiceClient;

    public BookingController(BookingService bookingService,
                              AdminServiceClient adminServiceClient) {
        this.bookingService = bookingService;
        this.adminServiceClient = adminServiceClient;
    }

    // ── Booking endpoints ──────────────────────────────────────────────

    /**
     * Attempts to book a seat for a user.
     *
     * <p>The full lock → pay → confirm flow is executed. The response
     * includes the {@link BookingOutcome} indicating what happened.</p>
     *
     * @param request the booking request (validated)
     * @return 201 Created if booked, 200 OK with outcome for other results
     */
    @PostMapping(Routes.BOOKINGS)
    public ResponseEntity<BookingResponseDto> bookSeat(
            @Valid @RequestBody BookingRequestDto request) {
        log.info("Booking request: show={} seat={} user={}",
                request.getShowId(), request.getSeatId(), request.getUserId());

        BookingResponseDto response = bookingService.bookSeat(request);

        HttpStatus status = response.getOutcome() == BookingOutcome.BOOKED
                ? HttpStatus.CREATED
                : HttpStatus.OK;

        return ResponseEntity.status(status).body(response);
    }

    /**
     * Retrieves an existing booking by its unique reference code.
     *
     * @param bookingReference the user-facing reference (e.g. "BXS-a3f8…")
     * @return the booking details
     */
    @GetMapping(Routes.BOOKINGS_REFERENCE)
    public ResponseEntity<BookingResponseDto> getBooking(
            @PathVariable String bookingReference) {
        BookingResponseDto response = bookingService.getBooking(bookingReference);
        return ResponseEntity.ok(response);
    }

    // ── Seat status endpoints ──────────────────────────────────────────

    @GetMapping(Routes.SHOWS_SEATS)
    public ResponseEntity<List<SeatStatusDto>> getSeatsForShow(
            @PathVariable Long showId) {
        return ResponseEntity.ok(bookingService.getSeatsForShow(showId));
    }

    @GetMapping(Routes.SHOWS_SEAT)
    public ResponseEntity<SeatStatusDto> getSeatStatus(
            @PathVariable Long showId,
            @PathVariable Long seatId) {
        return ResponseEntity.ok(bookingService.getSeatStatus(showId, seatId));
    }

    // ── Admin sync endpoint ────────────────────────────────────────────

    /**
     * Triggers a synchronization of a show's data from the Admin Service
     * into the local database.
     *
     * <p>This is intended for operational / back-office use. In production,
     * this could be triggered by a webhook or a message queue event.</p>
     *
     * @param externalShowId the Admin-Service-assigned show identifier
     * @return 200 OK on successful sync
     */
    @PostMapping(Routes.ADMIN_SHOWS_SYNC)
    public ResponseEntity<String> syncShow(@PathVariable String externalShowId) {
        log.info("Syncing show {} from Admin Service", externalShowId);
        adminServiceClient.syncShow(externalShowId);
        return ResponseEntity.ok(String.format(Constants.SYNC_SUCCESS, externalShowId));
    }

    // ── Cancel booking endpoint ────────────────────────────────────────

    /**
     * Cancels an existing confirmed booking. The seat is released back to
     * AVAILABLE and the booking status becomes CANCELLED.
     *
     * <p>Access is restricted to the owner of the booking or an admin user.</p>
     *
     * @param bookingReference the booking reference to cancel
     * @return 200 OK with the cancelled booking details
     */
    @PreAuthorize("@bookingOwnershipChecker.isOwner(#bookingReference, authentication) or hasAuthority('" + Constants.SCOPE_ADMIN + "')")
    @DeleteMapping(Routes.BOOKINGS_REFERENCE)
    public ResponseEntity<BookingResponseDto> cancelBooking(
            @PathVariable String bookingReference) {
        log.info("Cancel booking request: ref={}", bookingReference);
        BookingResponseDto response = bookingService.cancelBooking(bookingReference);
        return ResponseEntity.ok(response);
    }

    // ── User booking history endpoint ────────────────────────────────

    /**
     * Returns all bookings for a given user, identified by their OAuth2 {@code sub}.
     *
     * <p>Results are ordered most-recent-first.</p>
     *
     * <p>Access is restricted to the owner of the bookings or an admin user.</p>
     *
     * @param userId the OAuth2 subject claim of the user
     * @return 200 OK with the list of bookings (empty list if none)
     */
    @PreAuthorize("authentication.name == #userId or hasAuthority('" + Constants.SCOPE_ADMIN + "')")
    @GetMapping(Routes.USER_BOOKINGS)
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(
            @PathVariable String userId) {
        log.info("Getting bookings for user {}", userId);
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }
}
