package com.trendai.trendai.mapper;

import com.trendai.trendai.dto.CreateProductRequest;
import com.trendai.trendai.dto.ProductResponse;
import com.trendai.trendai.dto.UpdateProductRequest;
import com.trendai.trendai.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(CreateProductRequest request) {

        Product product = new Product();

        product.setName(
                request.getName() != null
                        ? request.getName().trim()
                        : null
        );

        product.setDescription(request.getDescription());

        product.setBrand(
                request.getBrand() != null
                        ? request.getBrand().trim()
                        : null
        );

        product.setColor(
                request.getColor() != null
                        ? request.getColor().trim()
                        : null
        );

        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());

        return product;
    }

    public void updateEntity(Product product, UpdateProductRequest request) {

        product.setName(
                request.getName() != null
                        ? request.getName().trim()
                        : null
        );

        product.setDescription(request.getDescription());

        product.setBrand(
                request.getBrand() != null
                        ? request.getBrand().trim()
                        : null
        );

        product.setColor(
                request.getColor() != null
                        ? request.getColor().trim()
                        : null
        );

        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
    }

    public ProductResponse toResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setBrand(product.getBrand());
        response.setColor(product.getColor());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setImageUrl(product.getImageUrl());
        response.setActive(product.getActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }

        return response;
    }
}