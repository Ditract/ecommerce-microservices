package com.ecommerce.auth.service.impl;


import com.ecommerce.auth.client.UserServiceClient;
import com.ecommerce.auth.dto.request.LoginRequestDTO;
import com.ecommerce.auth.dto.request.RefreshTokenRequestDTO;
import com.ecommerce.auth.dto.request.RegisterRequestDTO;
import com.ecommerce.auth.dto.response.AuthResponseDTO;
import com.ecommerce.auth.entity.Credential;
import com.ecommerce.auth.exception.AuthenticationException;
import com.ecommerce.auth.exception.InvalidTokenException;
import com.ecommerce.auth.model.UserDTO;
import com.ecommerce.auth.security.service.CustomUserDetails;
import com.ecommerce.auth.security.service.CustomUserDetailsService;
import com.ecommerce.auth.security.jwt.JwtUtil;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.auth.service.CredentialService;
import lombok.extern.slf4j.Slf4j;
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

    private final CustomUserDetailsService userDetailsService;
    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CredentialService credentialService;

    public AuthServiceImpl(
            CustomUserDetailsService userDetailsService,
            UserServiceClient userServiceClient,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            CredentialService credentialService) {
        this.userDetailsService = userDetailsService;
        this.userServiceClient = userServiceClient;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.credentialService = credentialService;
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {
        log.info("Intento de registro para el correo: {}", registerRequest.getEmail());

        try {
            // 1. Verificar email
            Map<String, Boolean> existsResponse = userServiceClient.checkEmailExists(registerRequest.getEmail());
            if (Boolean.TRUE.equals(existsResponse.get("exists"))) {
                throw new AuthenticationException("El email ya está registrado");
            }

            // 2. Crear usuario SIN password en User Service
            Map<String, String> userRequest = new HashMap<>();
            userRequest.put("email", registerRequest.getEmail());
            userRequest.put("firstName", registerRequest.getFirstName());
            userRequest.put("lastName", registerRequest.getLastName());
            if (registerRequest.getPhone() != null) {
                userRequest.put("phone", registerRequest.getPhone());
            }

            UserDTO createdUser = userServiceClient.createUser(userRequest);
            log.info("Usuario creado con ID: {}", createdUser.getId());

            // 3. Crear credenciales en Auth Service
            String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());
            credentialService.createCredential(
                    createdUser.getId(),
                    createdUser.getEmail(),
                    hashedPassword
            );
            log.info("Credenciales creadas para userId: {}", createdUser.getId());

            // 4. Generar tokens
            UserDetails userDetails = userDetailsService.loadUserByUsername(createdUser.getEmail());
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

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
        } catch (Exception e) {
            log.error("Error durante el registro: {}", e.getMessage(), e);
            throw new AuthenticationException("Error en el servicio de registro", e);
        }
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Intento de inicio de sesión para: {}", loginRequest.getEmail());

        try {
            // 1. Validar credenciales desde Auth DB
            if (!credentialService.validatePassword(loginRequest.getPassword(), loginRequest.getEmail())) {
                throw new AuthenticationException("Email o password incorrectos");
            }

            // 2. Obtener credencial
            Credential credential = credentialService.findByEmail(loginRequest.getEmail());

            // 3. Cargar UserDetails desde User Service
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

            // 4. Generar tokens
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            log.info("Inicio de sesión exitoso para: {}", loginRequest.getEmail());

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
        } catch (Exception e) {
            log.error("Error durante login: {}", e.getMessage());
            throw new AuthenticationException("Error en el servicio de autenticación", e);
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
