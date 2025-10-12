package com.ecommerce.auth.security.jwt;

import com.ecommerce.auth.security.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que intercepta cada request para validar el JWT.
 * hereda de OncePerRequestFilter para garantizar que se ejecuta solo una vez por request.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        //  Extraer el header Authorization
        final String authorizationHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        //  Verificar que el header existe y empieza con "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Remueve "Bearer "

            try {

                email = jwtUtil.extractEmail(jwt);
                log.debug("JWT extraído para el correo: {}", email);
            } catch (ExpiredJwtException e) {
                log.error("El token JWT ha expirado: {}", e.getMessage());
            } catch (Exception e) {
                log.error("Error al extraer el correo del JWT: {}", e.getMessage());
            }
        }


        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);


                if (jwtUtil.validateToken(jwt, userDetails)) {
                    log.debug("El JWT es válido para el correo: {}", email);


                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );


                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    log.info("Usuario autenticado correctamente: {}", email);
                } else {
                    log.warn("Falló la validación del JWT para el correo: {}", email);
                }
            } catch (Exception e) {
                log.error("No se puede establecer la autenticación del usuario: {}", e.getMessage());
            }
        }


        filterChain.doFilter(request, response);
    }
}
