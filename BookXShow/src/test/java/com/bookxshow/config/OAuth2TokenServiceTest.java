/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Unit tests for OAuth2TokenService.
 *
 * @since 1.0.0
 */
package com.bookxshow.config;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OAuth2TokenService}.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class OAuth2TokenServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private BookingProperties properties;
    private OAuth2TokenService tokenService;

    @BeforeEach
    void setUp() {
        properties = new BookingProperties();
        BookingProperties.Auth auth = new BookingProperties.Auth();
        auth.setTokenUrl("http://localhost:9999/oauth2/token");
        auth.setClientId("test-client");
        auth.setClientSecret("test-secret");
        auth.setScope("bookxshow.read");
        properties.setAuth(auth);

        tokenService = new OAuth2TokenService(webClient, properties);
    }

    @SuppressWarnings("unchecked")
    private void stubPostChain(Mono<Map> responseMono) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(any(MediaType.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(responseMono);
    }

    @Test
    @DisplayName("T01 - Successful token acquisition returns access token")
    @SuppressWarnings("unchecked")
    void successfulTokenAcquisition() {
        Map<String, Object> tokenResponse = Map.of(
                "access_token", "test-token-123",
                "expires_in", 3600);
        stubPostChain(Mono.just(tokenResponse));

        String token = tokenService.getAccessToken();

        assertEquals("test-token-123", token);
        verify(webClient).post();
    }

    @Test
    @DisplayName("T02 - Cached token is returned without HTTP call")
    @SuppressWarnings("unchecked")
    void cachedTokenReturnedWithoutHttpCall() {
        Map<String, Object> tokenResponse = Map.of(
                "access_token", "cached-token",
                "expires_in", 3600);
        stubPostChain(Mono.just(tokenResponse));

        String first = tokenService.getAccessToken();
        String second = tokenService.getAccessToken();

        assertEquals("cached-token", first);
        assertEquals("cached-token", second);
        // Only one HTTP call — second call used cache
        verify(webClient, times(1)).post();
    }

    @Test
    @DisplayName("T03 - Missing access_token field throws IllegalStateException")
    @SuppressWarnings("unchecked")
    void missingAccessToken_throwsException() {
        Map<String, Object> tokenResponse = Map.of("token_type", "bearer");
        stubPostChain(Mono.just(tokenResponse));

        assertThrows(IllegalStateException.class, () -> tokenService.getAccessToken());
    }

    @Test
    @DisplayName("T04 - Null response body throws IllegalStateException")
    @SuppressWarnings("unchecked")
    void nullResponseBody_throwsException() {
        stubPostChain(Mono.justOrEmpty(null));

        assertThrows(IllegalStateException.class, () -> tokenService.getAccessToken());
    }

    @Test
    @DisplayName("T05 - WebClient error throws IllegalStateException")
    @SuppressWarnings("unchecked")
    void webClientError_throwsException() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(any(MediaType.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(
                Mono.error(new WebClientResponseException(401, "Unauthorized", null, null, null)));

        assertThrows(IllegalStateException.class, () -> tokenService.getAccessToken());
    }

    @Test
    @DisplayName("T06 - Default expires_in of 3600 when not provided")
    @SuppressWarnings("unchecked")
    void defaultExpiresIn_whenNotProvided() {
        Map<String, Object> tokenResponse = Map.of("access_token", "token-no-expiry");
        stubPostChain(Mono.just(tokenResponse));

        String token = tokenService.getAccessToken();

        assertEquals("token-no-expiry", token);
    }
}
