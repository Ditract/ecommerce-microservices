package com.ecommerce.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;    // Segundos hasta que expire el access token

    // Información del usuario
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles;
}