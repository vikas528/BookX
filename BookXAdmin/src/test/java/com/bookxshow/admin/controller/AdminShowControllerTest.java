/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Unit tests for AdminShowController using MockMvc.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bookxshow.admin.config.TestSecurityConfig;
import com.bookxshow.admin.dto.SeatResponse;
import com.bookxshow.admin.dto.ShowResponse;
import com.bookxshow.admin.enums.SeatCategory;
import com.bookxshow.admin.enums.ShowStatus;
import com.bookxshow.admin.exception.InvalidShowStateException;
import com.bookxshow.admin.exception.ShowNotFoundException;
import com.bookxshow.admin.service.ShowService;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvcTest for {@link AdminShowController}. Tests HTTP request/response
 * mapping, validation, and status codes.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@WebMvcTest(AdminShowController.class)
@Import(TestSecurityConfig.class)
class AdminShowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShowService showService;

    private static final String BASE_URL = "/bookxshow/v1/admin/shows";

    private ShowResponse buildShowResponse(Long id, String name, ShowStatus status) {
        return ShowResponse.builder()
                .id(id)
                .externalShowId("SHOW-TEST" + id)
                .name(name)
                .venue("Test Venue")
                .showDateTime(LocalDateTime.of(2026, 12, 25, 19, 30))
                .totalSeats(10)
                .seatsPerRow(10)
                .basePrice(BigDecimal.valueOf(50.00))
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ── POST /bookxshow/v1/admin/shows ───────────────────────────────────────

    @Test
    @DisplayName("POST /shows - should create show and return 201")
    void createShow_returns201() throws Exception {
        ShowResponse response = buildShowResponse(1L, "New Show", ShowStatus.DRAFT);
        when(showService.createShow(any())).thenReturn(response);

        String body = """
                {
                    "name": "New Show",
                    "venue": "Concert Hall",
                    "showDateTime": "2026-12-25T19:30:00",
                    "totalSeats": 10,
                    "seatsPerRow": 10,
                    "basePrice": 50.00
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Show"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.externalShowId").value(org.hamcrest.Matchers.startsWith("SHOW-")));
    }

    @Test
    @DisplayName("POST /shows - validation failure returns 400")
    void createShow_validationFails_returns400() throws Exception {
        String body = """
                {
                    "name": "",
                    "venue": "",
                    "showDateTime": null,
                    "totalSeats": 0
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /shows - missing required fields returns 400")
    void createShow_missingFields_returns400() throws Exception {
        String body = "{}";

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── GET /bookxshow/v1/admin/shows ────────────────────────────────────────

    @Test
    @DisplayName("GET /shows - should return all shows")
    void getAllShows_returns200() throws Exception {
        List<ShowResponse> shows = Arrays.asList(
                buildShowResponse(1L, "Show A", ShowStatus.PUBLISHED),
                buildShowResponse(2L, "Show B", ShowStatus.DRAFT)
        );
        when(showService.getAllShows()).thenReturn(shows);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Show A"))
                .andExpect(jsonPath("$[1].name").value("Show B"));
    }

    @Test
    @DisplayName("GET /shows - should return empty list when no shows")
    void getAllShows_empty_returns200() throws Exception {
        when(showService.getAllShows()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /bookxshow/v1/admin/shows/{showId} ───────────────────────────────

    @Test
    @DisplayName("GET /shows/{id} - should return show details")
    void getShowById_returns200() throws Exception {
        ShowResponse response = buildShowResponse(1L, "Found Show", ShowStatus.PUBLISHED);
        when(showService.getShowById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Found Show"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /shows/{id} - not found returns 404")
    void getShowById_notFound_returns404() throws Exception {
        when(showService.getShowById(999L))
                .thenThrow(new ShowNotFoundException(999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /bookxshow/v1/admin/shows/{showId} ───────────────────────────────

    @Test
    @DisplayName("PUT /shows/{id} - should update show")
    void updateShow_returns200() throws Exception {
        ShowResponse updated = buildShowResponse(1L, "Updated Name", ShowStatus.DRAFT);
        when(showService.updateShow(eq(1L), any())).thenReturn(updated);

        String body = """
                {
                    "name": "Updated Name"
                }
                """;

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @DisplayName("PUT /shows/{id} - cancelled show returns 409")
    void updateShow_cancelled_returns409() throws Exception {
        when(showService.updateShow(eq(1L), any()))
                .thenThrow(new InvalidShowStateException("Invalid show state transition from: CANCELLED"));

        String body = """
                {
                    "name": "Won't Work"
                }
                """;

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    // ── POST /bookxshow/v1/admin/shows/{showId}/publish ──────────────────────

    @Test
    @DisplayName("POST /shows/{id}/publish - should publish show")
    void publishShow_returns200() throws Exception {
        ShowResponse published = buildShowResponse(1L, "Published Show", ShowStatus.PUBLISHED);
        when(showService.publishShow(1L)).thenReturn(published);

        mockMvc.perform(post(BASE_URL + "/1/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("POST /shows/{id}/publish - non-draft returns 409")
    void publishShow_nonDraft_returns409() throws Exception {
        when(showService.publishShow(1L))
                .thenThrow(new InvalidShowStateException("Invalid show state"));

        mockMvc.perform(post(BASE_URL + "/1/publish"))
                .andExpect(status().isConflict());
    }

    // ── DELETE /bookxshow/v1/admin/shows/{showId} ────────────────────────────

    @Test
    @DisplayName("DELETE /shows/{id} - should cancel show and return 204")
    void cancelShow_returns204() throws Exception {
        doNothing().when(showService).cancelShow(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /shows/{id} - not found returns 404")
    void cancelShow_notFound_returns404() throws Exception {
        doThrow(new ShowNotFoundException(999L)).when(showService).cancelShow(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    // ── GET /bookxshow/v1/admin/shows/{showId}/seats ─────────────────────────

    @Test
    @DisplayName("GET /shows/{id}/seats - should return seat list")
    void getSeatsForShow_returns200() throws Exception {
        List<SeatResponse> seats = Arrays.asList(
                SeatResponse.builder().id(1L).seatNumber("A01").showId(1L)
                        .category(SeatCategory.STANDARD).price(BigDecimal.valueOf(50.00)).build(),
                SeatResponse.builder().id(2L).seatNumber("A02").showId(1L)
                        .category(SeatCategory.STANDARD).price(BigDecimal.valueOf(50.00)).build()
        );
        when(showService.getSeatsForShow(1L)).thenReturn(seats);

        mockMvc.perform(get(BASE_URL + "/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].seatNumber").value("A01"))
                .andExpect(jsonPath("$[1].seatNumber").value("A02"));
    }
}
