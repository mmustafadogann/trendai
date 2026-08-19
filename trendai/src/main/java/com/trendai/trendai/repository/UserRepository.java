package com.trendai.trendai.repository;

import com.trendai.trendai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndActiveTrue(Long id);

    boolean existsByEmailIgnoreCase(String email);
}