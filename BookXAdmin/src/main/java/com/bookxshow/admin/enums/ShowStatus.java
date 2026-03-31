/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.admin.enums;

/**
 * Lifecycle states of a show in the admin catalogue.
 *
 * @since 1.0.0
 */
public enum ShowStatus {

    /** Show is being configured, not visible to booking service. */
    DRAFT,

    /** Show is published and available for seat booking. */
    PUBLISHED,

    /** Show has been cancelled, seats cannot be booked. */
    CANCELLED
}
