package com.ecommerce.auth.service.impl;


import com.ecommerce.auth.client.UserServiceClient;
import com.ecommerce.auth.dto.request.LoginRequestDTO;
import com.ecommerce.auth.dto.request.RefreshTokenRequestDTO;
import com.ecommerce.auth.dto.request.RegisterRequestDTO;
import com.ecommerce.auth.dto.response.AuthResponseDTO;
import com.ecommerce.auth.exception.AuthenticationException;
import com.ecommerce.auth.exception.InvalidTokenException;
import com.ecommerce.auth.model.UserDTO;
import com.ecommerce.auth.security.CustomUserDetails;
import com.ecommerce.auth.security.CustomUserDetailsService;
import com.ecommerce.auth.security.JwtUtil;
import com.ecommerce.auth.service.AuthService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            UserServiceClient userServiceClient,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.userServiceClient = userServiceClient;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Intento de inicio de sesión para el correo: {}", loginRequest.getEmail());

        try {
            // Verificar si el usuario existe
            Map<String, Boolean> existsResponse = userServiceClient.checkEmailExists(loginRequest.getEmail());
            if (!Boolean.TRUE.equals(existsResponse.get("exists"))) {
                log.warn("El email {} no está registrado", loginRequest.getEmail());
                throw new AuthenticationException("Email o password incorrectos");
            }

            // Obtener el password hasheado
            Map<String, String> passwordResponse = userServiceClient.getUserPasswordForAuth(
                    loginRequest.getEmail(),
                    "your-secret-key-123"
                    );
            String hashedPassword = passwordResponse.get("password");
            if (hashedPassword == null) {
                log.error("No se pudo obtener el password para {}", loginRequest.getEmail());
                throw new AuthenticationException("Error al validar las credenciales");
            }

            // Validar el password
            if (!passwordEncoder.matches(loginRequest.getPassword(), hashedPassword)) {
                log.warn("Password incorrecto para {}", loginRequest.getEmail());
                throw new AuthenticationException("Email o password incorrectos");
            }

            // Cargar UserDetails
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;


            // Generar tokens
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            log.info("Inicio de sesión exitoso para el correo: {}", loginRequest.getEmail());

            return AuthResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationInSeconds())
                    .userId(customUserDetails.getId())
                    .email(customUserDetails.getUsername())
                    .firstName(customUserDetails.getFirstName())
                    .lastName(customUserDetails.getLastName())
                    .roles(userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toSet()))
                    .build();

        } catch (AuthenticationException e) {
            throw e;
        } catch (FeignException e) {
            log.error("Error al comunicarse con el servicio de usuarios: {}", e.getMessage());
            throw new AuthenticationException("Error en el servicio de autenticación: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado durante el inicio de sesión: {}", e.getMessage(), e);
            throw new AuthenticationException("Error inesperado durante el login: " + e.getMessage(), e);
        }
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {
        log.info("Intento de registro para el correo: {}", registerRequest.getEmail());

        try {
            // Verificar email
            Map<String, Boolean> existsResponse = userServiceClient.checkEmailExists(registerRequest.getEmail());
            log.info("Respuesta de checkEmailExists para {}: {}", registerRequest.getEmail(), existsResponse);
            if (Boolean.TRUE.equals(existsResponse.get("exists"))) {
                log.warn("El email {} ya está registrado", registerRequest.getEmail());
                throw new AuthenticationException("El email ya está registrado");
            }

            // Hashear password
            String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());
            log.info("Password hasheado para {}: {}", registerRequest.getEmail(), hashedPassword);

            // Crear Map con datos del usuario
            Map<String, String> userRequest = new HashMap<>();
            userRequest.put("email", registerRequest.getEmail());
            userRequest.put("password", hashedPassword);
            userRequest.put("firstName", registerRequest.getFirstName());
            userRequest.put("lastName", registerRequest.getLastName());
            if (registerRequest.getPhone() != null) {
                userRequest.put("phone", registerRequest.getPhone());
            }

            // Crear usuario
            log.info("Creando usuario para {}", registerRequest.getEmail());
            UserDTO createdUser = userServiceClient.createUser(userRequest);
            log.info("Usuario creado: {}", createdUser.getEmail());

            // Generar tokens
            log.info("Cargando user details para {}", createdUser.getEmail());
            UserDetails userDetails = userDetailsService.loadUserByUsername(createdUser.getEmail());
            log.info("Generando tokens para {}", createdUser.getEmail());
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            return AuthResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationInSeconds())
                    .userId(createdUser.getId())
                    .email(createdUser.getEmail())
                    .firstName(createdUser.getFirstName())
                    .lastName(createdUser.getLastName())
                    .roles(userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toSet()))
                    .build();

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error durante el registro para {}: {}", registerRequest.getEmail(), e.getMessage(), e);
            throw new AuthenticationException("Error en el servicio de registro: " + e.getMessage(), e);
        }
    }

    @Override
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest) {
        log.info("Solicitud de renovación de token recibida");

        try {
            String refreshToken = refreshRequest.getRefreshToken();


            if (!jwtUtil.validateToken(refreshToken)) {
                log.warn("Token de actualización inválido");
                throw new InvalidTokenException("Refresh token inválido o expirado");
            }


            String email = jwtUtil.extractEmail(refreshToken);

            //  Cargar usuario
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);


            UserDTO user = userServiceClient.getUserByEmail(email);


            String newAccessToken = jwtUtil.generateAccessToken(userDetails);

            log.info("Token de acceso renovado exitosamente para el correo: {}", email);

            // Construir la respuesta
            return AuthResponseDTO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationInSeconds())
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .roles(userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toSet()))
                    .build();

        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al renovar el token: {}", e.getMessage());
            throw new InvalidTokenException("Error al renovar el token", e);
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            log.error("Error en la validación del token: {}", e.getMessage());
            return false;
        }
    }
}
