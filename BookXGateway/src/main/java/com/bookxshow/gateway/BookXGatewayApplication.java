/*
 * BookXShow - API Gateway
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Spring Boot entry point for the API Gateway.
 *
 * @since 1.0.0
 */
package com.bookxshow.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class for the BookXShow API Gateway.
 *
 * <p>Routes all incoming requests to downstream microservices
 * (BookXShow, AdminService) after authenticating and authorizing
 * via OAuth2 / JWT.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
public class BookXGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookXGatewayApplication.class, args);
    }
}
