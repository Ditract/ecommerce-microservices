package com.ecommerce.user.service;

import com.ecommerce.user.dto.request.UserCreateRequestDTO;
import com.ecommerce.user.dto.request.UserUpdateRequestDTO;
import com.ecommerce.user.dto.response.UserDetailResponseDTO;
import com.ecommerce.user.dto.response.UserResponseDTO;
import com.ecommerce.user.entity.User;

import java.util.List;


public interface UserService {


    UserDetailResponseDTO createUser(UserCreateRequestDTO requestDTO);

    UserDetailResponseDTO getUserById(Long id);


    UserDetailResponseDTO getUserByEmail(String email);

    List<UserResponseDTO> getAllUsers();


    List<UserResponseDTO> getActiveUsers();


    UserDetailResponseDTO updateUser(Long id, UserUpdateRequestDTO requestDTO);

    /**
     * Elimina un usuario (soft delete: cambia isActive a false).
     */
    void deactivateUser(Long id);


    void deleteUser(Long id);


    UserDetailResponseDTO assignRoleToUser(Long userId, Long roleId);


    UserDetailResponseDTO removeRoleFromUser(Long userId, Long roleId);


    List<UserResponseDTO> searchUsers(String searchTerm);


    List<UserResponseDTO> getUsersByRole(String roleName);


    boolean existsByEmail(String email);

    /**
     * Metodo interno para obtener la entidad User (usado por otros microservicios) (solo pruebas).
     */
    User findUserEntityById(Long id);

    /**
     * Metodo interno para obtener la entidad User por email (usado por Auth Service) (solo pruebas).
     */
    User findUserEntityByEmail(String email);
}