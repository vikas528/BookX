/*
 * BookXShow - Authorization Server
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Spring Authorization Server configuration — registered clients, token settings,
 * JWK source, and endpoint configuration.
 *
 * @since 1.0.0
 */
package com.bookxshow.authserver.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import com.bookxshow.authserver.common.Routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

/**
 * Configures the Spring Authorization Server with registered OAuth2 clients,
 * RSA key pairs for JWT signing, and database-backed user authentication.
 *
 * <h3>Security Filter Chain Order</h3>
 * <ol>
 *   <li><strong>@Order(0)</strong> — Public REST API endpoints ({@code /api/auth/**})</li>
 *   <li><strong>@Order(1)</strong> — Authorization Server protocol endpoints</li>
 *   <li><strong>@Order(2)</strong> — Default form login for OAuth2 flows</li>
 * </ol>
 *
 * <h3>Password Encoding</h3>
 * <p>BCrypt with strength 12 — provides strong hashing with ~300ms per hash.</p>
 *
 * <h3>JWT Signing</h3>
 * <p>RSA 4096-bit key pair with RS256 algorithm.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    @Value("${bookxshow.auth.issuer-uri:http://localhost:9000}")
    private String issuerUri;

    /**
     * Security filter chain for public REST API endpoints ({@code /api/auth/**}).
     * No authentication required. CSRF disabled for stateless API.
     */
    @Bean
    @Order(0)
    public SecurityFilterChain authApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(Routes.AUTH_PATTERN)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    /**
     * Security filter chain for the Authorization Server protocol endpoints
     * (token, authorize, jwks, etc.).
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());

        http
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        ))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * Default security filter chain for the login form and actuator endpoints.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(Routes.ACTUATOR_HEALTH, Routes.ACTUATOR_INFO).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    /**
     * BCrypt password encoder with strength 12.
     *
     * <p>Strength 12 results in approximately 300ms per hash, providing
     * excellent resistance against brute-force attacks while remaining
     * practical for web application use.</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * JDBC-backed client repository — reads/writes to the
     * {@code oauth2_registered_client} table.
     *
     * <p>Default clients are seeded on startup by
     * {@link DefaultClientInitializer}.</p>
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    /**
     * RSA 4096-bit key pair used to sign JWTs. The public key is exposed via
     * the {@code /oauth2/jwks} endpoint so that resource servers can verify tokens.
     *
     * <p>4096-bit RSA provides long-term security well beyond 2030.</p>
     *
     * <p><strong>Production note:</strong> Load keys from a keystore or vault
     * instead of generating them at startup.</p>
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(4096);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key pair", ex);
        }
    }

    /**
     * Decodes JWTs using the same key material used for signing.
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * Authorization Server settings with externalized issuer URI.
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(issuerUri)
                .build();
    }
}
