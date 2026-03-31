/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * OAuth2 Resource Server security configuration.
 * Validates JWT bearer tokens issued by the BookXShow Authorization Server.
 *
 * @since 1.0.0
 */
package com.bookxshow.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.bookxshow.common.Constants;
import com.bookxshow.common.Routes;

/**
 * Configures BookXShow as an OAuth2 <strong>Resource Server</strong>.
 *
 * <p>All incoming requests (except health/actuator) must carry a valid
 * JWT {@code Bearer} token in the {@code Authorization} header. The token
 * is validated against the Authorization Server's JWK Set endpoint.</p>
 *
 * <h3>Endpoint Access Rules</h3>
 * <ul>
 *   <li>Actuator health/info — public (no token required)</li>
 *   <li>GET on shows/seats/bookings/users — requires {@code SCOPE_bookxshow.read}</li>
 *   <li>POST on bookings — requires {@code SCOPE_bookxshow.write}</li>
 *   <li>Admin sync endpoint — requires {@code SCOPE_bookxshow.admin}</li>
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

    @Autowired
    private SecurityErrorHandler securityErrorHandler;

    /**
     * Main security filter chain — stateless JWT validation.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Stateless API — no CSRF needed
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler))
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints
                        .requestMatchers(Routes.ACTUATOR_HEALTH, Routes.ACTUATOR_INFO).permitAll()

                        // Read operations — require bookxshow.read scope
                        .requestMatchers(HttpMethod.GET, Routes.BOOKINGS_MATCHER).hasAuthority(Constants.SCOPE_READ)
                        .requestMatchers(HttpMethod.GET, Routes.SHOWS_MATCHER).hasAuthority(Constants.SCOPE_READ)
                        .requestMatchers(HttpMethod.GET, Routes.USERS_MATCHER).hasAuthority(Constants.SCOPE_READ)

                        // Write operations — require bookxshow.write scope
                        .requestMatchers(HttpMethod.POST, Routes.BOOKINGS_MATCHER).hasAuthority(Constants.SCOPE_WRITE)

                        // Cancel (delete) operations — require bookxshow.write scope
                        .requestMatchers(HttpMethod.DELETE, Routes.BOOKINGS_MATCHER).hasAuthority(Constants.SCOPE_WRITE)

                        // Admin sync endpoint — require admin scope (service-to-service / admin users only)
                        .requestMatchers(HttpMethod.POST, Routes.ADMIN_MATCHER).hasAuthority(Constants.SCOPE_ADMIN)

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(securityErrorHandler)
                        .jwt(jwt -> { }) // Uses spring.security.oauth2.resourceserver.jwt.issuer-uri
                );

        return http.build();
    }
}
