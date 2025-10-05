package com.ecommerce.user.controller;

import com.ecommerce.user.dto.request.RoleCreateRequestDTO;
import com.ecommerce.user.dto.response.RoleResponseDTO;
import com.ecommerce.user.service.RoleService;
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
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    //Crear nuevo rol
    @PostMapping
    public ResponseEntity<RoleResponseDTO> createRole(
            @Valid @RequestBody RoleCreateRequestDTO requestDTO) {

        log.info("Solicitud REST para crear rol: {}", requestDTO.getName());

        RoleResponseDTO createdRole = roleService.createRole(requestDTO);

        return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
    }

    //OBtener rol por id
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDTO> getRoleById(@PathVariable Long id) {
        log.info("Solicitud REST para obtener rol por ID: {}", id);

        RoleResponseDTO role = roleService.getRoleById(id);

        return ResponseEntity.ok(role);
    }

    //Obtener rol por nombre
    @GetMapping("/name/{name}")
    public ResponseEntity<RoleResponseDTO> getRoleByName(@PathVariable String name) {
        log.info("Solicitud REST para obtener rol por nombre: {}", name);

        RoleResponseDTO role = roleService.getRoleByName(name);

        return ResponseEntity.ok(role);
    }

    //Obtener todos los roles
    @GetMapping
    public ResponseEntity<List<RoleResponseDTO>> getAllRoles() {
        log.info("Solicitud REST para obtener todos los roles");

        List<RoleResponseDTO> roles = roleService.getAllRoles();

        return ResponseEntity.ok(roles);
    }

    //Actualizar rol
    @PutMapping("/{id}")
    public ResponseEntity<RoleResponseDTO> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleCreateRequestDTO requestDTO) {

        log.info("Solicitud REST para actualizar rol: {}", id);

        RoleResponseDTO updatedRole = roleService.updateRole(id, requestDTO);

        return ResponseEntity.ok(updatedRole);
    }

    //ELiminar rol solo si no está asignado
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        log.info("Solicitud REST para eliminar rol: {}", id);

        roleService.deleteRole(id);

        return ResponseEntity.noContent().build();
    }

    //Verificar si el rol existe
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> checkRoleExists(
            @RequestParam String name) {

        log.info("Solicitud REST para verificar si el rol existe: {}", name);

        boolean exists = roleService.existsByName(name);

        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);

        return ResponseEntity.ok(response);
    }
}