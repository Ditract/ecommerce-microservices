package com.ecommerce.user.mapper;

import com.ecommerce.user.dto.request.UserCreateRequestDTO;
import com.ecommerce.user.dto.request.UserUpdateRequestDTO;
import com.ecommerce.user.dto.response.UserDetailResponseDTO;
import com.ecommerce.user.dto.response.UserResponseDTO;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.entity.UserRole;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
public class UserMapper {

    private final RoleMapper roleMapper;

    public UserMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    /**
     * Convierte UserCreateRequestDTO a User entity.
     */
    public User toEntity(UserCreateRequestDTO dto) {
        return User.builder()
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .isActive(true)
                .build();
    }

    /**
     * Actualiza una entidad User existente con datos del DTO.
     */
    public void updateEntityFromDTO(User user, UserUpdateRequestDTO dto) {
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getIsActive() != null) {
            user.setIsActive(dto.getIsActive());
        }
    }

    /**
     * Convierte User entity a UserResponseDTO (sin roles).
     */
    public UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Convierte User entity a UserDetailResponseDTO (con roles).
     */
    public UserDetailResponseDTO toDetailResponseDTO(User user) {
        return UserDetailResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .roles(user.getUserRoles().stream()
                        .map(UserRole::getRole)
                        .map(roleMapper::toResponseDTO)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}