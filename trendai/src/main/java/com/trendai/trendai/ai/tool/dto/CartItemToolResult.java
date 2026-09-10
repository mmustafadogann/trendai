package com.trendai.trendai.ai.tool.dto;

import java.math.BigDecimal;

public record CartItemToolResult(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}