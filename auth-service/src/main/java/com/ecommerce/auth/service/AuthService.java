package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.request.LoginRequestDTO;
import com.ecommerce.auth.dto.request.RefreshTokenRequestDTO;
import com.ecommerce.auth.dto.request.RegisterRequestDTO;
import com.ecommerce.auth.dto.response.AuthResponseDTO;


public interface AuthService {


    AuthResponseDTO login(LoginRequestDTO loginRequest);

    AuthResponseDTO register(RegisterRequestDTO registerRequest);


    AuthResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest);


    boolean validateToken(String token);
}