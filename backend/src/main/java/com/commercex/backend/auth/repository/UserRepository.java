package com.commercex.backend.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commercex.backend.auth.entity.Users;

public interface UserRepository extends JpaRepository<Users, UUID> {
    
    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);
}
