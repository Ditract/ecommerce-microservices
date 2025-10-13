package com.ecommerce.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignRoleRequestDTO {

    @NotNull(message = "ID del usuario es obligatorio")
    @Positive(message = "ID del usuario debe ser positivo")
    private Long userId;

    @NotNull(message = "ID del rol es obligatorio")
    @Positive(message = "ID del rol debe ser positivo")
    private Long roleId;
}