/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Integration tests for the Admin Show API with a full Spring Boot context.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.bookxshow.admin.config.TestSecurityConfig;
import com.bookxshow.admin.dto.CreateShowRequest;
import com.bookxshow.admin.dto.SeatResponse;
import com.bookxshow.admin.dto.ShowResponse;
import com.bookxshow.admin.dto.UpdateShowRequest;
import com.bookxshow.admin.entity.Show;
import com.bookxshow.admin.enums.ShowStatus;
import com.bookxshow.admin.repository.SeatRepository;
import com.bookxshow.admin.repository.ShowRepository;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that boot the full Spring context with a random port,
 * using H2 in-memory database.
 *
 * <p>Verifies the full admin show lifecycle:
 * <ul>
 *   <li>IT01 — Create a show with auto-generated seats</li>
 *   <li>IT02 — List all shows</li>
 *   <li>IT03 — Get show by ID</li>
 *   <li>IT04 — Update show metadata</li>
 *   <li>IT05 — Publish a draft show</li>
 *   <li>IT06 — Cancel a show</li>
 *   <li>IT07 — Service-to-service read endpoints</li>
 *   <li>IT08 — Get seats for a show</li>
 *   <li>IT09 — Create show validation failure</li>
 *   <li>IT10 — Publish non-draft returns 409</li>
 * </ul>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
