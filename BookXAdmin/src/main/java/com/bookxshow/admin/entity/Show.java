/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * JPA entity representing a show in the admin catalogue.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bookxshow.admin.enums.ShowStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Master show entity in the admin catalogue. This is the source of truth
 * for show metadata. Downstream services (e.g. BookXShow) synchronize
 * their local copies from this record.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 * @see Seat
 */
@Entity
@Table(name = "admin_shows")
@Getter
@Setter
@NoArgsConstructor
public class Show {

    /** Auto-generated surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique external identifier for cross-service correlation.
     * Auto-generated as UUID-based string prefixed with "SHOW-".
     */
    @Column(name = "external_show_id", nullable = false, unique = true, length = 50)
    private String externalShowId;

    /** Human-readable name of the show. */
    @Column(nullable = false, length = 200)
    private String name;

    /** Venue or hall where the show takes place. */
    @Column(length = 200)
    private String venue;

    /** Scheduled date and time of the show. */
    @Column(name = "show_date_time", nullable = false)
    private LocalDateTime showDateTime;

    /** Total number of seats configured for this show. */
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    /** Number of seats per row (e.g. A1, A2, ... up to this limit). */
    @Column(name = "seats_per_row", nullable = false)
    private Integer seatsPerRow = 20;

    /** Base ticket price for the show. */
    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    /** Genre of the show (e.g. Action, Sci-Fi, Horror / Comedy). */
    @Column(length = 100)
    private String genre;

    /** Language of the show (e.g. English, Hindi, Telugu). */
    @Column(length = 50)
    private String language;

    /** Duration of the show (e.g. "2h 49m"). */
    @Column(length = 20)
    private String duration;

    /** Rating of the show (e.g. 4.8). */
    private Double rating;

    /** URL of the show poster image. */
    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    /** Current lifecycle status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShowStatus status = ShowStatus.DRAFT;

    /** All seats belonging to this show (lazy-loaded). */
    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Seat> seats = new ArrayList<>();

    /** Timestamp when this record was first persisted. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp of the most recent update to this record. */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** JPA lifecycle callback — sets audit timestamps before initial persist. */
    @PrePersist
    void onPrePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        if (this.externalShowId == null) {
            this.externalShowId = "SHOW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    /** JPA lifecycle callback — updates the modification timestamp. */
    @PreUpdate
    void onPreUpdate() {
        this.updatedAt = Instant.now();
    }
}
