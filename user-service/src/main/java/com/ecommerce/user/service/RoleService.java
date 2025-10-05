package com.ecommerce.user.service;

import com.ecommerce.user.dto.request.RoleCreateRequestDTO;
import com.ecommerce.user.dto.response.RoleResponseDTO;
import com.ecommerce.user.entity.Role;

import java.util.List;


public interface RoleService {


    RoleResponseDTO createRole(RoleCreateRequestDTO requestDTO);

    RoleResponseDTO getRoleById(Long id);

    RoleResponseDTO getRoleByName(String name);

    List<RoleResponseDTO> getAllRoles();

    RoleResponseDTO updateRole(Long id, RoleCreateRequestDTO requestDTO);

    void deleteRole(Long id);


    boolean existsByName(String name);

    /**
     * Metodo interno para obtener la entidad Role.
     */
    Role findRoleEntityById(Long id);

    /**
     * Metodo interno para obtener la entidad Role por nombre.
     */
    Role findRoleEntityByName(String name);
}