/*
 * BookXShow - API Gateway
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.gateway.exception;

import com.bookxshow.gateway.dto.ErrorResponse;
import com.bookxshow.gateway.common.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway-wide exception handler for the reactive WebFlux pipeline.
 *
 * <p>Intercepts all unhandled exceptions and routing/security errors and
 * serialises them into a consistent {@link ErrorResponse} JSON body so that
 * every error returned by the gateway follows the same contract as the
 * downstream services.</p>
 *
 * <p>Registered at {@code @Order(-2)} to take priority over Spring Boot's
 * default {@code DefaultErrorWebExceptionHandler} (order -1).</p>
 *
 * @since 1.0.0
 */
@Slf4j
@Order(-2)
@Component
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalErrorWebExceptionHandler() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = resolveStatus(ex);
        String errorStatus = String.valueOf(status.value());
        String message = resolveMessage(ex, status);
        String path = exchange.getRequest().getPath().value();

        log.error("Gateway error [{}] at {}: {}", status.value(), path, ex.getMessage());

        ErrorResponse body = ErrorResponse.builder()
                .errorStatus(errorStatus)
                .message(message)
                .path(path)
                .build();

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = ("{\"errorStatus\":\"" + Constants.ERROR_STATUS_FALLBACK + "\",\"message\":\"" + Constants.ERR_SERIALISATION + "\"}").getBytes();
        }
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    // ── Helpers ────────────────────────────────────────────────────────

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            return HttpStatus.resolve(rse.getStatusCode().value()) != null
                    ? HttpStatus.resolve(rse.getStatusCode().value())
                    : HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(Throwable ex, HttpStatus status) {
        if (ex instanceof ResponseStatusException rse && rse.getReason() != null) {
            return rse.getReason();
        }
        return ex.getMessage() != null ? ex.getMessage() : status.getReasonPhrase();
    }
}
