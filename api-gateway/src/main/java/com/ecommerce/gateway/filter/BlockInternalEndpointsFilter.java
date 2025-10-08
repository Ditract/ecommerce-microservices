package com.ecommerce.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class BlockInternalEndpointsFilter implements GlobalFilter, Ordered {

    private static final String INTERNAL_PATH = "/api/users/internal/";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        if (path.startsWith(INTERNAL_PATH)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getHeaders().add("X-Internal-Blocked", "true");
            return response.setComplete();  // Corta el flujo, no forward
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;  // Alta prioridad (ejecuta primero)
    }
}