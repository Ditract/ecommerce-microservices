package com.ecommerce.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleCreateRequestDTO {

    @NotBlank(message = "Nombre del rol es obligatorio")
    @Size(min = 5, max = 50, message = "Nombre del rol debe tener entre 5 y 50 caracteres")
    @Pattern(
            regexp = "^ROLE_[A-Z_]+$",
            message = "Nombre del rol debe empezar con 'ROLE_' y contener solo mayúsculas y guiones bajos"
    )
    private String name;

    @Size(max = 255, message = "Descripción no puede exceder 255 caracteres")
    private String description;
}