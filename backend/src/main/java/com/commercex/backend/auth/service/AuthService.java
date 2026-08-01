package com.commercex.backend.auth.service;

import com.commercex.backend.auth.dto.request.LoginRequest;
import com.commercex.backend.auth.dto.request.RegisterRequest;
import com.commercex.backend.auth.dto.response.LoginResponse;
import com.commercex.backend.auth.dto.response.RegisterResponse;

public interface AuthService {
    
    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
