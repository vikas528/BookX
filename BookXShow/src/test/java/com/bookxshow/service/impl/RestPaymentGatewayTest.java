/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Unit tests for RestPaymentGateway.
 *
 * @since 1.0.0
 */
package com.bookxshow.service.impl;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.bookxshow.config.BookingProperties;
import com.bookxshow.config.OAuth2TokenService;
import com.bookxshow.enums.PaymentResult;

import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RestPaymentGateway}.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class RestPaymentGatewayTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private OAuth2TokenService oAuth2TokenService;

    private BookingProperties properties;
    private RestPaymentGateway paymentGateway;

    @BeforeEach
    void setUp() {
        properties = new BookingProperties();
        BookingProperties.Payment payment = new BookingProperties.Payment();
        payment.setGatewayUrl("http://localhost:8082");
        payment.setChargeUri("/api/v1/payments/charge");
        properties.setPayment(payment);

        paymentGateway = new RestPaymentGateway(webClient, properties, oAuth2TokenService);

        lenient().when(oAuth2TokenService.getAccessToken()).thenReturn("test-bearer-token");
    }

    @SuppressWarnings("unchecked")
    private void stubPostChain(Mono<String> responseMono) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(any(MediaType.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(responseMono);
    }

    @SuppressWarnings("unchecked")
    private void stubPostChainError(Throwable error) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(any(MediaType.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(error));
    }

    @Test
    @DisplayName("T01 - Successful payment returns SUCCESS")
    void successfulPayment_returnSuccess() {
        stubPostChain(Mono.just("{\"status\":\"ok\"}"));

        PaymentResult result = paymentGateway.processPayment("alice", BigDecimal.valueOf(99.99));

        assertEquals(PaymentResult.SUCCESS, result);
        verify(webClient).post();
    }

    @Test
    @DisplayName("T02 - HTTP 4xx error returns FAILURE")
    void http4xxError_returnsFailure() {
        stubPostChainError(WebClientResponseException.create(
                HttpStatus.BAD_REQUEST.value(), "Bad Request",
                HttpHeaders.EMPTY, new byte[0], null));

        PaymentResult result = paymentGateway.processPayment("bob", BigDecimal.valueOf(50));

        assertEquals(PaymentResult.FAILURE, result);
    }

    @Test
    @DisplayName("T03 - HTTP 5xx error returns FAILURE")
    void http5xxError_returnsFailure() {
        stubPostChainError(WebClientResponseException.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                HttpHeaders.EMPTY, new byte[0], null));

        PaymentResult result = paymentGateway.processPayment("charlie", BigDecimal.valueOf(25));

        assertEquals(PaymentResult.FAILURE, result);
    }

    @Test
    @DisplayName("T04 - Network error returns TIMEOUT")
    void networkError_returnsTimeout() {
        stubPostChainError(new RuntimeException("Connection refused"));

        PaymentResult result = paymentGateway.processPayment("dave", BigDecimal.valueOf(10));

        assertEquals(PaymentResult.TIMEOUT, result);
    }

    @Test
    @DisplayName("T05 - Uses correct payment URL from properties")
    void usesCorrectPaymentUrl() {
        stubPostChain(Mono.just("ok"));

        paymentGateway.processPayment("alice", BigDecimal.ONE);

        verify(requestBodyUriSpec).uri("http://localhost:8082/api/v1/payments/charge");
    }
}
