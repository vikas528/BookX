/*
 * BookXShow - Authorization Server
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Spring Boot entry point for the OAuth2 Authorization Server.
 *
 * @since 1.0.0
 */
package com.bookxshow.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class for the BookXShow OAuth2 Authorization Server.
 *
 * <p>This server issues JWT access tokens using the following grant types:</p>
 * <ul>
 *   <li><strong>authorization_code</strong> — for user-facing clients (UI)</li>
 *   <li><strong>client_credentials</strong> — for service-to-service communication</li>
 *   <li><strong>refresh_token</strong> — to renew expired access tokens</li>
 * </ul>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
public class BookXAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookXAuthServerApplication.class, args);
    }
}
