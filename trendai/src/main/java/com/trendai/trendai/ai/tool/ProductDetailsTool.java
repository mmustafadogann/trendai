package com.trendai.trendai.ai.tool;

import com.trendai.trendai.ai.tool.dto.ProductDetailsResult;
import com.trendai.trendai.dto.ProductResponse;
import com.trendai.trendai.service.ProductService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class ProductDetailsTool {

    private final ProductService productService;

    public ProductDetailsTool(ProductService productService) {
        this.productService = productService;
    }

    @Tool("Gets details of an active product by product ID")
    public ProductDetailsResult getProductDetails(
            @P("Product ID") Long productId) {

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be a positive number"
            );
        }

        ProductResponse product =
                productService.getProductById(productId);

        return new ProductDetailsResult(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getColor(),
                product.getPrice(),
                product.getStock(),
                product.getActive(),
                product.getCategoryName()
        );
    }
}