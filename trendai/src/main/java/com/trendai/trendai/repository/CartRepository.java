package com.trendai.trendai.repository;

import com.trendai.trendai.entity.Cart;
import com.trendai.trendai.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);
}