package com.trendai.trendai.ai.tool;

import com.trendai.trendai.ai.tool.dto.CartItemToolResult;
import com.trendai.trendai.ai.tool.dto.CartToolResult;
import com.trendai.trendai.entity.Cart;
import com.trendai.trendai.entity.CartItem;
import com.trendai.trendai.entity.CartStatus;
import com.trendai.trendai.exception.ResourceNotFoundException;
import com.trendai.trendai.repository.CartItemRepository;
import com.trendai.trendai.repository.CartRepository;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CartTool {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartTool(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Tool("Gets the user's active shopping cart without modifying it")
    public CartToolResult getActiveCart(
            @P("User ID") Long userId) {

        validateUserId(userId);

        Cart cart = cartRepository
                .findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Active cart not found"));

        List<CartItemToolResult> items = cartItemRepository
                .findByCartId(cart.getId())
                .stream()
                .map(this::toItemResult)
                .toList();

        BigDecimal grandTotal = items.stream()
                .map(CartItemToolResult::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartToolResult(
                cart.getId(),
                cart.getUser().getId(),
                items,
                grandTotal
        );
    }

    private void validateUserId(Long userId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be a positive number"
            );
        }
    }

    private CartItemToolResult toItemResult(CartItem item) {

        BigDecimal lineTotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemToolResult(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                lineTotal
        );
    }
}