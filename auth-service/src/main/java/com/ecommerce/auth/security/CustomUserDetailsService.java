package com.ecommerce.auth.security;

import com.ecommerce.auth.client.UserServiceClient;
import com.ecommerce.auth.model.UserDTO;
import com.ecommerce.auth.model.RoleDTO;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

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
            UserDTO userDTO = userServiceClient.getUserByEmail(email);
            if (userDTO == null) {
                log.error("Usuario no encontrado: {}", email);
                throw new UsernameNotFoundException("Usuario no encontrado con el correo electrónico: " + email);
            }

            if (!userDTO.getIsActive()) {
                log.warn("La cuenta de usuario está inactiva: {}", email);
                throw new UsernameNotFoundException("La cuenta de usuario está inactiva");
            }

            Set<GrantedAuthority> authorities = userDTO.getRoles() != null
                    ? userDTO.getRoles().stream()
                    .filter(role -> role != null && role.getName() != null)
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toSet())
                    : Collections.emptySet();

            log.info("Usuario cargado exitosamente: {} con roles: {}", email, authorities);

            return new CustomUserDetails(
                    userDTO.getEmail(),
                    userDTO.getId(),
                    userDTO.getFirstName(),
                    userDTO.getLastName(),
                    authorities,
                    userDTO.getIsActive()
            );

        } catch (FeignException.NotFound e) {
            log.error("Usuario no encontrado: {}", email);
            throw new UsernameNotFoundException("Usuario no encontrado con el correo electrónico: " + email);
        } catch (FeignException e) {
            log.error("Error al comunicarse con el User Service: {}", e.getMessage());
            throw new RuntimeException("Error al cargar el usuario desde el User Service", e);
        }
    }

}