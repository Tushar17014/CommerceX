package com.commercex.backend.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.commercex.backend.auth.entity.Users;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;

    public JwtService(
            @Value("${security.jwt.secret}") String jwtSecret,
            @Value("${security.jwt.access-token-expiration-ms}") long accessTokenExpirationMs
    ) {
        byte[] keyBytes = parseSecret(jwtSecret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    private static byte[] parseSecret(String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must be configured and non-empty");
        }

        try {
            byte[] decoded = Decoders.BASE64.decode(jwtSecret);
            if (decoded.length >= 32) {
                return decoded;
            }
        } catch (IllegalArgumentException ignored) {
            // not base64, fall through to plain text handling
        }

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public String generateAccessToken(Users user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(accessTokenExpirationMs);

        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        Map<String, Object> claims = Map.of(
                "roles", roles
        );

        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claims(claims)
                .signWith(signingKey)
                .compact();
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();

        return expiration.before(new Date());
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

     public boolean isTokenValid(
            String token,
            Users user
    ) {
        String email = extractEmail(token);

        return email.equalsIgnoreCase(user.getEmail())
                && !isTokenExpired(token)
                && Boolean.TRUE.equals(user.getEnabled());
    }
    
}
