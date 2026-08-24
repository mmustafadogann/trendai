package com.trendai.trendai.controller;

import com.trendai.trendai.dto.AddCartItemRequest;
import com.trendai.trendai.dto.CartResponse;
import com.trendai.trendai.dto.UpdateCartItemQuantityRequest;
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
    @PatchMapping("/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request
    ) {
        return ResponseEntity.ok(
                cartService.updateItemQuantity(cartId, itemId, request)
        );
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long cartId,
            @PathVariable Long itemId
    ) {
        cartService.deleteItem(cartId, itemId);

        return ResponseEntity.noContent().build();
    }
}