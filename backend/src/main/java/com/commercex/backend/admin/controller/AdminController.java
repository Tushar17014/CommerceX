package com.commercex.backend.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.commercex.backend.common.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    
    @GetMapping("/dashboard")
    //Spring automatically adds the ROLE_ prefix when using hasRole.
    //Alternatively: @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> getDashboard() {
        ApiResponse<String> response = new ApiResponse<>(
                true,
                "Admin dashboard fetched successfully",
                "Welcome to the Admin Dashboard!"
        );
        return ResponseEntity.ok(response);
    }
}
