/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Unit tests for BookingServiceImpl using Mockito.
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bookxshow.config.BookingProperties;
import com.bookxshow.dto.BookingRequestDto;
import com.bookxshow.dto.BookingResponseDto;
import com.bookxshow.entity.Booking;
import com.bookxshow.entity.Seat;
import com.bookxshow.entity.Show;
import com.bookxshow.enums.BookingOutcome;
import com.bookxshow.enums.BookingStatus;
import com.bookxshow.enums.PaymentResult;
import com.bookxshow.enums.SeatStatus;
import com.bookxshow.exception.SeatNotFoundException;
import com.bookxshow.repository.BookingRepository;
import com.bookxshow.repository.SeatRepository;
import com.bookxshow.service.impl.BookingServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BookingServiceImpl}.
 *
 * <p>Uses Mockito to isolate the service from infrastructure dependencies
 * (database, payment gateway). Each test covers a specific scenario in
 * the lock → pay → confirm flow.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceUnitTest {

    @Mock
    private SeatLockManager seatLockManager;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private BookingProperties properties;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingRequestDto request;
    private Seat seat;
    private Show show;

    @BeforeEach
    void setUp() {
        // Configure properties mock
        BookingProperties.Booking bookingProps = new BookingProperties.Booking();
        bookingProps.setSeatLockTtlMs(5000);
        lenient().when(properties.getBooking()).thenReturn(bookingProps);

        // Set up test show and seat
        show = new Show();
        show.setId(1L);
        show.setExternalShowId("SHOW-001");
        show.setName("Test Show");

        seat = new Seat();
        seat.setId(10L);
        seat.setSeatNumber("A01");
        seat.setShow(show);
        seat.setStatus(SeatStatus.AVAILABLE);

        request = new BookingRequestDto(1L, 10L, "alice", BigDecimal.valueOf(99.99));
    }

    // ── Happy path ─────────────────────────────────────────────────────

    @Test
    @DisplayName("T01 - Happy path: lock → payment SUCCESS → BOOKED")
    void happyPath_bookSeat_returnsBooked() {
        // Arrange
        when(seatRepository.findById(10L)).thenReturn(Optional.of(seat));
        when(seatLockManager.lockSeat(eq(10L), eq("alice"), anyLong())).thenReturn(true);
        when(paymentGateway.processPayment(eq("alice"), any(BigDecimal.class)))
                .thenReturn(PaymentResult.SUCCESS);

        Booking booking = createBooking("BXS-test1234");
        when(seatLockManager.confirmAndPersistBooking(eq(10L), eq("alice"), any(), any()))
                .thenReturn(Optional.of(booking));

        // Act
        BookingResponseDto response = bookingService.bookSeat(request);

        // Assert
        assertEquals(BookingOutcome.BOOKED, response.getOutcome());
        assertEquals("BXS-test1234", response.getBookingReference());
        assertEquals(10L, response.getSeatId());
        assertEquals("alice", response.getUserId());

        verify(seatLockManager).lockSeat(eq(10L), eq("alice"), anyLong());
        verify(paymentGateway).processPayment(eq("alice"), any());
        verify(seatLockManager).confirmAndPersistBooking(eq(10L), eq("alice"), any(), any());
    }

    // ── Seat unavailable ───────────────────────────────────────────────

    @Test
    @DisplayName("T02 - Seat already locked returns SEAT_UNAVAILABLE")
    void lockedSeat_returnsUnavailable() {
        when(seatRepository.findById(10L)).thenReturn(Optional.of(seat));
        when(seatLockManager.lockSeat(eq(10L), eq("alice"), anyLong())).thenReturn(false);

        BookingResponseDto response = bookingService.bookSeat(request);

        assertEquals(BookingOutcome.SEAT_UNAVAILABLE, response.getOutcome());
        assertNull(response.getBookingReference());
        verify(paymentGateway, never()).processPayment(any(), any());
    }

    // ── Payment failure ────────────────────────────────────────────────

    @Test
    @DisplayName("T03 - Payment FAILURE releases seat and returns PAYMENT_FAILED")
    void paymentFailure_releasesSeat() {
        when(seatRepository.findById(10L)).thenReturn(Optional.of(seat));
        when(seatLockManager.lockSeat(eq(10L), eq("alice"), anyLong())).thenReturn(true);
        when(paymentGateway.processPayment(eq("alice"), any()))
                .thenReturn(PaymentResult.FAILURE);

        // After release, seat should be AVAILABLE
        Seat availableSeat = new Seat();
        availableSeat.setId(10L);
        availableSeat.setStatus(SeatStatus.AVAILABLE);
        when(seatRepository.findById(10L)).thenReturn(Optional.of(availableSeat));

        BookingResponseDto response = bookingService.bookSeat(request);

        assertEquals(BookingOutcome.PAYMENT_FAILED, response.getOutcome());
        verify(seatLockManager).releaseSeat(10L, "alice");
    }

    // ── Payment timeout ────────────────────────────────────────────────

    @Test
    @DisplayName("T04 - Payment TIMEOUT releases seat and returns LOCK_EXPIRED")
    void paymentTimeout_returnsLockExpired() {
        when(seatRepository.findById(10L)).thenReturn(Optional.of(seat));
        when(seatLockManager.lockSeat(eq(10L), eq("alice"), anyLong())).thenReturn(true);
        when(paymentGateway.processPayment(eq("alice"), any()))
                .thenReturn(PaymentResult.TIMEOUT);

        Seat availableSeat = new Seat();
        availableSeat.setId(10L);
        availableSeat.setStatus(SeatStatus.AVAILABLE);
        when(seatRepository.findById(10L)).thenReturn(Optional.of(availableSeat));

        BookingResponseDto response = bookingService.bookSeat(request);

        assertEquals(BookingOutcome.LOCK_EXPIRED, response.getOutcome());
        verify(seatLockManager).releaseSeat(10L, "alice");
    }

    // ── Lock expired during payment ────────────────────────────────────

    @Test
    @DisplayName("T05 - Lock expires during payment → LOCK_EXPIRED")
    void lockExpiresDuringPayment_returnsLockExpired() {
        when(seatRepository.findById(10L)).thenReturn(Optional.of(seat));
        when(seatLockManager.lockSeat(eq(10L), eq("alice"), anyLong())).thenReturn(true);
        when(paymentGateway.processPayment(eq("alice"), any()))
                .thenReturn(PaymentResult.SUCCESS);
        // confirmAndPersistBooking returns empty when lock expired
        when(seatLockManager.confirmAndPersistBooking(eq(10L), eq("alice"), any(), any()))
                .thenReturn(Optional.empty());

        Seat availableSeat = new Seat();
        availableSeat.setId(10L);
        availableSeat.setStatus(SeatStatus.AVAILABLE);
        when(seatRepository.findById(10L)).thenReturn(Optional.of(availableSeat));

        BookingResponseDto response = bookingService.bookSeat(request);

        assertEquals(BookingOutcome.LOCK_EXPIRED, response.getOutcome());
    }

    // ── Seat not found ─────────────────────────────────────────────────

    @Test
    @DisplayName("T06 - Non-existent seat throws SeatNotFoundException")
    void nonExistentSeat_throwsException() {
        when(seatRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(SeatNotFoundException.class, () -> bookingService.bookSeat(request));
    }

    // ── Get booking ────────────────────────────────────────────────────

    @Test
    @DisplayName("T07 - Get existing booking returns details")
    void getBooking_returnsDetails() {
        Booking booking = createBooking("BXS-abc12345");
        when(bookingRepository.findByBookingReference("BXS-abc12345"))
                .thenReturn(Optional.of(booking));

        BookingResponseDto response = bookingService.getBooking("BXS-abc12345");

        assertEquals("BXS-abc12345", response.getBookingReference());
        assertEquals(BookingOutcome.BOOKED, response.getOutcome());
    }

    // ── Helper methods ─────────────────────────────────────────────────

    /**
     * Creates a test Booking entity with the given reference.
     */
    private Booking createBooking(String reference) {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookingReference(reference);
        booking.setSeat(seat);
        booking.setShow(show);
        booking.setUserId("alice");
        booking.setAmount(BigDecimal.valueOf(99.99));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(Instant.now());
        return booking;
    }
}
