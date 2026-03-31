/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * DTO representing show information received from the Admin Service.
 *
 * @since 1.0.0
 */
package com.bookxshow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object carrying show metadata. Used both for the
 * Admin Service integration and for the REST API response.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class ShowDto {

    /** Primary key of the show (local DB). */
    private Long id;

    /** External identifier assigned by the Admin Service. */
    private String externalShowId;

    /** Human-readable show name. */
    private String name;

    /** Venue or hall where the show is hosted. */
    private String venue;

    /** Scheduled date and time. */
    private LocalDateTime showDateTime;

    /** Total number of seats configured for this show. */
    private Integer totalSeats;

    /** Number of seats per row. */
    private Integer seatsPerRow;

    /** Base ticket price. */
    private BigDecimal basePrice;

    /** Genre of the show. */
    private String genre;

    /** Language of the show. */
    private String language;

    /** Duration of the show. */
    private String duration;

    /** Rating of the show. */
    private Double rating;

    /** Poster image URL. */
    private String posterUrl;

    /** Default constructor for Jackson. */
    public ShowDto() {
    }

    /**
     * Constructs a show DTO with all fields.
     */
    public ShowDto(Long id, String externalShowId, String name,
                    String venue, LocalDateTime showDateTime, Integer totalSeats,
                    Integer seatsPerRow, BigDecimal basePrice, String genre,
                    String language, String duration, Double rating, String posterUrl) {
        this.id = id;
        this.externalShowId = externalShowId;
        this.name = name;
        this.venue = venue;
        this.showDateTime = showDateTime;
        this.totalSeats = totalSeats;
        this.seatsPerRow = seatsPerRow;
        this.basePrice = basePrice;
        this.genre = genre;
        this.language = language;
        this.duration = duration;
        this.rating = rating;
        this.posterUrl = posterUrl;
    }

    /** Returns the local primary key. */
    public Long getId() { return id; }

    /** Sets the local primary key. */
    public void setId(Long id) { this.id = id; }

    /** Returns the Admin-Service identifier. */
    public String getExternalShowId() { return externalShowId; }

    /** Sets the Admin-Service identifier. */
    public void setExternalShowId(String externalShowId) { this.externalShowId = externalShowId; }

    /** Returns the show name. */
    public String getName() { return name; }

    /** Sets the show name. */
    public void setName(String name) { this.name = name; }

    /** Returns the venue name. */
    public String getVenue() { return venue; }

    /** Sets the venue name. */
    public void setVenue(String venue) { this.venue = venue; }

    /** Returns the scheduled date/time. */
    public LocalDateTime getShowDateTime() { return showDateTime; }

    /** Sets the scheduled date/time. */
    public void setShowDateTime(LocalDateTime showDateTime) { this.showDateTime = showDateTime; }

    /** Returns the total seat count. */
    public Integer getTotalSeats() { return totalSeats; }

    /** Sets the total seat count. */
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    /** Returns the seats per row. */
    public Integer getSeatsPerRow() { return seatsPerRow; }

    /** Sets the seats per row. */
    public void setSeatsPerRow(Integer seatsPerRow) { this.seatsPerRow = seatsPerRow; }

    /** Returns the base price. */
    public BigDecimal getBasePrice() { return basePrice; }

    /** Sets the base price. */
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    /** Returns the genre. */
    public String getGenre() { return genre; }

    /** Sets the genre. */
    public void setGenre(String genre) { this.genre = genre; }

    /** Returns the language. */
    public String getLanguage() { return language; }

    /** Sets the language. */
    public void setLanguage(String language) { this.language = language; }

    /** Returns the duration. */
    public String getDuration() { return duration; }

    /** Sets the duration. */
    public void setDuration(String duration) { this.duration = duration; }

    /** Returns the rating. */
    public Double getRating() { return rating; }

    /** Sets the rating. */
    public void setRating(Double rating) { this.rating = rating; }

    /** Returns the poster URL. */
    public String getPosterUrl() { return posterUrl; }

    /** Sets the poster URL. */
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
}
