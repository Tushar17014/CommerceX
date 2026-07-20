package com.commercex.backend.auth.dto;

import java.util.UUID;

import lombok.Builder;

@Builder
public record RegisterResponse(
        UUID id,
        String name,
        String email

) {}