package com.trendai.trendai.controller;

import com.trendai.trendai.dto.CartResponse;
import com.trendai.trendai.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.trendai.trendai.dto.AddCartItemRequest;

@RestController
@RequestMapping("/api/users/{userId}/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                cartService.getOrCreateCart(userId)
        );
    }

    @PostMapping
    public ResponseEntity<CartResponse> createCart(
            @PathVariable Long userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.getOrCreateCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable Long cartId,
            @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addItem(cartId, request));
    }
}