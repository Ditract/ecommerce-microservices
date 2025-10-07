package com.ecommerce.auth.filter;

import com.ecommerce.auth.security.CustomUserDetailsService;
import com.ecommerce.auth.security.JwtUtil;
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
                log.debug("JWT extracted for email: {}", email);
            } catch (ExpiredJwtException e) {
                log.error("JWT Token has expired: {}", e.getMessage());
            } catch (Exception e) {
                log.error("Error extracting email from JWT: {}", e.getMessage());
            }
        }


        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);


                if (jwtUtil.validateToken(jwt, userDetails)) {
                    log.debug("JWT is valid for email: {}", email);


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

                    log.info("User authenticated successfully: {}", email);
                } else {
                    log.warn("JWT validation failed for email: {}", email);
                }
            } catch (Exception e) {
                log.error("Cannot set user authentication: {}", e.getMessage());
            }
        }


        filterChain.doFilter(request, response);
    }
}