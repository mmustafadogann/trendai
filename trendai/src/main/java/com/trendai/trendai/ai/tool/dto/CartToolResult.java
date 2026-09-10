package com.trendai.trendai.ai.tool.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartToolResult(
        Long cartId,
        Long userId,
        List<CartItemToolResult> items,
        BigDecimal grandTotal
) {
}