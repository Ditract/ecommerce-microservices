package com.ecommerce.user.service.impl;

import com.ecommerce.user.dto.request.RoleCreateRequestDTO;
import com.ecommerce.user.dto.response.RoleResponseDTO;
import com.ecommerce.user.entity.Role;
import com.ecommerce.user.exception.DuplicateResourceException;
import com.ecommerce.user.exception.InvalidOperationException;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.mapper.RoleMapper;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public RoleResponseDTO createRole(RoleCreateRequestDTO requestDTO) {
        log.info("Creando nuevo rol: {}", requestDTO.getName());


        if (roleRepository.existsByName(requestDTO.getName())) {
            throw new DuplicateResourceException("Role", "name", requestDTO.getName());
        }


        Role role = roleMapper.toEntity(requestDTO);
        Role savedRole = roleRepository.save(role);

        log.info("Rol creado exitosamente con ID: {}", savedRole.getId());

        return roleMapper.toResponseDTO(savedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDTO getRoleById(Long id) {
        log.info("Obteniendo rol por ID: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        return roleMapper.toResponseDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDTO getRoleByName(String name) {
        log.info("Obteniendo rol por nombre: {}", name);

        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name));

        return roleMapper.toResponseDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDTO> getAllRoles() {
        log.info("Obteniendo todos los roles");

        return roleRepository.findAll().stream()
                .map(roleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponseDTO updateRole(Long id, RoleCreateRequestDTO requestDTO) {
        log.info("Actualizando rol con ID: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Si se cambia el nombre, validar que no exista
        if (!role.getName().equals(requestDTO.getName()) &&
                roleRepository.existsByName(requestDTO.getName())) {
            throw new DuplicateResourceException("Role", "name", requestDTO.getName());
        }

        role.setName(requestDTO.getName());
        role.setDescription(requestDTO.getDescription());

        Role updatedRole = roleRepository.save(role);

        log.info("Rol actualizado exitosamente: {}", updatedRole.getId());

        return roleMapper.toResponseDTO(updatedRole);
    }

    @Override
    public void deleteRole(Long id) {
        log.info("Eliminando rol con ID: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (!role.getUserRoles().isEmpty()) {
            throw new InvalidOperationException(
                    "No se puede eliminar el rol porque está asignado a " +
                            role.getUserRoles().size() + " usuario(s)");
        }

        roleRepository.delete(role);

        log.info("Rol eliminado exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Role findRoleEntityById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Role findRoleEntityByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name));
    }
}