/*
 * BookXShow - Authorization Server
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.authserver.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.bookxshow.authserver.entity.AppUser;
import com.bookxshow.authserver.common.Constants;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

/**
 * Generates JWT access tokens signed with the Authorization Server's RSA key pair.
 *
 * <p>Tokens are signed with {@code RS256} using the same {@link JWKSource}
 * that the Spring Authorization Server uses. This means resource servers can
 * validate these tokens via the {@code /oauth2/jwks} endpoint.</p>
 *
 * @since 1.0.0
 */
@Slf4j
@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;

    @Value("${bookxshow.auth.issuer-uri:http://localhost:9000}")
    private String issuerUri;

    @Value("${bookxshow.auth.access-token-ttl-minutes:60}")
    private long accessTokenTtlMinutes;

    public JwtTokenService(JWKSource<SecurityContext> jwkSource) {
        this.jwtEncoder = new NimbusJwtEncoder(jwkSource);
    }

    /**
     * Generates a JWT access token for the given user.
     *
     * <p>The token includes:</p>
     * <ul>
     *   <li>{@code sub} — username</li>
     *   <li>{@code scope} — OAuth2 scopes (bookxshow.read bookxshow.write)</li>
     *   <li>{@code roles} — user roles</li>
     *   <li>{@code email} — user email</li>
     *   <li>{@code iss} — issuer URI (matches the Authorization Server)</li>
     * </ul>
     *
     * @param user the authenticated user
     * @return signed JWT token string
     */
    public String generateAccessToken(AppUser user) {
        Instant now = Instant.now();

        // Determine scopes based on roles
        String scopes = user.getRoles().contains(Constants.ROLE_ADMIN)
                ? Constants.SCOPES_ADMIN_FULL
                : Constants.SCOPES_STANDARD;

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuerUri)
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenTtlMinutes, ChronoUnit.MINUTES))
                .claim(Constants.CLAIM_SCOPE, scopes)
                .claim(Constants.CLAIM_ROLES, user.getRoles())
                .claim(Constants.CLAIM_EMAIL, user.getEmail())
                .claim(Constants.CLAIM_USER_ID, user.getId())
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        log.debug("Generated JWT for user: {}", user.getUsername());
        return token;
    }
}
