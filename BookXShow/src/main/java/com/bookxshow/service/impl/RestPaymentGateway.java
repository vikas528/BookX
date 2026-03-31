/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * REST-based implementation of PaymentGateway that calls an external
 * payment processing service via WebClient.
 *
 * @since 1.0.0
 */
package com.bookxshow.service.impl;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.bookxshow.config.BookingProperties;
import com.bookxshow.config.OAuth2TokenService;
import com.bookxshow.enums.PaymentResult;
import com.bookxshow.service.PaymentGateway;

/**
 * Calls an external payment gateway over REST to process charges.
 *
 * <p>This is the production {@link PaymentGateway} strategy. It sends an
 * HTTP POST to the configured gateway URL and maps the response (or any
 * error / timeout) into a {@link PaymentResult}.</p>
 *
 * <p>Authentication is performed using OAuth2 client credentials. An
 * access token is obtained from the configured authorization server via
 * {@link OAuth2TokenService} and passed as a
 * bearer token in the {@code Authorization} header.</p>
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(name = "bookxshow.payment.stub-enabled", havingValue = "false")
public class RestPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(RestPaymentGateway.class);

    private final WebClient webClient;
    private final BookingProperties properties;
    private final OAuth2TokenService oAuth2TokenService;

    /**
     * Constructs the gateway with a pre-configured {@link WebClient},
     * application properties, and the OAuth2 token service.
     *
     * @param webClient          shared HTTP client
     * @param properties         application configuration
     * @param oAuth2TokenService OAuth2 client-credentials token provider
     */
    public RestPaymentGateway(WebClient webClient,
                              BookingProperties properties,
                              OAuth2TokenService oAuth2TokenService) {
        this.webClient = webClient;
        this.properties = properties;
        this.oAuth2TokenService = oAuth2TokenService;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends a POST request to the payment service endpoint configured
     * at {@code bookxshow.payment.gateway-url} +
     * {@code bookxshow.payment.charge-uri}. On network errors the
     * result defaults to {@link PaymentResult#TIMEOUT}; on HTTP 4xx/5xx
     * it defaults to {@link PaymentResult#FAILURE}.</p>
     */
    @Override
    public PaymentResult processPayment(String userId, BigDecimal amount) {
        String url = properties.getPayment().getGatewayUrl()
                + properties.getPayment().getChargeUri();

        // Use a structured Map to avoid JSON injection via userId
        Map<String, Object> bodyMap = Map.of("userId", userId, "amount", amount);

        try {
            String response = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> h.setBearerAuth(oAuth2TokenService.getAccessToken()))
                    .bodyValue(bodyMap)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Payment succeeded for user {} amount {}", userId, amount);
            return PaymentResult.SUCCESS;

        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().is4xxClientError() || ex.getStatusCode().is5xxServerError()) {
                log.warn("Payment failed for user {} — HTTP {}", userId, ex.getStatusCode());
                return PaymentResult.FAILURE;
            }
            log.error("Payment gateway error for user {}: {}", userId, ex.getMessage());
            return PaymentResult.TIMEOUT;
        } catch (Exception ex) {
            log.error("Payment gateway error for user {}: {}", userId, ex.getMessage());
            return PaymentResult.TIMEOUT;
        }
    }
}
