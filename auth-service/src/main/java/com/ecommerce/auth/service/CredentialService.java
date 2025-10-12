package com.ecommerce.auth.service;

import com.ecommerce.auth.entity.Credential;

public interface CredentialService {

    Credential createCredential(Long userId, String email, String passwordHash);

    Credential findByEmail(String email);

    boolean validatePassword(String rawPassword, String email);

    void updatePassword(String email, String newPasswordHash);
}