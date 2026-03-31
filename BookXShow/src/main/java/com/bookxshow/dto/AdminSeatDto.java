/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * DTO matching the Admin Service's SeatResponse structure for deserialization.
 *
 * @since 1.0.0
 */
package com.bookxshow.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object that mirrors the Admin Service's {@code SeatResponse}
 * structure. Used by {@link com.bookxshow.service.AdminServiceClient} to
 * deserialize seat data fetched from the Admin Service during show sync.
 *
 * <p>Field names must match the Admin Service's JSON keys exactly:</p>
 * <ul>
 *   <li>{@code id} — seat primary key in the admin database</li>
 *   <li>{@code seatNumber} — human-readable label (e.g. "A01")</li>
 *   <li>{@code showId} — show primary key in the admin database</li>
 *   <li>{@code category} — pricing category (STANDARD, PREMIUM, VIP)</li>
 *   <li>{@code price} — seat price</li>
 * </ul>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class AdminSeatDto {

    /** Seat primary key in the Admin Service database. */
    private Long id;

    /** Human-readable seat label (e.g. "A01"). */
    private String seatNumber;

    /** Show primary key in the Admin Service database. */
    private Long showId;

    /** Pricing category (STANDARD, PREMIUM, VIP). */
    private String category;

    /** Seat price. */
    private BigDecimal price;

    /** Default constructor for Jackson deserialization. */
    public AdminSeatDto() {
    }

    public AdminSeatDto(Long id, String seatNumber, Long showId,
                        String category, BigDecimal price) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.showId = showId;
        this.category = category;
        this.price = price;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public Long getShowId() { return showId; }
    public void setShowId(Long showId) { this.showId = showId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
