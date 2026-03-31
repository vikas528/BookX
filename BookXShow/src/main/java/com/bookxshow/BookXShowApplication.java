/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Main entry point for the BookXShow seat booking microservice.
 * This application handles real-time seat reservations for shows,
 * integrating with external admin and payment services.
 *
 * @since 1.0.0
 */
package com.bookxshow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bootstrap class for the BookXShow Spring Boot application.
 *
 * <p>Enables component scanning across the {@code com.bookxshow} package,
 * autoconfiguration of Spring Boot starters (Web, JPA, Actuator), and
 * scheduling support for background tasks such as seat-lock expiry sweeps.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class BookXShowApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments forwarded to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(BookXShowApplication.class, args);
    }
}
