package com.ecommerce.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequestDTO {

    @NotBlank(message = "Email es obligatorio")
    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "Email no puede exceder 100 caracteres")
    private String email;

    @NotBlank(message = "Nombre es obligatorio")
    @Size(min = 2, max = 50, message = "Nombre debe tener entre 2 y 50 caracteres")
    private String firstName;

    @NotBlank(message = "Apellido es obligatorio")
    @Size(min = 2, max = 50, message = "Apellido debe tener entre 2 y 50 caracteres")
    private String lastName;

    @Pattern(
            regexp = "^\\+?[0-9]{10,20}$",
            message = "Teléfono debe tener entre 10 y 20 dígitos"
    )
    private String phone;
}