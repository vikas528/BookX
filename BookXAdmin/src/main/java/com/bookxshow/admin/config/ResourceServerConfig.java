/*
 * BookXAdmin - Admin Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * OAuth2 Resource Server security configuration.
 * Validates JWT bearer tokens issued by the BookXShow Authorization Server.
 *
 * @since 1.0.0
 */
package com.bookxshow.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.bookxshow.admin.common.Constants;
import com.bookxshow.admin.common.Routes;

/**
 * Configures BookXAdmin as an OAuth2 <strong>Resource Server</strong>.
 *
 * <p>All incoming requests (except health/actuator) must carry a valid
 * JWT {@code Bearer} token. The token is validated against the
 * Authorization Server's JWK Set endpoint.</p>
 *
 * <h3>Endpoint Access Rules</h3>
 * <ul>
 *   <li>Actuator health/info — public (no token required)</li>
 *   <li>GET on /bookxshow/v1/shows/** — requires {@code SCOPE_bookxshow.read} (service-to-service)</li>
 *   <li>All /bookxshow/v1/admin/** endpoints — requires {@code SCOPE_bookxshow.admin}</li>
 * </ul>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerConfig {

    /**
     * Main security filter chain — stateless JWT validation.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints
                        .requestMatchers(Routes.ACTUATOR_HEALTH, Routes.ACTUATOR_INFO).permitAll()

                        // Service-to-service read endpoints — require bookxshow.read scope
                        .requestMatchers(HttpMethod.GET, Routes.SHOWS_MATCHER).hasAuthority(Constants.SCOPE_READ)

                        // All admin endpoints (shows CRUD + M2M client management) — require admin scope
                        .requestMatchers(Routes.ADMIN_MATCHER).hasAuthority(Constants.SCOPE_ADMIN)

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> { })
                );

        return http.build();
    }
}
