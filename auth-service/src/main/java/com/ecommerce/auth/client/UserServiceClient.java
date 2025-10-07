package com.ecommerce.auth.client;

import com.ecommerce.auth.model.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client para comunicarse con user uervice.
 *
 * @FeignClient:
 * - name: Nombre del servicio en eureka (USER-SERVICE)
 * - path: Path base de los endpoints
 */
@FeignClient(
        name = "USER-SERVICE",
        path = "/users"
)
public interface UserServiceClient {

    /**
     * Obtiene un usuario por email.
     * Llamada: GET http://USER-SERVICE/users/email/{email}
     */
    @GetMapping("/email/{email}")
    UserDTO getUserByEmail(@PathVariable("email") String email);

    /**
     * Obtiene un usuario por ID.
     * Llamada: GET http://USER-SERVICE/users/{id}
     */
    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    /**
     * Verifica si un email existe.
     * Llamada: GET http://USER-SERVICE/users/exists?email={email}
     */
    @GetMapping("/exists")
    Boolean existsByEmail(@RequestParam("email") String email);

    /**
     * Crea un nuevo usuario.
     * Llamada: POST http://USER-SERVICE/users
     */
    @PostMapping
    UserDTO createUser(@RequestBody UserDTO user);
}