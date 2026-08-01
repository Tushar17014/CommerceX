package com.commercex.backend.user.dto.response;

import java.util.Set;
import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String name,
        String email,
        String phoneNumber,
        boolean enabled,
        boolean emailVerified,
        Set<String> roles
        ) {

}
