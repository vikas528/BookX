/*
 * BookXShow - API Gateway
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Security configuration for the API Gateway.
 *
 * @since 1.0.0
 */
package com.bookxshow.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.bookxshow.gateway.common.Routes;

/**
 * Configures the Gateway as both an <strong>OAuth2 Client</strong> (for
 * browser-based login via Authorization Code) and an <strong>OAuth2 Resource
 * Server</strong> (for machine-to-machine JWT bearer token validation).
 *
 * <h3>Request Flow</h3>
 * <ol>
 *   <li>Browser requests without a session are redirected to the Auth Server
 *       login page (Authorization Code flow).</li>
 *   <li>API requests with a {@code Bearer} token are validated as JWT
 *       (Resource Server flow).</li>
 *   <li>Authenticated requests are proxied to downstream services with the
 *       token relayed via the {@code TokenRelay} filter.</li>
 * </ol>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Main security filter chain for the reactive gateway.
     *
     * <p>Permits unauthenticated access to health/actuator endpoints.
     * All other requests require authentication via OAuth2 login (browser)
     * or JWT bearer token (API client).</p>
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        http
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints
                        .pathMatchers(Routes.ACTUATOR_HEALTH, Routes.ACTUATOR_INFO).permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth endpoints are public (login/signup)
                        .pathMatchers(Routes.AUTH_PATTERN).permitAll()

                        // OAuth2 token endpoint is public (M2M client_credentials)
                        .pathMatchers(Routes.OAUTH2_TOKEN).permitAll()

                        // All other requests require authentication
                        .anyExchange().authenticated()
                )
                // OAuth2 Login (Authorization Code flow for browsers)
                .oauth2Login(Customizer.withDefaults())

                // OAuth2 Resource Server (JWT validation for API clients)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )

                // Logout
                .logout(Customizer.withDefaults())

                // CSRF disabled for API gateway (stateless JWT)
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }
}
