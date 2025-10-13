package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {


    Optional<Credential> findByEmail(String email);

    Optional<Credential> findByUserId(Long userId);

    boolean existsByEmail(String email);

    /**
     * Verifica si existe credencial para ese usuario.
     */
    boolean existsByUserId(Long userId);
}