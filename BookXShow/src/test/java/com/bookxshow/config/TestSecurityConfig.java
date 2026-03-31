/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Test security configuration that disables OAuth2 Resource Server
 * authentication so unit and integration tests can run without a
 * live Authorization Server.
 *
 * @since 1.0.0
 */
package com.bookxshow.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Overrides the production {@link ResourceServerConfig} for tests,
 * permitting all requests without JWT authentication.
 *
 * <p>Import this configuration in test classes that boot a Spring context:
 * <pre>{@code @Import(TestSecurityConfig.class)}</pre>
 *
 * <p>For integration tests using {@code @SpringBootTest}, this config
 * is auto-discovered from the test classpath.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    /**
     * Permits all HTTP requests — no authentication required in tests.
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}
