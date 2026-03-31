/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * JPA entity representing a show (event) whose seats can be booked.
 * Show metadata is synced from the external Admin Service.
 *
 * @since 1.0.0
 */
package com.bookxshow.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persistent entity representing a show (event) within the BookXShow platform.
 *
 * <p>Show details — name, venue, date, and seat inventory — are received from
 * the external <em>Admin Service</em> and cached locally so that the booking
 * flow can operate against the local database without cross-service calls on
 * every request.</p>
 *
 * <p>Each {@code Show} owns a collection of {@link Seat} entities that
 * represent individual bookable positions.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 * @see Seat
 */
@Entity
@Table(name = "shows")
@Getter
@Setter
@NoArgsConstructor
public class Show {

    /** Auto-generated surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique identifier assigned by the Admin Service.
     * Used for cross-service correlation.
     */
    @Column(name = "external_show_id", nullable = false, unique = true)
    private String externalShowId;

    /** Human-readable name of the show (e.g. "Rock Concert 2026"). */
    @Column(nullable = false)
    private String name;

    /** Venue or hall where the show takes place. */
    private String venue;

    /** Scheduled date and time of the show. */
    @Column(name = "show_date_time")
    private LocalDateTime showDateTime;

    /** Total number of seats configured for this show. */
    @Column(name = "total_seats")
    private Integer totalSeats;

    /** Number of seats per row (e.g. A1, A2, ... up to this limit). */
    @Column(name = "seats_per_row")
    private Integer seatsPerRow;

    /** Base ticket price for the show. */
    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    /** Genre of the show (e.g. Action, Sci-Fi). */
    @Column(length = 100)
    private String genre;

    /** Language of the show (e.g. English, Hindi). */
    @Column(length = 50)
    private String language;

    /** Duration of the show (e.g. "2h 49m"). */
    @Column(length = 20)
    private String duration;

    /** Rating of the show. */
    private Double rating;

    /** URL of the show poster image. */
    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    /** All seats belonging to this show (lazy-loaded). */
    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats = new ArrayList<>();

    /** Timestamp when this record was first persisted. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp of the most recent update to this record. */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * JPA lifecycle callback — sets audit timestamps before initial persist.
     */
    @PrePersist
    void onPrePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * JPA lifecycle callback — updates the {@code updatedAt} timestamp.
     */
    @PreUpdate
    void onPreUpdate() {
        this.updatedAt = Instant.now();
    }
}
