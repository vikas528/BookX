/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Service implementation for managing shows and seats.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookxshow.admin.common.Constants;
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
import com.bookxshow.admin.service.ShowService;

/**
 * Manages the lifecycle of shows and their seats in the admin catalogue.
 *
 * <p>Shows are created in {@link ShowStatus#DRAFT} state. When published,
 * the show becomes visible to downstream services (e.g. BookXShow).
 * Seats are auto-generated from the {@code totalSeats} value during
 * show creation.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class ShowServiceImpl implements ShowService {

    private static final Logger log = LoggerFactory.getLogger(ShowServiceImpl.class);

    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;

    public ShowServiceImpl(ShowRepository showRepository, SeatRepository seatRepository) {
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    @Transactional
    public ShowResponse createShow(CreateShowRequest request) {
        log.info("Creating show: {}", request.getName());

        Show show = new Show();
        show.setName(request.getName());
        show.setVenue(request.getVenue());
        show.setShowDateTime(request.getShowDateTime());
        show.setTotalSeats(request.getTotalSeats());
        show.setSeatsPerRow(request.getSeatsPerRow());
        show.setBasePrice(request.getBasePrice());
        show.setGenre(request.getGenre());
        show.setLanguage(request.getLanguage());
        show.setDuration(request.getDuration());
        show.setRating(request.getRating());
        show.setPosterUrl(request.getPosterUrl());
        show.setStatus(ShowStatus.DRAFT);

        Show savedShow = showRepository.save(show);

        // Auto-generate seats
        List<Seat> seats = generateSeats(savedShow, request.getTotalSeats());
        seatRepository.saveAll(seats);

        log.info("Created show {} with {} seats (externalId={})",
                savedShow.getId(), seats.size(), savedShow.getExternalShowId());

        return toShowResponse(savedShow);
    }

    @Override
    public List<ShowResponse> getAllShows() {
        return showRepository.findAllByOrderByShowDateTimeDesc().stream()
                .map(this::toShowResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ShowResponse getShowById(Long showId) {
        Show show = findShowOrThrow(showId);
        return toShowResponse(show);
    }

    @Override
    public ShowResponse getShowByExternalId(String externalShowId) {
        Show show = showRepository.findByExternalShowId(externalShowId)
                .orElseThrow(() -> new ShowNotFoundException(
                        Constants.ERR_SHOW_NOT_FOUND_EXT + externalShowId));
        return toShowResponse(show);
    }

    @Override
    @Transactional
    public ShowResponse updateShow(Long showId, UpdateShowRequest request) {
        Show show = findShowOrThrow(showId);

        if (show.getStatus() == ShowStatus.CANCELLED) {
            throw new InvalidShowStateException(
                    Constants.ERR_INVALID_SHOW_STATE + show.getStatus());
        }

        if (request.getName() != null) {
            show.setName(request.getName());
        }
        if (request.getVenue() != null) {
            show.setVenue(request.getVenue());
        }
        if (request.getShowDateTime() != null) {
            show.setShowDateTime(request.getShowDateTime());
        }
        if (request.getBasePrice() != null) {
            show.setBasePrice(request.getBasePrice());
        }
        if (request.getGenre() != null) {
            show.setGenre(request.getGenre());
        }
        if (request.getLanguage() != null) {
            show.setLanguage(request.getLanguage());
        }
        if (request.getDuration() != null) {
            show.setDuration(request.getDuration());
        }
        if (request.getRating() != null) {
            show.setRating(request.getRating());
        }
        if (request.getPosterUrl() != null) {
            show.setPosterUrl(request.getPosterUrl());
        }

        Show updated = showRepository.save(show);
        log.info("Updated show {}", showId);
        return toShowResponse(updated);
    }

    @Override
    @Transactional
    public ShowResponse publishShow(Long showId) {
        Show show = findShowOrThrow(showId);

        if (show.getStatus() != ShowStatus.DRAFT) {
            throw new InvalidShowStateException(
                    Constants.ERR_INVALID_SHOW_STATE + show.getStatus()
                    + ". Only DRAFT shows can be published.");
        }

        show.setStatus(ShowStatus.PUBLISHED);
        Show published = showRepository.save(show);
        log.info("Published show {} (externalId={})", showId, show.getExternalShowId());
        return toShowResponse(published);
    }

    @Override
    @Transactional
    public void cancelShow(Long showId) {
        Show show = findShowOrThrow(showId);

        if (show.getStatus() == ShowStatus.CANCELLED) {
            throw new InvalidShowStateException(
                    Constants.ERR_INVALID_SHOW_STATE + show.getStatus()
                    + ". Show is already cancelled.");
        }

        show.setStatus(ShowStatus.CANCELLED);
        showRepository.save(show);
        log.info("Cancelled show {} (externalId={})", showId, show.getExternalShowId());
    }

    @Override
    public List<SeatResponse> getSeatsForShow(Long showId) {
        findShowOrThrow(showId);
        return seatRepository.findByShowIdOrderBySeatNumber(showId).stream()
                .map(this::toSeatResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatResponse> getSeatsByExternalShowId(String externalShowId) {
        Show show = showRepository.findByExternalShowId(externalShowId)
                .orElseThrow(() -> new ShowNotFoundException(
                        Constants.ERR_SHOW_NOT_FOUND_EXT + externalShowId));
        return seatRepository.findByShowIdOrderBySeatNumber(show.getId()).stream()
                .map(this::toSeatResponse)
                .collect(Collectors.toList());
    }

    // ── Helpers ────────────────────────────────────────────────────────

    private Show findShowOrThrow(Long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(showId));
    }

    /**
     * Generates seats in rows using the show's configured seatsPerRow.
     * Row labels: A, B, C, ... Z. Seat numbers: A01, A02, ..., B01, etc.
     */
    List<Seat> generateSeats(Show show, int totalSeats) {
        List<Seat> seats = new ArrayList<>();
        int seatsPerRow = show.getSeatsPerRow() != null ? show.getSeatsPerRow() : 20;
        int seatIndex = 0;

        for (int row = 0; seatIndex < totalSeats && row < Constants.ROW_LABELS.length(); row++) {
            char rowLabel = Constants.ROW_LABELS.charAt(row);
            int seatsInThisRow = Math.min(seatsPerRow, totalSeats - seatIndex);

            for (int col = 1; col <= seatsInThisRow; col++) {
                Seat seat = new Seat();
                seat.setSeatNumber(String.format(Constants.SEAT_NUMBER_FORMAT, rowLabel, col));
                seat.setShow(show);
                seat.setCategory(SeatCategory.STANDARD);
                seat.setPrice(show.getBasePrice());
                seats.add(seat);
                seatIndex++;
            }
        }

        return seats;
    }

    ShowResponse toShowResponse(Show show) {
        return ShowResponse.builder()
                .id(show.getId())
                .externalShowId(show.getExternalShowId())
                .name(show.getName())
                .venue(show.getVenue())
                .showDateTime(show.getShowDateTime())
                .totalSeats(show.getTotalSeats())
                .seatsPerRow(show.getSeatsPerRow())
                .basePrice(show.getBasePrice())
                .genre(show.getGenre())
                .language(show.getLanguage())
                .duration(show.getDuration())
                .rating(show.getRating())
                .posterUrl(show.getPosterUrl())
                .status(show.getStatus())
                .createdAt(show.getCreatedAt())
                .updatedAt(show.getUpdatedAt())
                .build();
    }

    SeatResponse toSeatResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .showId(seat.getShow().getId())
                .category(seat.getCategory())
                .price(seat.getPrice())
                .build();
    }
}
