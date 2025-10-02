package com.ecommerce.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Filtro global que loguea informacion de cada request que pasa por el Gateway.
 * Se ejecuta ANTES de redirigir al microservicio correspondiente.
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().toString();
        String method = exchange.getRequest().getMethod().toString();
        String remoteAddress = exchange.getRequest().getRemoteAddress().toString();

        log.info("=================================================");
        log.info("Gateway Request: {} {}", method, path);
        log.info("Remote Address: {}", remoteAddress);
        log.info("Timestamp: {}", LocalDateTime.now());
        log.info("=================================================");


        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            int statusCode = exchange.getResponse().getStatusCode().value();
            log.info("Response Status: {}", statusCode);
        }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}