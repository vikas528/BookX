/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * REST-based implementation of AdminServiceClient that fetches show and
 * seat configuration from the external Admin Service using WebClient.
 *
 * @since 1.0.0
 */
package com.bookxshow.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.bookxshow.config.BookingProperties;
import com.bookxshow.dto.AdminSeatDto;
import com.bookxshow.dto.ShowDto;
import com.bookxshow.entity.Seat;
import com.bookxshow.entity.Show;
import com.bookxshow.enums.SeatStatus;
import com.bookxshow.exception.ShowNotFoundException;
import com.bookxshow.repository.SeatRepository;
import com.bookxshow.repository.ShowRepository;
import com.bookxshow.config.OAuth2TokenService;
import com.bookxshow.service.AdminServiceClient;

/**
 * Fetches show and seat data from the external Admin Service over REST
 * and synchronizes it into the local database.
 *
 * <p>The Admin Service base URL and URI paths are configured via
 * {@code bookxshow.admin-service.*} properties. Authentication uses
 * an OAuth2 bearer token obtained via {@link OAuth2TokenService}.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class AdminServiceClientImpl implements AdminServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceClientImpl.class);

    private final WebClient webClient;
    private final BookingProperties properties;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final OAuth2TokenService oAuth2TokenService;

    /**
     * Constructs the client with required dependencies.
     *
     * @param webClient          shared HTTP client
     * @param properties         application configuration
     * @param showRepository     show persistence
     * @param seatRepository     seat persistence
     * @param oAuth2TokenService OAuth2 client-credentials token provider
     */
    public AdminServiceClientImpl(WebClient webClient,
                                  BookingProperties properties,
                                  ShowRepository showRepository,
                                  SeatRepository seatRepository,
                                  OAuth2TokenService oAuth2TokenService) {
        this.webClient = webClient;
        this.properties = properties;
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
        this.oAuth2TokenService = oAuth2TokenService;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends a GET request to
     * {@code {base-url}{shows-uri}/{externalShowId}}.</p>
     */
    @Override
    public ShowDto fetchShowDetails(String externalShowId) {
        String baseUrl = properties.getAdminService().getBaseUrl();
        String showsUri = properties.getAdminService().getShowsUri();
        String url = baseUrl + showsUri + "/" + sanitizePath(externalShowId);

        try {
            ShowDto body = webClient.get()
                    .uri(url)
                    .headers(h -> h.setBearerAuth(oAuth2TokenService.getAccessToken()))
                    .retrieve()
                    .bodyToMono(ShowDto.class)
                    .block();

            if (body == null) {
                throw new ShowNotFoundException(externalShowId);
            }
            return body;
        } catch (WebClientResponseException ex) {
            log.error("Failed to fetch show {} from Admin Service: {}",
                    externalShowId, ex.getMessage());
            throw new ShowNotFoundException(externalShowId);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends a GET request to
     * {@code {base-url}{shows-uri}/{externalShowId}/seats}.</p>
     */
    @Override
    public List<AdminSeatDto> fetchSeatsForShow(String externalShowId) {
        String baseUrl = properties.getAdminService().getBaseUrl();
        String showsUri = properties.getAdminService().getShowsUri();
        String url = baseUrl + showsUri + "/" + sanitizePath(externalShowId) + "/seats";

        try {
            AdminSeatDto[] body = webClient.get()
                    .uri(url)
                    .headers(h -> h.setBearerAuth(oAuth2TokenService.getAccessToken()))
                    .retrieve()
                    .bodyToMono(AdminSeatDto[].class)
                    .block();

            if (body == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(body);
        } catch (WebClientResponseException ex) {
            log.error("Failed to fetch seats for show {} from Admin Service: {}",
                    externalShowId, ex.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Fetches the show details and seat layout from the Admin Service
     * and upserts them into the local database within a single transaction.</p>
     */
    @Override
    @Transactional
    public void syncShow(String externalShowId) {
        ShowDto showDto = fetchShowDetails(externalShowId);
        List<AdminSeatDto> seatDtos = fetchSeatsForShow(externalShowId);

        // Upsert show
        Show show = showRepository.findByExternalShowId(externalShowId)
                .orElseGet(() -> {
                    Show newShow = new Show();
                    newShow.setExternalShowId(externalShowId);
                    return newShow;
                });

        show.setName(showDto.getName());
        show.setVenue(showDto.getVenue());
        show.setShowDateTime(showDto.getShowDateTime());
        show.setTotalSeats(showDto.getTotalSeats());
        show.setSeatsPerRow(showDto.getSeatsPerRow());
        show.setBasePrice(showDto.getBasePrice());
        show.setGenre(showDto.getGenre());
        show.setLanguage(showDto.getLanguage());
        show.setDuration(showDto.getDuration());
        show.setRating(showDto.getRating());
        show.setPosterUrl(showDto.getPosterUrl());
        Show savedShow = showRepository.save(show);

        // Upsert seats
        for (AdminSeatDto seatDto : seatDtos) {
            boolean exists = seatRepository
                    .findBySeatNumberAndShowId(seatDto.getSeatNumber(), savedShow.getId())
                    .isPresent();
            if (!exists) {
                Seat seat = new Seat();
                seat.setSeatNumber(seatDto.getSeatNumber());
                seat.setShow(savedShow);
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(seat);
            }
        }

        log.info("Synced show {} with {} seats", externalShowId, seatDtos.size());
    }

    /**
     * Sanitises a path segment to prevent path-traversal attacks.
     * Strips slashes, dots-only segments, and encodes unsafe chars.
     *
     * @param segment the raw path segment
     * @return a safe, URL-encoded path segment
     */
    private String sanitizePath(String segment) {
        if (segment == null || segment.isBlank()) {
            throw new IllegalArgumentException("Path segment must not be blank");
        }
        // Strip path separators and pure-dots segments
        String clean = segment.replaceAll("[/\\\\]", "");
        if (clean.matches("^\\.+$")) {
            throw new IllegalArgumentException("Invalid path segment: " + segment);
        }
        return java.net.URLEncoder.encode(clean, java.nio.charset.StandardCharsets.UTF_8);
    }
}
