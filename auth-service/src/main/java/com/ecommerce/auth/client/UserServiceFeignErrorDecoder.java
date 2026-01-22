package com.ecommerce.auth.client;

import com.ecommerce.auth.exception.ServiceUnavailableException;
import com.ecommerce.auth.exception.UserNotFoundException;
import com.ecommerce.auth.exception.UserServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Decodificador personalizado de errores para user-service.
 * Convierte respuestas HTTP de error en excepciones específicas del dominio.
 */
@Slf4j
public class UserServiceFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus statusCode = HttpStatus.valueOf(response.status());
        String responseBody = extractResponseBody(response);

        log.error("Error en llamada a user-service [{}]: Status={}, Body={}",
                methodKey, response.status(), responseBody);

        // Manejo específico por código de estado
        return switch (statusCode) {
            case NOT_FOUND -> new UserNotFoundException(
                    "Usuario no encontrado en user-service: " + responseBody
            );
            case BAD_REQUEST -> new UserServiceException(
                    "Solicitud inválida a user-service: " + responseBody,
                    400
            );
            case CONFLICT -> new UserServiceException(
                    "Conflicto en user-service (posible email duplicado): " + responseBody,
                    409
            );
            case SERVICE_UNAVAILABLE, GATEWAY_TIMEOUT, BAD_GATEWAY -> new ServiceUnavailableException(
                    "User-service no disponible temporalmente: " + statusCode
            );
            case INTERNAL_SERVER_ERROR -> new UserServiceException(
                    "Error interno en user-service: " + responseBody,
                    500
            );
            default ->
                // Para otros códigos, delegar al decodificador por defecto
                    defaultErrorDecoder.decode(methodKey, response);
        };
    }

    /**
     * Extrae el cuerpo de la respuesta de manera segura
     */
    private String extractResponseBody(Response response) {
        try {
            if (response.body() != null) {
                InputStream inputStream = response.body().asInputStream();
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("No se pudo leer el cuerpo de la respuesta de error", e);
        }
        return "Sin cuerpo de respuesta";
    }
}