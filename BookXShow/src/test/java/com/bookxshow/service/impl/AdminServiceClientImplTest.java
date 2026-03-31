/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Unit tests for AdminServiceClientImpl.
 *
 * @since 1.0.0
 */
package com.bookxshow.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.bookxshow.config.BookingProperties;
import com.bookxshow.config.OAuth2TokenService;
import com.bookxshow.dto.AdminSeatDto;
import com.bookxshow.dto.ShowDto;
import com.bookxshow.entity.Seat;
import com.bookxshow.entity.Show;
import com.bookxshow.exception.ShowNotFoundException;
import com.bookxshow.repository.SeatRepository;
import com.bookxshow.repository.ShowRepository;

import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AdminServiceClientImpl}.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceClientImplTest {

    @Mock
    private WebClient webClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private OAuth2TokenService oAuth2TokenService;

    private BookingProperties properties;
    private AdminServiceClientImpl adminServiceClient;

    @BeforeEach
    void setUp() {
        properties = new BookingProperties();
        BookingProperties.AdminService adminService = new BookingProperties.AdminService();
        adminService.setBaseUrl("http://localhost:8081");
        adminService.setShowsUri("/bookxshow/v1/shows");
        properties.setAdminService(adminService);

        adminServiceClient = new AdminServiceClientImpl(
                webClient, properties, showRepository, seatRepository, oAuth2TokenService);

        lenient().when(oAuth2TokenService.getAccessToken()).thenReturn("test-bearer-token");
    }

    @SuppressWarnings("unchecked")
    private <T> void stubGetChain(Mono<T> responseMono, Class<T> clazz) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(clazz)).thenReturn(responseMono);
    }

    @SuppressWarnings("unchecked")
    private void stubGetChainForArray(Mono<AdminSeatDto[]> responseMono) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AdminSeatDto[].class)).thenReturn(responseMono);
    }

    // ── fetchShowDetails ────────────────────────────────────────────────

    @Test
    @DisplayName("T01 - fetchShowDetails returns show DTO on success")
    void fetchShowDetails_success() {
        ShowDto expected = new ShowDto(null, "SHOW-001", "Concert", "Hall A", null, 100,
                20, null, null, null, null, null, null);
        stubGetChain(Mono.just(expected), ShowDto.class);

        ShowDto result = adminServiceClient.fetchShowDetails("SHOW-001");

        assertNotNull(result);
        assertEquals("Concert", result.getName());
    }

    @Test
    @DisplayName("T02 - fetchShowDetails throws when body is null")
    void fetchShowDetails_nullBody_throwsShowNotFound() {
        stubGetChain(Mono.justOrEmpty(null), ShowDto.class);

        assertThrows(ShowNotFoundException.class,
                () -> adminServiceClient.fetchShowDetails("SHOW-999"));
    }

    @Test
    @DisplayName("T03 - fetchShowDetails throws on WebClient error")
    void fetchShowDetails_httpError_throwsShowNotFound() {
        stubGetChain(Mono.error(new WebClientResponseException(
                404, "Not Found", null, null, null)), ShowDto.class);

        assertThrows(ShowNotFoundException.class,
                () -> adminServiceClient.fetchShowDetails("SHOW-404"));
    }

    // ── fetchSeatsForShow ───────────────────────────────────────────────

    @Test
    @DisplayName("T04 - fetchSeatsForShow returns seat list on success")
    void fetchSeatsForShow_success() {
        AdminSeatDto[] seats = {
                new AdminSeatDto(1L, "A01", 1L, "STANDARD", null),
                new AdminSeatDto(2L, "A02", 1L, "STANDARD", null)
        };
        stubGetChainForArray(Mono.just(seats));

        List<AdminSeatDto> result = adminServiceClient.fetchSeatsForShow("SHOW-001");

        assertEquals(2, result.size());
        assertEquals("A01", result.get(0).getSeatNumber());
    }

    @Test
    @DisplayName("T05 - fetchSeatsForShow returns empty list when body is null")
    void fetchSeatsForShow_nullBody_returnsEmptyList() {
        stubGetChainForArray(Mono.justOrEmpty(null));

        List<AdminSeatDto> result = adminServiceClient.fetchSeatsForShow("SHOW-001");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("T06 - fetchSeatsForShow returns empty list on HTTP error")
    void fetchSeatsForShow_httpError_returnsEmptyList() {
        stubGetChainForArray(Mono.error(new WebClientResponseException(
                500, "Internal Server Error", null, null, null)));

        List<AdminSeatDto> result = adminServiceClient.fetchSeatsForShow("SHOW-001");

        assertTrue(result.isEmpty());
    }

    // ── syncShow ────────────────────────────────────────────────────────

    @Test
    @DisplayName("T07 - syncShow creates new show and seats")
    @SuppressWarnings("unchecked")
    void syncShow_createsNewShowAndSeats() {
        ShowDto showDto = new ShowDto(null, "SHOW-NEW", "New Show", "Venue", null, 2,
                20, null, null, null, null, null, null);
        AdminSeatDto[] seatDtos = {
                new AdminSeatDto(null, "A01", null, "STANDARD", null),
                new AdminSeatDto(null, "A02", null, "STANDARD", null)
        };

        // First call: fetchShowDetails
        // Second call: fetchSeatsForShow
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ShowDto.class)).thenReturn(Mono.just(showDto));
        when(responseSpec.bodyToMono(AdminSeatDto[].class)).thenReturn(Mono.just(seatDtos));

        when(showRepository.findByExternalShowId("SHOW-NEW")).thenReturn(Optional.empty());
        Show savedShow = new Show();
        savedShow.setId(1L);
        savedShow.setExternalShowId("SHOW-NEW");
        when(showRepository.save(any(Show.class))).thenReturn(savedShow);
        when(seatRepository.findBySeatNumberAndShowId(anyString(), anyLong()))
                .thenReturn(Optional.empty());

        adminServiceClient.syncShow("SHOW-NEW");

        verify(showRepository).save(any(Show.class));
        verify(seatRepository, times(2)).save(any(Seat.class));
    }

    @Test
    @DisplayName("T08 - syncShow updates existing show")
    @SuppressWarnings("unchecked")
    void syncShow_updatesExistingShow() {
        ShowDto showDto = new ShowDto(null, "SHOW-EXIST", "Updated Show", "Venue2", null, 1,
                20, null, null, null, null, null, null);
        AdminSeatDto[] seatDtos = {
                new AdminSeatDto(null, "A01", null, "STANDARD", null)
        };

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ShowDto.class)).thenReturn(Mono.just(showDto));
        when(responseSpec.bodyToMono(AdminSeatDto[].class)).thenReturn(Mono.just(seatDtos));

        Show existingShow = new Show();
        existingShow.setId(5L);
        existingShow.setExternalShowId("SHOW-EXIST");
        when(showRepository.findByExternalShowId("SHOW-EXIST"))
                .thenReturn(Optional.of(existingShow));
        when(showRepository.save(any(Show.class))).thenReturn(existingShow);

        // Seat already exists
        Seat existingSeat = new Seat();
        existingSeat.setId(10L);
        when(seatRepository.findBySeatNumberAndShowId("A01", 5L))
                .thenReturn(Optional.of(existingSeat));

        adminServiceClient.syncShow("SHOW-EXIST");

        verify(showRepository).save(any(Show.class));
        // Existing seat should NOT be saved again
        verify(seatRepository, never()).save(any(Seat.class));
    }

    // ── sanitizePath (via fetchShowDetails) ─────────────────────────────

    @Test
    @DisplayName("T09 - Blank path segment throws IllegalArgumentException")
    void blankPathSegment_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> adminServiceClient.fetchShowDetails(""));
    }

    @Test
    @DisplayName("T10 - Null path segment throws IllegalArgumentException")
    void nullPathSegment_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> adminServiceClient.fetchShowDetails(null));
    }

    @Test
    @DisplayName("T11 - Dots-only path segment throws IllegalArgumentException")
    void dotsOnlyPath_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> adminServiceClient.fetchShowDetails(".."));
    }
}
