/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import com.bookxshow.admin.enums.ShowStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO representing a show in the admin catalogue.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowResponse {

    /** Local primary key. */
    private Long id;

    /** External identifier for cross-service correlation. */
    private String externalShowId;

    /** Human-readable show name. */
    private String name;

    /** Venue or hall. */
    private String venue;

    /** Scheduled date and time. */
    private LocalDateTime showDateTime;

    /** Total number of seats. */
    private Integer totalSeats;

    /** Number of seats per row. */
    private Integer seatsPerRow;

    /** Base ticket price. */
    private BigDecimal basePrice;

    /** Genre of the show. */
    private String genre;

    /** Language of the show. */
    private String language;

    /** Duration of the show. */
    private String duration;

    /** Rating of the show. */
    private Double rating;

    /** Poster image URL. */
    private String posterUrl;

    /** Current lifecycle status. */
    private ShowStatus status;

    /** Record creation timestamp. */
    private Instant createdAt;

    /** Last modification timestamp. */
    private Instant updatedAt;
}
