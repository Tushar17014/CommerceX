package com.commercex.backend.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.commercex.backend.auth.dto.request.LoginRequest;
import com.commercex.backend.auth.dto.request.RefreshTokenRequest;
import com.commercex.backend.auth.dto.request.RegisterRequest;
import com.commercex.backend.auth.dto.response.RegisterResponse;
import com.commercex.backend.auth.dto.response.TokenResponse;
import com.commercex.backend.auth.service.AuthService;
import com.commercex.backend.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<RegisterResponse>builder()
                        .success(true)
                        .message("User registered successfully")
                        .data(response)
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        TokenResponse tokenResponse = authService.login(request);

        ApiResponse<TokenResponse> response = new ApiResponse<>(
                true,
                "Login successful",
                tokenResponse
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        TokenResponse tokenResponse
                = authService.refreshToken(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Tokens refreshed successfully",
                        tokenResponse
                )
        );
    }

}
