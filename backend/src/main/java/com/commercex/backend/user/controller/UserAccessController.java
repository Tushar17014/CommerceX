package com.commercex.backend.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.commercex.backend.common.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/user-access")
public class UserAccessController {
    
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> getUserAccess() {
        
        ApiResponse<String> response = new ApiResponse<>(
                true,
                "User endpoint accessed successfully",
                "Authenticated user content"
        );

        return ResponseEntity.ok(response);
    }
}
