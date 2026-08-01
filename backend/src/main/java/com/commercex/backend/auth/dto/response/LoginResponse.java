package com.commercex.backend.auth.dto.response;

import java.util.Set;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        String name,
        String email,
        Set<String> roles
) {
}