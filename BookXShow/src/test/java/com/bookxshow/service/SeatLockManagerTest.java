/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Unit tests for SeatLockManager using a real H2 database.
 *
 * @since 1.0.0
 */
package com.bookxshow.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.bookxshow.entity.Booking;
import com.bookxshow.entity.Seat;
import com.bookxshow.entity.Show;
import com.bookxshow.enums.SeatStatus;
import com.bookxshow.repository.BookingRepository;
import com.bookxshow.repository.SeatRepository;
import com.bookxshow.repository.ShowRepository;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SeatLockManager} using a slice-test with a real
 * H2 database to verify transactional and locking behaviour.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@DataJpaTest
@Import(SeatLockManager.class)
@ActiveProfiles("test")
class SeatLockManagerTest {

    @Autowired
    private SeatLockManager seatLockManager;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Show show;
    private Seat seat;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        seatRepository.deleteAll();
        showRepository.deleteAll();

        show = new Show();
        show.setExternalShowId("SHOW-TEST-001");
        show.setName("Test Concert");
        show.setTotalSeats(10);
        show = showRepository.save(show);

        seat = new Seat();
        seat.setSeatNumber("A01");
        seat.setShow(show);
        seat.setStatus(SeatStatus.AVAILABLE);
        seat = seatRepository.save(seat);
    }

    @Test
    @DisplayName("T01 - Lock an available seat succeeds")
    void lockAvailableSeat_succeeds() {
        boolean locked = seatLockManager.lockSeat(seat.getId(), "alice", 5000);

        assertTrue(locked);
        Seat updated = seatRepository.findById(seat.getId()).orElseThrow();
        assertEquals(SeatStatus.LOCKED, updated.getStatus());
        assertEquals("alice", updated.getLockedBy());
        assertNotNull(updated.getLockExpiry());
    }

    @Test
    @DisplayName("T02 - Cannot lock an already-locked seat")
    void lockLockedSeat_fails() {
        seatLockManager.lockSeat(seat.getId(), "alice", 5000);
        boolean secondLock = seatLockManager.lockSeat(seat.getId(), "bob", 5000);

        assertFalse(secondLock);
        Seat updated = seatRepository.findById(seat.getId()).orElseThrow();
        assertEquals("alice", updated.getLockedBy());
    }

    @Test
    @DisplayName("T03 - Confirm booking after lock creates Booking record")
    void confirmBooking_createsRecord() {
        seatLockManager.lockSeat(seat.getId(), "alice", 5000);

        Optional<Booking> bookingOpt = seatLockManager.confirmAndPersistBooking(
                seat.getId(), "alice", BigDecimal.valueOf(99.99), "TXN-001");

        assertTrue(bookingOpt.isPresent());
        Booking booking = bookingOpt.get();
        assertNotNull(booking.getBookingReference());
        assertTrue(booking.getBookingReference().startsWith("BXS-"));
        assertEquals("alice", booking.getUserId());

        Seat updated = seatRepository.findById(seat.getId()).orElseThrow();
        assertEquals(SeatStatus.BOOKED, updated.getStatus());
    }

    @Test
    @DisplayName("T04 - Confirm by wrong user fails")
    void confirmByWrongUser_fails() {
        seatLockManager.lockSeat(seat.getId(), "alice", 5000);

        Optional<Booking> bookingOpt = seatLockManager.confirmAndPersistBooking(
                seat.getId(), "bob", BigDecimal.valueOf(99.99), "TXN-001");

        assertTrue(bookingOpt.isEmpty());
        Seat updated = seatRepository.findById(seat.getId()).orElseThrow();
        assertEquals(SeatStatus.LOCKED, updated.getStatus());
    }

    @Test
    @DisplayName("T05 - Release seat makes it AVAILABLE again")
    void releaseSeat_marksAvailable() {
        seatLockManager.lockSeat(seat.getId(), "alice", 5000);

        boolean released = seatLockManager.releaseSeat(seat.getId(), "alice");

        assertTrue(released);
        Seat updated = seatRepository.findById(seat.getId()).orElseThrow();
        assertEquals(SeatStatus.AVAILABLE, updated.getStatus());
        assertNull(updated.getLockedBy());
    }

    @Test
    @DisplayName("T06 - Release by wrong user has no effect")
    void releaseByWrongUser_fails() {
        seatLockManager.lockSeat(seat.getId(), "alice", 5000);

        boolean released = seatLockManager.releaseSeat(seat.getId(), "bob");

        assertFalse(released);
        Seat updated = seatRepository.findById(seat.getId()).orElseThrow();
        assertEquals(SeatStatus.LOCKED, updated.getStatus());
    }

    @Test
    @DisplayName("T07 - Lock on non-existent seat returns false")
    void lockNonExistentSeat_returnsFalse() {
        boolean locked = seatLockManager.lockSeat(99999L, "alice", 5000);
        assertFalse(locked);
    }

    @Test
    @DisplayName("T08 - Cannot lock a BOOKED seat")
    void lockBookedSeat_fails() {
        seatLockManager.lockSeat(seat.getId(), "alice", 5000);
        seatLockManager.confirmAndPersistBooking(
                seat.getId(), "alice", BigDecimal.valueOf(50), null);

        boolean locked = seatLockManager.lockSeat(seat.getId(), "bob", 5000);

        assertFalse(locked);
        Seat updated = seatRepository.findById(seat.getId()).orElseThrow();
        assertEquals(SeatStatus.BOOKED, updated.getStatus());
    }

    @Test
    @DisplayName("T09 - After release, another user can book")
    void afterRelease_anotherUserCanBook() {
        seatLockManager.lockSeat(seat.getId(), "alice", 5000);
        seatLockManager.releaseSeat(seat.getId(), "alice");

        boolean locked = seatLockManager.lockSeat(seat.getId(), "bob", 5000);
        assertTrue(locked);

        Optional<Booking> booking = seatLockManager.confirmAndPersistBooking(
                seat.getId(), "bob", BigDecimal.valueOf(75), null);
        assertTrue(booking.isPresent());
    }

    @Test
    @DisplayName("T10 - Expired lock detected on confirm returns empty")
    void expiredLock_confirmFails() {
        // Lock with a TTL that's already in the past
        seatLockManager.lockSeat(seat.getId(), "alice", 1);

        // Wait briefly so lock expires
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}

        Optional<Booking> bookingOpt = seatLockManager.confirmAndPersistBooking(
                seat.getId(), "alice", BigDecimal.valueOf(99.99), null);

        assertTrue(bookingOpt.isEmpty());
    }

    @Test
    @DisplayName("T11 - Expired lock is self-healed inline: another user can lock immediately")
    void expiredLock_selfHealedOnNextLockAttempt() {
        // Alice locks the seat with a 1 ms TTL — it expires almost immediately
        seatLockManager.lockSeat(seat.getId(), "alice", 1);

        // Wait for the TTL to lapse
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}

        // Bob attempts to lock: lockSeat must detect the expired lock and self-heal
        boolean locked = seatLockManager.lockSeat(seat.getId(), "bob", 5000);

        assertTrue(locked, "Bob should be able to lock a seat whose TTL has expired");
        Seat updated = seatRepository.findById(seat.getId()).orElseThrow();
        assertEquals(SeatStatus.LOCKED, updated.getStatus());
        assertEquals("bob", updated.getLockedBy());
    }

    @Test
    @DisplayName("T12 - Non-expired lock is NOT self-healed: second user still blocked")
    void nonExpiredLock_notSelfHealed() {
        // Alice locks with a generous TTL
        seatLockManager.lockSeat(seat.getId(), "alice", 60_000);

        // Bob attempts to lock immediately — should fail, lock is still valid
        boolean locked = seatLockManager.lockSeat(seat.getId(), "bob", 5000);

        assertFalse(locked, "Bob must not steal a seat whose lock has not expired");
        Seat updated = seatRepository.findById(seat.getId()).orElseThrow();
        assertEquals("alice", updated.getLockedBy());
    }
}
