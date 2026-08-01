package com.commercex.backend.auth.service;

import com.commercex.backend.auth.dto.request.LoginRequest;
import com.commercex.backend.auth.dto.request.RefreshTokenRequest;
import com.commercex.backend.auth.dto.request.RegisterRequest;
import com.commercex.backend.auth.dto.response.RegisterResponse;
import com.commercex.backend.auth.dto.response.TokenResponse;

public interface AuthService {
    
    RegisterResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refreshToken(RefreshTokenRequest request);

}
