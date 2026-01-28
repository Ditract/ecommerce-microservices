package com.ecommerce.auth.service.impl;

import com.ecommerce.auth.entity.Credential;
import com.ecommerce.auth.exception.AuthenticationException;
import com.ecommerce.auth.repository.CredentialRepository;
import com.ecommerce.auth.service.CredentialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class CredentialServiceImpl implements CredentialService {

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    public CredentialServiceImpl(CredentialRepository credentialRepository,
                                 PasswordEncoder passwordEncoder) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Credential createCredential(Long userId, String email, String passwordHash) {
        log.info("Creando credenciales para userId: {}, email: {}", userId, email);

        if (credentialRepository.existsByEmail(email)) {
            throw new AuthenticationException("Las credenciales ya existen para este email");
        }

        Credential credential = Credential.builder()
                .userId(userId)
                .email(email)
                .passwordHash(passwordHash)
                .isActive(false)
                .build();

        return credentialRepository.save(credential);
    }

    @Override
    @Transactional(readOnly = true)
    public Credential findByEmail(String email) {
        return credentialRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Credenciales no encontradas"));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePassword(String rawPassword, String email) {
        Credential credential = findByEmail(email);

        //Validar que la credencial esté activa
        if (!credential.getIsActive()) {
            log.warn("Intento de login con credencial inactiva: {}", email);
            throw new AuthenticationException("Cuenta no activada");
        }

        return passwordEncoder.matches(rawPassword, credential.getPasswordHash());
    }

    @Override
    public void updatePassword(String email, String newPasswordHash) {
        Credential credential = findByEmail(email);
        credential.setPasswordHash(newPasswordHash);
        credentialRepository.save(credential);
    }

    @Override
    public void activateCredential(Long userId) {
        log.info("Activando credencial para userId: {}", userId);
        Credential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthenticationException("Credencial no encontrada para userId: " + userId));

        credential.setIsActive(true);
        credentialRepository.save(credential);
        log.info("Credencial activada exitosamente para userId: {}", userId);
    }

    @Override
    public void deactivateCredential(Long userId) {
        log.warn("Desactivando credencial para userId: {}", userId);
        credentialRepository.findByUserId(userId).ifPresent(credential -> {
            credential.setIsActive(false);
            credentialRepository.save(credential);
        });
    }
}