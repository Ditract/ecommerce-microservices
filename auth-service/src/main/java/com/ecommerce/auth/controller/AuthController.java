package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.request.LoginRequestDTO;
import com.ecommerce.auth.dto.request.RefreshTokenRequestDTO;
import com.ecommerce.auth.dto.request.RegisterRequestDTO;
import com.ecommerce.auth.dto.response.AuthResponseDTO;
import com.ecommerce.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /auth/login - Iniciar sesión
     *
     * Request Body:
     * {
     *   "email": "juan@example.com",
     *   "password": "Password123"
     * }
     *
     * Response: 200 OK
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {

        log.info("Solicitud REST para iniciar sesión del usuario: {}", loginRequest.getEmail());

        AuthResponseDTO response = authService.login(loginRequest);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/register - Registrar nuevo usuario
     *
     * Request Body:
     * {
     *   "email": "nuevo@example.com",
     *   "password": "Password123",
     *   "firstName": "Nuevo",
     *   "lastName": "Usuario",
     *   "phone": "+573001234567"
     * }
     *
     * Response: 201 Created
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO registerRequest) {

        log.info("Solicitud REST para registrar nuevo usuario: {}", registerRequest.getEmail());

        AuthResponseDTO response = authService.register(registerRequest);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {

        log.info("Solicitud REST para renovar el token");

        AuthResponseDTO response = authService.refreshToken(refreshRequest);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestBody Map<String, String> request) {

        String token = request.get("token");
        log.info("Solicitud REST para validar el token");

        boolean isValid = authService.validateToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /auth/me - Obtener información del usuario autenticado
     * Requiere token JWT válido en el header Authorization
     *
     * Response: 200 OK
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        log.info("Solicitud REST para obtener la información del usuario actual");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("email", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("authenticated", true);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        log.info("Solicitud REST para cerrar sesión");

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout exitoso. Por favor, elimina el token del cliente.");

        return ResponseEntity.ok(response);
    }
}
