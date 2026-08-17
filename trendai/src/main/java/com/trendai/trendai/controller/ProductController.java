package com.trendai.trendai.controller;

import com.trendai.trendai.dto.CreateProductRequest;
import com.trendai.trendai.dto.ProductPageResponse;
import com.trendai.trendai.dto.ProductResponse;
import com.trendai.trendai.dto.UpdateProductRequest;
import com.trendai.trendai.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        return productService.createProduct(request);
    }

    @GetMapping
    public ProductPageResponse searchProducts(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page cannot be negative")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size cannot exceed 100")
            int size,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            Long categoryId,

            @RequestParam(required = false)
            String brand,

            @RequestParam(required = false)
            @DecimalMin(
                    value = "0.0",
                    inclusive = true,
                    message = "Minimum price cannot be negative"
            )
            BigDecimal minPrice,

            @RequestParam(required = false)
            @DecimalMin(
                    value = "0.0",
                    inclusive = true,
                    message = "Maximum price cannot be negative"
            )
            BigDecimal maxPrice,

            @RequestParam(defaultValue = "id,asc")
            String sort) {

        return productService.searchProducts(
                page,
                size,
                keyword,
                categoryId,
                brand,
                minPrice,
                maxPrice,
                sort
        );
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(
            @PathVariable Long id) {

        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {

        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id) {

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }
}