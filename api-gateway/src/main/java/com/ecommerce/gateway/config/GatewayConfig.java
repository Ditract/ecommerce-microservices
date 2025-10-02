package com.ecommerce.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion adicional del Gateway usando Java en lugar de YAML.
 */
@Configuration
public class GatewayConfig {

    /**
     * Define rutas mediante codigo (alternativa a application.yml).
     * Por si se  necesitaa logica condicional o rutas dinamicas.
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("eureka-dashboard", r -> r
                        .path("/eureka")
                        .uri("http://localhost:8761"))


                .route("gateway-health", r -> r
                        .path("/actuator/**")
                        .uri("forward:///actuator"))

                .build();
    }
}