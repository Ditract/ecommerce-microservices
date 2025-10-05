package com.ecommerce.user.initializer;

import com.ecommerce.user.entity.Role;
import com.ecommerce.user.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Se ejecuta automaticamente al arrancar la aplicación.
 * Crea los roles por defecto si no existen.
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando inicialización de datos...");

        initializeRoles();

        log.info("¡Inicialización de datos completada!");
    }

    private void initializeRoles() {
        createRoleIfNotExists("ROLE_USER", "Usuario estándar del sistema");
        createRoleIfNotExists("ROLE_ADMIN", "Administrador con acceso total");
        createRoleIfNotExists("ROLE_MANAGER", "Gestor de productos e inventario");
    }

    private void createRoleIfNotExists(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .build();

            roleRepository.save(role);
            log.info("Rol creado: {}", name);
        } else {
            log.info("El rol ya existe: {}", name);
        }
    }
}