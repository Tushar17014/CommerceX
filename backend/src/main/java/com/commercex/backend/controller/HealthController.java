package com.commercex.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.commercex.backend.common.exception.ResourceNotFoundException;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        throw new ResourceNotFoundException("Health endpoint not found.");
    }
}
