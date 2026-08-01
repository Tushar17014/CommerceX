package com.commercex.backend.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commercex.backend.auth.entity.RefreshToken;
import com.commercex.backend.auth.entity.Users;
import com.commercex.backend.auth.repository.RefreshTokenRepository;
import com.commercex.backend.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int TOKEN_SIZE_BYTES = 64;

    private final RefreshTokenRepository refreshTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${security.refresh-token.expiration-ms}")
    private long refreshTokenExpirationMs;

    @Transactional
    public String createRefreshToken(Users user) {
        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .expiresAt(
                        LocalDateTime.now().plus(
                                Duration.ofMillis(refreshTokenExpirationMs)
                        )
                )
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        log.info(
                "Refresh token created for user id: {}",
                user.getId()
        );

        return rawToken;
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken
                = refreshTokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() -> {
                            log.warn("Unknown refresh token received");
                            return new BusinessException(
                                    "Invalid refresh token"
                            );
                        });

        if (refreshToken.isRevoked()) {
            log.warn(
                    "Revoked refresh token used for user id: {}",
                    refreshToken.getUser().getId()
            );

            throw new BusinessException(
                    "Refresh token has been revoked"
            );
        }

        if (refreshToken.isExpired()) {
            log.warn(
                    "Expired refresh token used for user id: {}",
                    refreshToken.getUser().getId()
            );

            throw new BusinessException(
                    "Refresh token has expired"
            );
        }

        if (!Boolean.TRUE.equals(
                refreshToken.getUser().getEnabled()
        )) {
            throw new BusinessException("Account is disabled");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());

        refreshTokenRepository.save(refreshToken);
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[TOKEN_SIZE_BYTES];
        secureRandom.nextBytes(tokenBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest
                    = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    exception
            );
        }
    }
}
