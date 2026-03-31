/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating a new show.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateShowRequest {

    /** Human-readable show name. */
    @NotBlank(message = "Show name is required")
    @Size(min = 2, max = 200, message = "Show name must be between 2 and 200 characters")
    private String name;

    /** Venue or hall. */
    @NotBlank(message = "Venue is required")
    @Size(min = 2, max = 200, message = "Venue must be between 2 and 200 characters")
    private String venue;

    /** Scheduled date and time (must be in the future). */
    @NotNull(message = "Show date and time is required")
    @Future(message = "Show date and time must be in the future")
    private LocalDateTime showDateTime;

    /** Total number of seats (1–10000). */
    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    @Max(value = 10000, message = "Total seats must not exceed 10000")
    private Integer totalSeats;

    /** Number of seats per row (1–50). */
    @NotNull(message = "Seats per row is required")
    @Min(value = 1, message = "Seats per row must be at least 1")
    @Max(value = 50, message = "Seats per row must not exceed 50")
    private Integer seatsPerRow;

    /** Base ticket price. */
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Base price must be at least 0.01")
    private BigDecimal basePrice;

    /** Genre of the show. */
    @Size(max = 100, message = "Genre must not exceed 100 characters")
    private String genre;

    /** Language of the show. */
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;

    /** Duration of the show (e.g. "2h 49m"). */
    @Size(max = 20, message = "Duration must not exceed 20 characters")
    private String duration;

    /** Rating of the show. */
    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    private Double rating;

    /** Poster image URL. */
    @Size(max = 500, message = "Poster URL must not exceed 500 characters")
    private String posterUrl;
}
