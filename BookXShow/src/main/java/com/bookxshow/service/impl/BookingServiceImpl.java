/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Concrete implementation of BookingService that orchestrates the
 * lock → pay → confirm flow.
 *
 * @since 1.0.0
 */
package com.bookxshow.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bookxshow.config.BookingProperties;
import com.bookxshow.dto.BookingRequestDto;
import com.bookxshow.dto.BookingResponseDto;
import com.bookxshow.dto.SeatStatusDto;
import com.bookxshow.entity.Booking;
import com.bookxshow.entity.Seat;
import com.bookxshow.enums.BookingOutcome;
import com.bookxshow.enums.BookingStatus;
import com.bookxshow.enums.PaymentResult;
import com.bookxshow.enums.SeatStatus;
import com.bookxshow.exception.BookingNotFoundException;
import com.bookxshow.exception.SeatNotFoundException;
import com.bookxshow.exception.ShowNotFoundException;
import com.bookxshow.repository.BookingRepository;
import com.bookxshow.repository.SeatRepository;
import com.bookxshow.repository.ShowRepository;
import com.bookxshow.service.BookingService;
import com.bookxshow.service.PaymentGateway;
import com.bookxshow.service.SeatLockManager;

/**
 * Production implementation of {@link BookingService}.
 *
 * <h3>Booking flow</h3>
 * <ol>
 *   <li><b>Lock</b> — Acquire an exclusive lock on the seat via
 *       {@link SeatLockManager#lockSeat}. If the seat is not
 *       available, return {@link BookingOutcome#SEAT_UNAVAILABLE}.</li>
 *   <li><b>Pay</b> — Call the external {@link PaymentGateway} to charge
 *       the user. This step runs <em>outside</em> a database transaction
 *       to avoid holding row locks during a potentially slow network call.</li>
 *   <li><b>Confirm / Release</b> — Based on the payment result, either
 *       confirm the booking (seat → BOOKED, persist Booking record) or
 *       release the seat back to AVAILABLE.</li>
 * </ol>
 *
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><em>Strategy</em> — {@link PaymentGateway} is injected as an
 *       interface, allowing hot-swapping of payment providers.</li>
 *   <li><em>Template Method</em> — The three-step flow is fixed; only
 *       the payment strategy varies.</li>
 * </ul>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final SeatLockManager seatLockManager;
    private final PaymentGateway paymentGateway;
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final BookingProperties properties;

    public BookingServiceImpl(SeatLockManager seatLockManager,
                              PaymentGateway paymentGateway,
                              BookingRepository bookingRepository,
                              SeatRepository seatRepository,
                              ShowRepository showRepository,
                              BookingProperties properties) {
        this.seatLockManager = seatLockManager;
        this.paymentGateway = paymentGateway;
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This method is deliberately <strong>non-transactional</strong> so
     * that the payment gateway call does not hold a database lock open.</p>
     */
    @Override
    public BookingResponseDto bookSeat(BookingRequestDto request) {
        Long seatId = request.getSeatId();
        String userId = request.getUserId();

        // Validate seat exists
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new SeatNotFoundException(seatId));

        // ── Step 1: Lock the seat ──────────────────────────────────────────
        long ttlMs = properties.getBooking().getSeatLockTtlMs();
        boolean locked = seatLockManager.lockSeat(seatId, userId, ttlMs);
        if (!locked) {
            log.debug("Seat {} unavailable for user {}", seatId, userId);
            // Re-read seat to distinguish BOOKED vs LOCKED
            Seat current = seatRepository.findById(seatId).orElse(null);
            BookingOutcome outcome = (current != null && current.getStatus() == SeatStatus.BOOKED)
                    ? BookingOutcome.ALREADY_BOOKED
                    : BookingOutcome.SEAT_UNAVAILABLE;
            return buildResponse(seatId, seat.getSeatNumber(), userId, outcome, null);
        }

        // ── Step 2: Process payment (non-transactional) ────────────────────
        PaymentResult paymentResult = paymentGateway.processPayment(
                userId, request.getAmount());
        log.info("Payment result for user {} seat {}: {}", userId, seatId, paymentResult);

        // ── Step 3: Confirm or release ─────────────────────────────────────
        return switch (paymentResult) {
            case SUCCESS -> handlePaymentSuccess(seatId, seat.getSeatNumber(), userId, request);
            case FAILURE -> handlePaymentFailure(seatId, seat.getSeatNumber(), userId);
            case TIMEOUT -> handlePaymentTimeout(seatId, seat.getSeatNumber(), userId);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BookingResponseDto getBooking(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new BookingNotFoundException(bookingReference));

        return BookingResponseDto.builder()
                .bookingReference(booking.getBookingReference())
                .seatId(booking.getSeat().getId())
                .seatNumber(booking.getSeat().getSeatNumber())
                .showId(booking.getShow().getId())
                .userId(booking.getUserId())
                .outcome(BookingOutcome.BOOKED)
                .seatStatus(booking.getSeat().getStatus())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SeatStatusDto> getSeatsForShow(Long showId) {
        if (!showRepository.existsById(showId)) {
            throw new ShowNotFoundException(showId);
        }
        return seatRepository.findByShowIdOrderBySeatNumber(showId)
                .stream()
                .map(this::toSeatStatusDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeatStatusDto getSeatStatus(Long showId, Long seatId) {
        if (!showRepository.existsById(showId)) {
            throw new ShowNotFoundException(showId);
        }
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new SeatNotFoundException(seatId));
        if (!seat.getShow().getId().equals(showId)) {
            throw new SeatNotFoundException(seatId);
        }
        return toSeatStatusDto(seat);
    }

    // ── Private helpers ────────────────────────────────────────────────────

    /** Maps a {@link Seat} entity to a {@link SeatStatusDto}. */
    private SeatStatusDto toSeatStatusDto(Seat seat) {
        return new SeatStatusDto(
                seat.getId(),
                seat.getSeatNumber(),
                seat.getShow().getId(),
                seat.getStatus(),
                seat.getLockedBy(),
                seat.getLockExpiry());
    }

    /**
     * Handles a successful payment by confirming the seat and persisting
     * the booking record atomically.
     */
    private BookingResponseDto handlePaymentSuccess(Long seatId, String seatNumber,
                                                     String userId,
                                                     BookingRequestDto request) {
        Optional<Booking> bookingOpt = seatLockManager.confirmAndPersistBooking(
                seatId, userId, request.getAmount(), null);

        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            return BookingResponseDto.builder()
                    .bookingReference(booking.getBookingReference())
                    .seatId(seatId)
                    .seatNumber(seatNumber)
                    .showId(request.getShowId())
                    .userId(userId)
                    .outcome(BookingOutcome.BOOKED)
                    .seatStatus(SeatStatus.BOOKED)
                    .build();
        }

        // Lock expired during payment processing
        return buildResponse(seatId, seatNumber, userId,
                BookingOutcome.LOCK_EXPIRED, null);
    }

    /**
     * Handles a payment failure by releasing the seat lock.
     */
    private BookingResponseDto handlePaymentFailure(Long seatId, String seatNumber,
                                                     String userId) {
        seatLockManager.releaseSeat(seatId, userId);
        return buildResponse(seatId, seatNumber, userId,
                BookingOutcome.PAYMENT_FAILED, null);
    }

    /**
     * Handles a payment timeout by releasing the seat lock.
     */
    private BookingResponseDto handlePaymentTimeout(Long seatId, String seatNumber,
                                                     String userId) {
        seatLockManager.releaseSeat(seatId, userId);
        return buildResponse(seatId, seatNumber, userId,
                BookingOutcome.LOCK_EXPIRED, null);
    }

    /**
     * Builds a standard response DTO.
     */
    private BookingResponseDto buildResponse(Long seatId, String seatNumber,
                                              String userId,
                                              BookingOutcome outcome,
                                              String bookingReference) {
        SeatStatus currentStatus = seatRepository.findById(seatId)
                .map(Seat::getStatus)
                .orElse(null);

        return BookingResponseDto.builder()
                .bookingReference(bookingReference)
                .seatId(seatId)
                .seatNumber(seatNumber)
                .userId(userId)
                .outcome(outcome)
                .seatStatus(currentStatus)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BookingResponseDto> getBookingsByUser(String userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(booking -> BookingResponseDto.builder()
                        .bookingReference(booking.getBookingReference())
                        .seatId(booking.getSeat().getId())
                        .seatNumber(booking.getSeat().getSeatNumber())
                        .showId(booking.getShow().getId())
                        .userId(booking.getUserId())
                        .outcome(BookingOutcome.BOOKED)
                        .seatStatus(booking.getSeat().getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BookingResponseDto cancelBooking(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new BookingNotFoundException(bookingReference));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            // Idempotent — already cancelled
            log.info("Booking {} is already cancelled", bookingReference);
            return BookingResponseDto.builder()
                    .bookingReference(booking.getBookingReference())
                    .seatId(booking.getSeat().getId())
                    .seatNumber(booking.getSeat().getSeatNumber())
                    .showId(booking.getShow().getId())
                    .userId(booking.getUserId())
                    .outcome(BookingOutcome.CANCELLED)
                    .seatStatus(booking.getSeat().getStatus())
                    .build();
        }

        seatLockManager.cancelBooking(booking);

        return BookingResponseDto.builder()
                .bookingReference(booking.getBookingReference())
                .seatId(booking.getSeat().getId())
                .seatNumber(booking.getSeat().getSeatNumber())
                .showId(booking.getShow().getId())
                .userId(booking.getUserId())
                .outcome(BookingOutcome.CANCELLED)
                .seatStatus(SeatStatus.AVAILABLE)
                .build();
    }
}
