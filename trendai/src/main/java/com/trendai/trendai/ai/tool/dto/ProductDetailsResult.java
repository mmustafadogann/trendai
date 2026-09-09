package com.trendai.trendai.ai.tool.dto;

import java.math.BigDecimal;

public record ProductDetailsResult(
        Long id,
        String name,
        String description,
        String brand,
        String color,
        BigDecimal price,
        Integer stock,
        Boolean active,
        String categoryName
) {
}