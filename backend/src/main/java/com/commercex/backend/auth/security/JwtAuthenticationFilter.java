package com.commercex.backend.auth.security;

import com.commercex.backend.auth.entity.Role;
import com.commercex.backend.auth.entity.Users;
import com.commercex.backend.auth.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(
                BEARER_PREFIX.length()
        );

        try {
            authenticateRequest(token, request);
        } catch (JwtException | IllegalArgumentException exception) {
            log.warn(
                    "Invalid JWT received for path {}: {}",
                    request.getRequestURI(),
                    exception.getMessage()
            );

            SecurityContextHolder.clearContext();
        } catch (Exception exception) {
            log.error(
                    "Unexpected JWT authentication error for path {}",
                    request.getRequestURI(),
                    exception
            );

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateRequest(
            String token,
            HttpServletRequest request
    ) {
        String email = jwtService.extractEmail(token);

        if (email == null || email.isBlank()) {
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        Users user = userRepository.findByEmailIgnoreCase(email)
                .orElse(null);

        if (user == null) {
            log.warn(
                    "JWT subject does not match an existing user: {}",
                    email
            );
            return;
        }

        if (!jwtService.isTokenValid(token, user)) {
            return;
        }

        List<SimpleGrantedAuthority> authorities = user.getRoles()
                .stream()
                .map(Role::getName)
                .map(SimpleGrantedAuthority::new)
                .toList();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        authorities
                );

        authentication.setDetails(
                request.getRemoteAddr()
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        log.debug(
                "JWT authentication succeeded for user: {}",
                user.getEmail()
        );
    }
}
