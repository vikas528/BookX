/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Spring Boot application entry point for the Admin microservice.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the BookXAdmin service.
 *
 * <p>This service manages the master show and seat catalogue. Other
 * services (e.g. BookXShow) synchronize their local copies from here.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
public class BookXAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookXAdminApplication.class, args);
    }
}
