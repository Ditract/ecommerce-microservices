package com.ecommerce.user.controller;

import com.ecommerce.user.dto.request.AssignRoleRequestDTO;
import com.ecommerce.user.dto.request.UserCreateRequestDTO;
import com.ecommerce.user.dto.request.UserUpdateRequestDTO;
import com.ecommerce.user.dto.response.UserDetailResponseDTO;
import com.ecommerce.user.dto.response.UserResponseDTO;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //Crear nuevo usuario
    @PostMapping
    public ResponseEntity<UserDetailResponseDTO> createUser(
            @Valid @RequestBody UserCreateRequestDTO requestDTO) {

        log.info("Solicitud REST para crear usuario: {}", requestDTO.getEmail());

        UserDetailResponseDTO createdUser = userService.createUser(requestDTO);

        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    //Obtener usuario por id
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailResponseDTO> getUserById(@PathVariable Long id) {
        log.info("Solicitud REST para obtener usuario por ID: {}", id);

        UserDetailResponseDTO user = userService.getUserById(id);

        return ResponseEntity.ok(user);
    }

    //obtener usuario por email
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDetailResponseDTO> getUserByEmail(@PathVariable String email) {
        log.info("Solicitud REST para obtener usuario por email: {}", email);

        UserDetailResponseDTO user = userService.getUserByEmail(email);

        return ResponseEntity.ok(user);
    }

    //obtener todos los usuarios
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.info("Solicitud REST para obtener todos los usuarios");

        List<UserResponseDTO> users = userService.getAllUsers();

        return ResponseEntity.ok(users);
    }

    //Obtener usuarios activos
    @GetMapping("/active")
    public ResponseEntity<List<UserResponseDTO>> getActiveUsers() {
        log.info("Solicitud REST para obtener usuarios activos");

        List<UserResponseDTO> users = userService.getActiveUsers();

        return ResponseEntity.ok(users);
    }

    //Buscar usuarios
    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchUsers(
            @RequestParam String term) {

        log.info("Solicitud REST para buscar usuarios con término: {}", term);

        List<UserResponseDTO> users = userService.searchUsers(term);

        return ResponseEntity.ok(users);
    }

    //Obtener usuarios por rol
    @GetMapping("/role/{roleName}")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@PathVariable String roleName) {
        log.info("Solicitud REST para obtener usuarios con rol: {}", roleName);

        List<UserResponseDTO> users = userService.getUsersByRole(roleName);

        return ResponseEntity.ok(users);
    }

    //Actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<UserDetailResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDTO requestDTO) {

        log.info("Solicitud REST para actualizar usuario: {}", id);

        UserDetailResponseDTO updatedUser = userService.updateUser(id, requestDTO);

        return ResponseEntity.ok(updatedUser);
    }

    //Desactivar usuario
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable Long id) {
        log.info("Solicitud REST para desactivar usuario: {}", id);

        userService.deactivateUser(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Usuario desactivado exitosamente");
        response.put("userId", id.toString());

        return ResponseEntity.ok(response);
    }

    //Eliminar usuario de forma permanente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Solicitud REST para eliminar usuario: {}", id);

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    //Asignar rol a usuario
    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<UserDetailResponseDTO> assignRoleToUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {

        log.info("Solicitud REST para asignar rol {} al usuario {}", roleId, userId);

        UserDetailResponseDTO user = userService.assignRoleToUser(userId, roleId);

        return ResponseEntity.ok(user);
    }

    //Quitar rol a usuario
    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<UserDetailResponseDTO> removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {

        log.info("Solicitud REST para quitar rol {} del usuario {}", roleId, userId);

        UserDetailResponseDTO user = userService.removeRoleFromUser(userId, roleId);

        return ResponseEntity.ok(user);
    }

    //Verificar si el email existe
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(
            @RequestParam String email) {

        log.info("Solicitud REST para verificar si el email existe: {}", email);

        boolean exists = userService.existsByEmail(email);

        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);

        return ResponseEntity.ok(response);
    }


}