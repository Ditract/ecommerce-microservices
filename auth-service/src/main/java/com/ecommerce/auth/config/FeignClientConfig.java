package com.ecommerce.auth.config;

import com.ecommerce.auth.client.UserServiceFeignErrorDecoder;
import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración personalizada para clientes Feign
 */
@Configuration
public class FeignClientConfig {

    /**
     * ErrorDecoder personalizado para user-service
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new UserServiceFeignErrorDecoder();
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}