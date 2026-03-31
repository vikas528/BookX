/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Transactional component that manages seat locking, confirmation, and
 * release using pessimistic database locks.
 *
 * @since 1.0.0
 */
package com.bookxshow.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bookxshow.entity.Booking;
import com.bookxshow.entity.Seat;
import com.bookxshow.enums.BookingStatus;
import com.bookxshow.enums.SeatStatus;
import com.bookxshow.repository.BookingRepository;
import com.bookxshow.repository.SeatRepository;

/**
 * Manages the database-level seat-lock lifecycle using pessimistic row locks.
 *
 * <p>Every public method in this class runs within its own database
 * transaction. The use of {@code PESSIMISTIC_WRITE} on the seat row
 * ensures that two concurrent transactions cannot both lock the same
 * seat, eliminating double-booking at the database level.</p>
 *
 * <h3>Design rationale</h3>
 * <p>This component is deliberately kept <em>separate</em> from
 * {@link BookingService} so that:
 * <ul>
 *   <li>Spring's AOP proxy can intercept transactional boundaries.</li>
 *   <li>The booking orchestrator can perform a non-transactional payment
 *       call <em>between</em> the lock and confirm transactions.</li>
 * </ul>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 * @see SeatRepository#findByIdWithLock(Long)
 */
@Component
public class SeatLockManager {

    private static final Logger log = LoggerFactory.getLogger(SeatLockManager.class);

    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;

    /**
     * Constructs a {@code SeatLockManager} with the required repositories.
     *
     * @param seatRepository    repository for seat persistence
     * @param bookingRepository repository for booking persistence
     */
    public SeatLockManager(SeatRepository seatRepository,
                           BookingRepository bookingRepository) {
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Attempts to acquire an exclusive lock on the specified seat for the
     * given user. The lock will auto-expire after {@code ttlMs} milliseconds
     * if not confirmed or released.
     *
     * <p>Internally issues a {@code SELECT … FOR UPDATE} to serialise
     * concurrent access at the database row level.</p>
     *
     * @param seatId the primary key of the seat to lock
     * @param userId the user requesting the lock
     * @param ttlMs  duration in milliseconds before the lock auto-expires
     * @return {@code true} if the lock was successfully acquired;
     *         {@code false} if the seat is not available
     */
    @Transactional
    public boolean lockSeat(Long seatId, String userId, long ttlMs) {
        Optional<Seat> seatOpt = seatRepository.findByIdWithLock(seatId);
        if (seatOpt.isEmpty()) {
            log.warn("Lock failed — seat not found: {}", seatId);
            return false;
        }

        Seat seat = seatOpt.get();

        // Self-heal: if the seat is LOCKED but the TTL has expired, treat it as
        // available immediately — no need to wait for the background sweeper.
        if (seat.getStatus() == SeatStatus.LOCKED
                && seat.getLockExpiry() != null
                && Instant.now().isAfter(seat.getLockExpiry())) {
            log.info("Seat {} had an expired lock (held by {}), self-healing to AVAILABLE",
                    seatId, seat.getLockedBy());
            seat.markAvailable();
        }

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            log.debug("Lock failed — seat {} is {} (held by {})",
                    seatId, seat.getStatus(), seat.getLockedBy());
            return false;
        }

        Instant expiry = Instant.now().plusMillis(ttlMs);
        seat.markLocked(userId, expiry);
        seatRepository.save(seat);

        log.info("Locked seat {} for user {} (expires {})", seatId, userId, expiry);
        return true;
    }

    /**
     * Confirms the booking after a successful payment, transitioning the
     * seat to {@link SeatStatus#BOOKED} and persisting a {@link Booking}
     * record — all within a single atomic transaction.
     *
     * @param seatId               the primary key of the locked seat
     * @param userId               the user who holds the lock
     * @param amount               the charged amount
     * @param paymentTransactionId the payment reference from the gateway
     * @return an {@link Optional} containing the newly created booking, or
     *         empty if the lock had expired or belongs to a different user
     */
    @Transactional
    public Optional<Booking> confirmAndPersistBooking(Long seatId,
                                                       String userId,
                                                       BigDecimal amount,
                                                       String paymentTransactionId) {
        Optional<Seat> seatOpt = seatRepository.findByIdWithLock(seatId);
        if (seatOpt.isEmpty()) {
            return Optional.empty();
        }

        Seat seat = seatOpt.get();

        // Verify the lock is still valid and owned by this user
        if (seat.getStatus() != SeatStatus.LOCKED || !userId.equals(seat.getLockedBy())) {
            log.warn("Confirm failed — seat {} not locked by user {}", seatId, userId);
            return Optional.empty();
        }
        if (seat.getLockExpiry() != null && Instant.now().isAfter(seat.getLockExpiry())) {
            log.warn("Confirm failed — lock expired for seat {} (user {})", seatId, userId);
            seat.markAvailable();
            seatRepository.save(seat);
            return Optional.empty();
        }

        // Transition seat to BOOKED
        seat.markBooked();
        seatRepository.save(seat);

        // Persist the booking record
        Booking booking = new Booking();
        booking.setBookingReference("BXS-" + UUID.randomUUID().toString().substring(0, 8));
        booking.setSeat(seat);
        booking.setShow(seat.getShow());
        booking.setUserId(userId);
        booking.setAmount(amount);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentTransactionId(paymentTransactionId);

        Booking saved = bookingRepository.save(booking);
        log.info("Booking confirmed: ref={} seat={} user={}",
                saved.getBookingReference(), seatId, userId);
        return Optional.of(saved);
    }

    /**
     * Releases a previously acquired seat lock, returning the seat to
     * {@link SeatStatus#AVAILABLE}.
     *
     * <p>This is a <em>safe no-op</em> if the seat is no longer locked
     * by the specified user (e.g. the TTL sweeper already reclaimed it).</p>
     *
     * @param seatId the primary key of the seat to release
     * @param userId the user who should own the lock
     * @return {@code true} if the seat was released; {@code false} otherwise
     */
    @Transactional
    public boolean releaseSeat(Long seatId, String userId) {
        Optional<Seat> seatOpt = seatRepository.findByIdWithLock(seatId);
        if (seatOpt.isEmpty()) {
            return false;
        }

        Seat seat = seatOpt.get();
        if (seat.getStatus() != SeatStatus.LOCKED || !userId.equals(seat.getLockedBy())) {
            return false;
        }

        seat.markAvailable();
        seatRepository.save(seat);
        log.info("Released seat {} (user {})", seatId, userId);
        return true;
    }

    /**
     * Cancels a confirmed booking by transitioning the seat back to
     * {@link SeatStatus#AVAILABLE} and updating the booking status to
     * {@link BookingStatus#CANCELLED} — all within a single atomic transaction.
     *
     * <p>If the booking is already cancelled, this is a no-op.</p>
     *
     * @param booking the booking entity to cancel (must be managed / attached)
     */
    @Transactional
    public void cancelBooking(Booking booking) {
        Seat seat = seatRepository.findByIdWithLock(booking.getSeat().getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Seat not found for booking: " + booking.getBookingReference()));

        if (seat.getStatus() == SeatStatus.BOOKED) {
            seat.markAvailable();
            seatRepository.save(seat);
            log.info("Seat {} released (cancel booking {})", seat.getId(), booking.getBookingReference());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking {} cancelled", booking.getBookingReference());
    }
}
