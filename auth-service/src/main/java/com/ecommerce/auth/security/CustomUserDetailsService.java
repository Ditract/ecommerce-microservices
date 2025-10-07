package com.ecommerce.auth.security;

import com.ecommerce.auth.client.UserServiceClient;
import com.ecommerce.auth.model.UserDTO;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación personalizada de UserDetailsService.
 * Carga usuarios desde User Service vía Feign.
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    public CustomUserDetailsService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Cargando usuario por correo electrónico: {}", email);

        try {
            // Llama a User Service para obtener el usuario
            UserDTO userDTO = userServiceClient.getUserByEmail(email);

            // Verifica que el usuario esté activo
            if (!userDTO.getIsActive()) {
                log.warn("La cuenta de usuario está inactiva: {}", email);
                throw new UsernameNotFoundException("La cuenta de usuario está inactiva");
            }


            Set<GrantedAuthority> authorities = userDTO.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toSet());

            log.info("Usuario cargado exitosamente: {} con roles: {}", email, authorities);

            return User.builder()
                    .username(userDTO.getEmail())
                    .password(userDTO.getPassword())  // Password hasheado
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(!userDTO.getIsActive())
                    .build();

        } catch (FeignException.NotFound e) {
            log.error("Usuario no encontrado: {}", email);
            throw new UsernameNotFoundException("Usuario no encontrado con el correo electrónico: " + email);
        } catch (FeignException e) {
            log.error("Error al comunicarse con el User Service: {}", e.getMessage());
            throw new RuntimeException("Error al cargar el usuario desde el User Service", e);
        }
    }
}
