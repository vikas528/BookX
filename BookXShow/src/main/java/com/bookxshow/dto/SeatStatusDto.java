/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * DTO representing the status of a single seat.
 *
 * @since 1.0.0
 */
package com.bookxshow.dto;

import java.time.Instant;

import com.bookxshow.enums.SeatStatus;

/**
 * Data Transfer Object exposing seat status information via the REST API.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class SeatStatusDto {

    /** Primary key of the seat. */
    private Long seatId;

    /** Human-readable seat label (e.g. "A01"). */
    private String seatNumber;

    /** Primary key of the show this seat belongs to. */
    private Long showId;

    /** Current booking lifecycle status. */
    private SeatStatus status;

    /** User ID of the current lock holder (null when available). */
    private String lockedBy;

    /** Instant when the current lock expires (null when not locked). */
    private Instant lockExpiry;

    /** Default constructor for Jackson deserialization. */
    public SeatStatusDto() {
    }

    /**
     * Constructs a fully-populated seat status DTO.
     *
     * @param seatId     the seat primary key
     * @param seatNumber the seat label
     * @param showId     the show primary key
     * @param status     the current status
     * @param lockedBy   the lock holder user ID
     * @param lockExpiry the lock expiry instant
     */
    public SeatStatusDto(Long seatId, String seatNumber, Long showId,
                          SeatStatus status, String lockedBy, Instant lockExpiry) {
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.showId = showId;
        this.status = status;
        this.lockedBy = lockedBy;
        this.lockExpiry = lockExpiry;
    }

    /** Returns the seat primary key. */
    public Long getSeatId() { return seatId; }

    /** Sets the seat primary key. */
    public void setSeatId(Long seatId) { this.seatId = seatId; }

    /** Returns the seat label. */
    public String getSeatNumber() { return seatNumber; }

    /** Sets the seat label. */
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    /** Returns the show primary key. */
    public Long getShowId() { return showId; }

    /** Sets the show primary key. */
    public void setShowId(Long showId) { this.showId = showId; }

    /** Returns the current seat status. */
    public SeatStatus getStatus() { return status; }

    /** Sets the current seat status. */
    public void setStatus(SeatStatus status) { this.status = status; }

    /** Returns the current lock holder, or {@code null}. */
    public String getLockedBy() { return lockedBy; }

    /** Sets the current lock holder. */
    public void setLockedBy(String lockedBy) { this.lockedBy = lockedBy; }

    /** Returns the lock expiry instant, or {@code null}. */
    public Instant getLockExpiry() { return lockExpiry; }

    /** Sets the lock expiry instant. */
    public void setLockExpiry(Instant lockExpiry) { this.lockExpiry = lockExpiry; }
}
