/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Unit tests for ShowReadController using MockMvc.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.bookxshow.admin.config.TestSecurityConfig;
import com.bookxshow.admin.dto.SeatResponse;
import com.bookxshow.admin.dto.ShowResponse;
import com.bookxshow.admin.enums.SeatCategory;
import com.bookxshow.admin.enums.ShowStatus;
import com.bookxshow.admin.exception.ShowNotFoundException;
import com.bookxshow.admin.service.ShowService;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvcTest for {@link ShowReadController}. Tests the service-to-service
 * read endpoints used by BookXShow.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@WebMvcTest(ShowReadController.class)
@Import(TestSecurityConfig.class)
class ShowReadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShowService showService;

    private static final String BASE_URL = "/bookxshow/v1/shows";

    // ── GET /bookxshow/v1/shows/{externalShowId} ─────────────────────────────

    @Test
    @DisplayName("GET /shows/{extId} - should return show details")
    void getShowByExternalId_returns200() throws Exception {
        ShowResponse response = ShowResponse.builder()
                .id(1L)
                .externalShowId("SHOW-TEST1234")
                .name("Rock Concert")
                .venue("Arena")
                .showDateTime(LocalDateTime.of(2026, 12, 25, 19, 30))
                .totalSeats(100)
                .seatsPerRow(20)
                .basePrice(BigDecimal.valueOf(75.00))
                .status(ShowStatus.PUBLISHED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(showService.getShowByExternalId("SHOW-TEST1234")).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/SHOW-TEST1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalShowId").value("SHOW-TEST1234"))
                .andExpect(jsonPath("$.name").value("Rock Concert"))
                .andExpect(jsonPath("$.totalSeats").value(100));
    }

    @Test
    @DisplayName("GET /shows/{extId} - not found returns 404")
    void getShowByExternalId_notFound_returns404() throws Exception {
        when(showService.getShowByExternalId("SHOW-INVALID"))
                .thenThrow(new ShowNotFoundException("Show not found with external ID: SHOW-INVALID"));

        mockMvc.perform(get(BASE_URL + "/SHOW-INVALID"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("SHOW-INVALID")));
    }

    // ── GET /bookxshow/v1/shows/{externalShowId}/seats ───────────────────────

    @Test
    @DisplayName("GET /shows/{extId}/seats - should return seat list")
    void getSeatsByExternalShowId_returns200() throws Exception {
        List<SeatResponse> seats = Arrays.asList(
                SeatResponse.builder().id(1L).seatNumber("A01").showId(1L)
                        .category(SeatCategory.STANDARD).price(BigDecimal.valueOf(75.00)).build(),
                SeatResponse.builder().id(2L).seatNumber("A02").showId(1L)
                        .category(SeatCategory.PREMIUM).price(BigDecimal.valueOf(100.00)).build()
        );

        when(showService.getSeatsByExternalShowId("SHOW-TEST1234")).thenReturn(seats);

        mockMvc.perform(get(BASE_URL + "/SHOW-TEST1234/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].seatNumber").value("A01"))
                .andExpect(jsonPath("$[1].seatNumber").value("A02"))
                .andExpect(jsonPath("$[1].category").value("PREMIUM"));
    }

    @Test
    @DisplayName("GET /shows/{extId}/seats - show not found returns 404")
    void getSeatsByExternalShowId_notFound_returns404() throws Exception {
        when(showService.getSeatsByExternalShowId("SHOW-INVALID"))
                .thenThrow(new ShowNotFoundException("Show not found with external ID: SHOW-INVALID"));

        mockMvc.perform(get(BASE_URL + "/SHOW-INVALID/seats"))
                .andExpect(status().isNotFound());
    }
}
