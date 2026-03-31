/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for updating an existing show. All fields are optional —
 * only non-null fields will be applied.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShowRequest {

    /** Updated show name (optional). */
    @Size(min = 2, max = 200, message = "Show name must be between 2 and 200 characters")
    private String name;

    /** Updated venue (optional). */
    @Size(min = 2, max = 200, message = "Venue must be between 2 and 200 characters")
    private String venue;

    /** Updated show date/time (optional, must be future). */
    @Future(message = "Show date and time must be in the future")
    private LocalDateTime showDateTime;

    /** Updated base price (optional). */
    @DecimalMin(value = "0.01", message = "Base price must be at least 0.01")
    private BigDecimal basePrice;

    /** Updated genre (optional). */
    @Size(max = 100, message = "Genre must not exceed 100 characters")
    private String genre;

    /** Updated language (optional). */
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;

    /** Updated duration (optional). */
    @Size(max = 20, message = "Duration must not exceed 20 characters")
    private String duration;

    /** Updated rating (optional). */
    private Double rating;

    /** Updated poster URL (optional). */
    @Size(max = 500, message = "Poster URL must not exceed 500 characters")
    private String posterUrl;
}
