/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Unit tests for ShowServiceImpl.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bookxshow.admin.dto.CreateShowRequest;
import com.bookxshow.admin.dto.SeatResponse;
import com.bookxshow.admin.dto.ShowResponse;
import com.bookxshow.admin.dto.UpdateShowRequest;
import com.bookxshow.admin.entity.Seat;
import com.bookxshow.admin.entity.Show;
import com.bookxshow.admin.enums.SeatCategory;
import com.bookxshow.admin.enums.ShowStatus;
import com.bookxshow.admin.exception.InvalidShowStateException;
import com.bookxshow.admin.exception.ShowNotFoundException;
import com.bookxshow.admin.repository.SeatRepository;
import com.bookxshow.admin.repository.ShowRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ShowServiceImpl}. All database interactions
 * are mocked via Mockito.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ShowServiceImplTest {

    @Mock
    private ShowRepository showRepository;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private ShowServiceImpl showService;

    @Captor
    private ArgumentCaptor<Show> showCaptor;

    @Captor
    private ArgumentCaptor<List<Seat>> seatsCaptor;

    private Show testShow;
    private Seat testSeat;

    @BeforeEach
    void setUp() {
        testShow = new Show();
        testShow.setId(1L);
        testShow.setExternalShowId("SHOW-ABC12345");
        testShow.setName("Test Concert");
        testShow.setVenue("Main Hall");
        testShow.setShowDateTime(LocalDateTime.of(2026, 12, 25, 19, 30));
        testShow.setTotalSeats(5);
        testShow.setSeatsPerRow(20);
        testShow.setBasePrice(BigDecimal.valueOf(99.99));
        testShow.setGenre("Rock");
        testShow.setLanguage("English");
        testShow.setDuration("2h 30m");
        testShow.setRating(4.5);
        testShow.setPosterUrl("https://example.com/poster.jpg");
        testShow.setStatus(ShowStatus.DRAFT);
        testShow.setCreatedAt(Instant.now());
        testShow.setUpdatedAt(Instant.now());

        testSeat = new Seat();
        testSeat.setId(10L);
        testSeat.setSeatNumber("A01");
        testSeat.setShow(testShow);
        testSeat.setCategory(SeatCategory.STANDARD);
        testSeat.setPrice(BigDecimal.valueOf(99.99));
    }

    // ── Create Show Tests ──────────────────────────────────────────────

    @Nested
    @DisplayName("createShow")
    class CreateShowTests {

        @Test
        @DisplayName("should create show with auto-generated seats")
        void createShow_success() {
            CreateShowRequest request = new CreateShowRequest(
                    "Rock Concert", "Arena", LocalDateTime.of(2026, 12, 25, 19, 30),
                    5, 20, BigDecimal.valueOf(50.00), "Rock", "English", "2h", 4.5, null);

            when(showRepository.save(any(Show.class))).thenAnswer(invocation -> {
                Show saved = invocation.getArgument(0);
                saved.setId(1L);
                saved.setExternalShowId("SHOW-12345678");
                saved.setCreatedAt(Instant.now());
                saved.setUpdatedAt(Instant.now());
                return saved;
            });
            when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            ShowResponse response = showService.createShow(request);

            assertNotNull(response);
            assertEquals("Rock Concert", response.getName());
            assertEquals("Arena", response.getVenue());
            assertEquals(5, response.getTotalSeats());
            assertEquals(ShowStatus.DRAFT, response.getStatus());

            verify(showRepository).save(showCaptor.capture());
            Show captured = showCaptor.getValue();
            assertEquals("Rock Concert", captured.getName());
            assertEquals(ShowStatus.DRAFT, captured.getStatus());

            verify(seatRepository).saveAll(seatsCaptor.capture());
            List<Seat> seats = seatsCaptor.getValue();
            assertEquals(5, seats.size());
            assertEquals("A01", seats.get(0).getSeatNumber());
            assertEquals("A05", seats.get(4).getSeatNumber());
        }

        @Test
        @DisplayName("should generate seats across multiple rows when totalSeats > 20")
        void createShow_multipleRows() {
            CreateShowRequest request = new CreateShowRequest(
                    "Big Show", "Stadium", LocalDateTime.of(2026, 12, 25, 19, 30),
                    25, 20, BigDecimal.valueOf(100.00), null, null, null, null, null);

            when(showRepository.save(any(Show.class))).thenAnswer(invocation -> {
                Show saved = invocation.getArgument(0);
                saved.setId(2L);
                saved.setExternalShowId("SHOW-99887766");
                saved.setCreatedAt(Instant.now());
                saved.setUpdatedAt(Instant.now());
                return saved;
            });
            when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            showService.createShow(request);

            verify(seatRepository).saveAll(seatsCaptor.capture());
            List<Seat> seats = seatsCaptor.getValue();
            assertEquals(25, seats.size());
            // Row A: A01-A20
            assertEquals("A01", seats.get(0).getSeatNumber());
            assertEquals("A20", seats.get(19).getSeatNumber());
            // Row B: B01-B05
            assertEquals("B01", seats.get(20).getSeatNumber());
            assertEquals("B05", seats.get(24).getSeatNumber());
        }
    }

    // ── Get Show Tests ─────────────────────────────────────────────────

    @Nested
    @DisplayName("getShowById")
    class GetShowByIdTests {

        @Test
        @DisplayName("should return show when found")
        void getShowById_found() {
            when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));

            ShowResponse response = showService.getShowById(1L);

            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("Test Concert", response.getName());
            assertEquals("SHOW-ABC12345", response.getExternalShowId());
        }

        @Test
        @DisplayName("should throw ShowNotFoundException when not found")
        void getShowById_notFound() {
            when(showRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ShowNotFoundException.class,
                    () -> showService.getShowById(999L));
        }
    }

    @Nested
    @DisplayName("getShowByExternalId")
    class GetShowByExternalIdTests {

        @Test
        @DisplayName("should return show when found by external ID")
        void getShowByExternalId_found() {
            when(showRepository.findByExternalShowId("SHOW-ABC12345"))
                    .thenReturn(Optional.of(testShow));

            ShowResponse response = showService.getShowByExternalId("SHOW-ABC12345");

            assertNotNull(response);
            assertEquals("SHOW-ABC12345", response.getExternalShowId());
        }

        @Test
        @DisplayName("should throw ShowNotFoundException when external ID not found")
        void getShowByExternalId_notFound() {
            when(showRepository.findByExternalShowId("SHOW-INVALID"))
                    .thenReturn(Optional.empty());

            assertThrows(ShowNotFoundException.class,
                    () -> showService.getShowByExternalId("SHOW-INVALID"));
        }
    }

    // ── Get All Shows Tests ────────────────────────────────────────────

    @Nested
    @DisplayName("getAllShows")
    class GetAllShowsTests {

        @Test
        @DisplayName("should return all shows ordered by date")
        void getAllShows_returnsList() {
            Show show2 = new Show();
            show2.setId(2L);
            show2.setExternalShowId("SHOW-DEF67890");
            show2.setName("Jazz Evening");
            show2.setVenue("Jazz Club");
            show2.setShowDateTime(LocalDateTime.of(2026, 11, 15, 20, 0));
            show2.setTotalSeats(3);
            show2.setBasePrice(BigDecimal.valueOf(75.00));
            show2.setStatus(ShowStatus.PUBLISHED);
            show2.setCreatedAt(Instant.now());
            show2.setUpdatedAt(Instant.now());

            when(showRepository.findAllByOrderByShowDateTimeDesc())
                    .thenReturn(Arrays.asList(testShow, show2));

            List<ShowResponse> result = showService.getAllShows();

            assertEquals(2, result.size());
            assertEquals("Test Concert", result.get(0).getName());
            assertEquals("Jazz Evening", result.get(1).getName());
        }

        @Test
        @DisplayName("should return empty list when no shows exist")
        void getAllShows_emptyList() {
            when(showRepository.findAllByOrderByShowDateTimeDesc())
                    .thenReturn(Collections.emptyList());

            List<ShowResponse> result = showService.getAllShows();

            assertTrue(result.isEmpty());
        }
    }

    // ── Update Show Tests ──────────────────────────────────────────────

    @Nested
    @DisplayName("updateShow")
    class UpdateShowTests {

        @Test
        @DisplayName("should update show name and venue")
        void updateShow_partialUpdate() {
            when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
            when(showRepository.save(any(Show.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateShowRequest request = new UpdateShowRequest();
            request.setName("Updated Concert");
            request.setVenue("New Hall");

            ShowResponse response = showService.updateShow(1L, request);

            assertEquals("Updated Concert", response.getName());
            assertEquals("New Hall", response.getVenue());
            // Unchanged fields should remain
            assertEquals(testShow.getShowDateTime(), response.getShowDateTime());
        }

        @Test
        @DisplayName("should not update null fields")
        void updateShow_nullFieldsIgnored() {
            when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
            when(showRepository.save(any(Show.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateShowRequest request = new UpdateShowRequest();
            // All fields null — nothing should change

            ShowResponse response = showService.updateShow(1L, request);

            assertEquals("Test Concert", response.getName());
            assertEquals("Main Hall", response.getVenue());
        }

        @Test
        @DisplayName("should throw ShowNotFoundException when show not found")
        void updateShow_notFound() {
            when(showRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ShowNotFoundException.class,
                    () -> showService.updateShow(999L, new UpdateShowRequest()));
        }

        @Test
        @DisplayName("should throw InvalidShowStateException when updating cancelled show")
        void updateShow_cancelledShow() {
            testShow.setStatus(ShowStatus.CANCELLED);
            when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));

            assertThrows(InvalidShowStateException.class,
                    () -> showService.updateShow(1L, new UpdateShowRequest()));
        }
    }

    // ── Publish Show Tests ─────────────────────────────────────────────

    @Nested
    @DisplayName("publishShow")
    class PublishShowTests {

        @Test
        @DisplayName("should publish draft show")
        void publishShow_success() {
            testShow.setStatus(ShowStatus.DRAFT);
            when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
            when(showRepository.save(any(Show.class))).thenAnswer(inv -> inv.getArgument(0));

            ShowResponse response = showService.publishShow(1L);

            assertEquals(ShowStatus.PUBLISHED, response.getStatus());
        }

        @Test
        @DisplayName("should throw InvalidShowStateException when publishing non-draft")
        void publishShow_nonDraft() {
            testShow.setStatus(ShowStatus.PUBLISHED);
            when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));

            assertThrows(InvalidShowStateException.class,
                    () -> showService.publishShow(1L));
        }

        @Test
        @DisplayName("should throw ShowNotFoundException when show not found")
        void publishShow_notFound() {
            when(showRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ShowNotFoundException.class,
                    () -> showService.publishShow(999L));
        }
    }

    // ── Cancel Show Tests ──────────────────────────────────────────────

    @Nested
    @DisplayName("cancelShow")
    class CancelShowTests {

        @Test
        @DisplayName("should cancel a draft show")
        void cancelShow_draft() {
            testShow.setStatus(ShowStatus.DRAFT);
            when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
            when(showRepository.save(any(Show.class))).thenAnswer(inv -> inv.getArgument(0));

            showService.cancelShow(1L);

            verify(showRepository).save(showCaptor.capture());
            assertEquals(ShowStatus.CANCELLED, showCaptor.getValue().getStatus());
        }

        @Test
        @DisplayName("should cancel a published show")
        void cancelShow_published() {
            testShow.setStatus(ShowStatus.PUBLISHED);
            when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
            when(showRepository.save(any(Show.class))).thenAnswer(inv -> inv.getArgument(0));

            showService.cancelShow(1L);

            verify(showRepository).save(showCaptor.capture());
            assertEquals(ShowStatus.CANCELLED, showCaptor.getValue().getStatus());
        }

        @Test
        @DisplayName("should throw InvalidShowStateException when already cancelled")
        void cancelShow_alreadyCancelled() {
            testShow.setStatus(ShowStatus.CANCELLED);
            when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));

            assertThrows(InvalidShowStateException.class,
                    () -> showService.cancelShow(1L));
        }

        @Test
        @DisplayName("should throw ShowNotFoundException when show not found")
        void cancelShow_notFound() {
            when(showRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ShowNotFoundException.class,
                    () -> showService.cancelShow(999L));
        }
    }

    // ── Get Seats Tests ────────────────────────────────────────────────

    @Nested
    @DisplayName("getSeatsForShow")
    class GetSeatsForShowTests {

        @Test
        @DisplayName("should return seats for a show")
        void getSeatsForShow_returnsList() {
            when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));

            Seat seat2 = new Seat();
            seat2.setId(11L);
            seat2.setSeatNumber("A02");
            seat2.setShow(testShow);
            seat2.setCategory(SeatCategory.STANDARD);
            seat2.setPrice(BigDecimal.valueOf(99.99));

            when(seatRepository.findByShowIdOrderBySeatNumber(1L))
                    .thenReturn(Arrays.asList(testSeat, seat2));

            List<SeatResponse> result = showService.getSeatsForShow(1L);

            assertEquals(2, result.size());
            assertEquals("A01", result.get(0).getSeatNumber());
            assertEquals("A02", result.get(1).getSeatNumber());
            assertEquals(1L, result.get(0).getShowId());
        }

        @Test
        @DisplayName("should throw ShowNotFoundException when show not found")
        void getSeatsForShow_showNotFound() {
            when(showRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ShowNotFoundException.class,
                    () -> showService.getSeatsForShow(999L));
        }
    }

    @Nested
    @DisplayName("getSeatsByExternalShowId")
    class GetSeatsByExternalShowIdTests {

        @Test
        @DisplayName("should return seats by external show ID")
        void getSeatsByExternalShowId_found() {
            when(showRepository.findByExternalShowId("SHOW-ABC12345"))
                    .thenReturn(Optional.of(testShow));
            when(seatRepository.findByShowIdOrderBySeatNumber(1L))
                    .thenReturn(List.of(testSeat));

            List<SeatResponse> result = showService.getSeatsByExternalShowId("SHOW-ABC12345");

            assertEquals(1, result.size());
            assertEquals("A01", result.get(0).getSeatNumber());
        }

        @Test
        @DisplayName("should throw ShowNotFoundException when external ID not found")
        void getSeatsByExternalShowId_notFound() {
            when(showRepository.findByExternalShowId("SHOW-INVALID"))
                    .thenReturn(Optional.empty());

            assertThrows(ShowNotFoundException.class,
                    () -> showService.getSeatsByExternalShowId("SHOW-INVALID"));
        }
    }

    // ── Seat Generation Tests ──────────────────────────────────────────

    @Nested
    @DisplayName("generateSeats")
    class GenerateSeatsTests {

        @Test
        @DisplayName("should generate correct seat numbers for small show")
        void generateSeats_small() {
            List<Seat> seats = showService.generateSeats(testShow, 3);

            assertEquals(3, seats.size());
            assertEquals("A01", seats.get(0).getSeatNumber());
            assertEquals("A02", seats.get(1).getSeatNumber());
            assertEquals("A03", seats.get(2).getSeatNumber());
            seats.forEach(s -> {
                assertEquals(SeatCategory.STANDARD, s.getCategory());
                assertEquals(testShow, s.getShow());
                assertEquals(testShow.getBasePrice(), s.getPrice());
            });
        }

        @Test
        @DisplayName("should generate zero seats when totalSeats is 0")
        void generateSeats_zero() {
            List<Seat> seats = showService.generateSeats(testShow, 0);
            assertTrue(seats.isEmpty());
        }

        @Test
        @DisplayName("should handle exactly 20 seats (one full row)")
        void generateSeats_oneFullRow() {
            List<Seat> seats = showService.generateSeats(testShow, 20);

            assertEquals(20, seats.size());
            assertEquals("A01", seats.get(0).getSeatNumber());
            assertEquals("A20", seats.get(19).getSeatNumber());
        }

        @Test
        @DisplayName("should span multiple rows for 40+ seats")
        void generateSeats_multipleFullRows() {
            List<Seat> seats = showService.generateSeats(testShow, 41);

            assertEquals(41, seats.size());
            assertEquals("A01", seats.get(0).getSeatNumber());
            assertEquals("A20", seats.get(19).getSeatNumber());
            assertEquals("B01", seats.get(20).getSeatNumber());
            assertEquals("B20", seats.get(39).getSeatNumber());
            assertEquals("C01", seats.get(40).getSeatNumber());
        }
    }

    // ── Mapping Tests ──────────────────────────────────────────────────

    @Nested
    @DisplayName("DTO mapping")
    class MappingTests {

        @Test
        @DisplayName("toShowResponse should map all fields correctly")
        void toShowResponse_mapsAllFields() {
            ShowResponse response = showService.toShowResponse(testShow);

            assertEquals(testShow.getId(), response.getId());
            assertEquals(testShow.getExternalShowId(), response.getExternalShowId());
            assertEquals(testShow.getName(), response.getName());
            assertEquals(testShow.getVenue(), response.getVenue());
            assertEquals(testShow.getShowDateTime(), response.getShowDateTime());
            assertEquals(testShow.getTotalSeats(), response.getTotalSeats());
            assertEquals(testShow.getSeatsPerRow(), response.getSeatsPerRow());
            assertEquals(testShow.getBasePrice(), response.getBasePrice());
            assertEquals(testShow.getGenre(), response.getGenre());
            assertEquals(testShow.getLanguage(), response.getLanguage());
            assertEquals(testShow.getDuration(), response.getDuration());
            assertEquals(testShow.getRating(), response.getRating());
            assertEquals(testShow.getPosterUrl(), response.getPosterUrl());
            assertEquals(testShow.getStatus(), response.getStatus());
            assertEquals(testShow.getCreatedAt(), response.getCreatedAt());
            assertEquals(testShow.getUpdatedAt(), response.getUpdatedAt());
        }

        @Test
        @DisplayName("toSeatResponse should map all fields correctly")
        void toSeatResponse_mapsAllFields() {
            SeatResponse response = showService.toSeatResponse(testSeat);

            assertEquals(testSeat.getId(), response.getId());
            assertEquals(testSeat.getSeatNumber(), response.getSeatNumber());
            assertEquals(testShow.getId(), response.getShowId());
            assertEquals(testSeat.getCategory(), response.getCategory());
            assertEquals(testSeat.getPrice(), response.getPrice());
        }
    }
}
