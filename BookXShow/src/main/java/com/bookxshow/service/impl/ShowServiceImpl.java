/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookxshow.dto.ShowDto;
import com.bookxshow.entity.Show;
import com.bookxshow.repository.ShowRepository;
import com.bookxshow.repository.ShowSpecification;
import com.bookxshow.service.ShowService;

/**
 * Read-only service for querying the local show catalogue.
 *
 * <p>Delegates to {@link ShowRepository#findByFilters} which applies
 * optional date-range and name-pattern predicates in JPQL. A plain
 * name without SQL wildcards ({@code %}, {@code _}) is automatically
 * wrapped as {@code %name%} for a case-insensitive contains search.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;

    public ShowServiceImpl(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    @Override
    public List<ShowDto> getShows(LocalDateTime fromDate, LocalDateTime toDate, String name) {
        String namePattern = buildNamePattern(name);
        return showRepository
                .findAll(ShowSpecification.withFilters(fromDate, toDate, namePattern),
                         Sort.by(Sort.Direction.ASC, "showDateTime"))
                .stream()
                .map(this::toShowDto)
                .collect(Collectors.toList());
    }

    // ── Helpers ────────────────────────────────────────────────────────

    /**
     * Converts an optional raw name string to a SQL LIKE pattern.
     * <ul>
     *   <li>Null input → null (no filter applied)</li>
     *   <li>Input already containing {@code %} or {@code _} → used as-is</li>
     *   <li>Plain text → wrapped with {@code %...%} for contains matching</li>
     * </ul>
     */
    private String buildNamePattern(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        if (name.contains("%") || name.contains("_")) {
            return name.toLowerCase(); // caller supplied an explicit LIKE pattern
        }
        return "%" + name.toLowerCase() + "%"; // default: case-insensitive contains
    }

    /** Maps a {@link Show} entity to a {@link ShowDto}. */
    private ShowDto toShowDto(Show show) {
        ShowDto dto = new ShowDto();
        dto.setId(show.getId());
        dto.setExternalShowId(show.getExternalShowId());
        dto.setName(show.getName());
        dto.setVenue(show.getVenue());
        dto.setShowDateTime(show.getShowDateTime());
        dto.setTotalSeats(show.getTotalSeats());
        dto.setSeatsPerRow(show.getSeatsPerRow());
        dto.setBasePrice(show.getBasePrice());
        dto.setGenre(show.getGenre());
        dto.setLanguage(show.getLanguage());
        dto.setDuration(show.getDuration());
        dto.setRating(show.getRating());
        dto.setPosterUrl(show.getPosterUrl());
        return dto;
    }
}
