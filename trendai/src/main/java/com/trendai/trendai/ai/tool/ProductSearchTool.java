package com.trendai.trendai.ai.tool;

import com.trendai.trendai.ai.tool.dto.ProductSearchResult;
import com.trendai.trendai.dto.ProductPageResponse;
import com.trendai.trendai.dto.ProductResponse;
import com.trendai.trendai.repository.CategoryRepository;
import com.trendai.trendai.service.ProductService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductSearchTool {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 10;

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public ProductSearchTool(
            ProductService productService,
            CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @Tool("Searches active products using keyword, category, brand, price range, sorting and result limit")
    public List<ProductSearchResult> searchProducts(
            @P("Product keyword") String keyword,
            @P("Category ID or category name") String category,
            @P("Brand name") String brand,
            @P("Minimum price") BigDecimal minPrice,
            @P("Maximum price") BigDecimal maxPrice,
            @P("Sort in the format field,direction") String sort,
            @P("Maximum number of results, from 1 to 10") Integer limit) {

        validatePrices(minPrice, maxPrice);

        int validatedLimit = validateLimit(limit);

        Long categoryId = resolveCategoryId(category);

        ProductPageResponse response = productService.searchProducts(
                0,
                validatedLimit,
                normalize(keyword),
                categoryId,
                normalize(brand),
                minPrice,
                maxPrice,
                normalizeSort(sort)
        );

        return response.getContent()
                .stream()
                .limit(MAX_LIMIT)
                .map(this::toSearchResult)
                .toList();
    }

    private int validateLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }

        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException(
                    "Limit must be between 1 and 10"
            );
        }

        return limit;
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "id,asc";
        }

        return sort.trim();
    }

    private Long resolveCategoryId(String category) {

        if (category == null || category.isBlank()) {
            return null;
        }

        String normalizedCategory = category.trim();

        try {
            return Long.valueOf(normalizedCategory);
        } catch (NumberFormatException ignored) {
            return categoryRepository
                    .findByNameIgnoreCaseAndActiveTrue(normalizedCategory)
                    .map(categoryEntity -> categoryEntity.getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Category not found"));
        }
    }

    private void validatePrices(
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        if (minPrice != null && minPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Minimum price cannot be negative"
            );
        }

        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Maximum price cannot be negative"
            );
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot be greater than maximum price"
            );
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private ProductSearchResult toSearchResult(ProductResponse product) {

        return new ProductSearchResult(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getPrice(),
                product.getStock(),
                product.getCategoryName()
        );
    }
}