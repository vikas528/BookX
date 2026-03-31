/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Integration tests for the booking API with a full Spring Boot context.
 *
 * @since 1.0.0
 */
package com.bookxshow;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.bookxshow.config.TestSecurityConfig;
import com.bookxshow.dto.BookingRequestDto;
import com.bookxshow.dto.BookingResponseDto;
import com.bookxshow.entity.Seat;
import com.bookxshow.entity.Show;
import com.bookxshow.enums.BookingOutcome;
import com.bookxshow.enums.PaymentResult;
import com.bookxshow.enums.SeatStatus;
import com.bookxshow.repository.BookingRepository;
import com.bookxshow.repository.SeatRepository;
import com.bookxshow.repository.ShowRepository;
import com.bookxshow.service.AdminServiceClient;
import com.bookxshow.service.PaymentGateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests that boot the full Spring context with a random port,
 * using H2 in-memory database and mocked external services.
 *
 * <p>Verifies the end-to-end booking flow through HTTP, covering:
 * <ul>
 *   <li>IT01 — Single booking happy path</li>
 *   <li>IT02 — Seat status endpoint</li>
 *   <li>IT03 — Unknown seat returns 404</li>
 *   <li>IT04 — Payment failure releases seat</li>
 *   <li>IT05 — Already booked seat returns SEAT_UNAVAILABLE</li>
 * </ul>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
@Import(TestSecurityConfig.class)
class BookingIntegrationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @MockBean
    private PaymentGateway paymentGateway;

    @MockBean
    private AdminServiceClient adminServiceClient;

    private String baseUrl;
    private Show show;
    private Seat seat1;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";

        // Clean database
        bookingRepository.deleteAll();
        seatRepository.deleteAll();
        showRepository.deleteAll();

        // Set up test show and seats
        show = new Show();
        show.setExternalShowId("SHOW-IT-001");
        show.setName("Integration Test Show");
        show.setTotalSeats(5);
        show = showRepository.save(show);

        seat1 = new Seat();
        seat1.setSeatNumber("A01");
        seat1.setShow(show);
        seat1.setStatus(SeatStatus.AVAILABLE);
        seat1 = seatRepository.save(seat1);

        for (int i = 2; i <= 5; i++) {
            Seat s = new Seat();
            s.setSeatNumber(String.format("A%02d", i));
            s.setShow(show);
            s.setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(s);
        }
    }

    // ── IT01 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("IT01 - Single booking happy path via HTTP")
    void singleBookingHappyPath() {
        when(paymentGateway.processPayment(eq("alice"), any(BigDecimal.class)))
                .thenReturn(PaymentResult.SUCCESS);

        BookingRequestDto req = new BookingRequestDto(
                show.getId(), seat1.getId(), "alice", BigDecimal.valueOf(99.00));

        ResponseEntity<BookingResponseDto> resp = rest.postForEntity(
                baseUrl + "/bookings", req, BookingResponseDto.class);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(BookingOutcome.BOOKED, resp.getBody().getOutcome());
        assertNotNull(resp.getBody().getBookingReference());
        assertTrue(resp.getBody().getBookingReference().startsWith("BXS-"));
    }

    // ── IT02 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("IT02 - GET seats for show returns correct list")
    void getSeatsForShow() {
        ResponseEntity<Object[]> resp = rest.getForEntity(
                baseUrl + "/shows/" + show.getId() + "/seats", Object[].class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(5, resp.getBody().length);
    }

    // ── IT03 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("IT03 - Booking non-existent seat returns 404")
    void bookNonExistentSeat_returns404() {
        BookingRequestDto req = new BookingRequestDto(
                show.getId(), 99999L, "bob", BigDecimal.valueOf(10.00));

        ResponseEntity<Map> resp = rest.postForEntity(
                baseUrl + "/bookings", req, Map.class);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    // ── IT04 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("IT04 - Payment failure returns PAYMENT_FAILED")
    void paymentFailure_returnsPaymentFailed() {
        when(paymentGateway.processPayment(eq("charlie"), any(BigDecimal.class)))
                .thenReturn(PaymentResult.FAILURE);

        BookingRequestDto req = new BookingRequestDto(
                show.getId(), seat1.getId(), "charlie", BigDecimal.valueOf(99.00));

        ResponseEntity<BookingResponseDto> resp = rest.postForEntity(
                baseUrl + "/bookings", req, BookingResponseDto.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(BookingOutcome.PAYMENT_FAILED, resp.getBody().getOutcome());

        // Verify seat is back to AVAILABLE
        Seat updated = seatRepository.findById(seat1.getId()).orElseThrow();
        assertEquals(SeatStatus.AVAILABLE, updated.getStatus());
    }

    // ── IT05 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("IT05 - Already booked seat returns SEAT_UNAVAILABLE")
    void alreadyBookedSeat_returnsUnavailable() {
        when(paymentGateway.processPayment(anyString(), any(BigDecimal.class)))
                .thenReturn(PaymentResult.SUCCESS);

        // Alice books seat
        BookingRequestDto req1 = new BookingRequestDto(
                show.getId(), seat1.getId(), "alice", BigDecimal.valueOf(99.00));
        rest.postForEntity(baseUrl + "/bookings", req1, BookingResponseDto.class);

        // Bob tries the same seat
        BookingRequestDto req2 = new BookingRequestDto(
                show.getId(), seat1.getId(), "bob", BigDecimal.valueOf(99.00));
        ResponseEntity<BookingResponseDto> resp = rest.postForEntity(
                baseUrl + "/bookings", req2, BookingResponseDto.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(BookingOutcome.SEAT_UNAVAILABLE, resp.getBody().getOutcome());
    }
}
