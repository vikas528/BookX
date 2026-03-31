/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * JPA entity representing a single seat within a show's layout.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.bookxshow.admin.enums.SeatCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a single seat within a show's seating layout.
 * Seats are auto-generated when a show is created, based on {@code totalSeats}.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 * @see Show
 */
@Entity
@Table(name = "admin_seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"seat_number", "show_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class Seat {

    /** Auto-generated surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Seat label visible to end-users (e.g. "A01", "B12"). */
    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    /** The show this seat belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    /** Seat pricing category. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatCategory category = SeatCategory.STANDARD;

    /** Price for this specific seat (may differ from show base price). */
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /** Timestamp when this record was first persisted. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp of the most recent update. */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** JPA lifecycle callback — sets audit timestamps before initial persist. */
    @PrePersist
    void onPrePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /** JPA lifecycle callback — updates the modification timestamp. */
    @PreUpdate
    void onPreUpdate() {
        this.updatedAt = Instant.now();
    }
}
