/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Spring-managed bean used in @PreAuthorize SpEL expressions to verify
 * booking ownership.
 *
 * @since 1.0.0
 */
package com.bookxshow.config;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.bookxshow.entity.Booking;
import com.bookxshow.repository.BookingRepository;

/**
 * Checks whether the currently authenticated principal owns a given booking.
 *
 * <p>Used in {@code @PreAuthorize} SpEL expressions on the cancel-booking
 * endpoint to ensure a user can only cancel their own bookings (unless
 * they have admin scope).</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component("bookingOwnershipChecker")
public class BookingOwnershipChecker {

    private final BookingRepository bookingRepository;

    public BookingOwnershipChecker(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Returns {@code true} if the booking identified by {@code bookingReference}
     * belongs to the authenticated user.
     *
     * @param bookingReference the booking reference to check
     * @param authentication   the current Spring Security authentication
     * @return {@code true} if the authenticated user owns the booking
     */
    public boolean isOwner(String bookingReference, Authentication authentication) {
        if (authentication == null || bookingReference == null) {
            return false;
        }
        return bookingRepository.findByBookingReference(bookingReference)
                .map(Booking::getUserId)
                .map(userId -> userId.equals(authentication.getName()))
                .orElse(false);
    }
}
