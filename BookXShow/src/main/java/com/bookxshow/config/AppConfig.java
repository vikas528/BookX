/*
 * BookXShow - Seat Booking Service
 * Copyright (c) 2026 BookXShow. All rights reserved.
 *
 * Application-level Spring configuration (beans, WebClient, etc.).
 *
 * @since 1.0.0
 */
package com.bookxshow.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import com.bookxshow.service.impl.AdminServiceClientImpl;
import com.bookxshow.service.impl.RestPaymentGateway;

/**
 * Central configuration class that registers infrastructure beans used
 * across the application.
 *
 * @author BookXShow Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class AppConfig {

    /**
     * Provides a pre-configured {@link WebClient} with sensible
     * connection and read timeouts.
     *
     * <p>Used by both the {@link RestPaymentGateway}
     * and the {@link AdminServiceClientImpl}.</p>
     *
     * @return a shared WebClient instance
     */
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(30))
                        .addHandlerLast(new WriteTimeoutHandler(30)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
