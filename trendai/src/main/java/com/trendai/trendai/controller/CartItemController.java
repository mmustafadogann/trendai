package com.trendai.trendai.controller;

import com.trendai.trendai.dto.AddCartItemRequest;
import com.trendai.trendai.dto.CartResponse;
import com.trendai.trendai.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts/{cartId}/items")
public class CartItemController {

    private final CartService cartService;

    public CartItemController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<CartResponse> addItem(
            @PathVariable Long cartId,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addItem(cartId, request));
    }
}