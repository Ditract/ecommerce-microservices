package com.ecommerce.auth.service.impl;


import com.ecommerce.auth.client.UserServiceClient;
import com.ecommerce.auth.dto.request.LoginRequestDTO;
import com.ecommerce.auth.dto.request.RefreshTokenRequestDTO;
import com.ecommerce.auth.dto.request.RegisterRequestDTO;
import com.ecommerce.auth.dto.request.UserCreateDTO;
import com.ecommerce.auth.dto.response.AuthResponseDTO;
import com.ecommerce.auth.exception.AuthenticationException;
import com.ecommerce.auth.exception.InvalidTokenException;
import com.ecommerce.auth.model.UserDTO;
import com.ecommerce.auth.security.CustomUserDetailsService;
import com.ecommerce.auth.security.JwtUtil;
import com.ecommerce.auth.service.AuthService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );


            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Obtener la información completa del usuario desde User Service
            UserDTO user = userServiceClient.getUserByEmail(loginRequest.getEmail());

            // Generar tokens JWT
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            log.info("Inicio de sesión exitoso para el correo: {}", loginRequest.getEmail());

            // Construir la respuesta
            return AuthResponseDTO.builder()
                    .accessToken(accessToken)
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

        } catch (BadCredentialsException e) {
            log.error("Credenciales inválidas para el correo: {}", loginRequest.getEmail());
            throw new AuthenticationException("Email o password incorrectos");
        } catch (FeignException e) {
            log.error("Error al comunicarse con el servicio de usuarios: {}", e.getMessage());
            throw new AuthenticationException("Error en el servicio de autenticación", e);
        } catch (Exception e) {
            log.error("Error inesperado durante el inicio de sesión: {}", e.getMessage());
            throw new AuthenticationException("Error inesperado durante el login", e);
        }
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {
        log.info("Intento de registro para el correo: {}", registerRequest.getEmail());

        try {
            Boolean emailExists = userServiceClient.existsByEmail(registerRequest.getEmail());
            if (emailExists) {
                log.warn("El correo ya existe: {}", registerRequest.getEmail());
                throw new AuthenticationException("El email ya está registrado");
            }


            String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());


            UserCreateDTO userCreateDTO = UserCreateDTO.builder()
                    .email(registerRequest.getEmail())
                    .password(hashedPassword)
                    .firstName(registerRequest.getFirstName())
                    .lastName(registerRequest.getLastName())
                    .phone(registerRequest.getPhone())
                    .build();

            // Crear usuario en User Service
            UserDTO createdUser = userServiceClient.createUser(userCreateDTO);

            log.info("Usuario registrado exitosamente: {}", createdUser.getEmail());


            UserDetails userDetails = userDetailsService.loadUserByUsername(createdUser.getEmail());

            //  Generar tokens JWT
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Construir la respuesta
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

        } catch (FeignException.Conflict e) {
            log.error("El correo ya existe: {}", registerRequest.getEmail());
            throw new AuthenticationException("El email ya está registrado");
        } catch (FeignException e) {
            log.error("Error al comunicarse con el servicio de usuarios: {}", e.getMessage());
            throw new AuthenticationException("Error en el servicio de registro", e);
        } catch (Exception e) {
            log.error("Error inesperado durante el registro: {}", e.getMessage());
            throw new AuthenticationException("Error inesperado durante el registro", e);
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
