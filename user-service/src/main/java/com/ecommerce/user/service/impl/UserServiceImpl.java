package com.ecommerce.user.service.impl;

import com.ecommerce.user.dto.request.UserCreateRequestDTO;
import com.ecommerce.user.dto.request.UserUpdateRequestDTO;
import com.ecommerce.user.dto.response.UserDetailResponseDTO;
import com.ecommerce.user.dto.response.UserResponseDTO;
import com.ecommerce.user.entity.Role;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.exception.DuplicateResourceException;
import com.ecommerce.user.exception.InvalidOperationException;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.mapper.UserMapper;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public UserDetailResponseDTO createUser(UserCreateRequestDTO requestDTO) {
        log.info("Creando nuevo usuario con email: {}", requestDTO.getEmail());


        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            log.error("El email ya existe: {}", requestDTO.getEmail());
            throw new DuplicateResourceException("User", "email", requestDTO.getEmail());
        }

        //String hashedPassword = passwordEncoder.encode(requestDTO.getPassword());
        //requestDTO.setPassword(requestDTO.getPassword());

        User user = userMapper.toEntity(requestDTO);

        //Asignar rol por defecto (ROLE_USER)
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));
        user.addRole(defaultRole);


        User savedUser = userRepository.save(user);

        log.info("Usuario creado exitosamente con ID: {}", savedUser.getId());


        return userMapper.toDetailResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponseDTO getUserById(Long id) {
        log.info("Obteniendo usuario por ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return userMapper.toDetailResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponseDTO getUserByEmail(String email) {
        log.info("Obteniendo usuario por email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return userMapper.toDetailResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.info("Obteniendo todos los usuarios");

        return userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getActiveUsers() {
        log.info("Obteniendo usuarios activos");

        return userRepository.findByIsActive(true).stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDetailResponseDTO updateUser(Long id, UserUpdateRequestDTO requestDTO) {
        log.info("Actualizando usuario con ID: {}", id);


        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Si se está cambiando el email, validar que no exista
        if (requestDTO.getEmail() != null &&
                !requestDTO.getEmail().equals(user.getEmail())) {

            if (userRepository.existsByEmail(requestDTO.getEmail())) {
                throw new DuplicateResourceException("User", "email", requestDTO.getEmail());
            }
        }


        userMapper.updateEntityFromDTO(user, requestDTO);


        User updatedUser = userRepository.save(user);

        log.info("Usuario actualizado exitosamente: {}", updatedUser.getId());

        return userMapper.toDetailResponseDTO(updatedUser);
    }

    @Override
    public void deactivateUser(Long id) {
        log.info("Desactivando usuario con ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("Usuario desactivado exitosamente: {}", id);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Eliminando usuario con ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userRepository.delete(user);

        log.info("Usuario eliminado exitosamente: {}", id);
    }

    @Override
    public UserDetailResponseDTO assignRoleToUser(Long userId, Long roleId) {
        log.info("Asignando rol {} al usuario {}", roleId, userId);


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        // Verificar que el usuario no tenga ya ese rol
        boolean hasRole = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getId().equals(roleId));

        if (hasRole) {
            throw new InvalidOperationException(
                    "El usuario ya tiene el rol: " + role.getName());
        }

        // Asignar rol
        user.addRole(role);

        User updatedUser = userRepository.save(user);

        log.info("Rol asignado exitosamente");

        return userMapper.toDetailResponseDTO(updatedUser);
    }

    @Override
    public UserDetailResponseDTO removeRoleFromUser(Long userId, Long roleId) {
        log.info("Removiendo rol {} del usuario {}", roleId, userId);


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        // Verificar que el usuario tiene ese rol
        boolean hasRole = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getId().equals(roleId));

        if (!hasRole) {
            throw new InvalidOperationException(
                    "El usuario no tiene el rol: " + role.getName());
        }

        // Verificar que no es el último rol (un usuario debe tener al menos un rol)
        if (user.getUserRoles().size() == 1) {
            throw new InvalidOperationException(
                    "No se puede remover el último rol del usuario");
        }


        user.removeRole(role);


        User updatedUser = userRepository.save(user);

        log.info("Rol removido exitosamente");

        return userMapper.toDetailResponseDTO(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> searchUsers(String searchTerm) {
        log.info("Buscando usuarios con término: {}", searchTerm);

        return userRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        searchTerm, searchTerm)
                .stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRole(String roleName) {
        log.info("Obteniendo usuarios con rol: {}", roleName);

        return userRepository.findByRoleName(roleName).stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}