/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Configuration for the JDBC-backed RegisteredClientRepository,
 * allowing the Admin Service to manage OAuth2 M2M client credentials
 * in the shared {@code oauth2_registered_client} table.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/**
 * Creates the {@link RegisteredClientRepository} and {@link PasswordEncoder}
 * beans used by the M2M client management service.
 *
 * <p>The {@code JdbcRegisteredClientRepository} reads/writes to the same
 * {@code oauth2_registered_client} table that the Authorization Server uses,
 * since both services share the {@code bookingDb} database.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class M2mClientConfig {

    /**
     * JDBC-backed client repository — reads/writes to the
     * {@code oauth2_registered_client} table shared with the Auth Server.
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    /**
     * BCrypt password encoder with strength 12.
     * Used to hash client secrets before persisting.
     * Matches the Auth Server's encoder strength.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
