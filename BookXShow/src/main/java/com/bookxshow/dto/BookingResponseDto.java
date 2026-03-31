/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Response DTO returned after a booking attempt.
 *
 * @since 1.0.0
 */
package com.bookxshow.dto;

import com.bookxshow.enums.BookingOutcome;
import com.bookxshow.enums.SeatStatus;

/**
 * Outbound DTO describing the result of a booking attempt.
 *
 * <p>Uses the builder pattern for clean, readable construction.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class BookingResponseDto {

    /** Unique booking reference (null if booking was not confirmed). */
    private String bookingReference;

    /** Primary key of the seat. */
    private Long seatId;

    /** Human-readable seat label. */
    private String seatNumber;

    /** Primary key of the show. */
    private Long showId;

    /** Identifier of the user who attempted the booking. */
    private String userId;

    /** Outcome of the booking attempt. */
    private BookingOutcome outcome;

    /** Current status of the seat after the attempt. */
    private SeatStatus seatStatus;

    /** Default constructor. */
    public BookingResponseDto() {
    }

    private BookingResponseDto(Builder builder) {
        this.bookingReference = builder.bookingReference;
        this.seatId = builder.seatId;
        this.seatNumber = builder.seatNumber;
        this.showId = builder.showId;
        this.userId = builder.userId;
        this.outcome = builder.outcome;
        this.seatStatus = builder.seatStatus;
    }

    /**
     * Creates a new builder instance.
     *
     * @return a fresh {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    // ── Getters ─────────────────────────────────────────────────────────

    /** Returns the booking reference, or {@code null} if not confirmed. */
    public String getBookingReference() { return bookingReference; }

    /** Returns the seat primary key. */
    public Long getSeatId() { return seatId; }

    /** Returns the human-readable seat label. */
    public String getSeatNumber() { return seatNumber; }

    /** Returns the show primary key. */
    public Long getShowId() { return showId; }

    /** Returns the user identifier. */
    public String getUserId() { return userId; }

    /** Returns the booking outcome. */
    public BookingOutcome getOutcome() { return outcome; }

    /** Returns the current seat status. */
    public SeatStatus getSeatStatus() { return seatStatus; }

    // ── Builder ─────────────────────────────────────────────────────────

    /**
     * Fluent builder for {@link BookingResponseDto}.
     */
    public static class Builder {
        private String bookingReference;
        private Long seatId;
        private String seatNumber;
        private Long showId;
        private String userId;
        private BookingOutcome outcome;
        private SeatStatus seatStatus;

        /** Sets the booking reference. */
        public Builder bookingReference(String bookingReference) {
            this.bookingReference = bookingReference;
            return this;
        }

        /** Sets the seat primary key. */
        public Builder seatId(Long seatId) {
            this.seatId = seatId;
            return this;
        }

        /** Sets the human-readable seat label. */
        public Builder seatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
            return this;
        }

        /** Sets the show primary key. */
        public Builder showId(Long showId) {
            this.showId = showId;
            return this;
        }

        /** Sets the user identifier. */
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        /** Sets the booking outcome. */
        public Builder outcome(BookingOutcome outcome) {
            this.outcome = outcome;
            return this;
        }

        /** Sets the current seat status. */
        public Builder seatStatus(SeatStatus seatStatus) {
            this.seatStatus = seatStatus;
            return this;
        }

        /**
         * Builds the response DTO.
         *
         * @return an immutable {@link BookingResponseDto}
         */
        public BookingResponseDto build() {
            return new BookingResponseDto(this);
        }
    }
}
