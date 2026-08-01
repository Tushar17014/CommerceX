package com.commercex.backend.auth.dto.response;

import java.util.Set;
import java.util.UUID;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        UUID userId,
        String name,
        String email,
        Set<String> roles
        ) {
}