@Import(TestSecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminShowIntegrationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeatRepository seatRepository;

    private String adminUrl;
    private String showsUrl;

    @BeforeEach
    void setUp() {
        adminUrl = "http://localhost:" + port + "/bookxshow/v1/admin/shows";
        showsUrl = "http://localhost:" + port + "/bookxshow/v1/shows";

        // Clean database
        seatRepository.deleteAll();
        showRepository.deleteAll();
    }

    // ── IT01 ─────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("IT01 - Create a show with auto-generated seats")
    void createShow_happyPath() {
        CreateShowRequest request = new CreateShowRequest(
                "Integration Test Show", "IT Hall",
                LocalDateTime.of(2026, 12, 25, 19, 30),
                10, 10, BigDecimal.valueOf(49.99),
                "Rock", "English", "2h", 4.5, null);

        ResponseEntity<ShowResponse> resp = rest.postForEntity(
                adminUrl, request, ShowResponse.class);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Integration Test Show", resp.getBody().getName());
        assertEquals("IT Hall", resp.getBody().getVenue());
        assertEquals(10, resp.getBody().getTotalSeats());
        assertEquals(ShowStatus.DRAFT, resp.getBody().getStatus());
        assertNotNull(resp.getBody().getExternalShowId());
        assertTrue(resp.getBody().getExternalShowId().startsWith("SHOW-"));

        // Verify seats were created in DB
        long seatCount = seatRepository.countByShowId(resp.getBody().getId());
        assertEquals(10, seatCount);
    }

    // ── IT02 ─────────────────────────────────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("IT02 - List all shows")
    void getAllShows() {
        // Create two shows
        createTestShow("Show A", 5);
        createTestShow("Show B", 3);

        ResponseEntity<ShowResponse[]> resp = rest.getForEntity(
                adminUrl, ShowResponse[].class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().length);
    }

    // ── IT03 ─────────────────────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("IT03 - Get show by ID")
    void getShowById() {
        ShowResponse created = createTestShow("Get By ID Show", 5);

        ResponseEntity<ShowResponse> resp = rest.getForEntity(
                adminUrl + "/" + created.getId(), ShowResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Get By ID Show", resp.getBody().getName());
    }

    // ── IT04 ─────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("IT04 - Update show metadata")
    void updateShow() {
        ShowResponse created = createTestShow("Original Name", 5);

        UpdateShowRequest update = new UpdateShowRequest();
        update.setName("Updated Name");
        update.setVenue("New Venue");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateShowRequest> entity = new HttpEntity<>(update, headers);

        ResponseEntity<ShowResponse> resp = rest.exchange(
                adminUrl + "/" + created.getId(),
                HttpMethod.PUT, entity, ShowResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Updated Name", resp.getBody().getName());
        assertEquals("New Venue", resp.getBody().getVenue());
    }

    // ── IT05 ─────────────────────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("IT05 - Publish a draft show")
    void publishShow() {
        ShowResponse created = createTestShow("Publishable Show", 5);

        ResponseEntity<ShowResponse> resp = rest.postForEntity(
                adminUrl + "/" + created.getId() + "/publish",
                null, ShowResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(ShowStatus.PUBLISHED, resp.getBody().getStatus());
    }

    // ── IT06 ─────────────────────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("IT06 - Cancel a show")
    void cancelShow() {
        ShowResponse created = createTestShow("Cancellable Show", 3);

        ResponseEntity<Void> resp = rest.exchange(
                adminUrl + "/" + created.getId(),
                HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());

        // Verify status in DB
        Show cancelled = showRepository.findById(created.getId()).orElseThrow();
        assertEquals(ShowStatus.CANCELLED, cancelled.getStatus());
    }

    // ── IT07 ─────────────────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("IT07 - Service-to-service: get show by external ID")
    void s2s_getShowByExternalId() {
        ShowResponse created = createTestShow("S2S Show", 5);

        ResponseEntity<ShowResponse> resp = rest.getForEntity(
                showsUrl + "/" + created.getExternalShowId(), ShowResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(created.getExternalShowId(), resp.getBody().getExternalShowId());
        assertEquals("S2S Show", resp.getBody().getName());
    }

    // ── IT08 ─────────────────────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("IT08 - Get seats for a show via admin and S2S endpoints")
    void getSeatsForShow() {
        ShowResponse created = createTestShow("Seats Show", 5);

        // Via admin endpoint
        ResponseEntity<SeatResponse[]> adminResp = rest.getForEntity(
                adminUrl + "/" + created.getId() + "/seats", SeatResponse[].class);

        assertEquals(HttpStatus.OK, adminResp.getStatusCode());
        assertNotNull(adminResp.getBody());
        assertEquals(5, adminResp.getBody().length);
        assertEquals("A01", adminResp.getBody()[0].getSeatNumber());
        assertEquals("A05", adminResp.getBody()[4].getSeatNumber());

        // Via S2S endpoint
        ResponseEntity<SeatResponse[]> s2sResp = rest.getForEntity(
                showsUrl + "/" + created.getExternalShowId() + "/seats", SeatResponse[].class);

        assertEquals(HttpStatus.OK, s2sResp.getStatusCode());
        assertNotNull(s2sResp.getBody());
        assertEquals(5, s2sResp.getBody().length);
    }

    // ── IT09 ─────────────────────────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("IT09 - Create show with invalid data returns 400")
    void createShow_validationFailure() {
        CreateShowRequest request = new CreateShowRequest(
                "", "", null, 0, null, null,
                null, null, null, null, null);

        ResponseEntity<Map> resp = rest.postForEntity(
                adminUrl, request, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── IT10 ─────────────────────────────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("IT10 - Publish already published show returns 409")
    void publishAlreadyPublished_returns409() {
        ShowResponse created = createTestShow("Already Published", 3);

        // First publish — succeeds
        rest.postForEntity(
                adminUrl + "/" + created.getId() + "/publish",
                null, ShowResponse.class);

        // Second publish — should fail
        ResponseEntity<Map> resp = rest.postForEntity(
                adminUrl + "/" + created.getId() + "/publish",
                null, Map.class);

        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
    }

    // ── IT11 ─────────────────────────────────────────────────────────────

    @Test
    @Order(11)
    @DisplayName("IT11 - Get non-existent show returns 404")
    void getNonExistentShow_returns404() {
        ResponseEntity<Map> resp = rest.getForEntity(
                adminUrl + "/99999", Map.class);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    // ── IT12 ─────────────────────────────────────────────────────────────

    @Test
    @Order(12)
    @DisplayName("IT12 - S2S: non-existent external ID returns 404")
    void s2s_nonExistentExternalId_returns404() {
        ResponseEntity<Map> resp = rest.getForEntity(
                showsUrl + "/SHOW-NONEXISTENT", Map.class);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    // ── IT13 ─────────────────────────────────────────────────────────────

    @Test
    @Order(13)
    @DisplayName("IT13 - Cancel already cancelled show returns 409")
    void cancelAlreadyCancelled_returns409() {
        ShowResponse created = createTestShow("Double Cancel", 2);

        // First cancel
        rest.exchange(adminUrl + "/" + created.getId(),
                HttpMethod.DELETE, null, Void.class);

        // Second cancel — should fail
        ResponseEntity<Map> resp = rest.exchange(
                adminUrl + "/" + created.getId(),
                HttpMethod.DELETE, null, Map.class);

        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
    }

    // ── IT14 ─────────────────────────────────────────────────────────────

    @Test
    @Order(14)
    @DisplayName("IT14 - Large show creates seats across multiple rows")
    void createLargeShow_multipleRows() {
        ShowResponse created = createTestShow("Large Show", 25);

        ResponseEntity<SeatResponse[]> resp = rest.getForEntity(
                adminUrl + "/" + created.getId() + "/seats", SeatResponse[].class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(25, resp.getBody().length);
        // First row ends at A20
        assertEquals("A20", resp.getBody()[19].getSeatNumber());
        // Second row starts at B01
        assertEquals("B01", resp.getBody()[20].getSeatNumber());
    }

    // ── Helper ───────────────────────────────────────────────────────────

    private ShowResponse createTestShow(String name, int totalSeats) {
        CreateShowRequest request = new CreateShowRequest(
                name, "Test Venue",
                LocalDateTime.of(2026, 12, 25, 19, 30),
                totalSeats, 20, BigDecimal.valueOf(50.00),
                null, null, null, null, null);

        ResponseEntity<ShowResponse> resp = rest.postForEntity(
                adminUrl, request, ShowResponse.class);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        return resp.getBody();
    }
}
