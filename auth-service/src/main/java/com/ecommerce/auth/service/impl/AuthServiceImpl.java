package com.ecommerce.auth.service.impl;

import com.ecommerce.auth.client.UserServiceClient;
import com.ecommerce.auth.dto.request.LoginRequestDTO;
import com.ecommerce.auth.dto.request.RefreshTokenRequestDTO;
import com.ecommerce.auth.dto.request.RegisterRequestDTO;
import com.ecommerce.auth.dto.response.AuthResponseDTO;
import com.ecommerce.auth.entity.Credential;
import com.ecommerce.auth.exception.AuthenticationException;
import com.ecommerce.auth.exception.InvalidTokenException;
import com.ecommerce.auth.exception.ServiceUnavailableException;
import com.ecommerce.auth.exception.UserServiceException;
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
        log.info("Iniciando proceso de registro para el correo: {}", registerRequest.getEmail());

        Long userId = null;

        try {
            log.debug("Verificando disponibilidad del email...");
            Map<String, Boolean> existsResponse = userServiceClient.checkEmailExists(registerRequest.getEmail());
            if (Boolean.TRUE.equals(existsResponse.get("exists"))) {
                throw new AuthenticationException("El email ya está registrado");
            }

            //Crear usuario en user-service
            log.debug("Creando usuario en user-service...");
            Map<String, String> userRequest = new HashMap<>();
            userRequest.put("email", registerRequest.getEmail());
            userRequest.put("firstName", registerRequest.getFirstName());
            userRequest.put("lastName", registerRequest.getLastName());
            if (registerRequest.getPhone() != null) {
                userRequest.put("phone", registerRequest.getPhone());
            }

            UserDTO createdUser = userServiceClient.createUser(userRequest);
            userId = createdUser.getId();
            log.info("Usuario creado en user-service con ID: {}", userId);

            //Crear credenciales INACTIVAS en auth-service
            log.debug("Creando credenciales para userId: {}", userId);
            String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());
            credentialService.createCredential(userId, createdUser.getEmail(), hashedPassword);
            log.info("Credenciales creadas (inactivas) para userId: {}", userId);

            //Activar credenciales (solo si todo fue exitoso)
            log.debug("Activando credenciales para userId: {}", userId);
            credentialService.activateCredential(userId);
            log.info("Credenciales activadas para userId: {}", userId);

            //Cargar usuario completo con roles y generar tokens
            log.debug("Cargando UserDetails y generando tokens...");
            UserDetails userDetails = userDetailsService.loadUserByUsername(createdUser.getEmail());
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            log.info("Registro completado exitosamente para: {}", registerRequest.getEmail());

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
            // Error de validación, no requiere compensación
            log.error("Error de validación en registro: {}", e.getMessage());
            throw e;

        } catch (ServiceUnavailableException e) {
            // user-service no disponible, compensar si se crearon credenciales
            log.error("User-service no disponible durante registro: {}", e.getMessage());
            if (userId != null) {
                compensateRegistration(userId, "User-service no disponible");
            }
            throw new AuthenticationException("Servicio temporalmente no disponible. Intenta más tarde.", e);

        } catch (UserServiceException e) {
            // Error en user-service, compensar si se crearon credenciales
            log.error("Error en user-service durante registro: {}", e.getMessage());
            if (userId != null) {
                compensateRegistration(userId, "Error en user-service");
            }
            throw new AuthenticationException("Error al crear usuario: " + e.getMessage(), e);

        } catch (Exception e) {
            // Error inesperado, compensar si se crearon credenciales
            log.error("Error inesperado durante registro: {}", e.getMessage(), e);
            if (userId != null) {
                compensateRegistration(userId, "Error inesperado");
            }
            throw new AuthenticationException("Error en el servicio de registro", e);
        }
    }

    /**
     * Compensa el proceso de registro desactivando credenciales.
     */
    private void compensateRegistration(Long userId, String reason) {
        log.warn("COMPENSACIÓN INICIADA - userId: {}, razón: {}", userId, reason);

        try {
            // Desactivar credenciales
            credentialService.deactivateCredential(userId);
            log.info("Compensación exitosa: credenciales desactivadas para userId: {}", userId);

            // TODO (para después): Llamar a user-service para marcar usuario como inactivo
            // userServiceClient.deactivateUser(userId);

        } catch (Exception compensationError) {
            // Si la compensación falla, registrar para revisión manual
            log.error("FALLÓ LA COMPENSACIÓN para userId: {} - REQUIERE REVISIÓN MANUAL",
                    userId, compensationError);

            // TODO (para después): Enviar evento a Dead Letter Queue para revisión manual
            // eventPublisher.publish(new RegistrationCompensationFailedEvent(userId, reason));
        }
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Intento de inicio de sesión para: {}", loginRequest.getEmail());

        try {
            // Validar credenciales desde Auth DB
            if (!credentialService.validatePassword(loginRequest.getPassword(), loginRequest.getEmail())) {
                throw new AuthenticationException("Email o password incorrectos");
            }

            // Obtener credencial
            Credential credential = credentialService.findByEmail(loginRequest.getEmail());

            // Cargar UserDetails desde User Service
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

            // Generar tokens
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

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            UserDTO user = userServiceClient.getUserByEmail(email);

            String newAccessToken = jwtUtil.generateAccessToken(userDetails);

            log.info("Token de acceso renovado exitosamente para el correo: {}", email);

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