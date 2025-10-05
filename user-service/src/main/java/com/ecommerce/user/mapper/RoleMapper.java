package com.ecommerce.user.mapper;

import com.ecommerce.user.dto.request.RoleCreateRequestDTO;
import com.ecommerce.user.dto.response.RoleResponseDTO;
import com.ecommerce.user.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    /**
     * Convierte RoleCreateRequestDTO a Role entity.
     */
    public Role toEntity(RoleCreateRequestDTO dto) {
        return Role.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }

    /**
     * Convierte Role entity a RoleResponseDTO.
     */
    public RoleResponseDTO toResponseDTO(Role role) {
        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}