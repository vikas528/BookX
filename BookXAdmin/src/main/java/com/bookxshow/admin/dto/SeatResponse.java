/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.dto;

import java.math.BigDecimal;

import com.bookxshow.admin.enums.SeatCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO representing a seat in a show's layout.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatResponse {

    /** Seat primary key. */
    private Long id;

    /** Human-readable seat label (e.g. "A01"). */
    private String seatNumber;

    /** Show primary key. */
    private Long showId;

    /** Seat pricing category. */
    private SeatCategory category;

    /** Price for this seat. */
    private BigDecimal price;
}
