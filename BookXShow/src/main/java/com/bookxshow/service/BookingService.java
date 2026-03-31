/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Service interface defining the core booking operations.
 *
 * @since 1.0.0
 */
package com.bookxshow.service;

import com.bookxshow.dto.BookingRequestDto;
import com.bookxshow.dto.BookingResponseDto;
import com.bookxshow.dto.SeatStatusDto;
import com.bookxshow.entity.Booking;
import com.bookxshow.exception.BookingNotFoundException;
import com.bookxshow.exception.SeatNotFoundException;
import com.bookxshow.exception.ShowNotFoundException;

import java.util.List;

/**
 * Primary service interface for seat booking operations.
 *
 * <p>Implementations orchestrate the full booking lifecycle:</p>
 * <ol>
 *   <li>Lock the requested seat with a time-to-live (TTL).</li>
 *   <li>Charge the user via the {@link PaymentGateway}.</li>
 *   <li>Confirm or release the seat based on the payment outcome.</li>
 *   <li>Persist the {@link Booking} record.</li>
 * </ol>
 *
 * <p>This interface follows the <em>Dependency Inversion Principle</em> —
 * controllers and other consumers depend on this abstraction rather than
 * a concrete class.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface BookingService {

    /**
     * Attempts to book a seat for the specified user and show.
     *
     * <p>The method is <b>not</b> transactional itself; it delegates to
     * transactional components for the lock, confirm, and persist steps
     * so that the potentially long-running payment call does not hold a
     * database transaction open.</p>
     *
     * @param request the booking request containing show ID, seat ID,
     *                user ID, and payment amount
     * @return a response describing the outcome of the booking attempt
     */
    BookingResponseDto bookSeat(BookingRequestDto request);

    /**
     * {@inheritDoc}
     */
    BookingResponseDto getBooking(String bookingReference);

    /**
     * Returns all seats for a show with their current status.
     *
     * @param showId the show primary key
     * @return ordered list of seat status DTOs
     * @throws ShowNotFoundException if no show exists with the given ID
     */
    List<SeatStatusDto> getSeatsForShow(Long showId);

    /**
     * Returns the status of a single seat, verifying it belongs to the given show.
     *
     * @param showId the show primary key
     * @param seatId the seat primary key
     * @return the seat status DTO
     * @throws ShowNotFoundException if no show exists with the given ID
     * @throws SeatNotFoundException if the seat does not exist or does not belong to the show
     */
    SeatStatusDto getSeatStatus(Long showId, Long seatId);

    /**
     * Returns all confirmed bookings made by the specified user, ordered by
     * creation date descending (most recent first).
     *
     * @param userId the OAuth2 {@code sub} of the user
     * @return list of the user's bookings (empty list if none found)
     */
    List<BookingResponseDto> getBookingsByUser(String userId);

    /**
     * Cancels an existing confirmed booking, releasing the seat back to
     * {@code AVAILABLE} and setting the booking status to {@code CANCELLED}.
     *
     * <p>Calling this on an already-cancelled booking is idempotent.</p>
     *
     * @param bookingReference the unique booking reference code
     * @return a response with outcome {@code CANCELLED}
     * @throws BookingNotFoundException if no booking exists with the given reference
     */
    BookingResponseDto cancelBooking(String bookingReference);
}
