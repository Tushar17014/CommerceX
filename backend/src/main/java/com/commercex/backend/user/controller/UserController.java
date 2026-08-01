package com.commercex.backend.user.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.commercex.backend.auth.entity.Role;
import com.commercex.backend.auth.entity.Users;
import com.commercex.backend.common.response.ApiResponse;
import com.commercex.backend.user.dto.response.CurrentUserResponse;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser(
            @AuthenticationPrincipal Users user
    ) {

        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        CurrentUserResponse currentUser = new CurrentUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                Boolean.TRUE.equals(user.getEnabled()),
                Boolean.TRUE.equals(user.getEmailVerified()),
                roles
        ); 

        ApiResponse<CurrentUserResponse> response
                = new ApiResponse<>(
                        true,
                        "Current user fetched successfully",
                        currentUser
                );
        return ResponseEntity.ok(response);
    }

}
