/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Unit tests for BookingController.
 *
 * @since 1.0.0
 */
package com.bookxshow.controller;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.bookxshow.dto.BookingRequestDto;
import com.bookxshow.dto.BookingResponseDto;
import com.bookxshow.dto.SeatStatusDto;
import com.bookxshow.enums.BookingOutcome;
import com.bookxshow.enums.SeatStatus;
import com.bookxshow.exception.SeatNotFoundException;
import com.bookxshow.exception.ShowNotFoundException;
import com.bookxshow.service.AdminServiceClient;
import com.bookxshow.service.BookingService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BookingController}.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private AdminServiceClient adminServiceClient;

    @InjectMocks
    private BookingController controller;

    // ── bookSeat ────────────────────────────────────────────────────────

    @Test
    @DisplayName("T01 - bookSeat with BOOKED outcome returns 201")
    void bookSeat_booked_returns201() {
        BookingRequestDto request = new BookingRequestDto(1L, 10L, "alice", BigDecimal.valueOf(99));
        BookingResponseDto response = BookingResponseDto.builder()
                .outcome(BookingOutcome.BOOKED)
                .bookingReference("BXS-abc12345")
                .seatId(10L)
                .userId("alice")
                .build();
        when(bookingService.bookSeat(any())).thenReturn(response);

        ResponseEntity<BookingResponseDto> result = controller.bookSeat(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(BookingOutcome.BOOKED, result.getBody().getOutcome());
    }

    @Test
    @DisplayName("T02 - bookSeat with SEAT_UNAVAILABLE outcome returns 200")
    void bookSeat_seatUnavailable_returns200() {
        BookingRequestDto request = new BookingRequestDto(1L, 10L, "alice", BigDecimal.valueOf(99));
        BookingResponseDto response = BookingResponseDto.builder()
                .outcome(BookingOutcome.SEAT_UNAVAILABLE)
                .seatId(10L)
                .userId("alice")
                .build();
        when(bookingService.bookSeat(any())).thenReturn(response);

        ResponseEntity<BookingResponseDto> result = controller.bookSeat(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(BookingOutcome.SEAT_UNAVAILABLE, result.getBody().getOutcome());
    }

    @Test
    @DisplayName("T03 - bookSeat with PAYMENT_FAILED outcome returns 200")
    void bookSeat_paymentFailed_returns200() {
        BookingRequestDto request = new BookingRequestDto(1L, 10L, "alice", BigDecimal.valueOf(99));
        BookingResponseDto response = BookingResponseDto.builder()
                .outcome(BookingOutcome.PAYMENT_FAILED)
                .seatId(10L)
                .userId("alice")
                .build();
        when(bookingService.bookSeat(any())).thenReturn(response);

        ResponseEntity<BookingResponseDto> result = controller.bookSeat(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(BookingOutcome.PAYMENT_FAILED, result.getBody().getOutcome());
    }

    @Test
    @DisplayName("T04 - bookSeat with LOCK_EXPIRED outcome returns 200")
    void bookSeat_lockExpired_returns200() {
        BookingRequestDto request = new BookingRequestDto(1L, 10L, "alice", BigDecimal.valueOf(99));
        BookingResponseDto response = BookingResponseDto.builder()
                .outcome(BookingOutcome.LOCK_EXPIRED)
                .seatId(10L)
                .userId("alice")
                .build();
        when(bookingService.bookSeat(any())).thenReturn(response);

        ResponseEntity<BookingResponseDto> result = controller.bookSeat(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(BookingOutcome.LOCK_EXPIRED, result.getBody().getOutcome());
    }

    // ── getBooking ──────────────────────────────────────────────────────

    @Test
    @DisplayName("T05 - getBooking returns booking details")
    void getBooking_returnsDetails() {
        BookingResponseDto response = BookingResponseDto.builder()
                .bookingReference("BXS-abc12345")
                .outcome(BookingOutcome.BOOKED)
                .seatId(10L)
                .userId("alice")
                .build();
        when(bookingService.getBooking("BXS-abc12345")).thenReturn(response);

        ResponseEntity<BookingResponseDto> result = controller.getBooking("BXS-abc12345");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("BXS-abc12345", result.getBody().getBookingReference());
    }

    // ── getSeatsForShow ─────────────────────────────────────────────────

    @Test
    @DisplayName("T06 - getSeatsForShow returns seat list")
    void getSeatsForShow_returnsSeatList() {
        SeatStatusDto seatStatusDto = new SeatStatusDto(10L, "A01", 1L, SeatStatus.AVAILABLE, null, null);
        when(bookingService.getSeatsForShow(1L)).thenReturn(List.of(seatStatusDto));

        ResponseEntity<List<SeatStatusDto>> result = controller.getSeatsForShow(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals("A01", result.getBody().get(0).getSeatNumber());
    }

    @Test
    @DisplayName("T07 - getSeatsForShow throws when show not found")
    void getSeatsForShow_showNotFound_throws() {
        when(bookingService.getSeatsForShow(999L)).thenThrow(new ShowNotFoundException("Show not found: 999"));

        assertThrows(ShowNotFoundException.class, () -> controller.getSeatsForShow(999L));
    }

    @Test
    @DisplayName("T08 - getSeatsForShow returns empty list for show with no seats")
    void getSeatsForShow_noSeats_returnsEmptyList() {
        when(bookingService.getSeatsForShow(1L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<SeatStatusDto>> result = controller.getSeatsForShow(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    // ── getSeatStatus ───────────────────────────────────────────────────

    @Test
    @DisplayName("T09 - getSeatStatus returns seat status")
    void getSeatStatus_returnsStatus() {
        SeatStatusDto seatStatusDto = new SeatStatusDto(10L, "A01", 1L, SeatStatus.AVAILABLE, null, null);
        when(bookingService.getSeatStatus(1L, 10L)).thenReturn(seatStatusDto);

        ResponseEntity<SeatStatusDto> result = controller.getSeatStatus(1L, 10L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("A01", result.getBody().getSeatNumber());
        assertEquals(SeatStatus.AVAILABLE, result.getBody().getStatus());
    }

    @Test
    @DisplayName("T10 - getSeatStatus throws when show not found")
    void getSeatStatus_showNotFound_throws() {
        when(bookingService.getSeatStatus(999L, 10L)).thenThrow(new ShowNotFoundException("Show not found: 999"));

        assertThrows(ShowNotFoundException.class, () -> controller.getSeatStatus(999L, 10L));
    }

    @Test
    @DisplayName("T11 - getSeatStatus throws when seat not found")
    void getSeatStatus_seatNotFound_throws() {
        when(bookingService.getSeatStatus(1L, 999L)).thenThrow(new SeatNotFoundException("Seat not found: 999"));

        assertThrows(SeatNotFoundException.class, () -> controller.getSeatStatus(1L, 999L));
    }

    @Test
    @DisplayName("T12 - getSeatStatus throws when seat belongs to different show")
    void getSeatStatus_seatWrongShow_throws() {
        when(bookingService.getSeatStatus(1L, 20L)).thenThrow(new SeatNotFoundException("Seat 20 not in show 1"));

        assertThrows(SeatNotFoundException.class, () -> controller.getSeatStatus(1L, 20L));
    }

    // ── syncShow ────────────────────────────────────────────────────────

    @Test
    @DisplayName("T13 - syncShow calls admin service and returns success message")
    void syncShow_returnsSuccess() {
        doNothing().when(adminServiceClient).syncShow("SHOW-EXT-001");

        ResponseEntity<String> result = controller.syncShow("SHOW-EXT-001");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().contains("SHOW-EXT-001"));
        verify(adminServiceClient).syncShow("SHOW-EXT-001");
    }
}
