package com.ecommerce.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear usuario en User Service via Feign.
 * coincide con UserCreateRequestDTO del microservicio User Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDTO {

    private String email;
    private String password;  
    private String firstName;
    private String lastName;
    private String phone;
}