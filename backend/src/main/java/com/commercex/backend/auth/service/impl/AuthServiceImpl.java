package com.commercex.backend.auth.service.impl;

import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.commercex.backend.auth.dto.RegisterRequest;
import com.commercex.backend.auth.dto.RegisterResponse;
import com.commercex.backend.auth.entity.Role;
import com.commercex.backend.auth.entity.Users;
import com.commercex.backend.auth.repository.RoleRepository;
import com.commercex.backend.auth.repository.UserRepository;
import com.commercex.backend.auth.service.AuthService;
import com.commercex.backend.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    "Email already registered"
            );
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                        .orElseThrow(
                                () -> new BusinessException("Default role not found")
                        );

        Users user = Users.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(
                        passwordEncoder.encode(request.getPassword())
                )
                .phoneNumber(request.getPhoneNumber())
                .enabled(true)
                .emailVerified(false)
                .roles(Set.of(userRole))
                .build();

        Users savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .build();

    }
}
