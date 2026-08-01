package com.commercex.backend.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.commercex.backend.auth.entity.RefreshToken;

import io.lettuce.core.dynamic.annotation.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshToken refreshToken
            set refreshToken.revoked = true,
                refreshToken.revokedAt = :revokedAt
            where refreshToken.user.id = :userId
              and refreshToken.revoked = false
            """)
    int revokeAllActiveTokensByUserId(
            @Param("userId") UUID userId,
            @Param("revokedAt") LocalDateTime revokedAt
    );

    @Modifying
    @Query("""
            delete from RefreshToken refreshToken
            where refreshToken.expiresAt < :currentTime
               or refreshToken.revoked = true
            """)
    int deleteExpiredAndRevokedTokens(
            @Param("currentTime") LocalDateTime currentTime
    );
}
