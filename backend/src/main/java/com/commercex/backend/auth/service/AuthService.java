package com.commercex.backend.auth.service;

import com.commercex.backend.auth.dto.RegisterRequest;
import com.commercex.backend.auth.dto.RegisterResponse;

public interface AuthService {
    
    RegisterResponse register(RegisterRequest request);
}
