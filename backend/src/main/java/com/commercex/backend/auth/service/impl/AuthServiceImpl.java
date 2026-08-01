package com.commercex.backend.auth.service.impl;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commercex.backend.auth.dto.request.LoginRequest;
import com.commercex.backend.auth.dto.request.RefreshTokenRequest;
import com.commercex.backend.auth.dto.request.RegisterRequest;
import com.commercex.backend.auth.dto.response.RegisterResponse;
import com.commercex.backend.auth.dto.response.TokenResponse;
import com.commercex.backend.auth.entity.RefreshToken;
import com.commercex.backend.auth.entity.Role;
import com.commercex.backend.auth.entity.Users;
import com.commercex.backend.auth.repository.RoleRepository;
import com.commercex.backend.auth.repository.UserRepository;
import com.commercex.backend.auth.security.JwtService;
import com.commercex.backend.auth.service.AuthService;
import com.commercex.backend.auth.service.RefreshTokenService;
import com.commercex.backend.common.exception.BusinessException;

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

    private final RefreshTokenService refreshTokenService;

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
    @Transactional
    public TokenResponse login(LoginRequest request) {

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

        String refreshToken = refreshTokenService.createRefreshToken(user);

        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        log.info(
                "User logged in successfully with id: {}",
                user.getId()
        );

        return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpirationMs(),
                refreshTokenService.getRefreshTokenExpirationMs(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                roles
        );
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(
            RefreshTokenRequest request
    ) {
        RefreshToken existingToken
                = refreshTokenService.validateRefreshToken(
                        request.refreshToken()
                );

        Users user = existingToken.getUser();

        // Old refresh token can no longer be reused.
        refreshTokenService.revokeToken(existingToken);

        String newAccessToken
                = jwtService.generateAccessToken(user);

        String newRefreshToken
                = refreshTokenService.createRefreshToken(user);

        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        log.info(
                "Refresh token rotated successfully for user id: {}",
                user.getId()
        );

        return new TokenResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtService.getAccessTokenExpirationMs(),
                refreshTokenService.getRefreshTokenExpirationMs(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                roles
        );
    }

    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken
                = refreshTokenService.validateRefreshToken(
                        request.refreshToken()
                );

        refreshTokenService.revokeToken(refreshToken);

        log.info(
                "User logged out successfully. User id: {}",
                refreshToken.getUser().getId()
        );
    }

}
