package com.commercex.backend.auth.service.impl;

import java.util.Locale;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.commercex.backend.auth.dto.request.RegisterRequest;
import com.commercex.backend.auth.dto.response.RegisterResponse;
import com.commercex.backend.auth.entity.Role;
import com.commercex.backend.auth.entity.Users;
import com.commercex.backend.auth.repository.RoleRepository;
import com.commercex.backend.auth.repository.UserRepository;
import com.commercex.backend.auth.service.AuthService;
import com.commercex.backend.common.exception.BusinessException;
import com.commercex.backend.auth.dto.request.LoginRequest;
import com.commercex.backend.auth.dto.response.LoginResponse;
import com.commercex.backend.auth.security.JwtService;

import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

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

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        log.info("Login requested for email: {}", normalizedEmail);

        Users user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn(
                            "Login failed because credentials were invalid for email: {}",
                            normalizedEmail
                    );

                    return new BusinessException("Invalid email or password");
                });

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            log.warn(
                    "Login rejected because account is disabled: {}",
                    normalizedEmail
            );

            throw new BusinessException("Account is disabled");
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            log.warn(
                    "Login failed because credentials were invalid for email: {}",
                    normalizedEmail
            );

            throw new BusinessException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user);

        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        log.info(
                "User logged in successfully with id: {}",
                user.getId()
        );

        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtService.getAccessTokenExpirationMs(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                roles
        );
    }
}
