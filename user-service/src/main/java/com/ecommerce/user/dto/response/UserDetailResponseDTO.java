package com.ecommerce.user.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailResponseDTO {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean isActive;
    private Set<RoleResponseDTO> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}