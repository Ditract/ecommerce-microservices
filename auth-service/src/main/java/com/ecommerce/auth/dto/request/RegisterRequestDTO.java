package com.ecommerce.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {

    @NotBlank(message = "Email es obligatorio")
    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "Email no puede exceder 100 caracteres")
    private String email;

    @NotBlank(message = "Password es obligatorio")
    @Size(min = 8, max = 100, message = "Password debe tener entre 8 y 100 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password debe contener al menos una mayúscula, una minúscula y un número"
    )
    private String password;

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