/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Request DTO for booking a seat.
 *
 * @since 1.0.0
 */
package com.bookxshow.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Inbound DTO for the booking request submitted by a user.
 *
 * <p>Validated using Jakarta Bean Validation constraints. All fields
 * are required for a valid booking attempt.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class BookingRequestDto {

    /** Primary key of the show in the local database. */
    @NotNull(message = "Show ID is required")
    private Long showId;

    /** Primary key of the seat in the local database. */
    @NotNull(message = "Seat ID is required")
    private Long seatId;

    /** Unique identifier of the user making the booking. */
    @NotBlank(message = "User ID is required")
    private String userId;

    /** Amount to charge (must be positive). */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    /** Default constructor for Jackson deserialization. */
    public BookingRequestDto() {
    }

    /**
     * Constructs a fully-populated booking request.
     *
     * @param showId the show primary key
     * @param seatId the seat primary key
     * @param userId the user identifier
     * @param amount the payment amount
     */
    public BookingRequestDto(Long showId, Long seatId, String userId, BigDecimal amount) {
        this.showId = showId;
        this.seatId = seatId;
        this.userId = userId;
        this.amount = amount;
    }

    /** Returns the show primary key. */
    public Long getShowId() { return showId; }

    /** Sets the show primary key. */
    public void setShowId(Long showId) { this.showId = showId; }

    /** Returns the seat primary key. */
    public Long getSeatId() { return seatId; }

    /** Sets the seat primary key. */
    public void setSeatId(Long seatId) { this.seatId = seatId; }

    /** Returns the user identifier. */
    public String getUserId() { return userId; }

    /** Sets the user identifier. */
    public void setUserId(String userId) { this.userId = userId; }

    /** Returns the payment amount. */
    public BigDecimal getAmount() { return amount; }

    /** Sets the payment amount. */
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
