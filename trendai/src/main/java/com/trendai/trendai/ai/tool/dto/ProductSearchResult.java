package com.trendai.trendai.ai.tool.dto;

import java.math.BigDecimal;

public record ProductSearchResult(
        Long productId,
        String name,
        String brand,
        BigDecimal price,
        Integer stock,
        String categoryName
) {
}