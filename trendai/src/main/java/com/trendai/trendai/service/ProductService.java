package com.trendai.trendai.service;

import com.trendai.trendai.dto.CreateProductRequest;
import com.trendai.trendai.dto.ProductResponse;
import com.trendai.trendai.entity.Category;
import com.trendai.trendai.entity.Product;
import com.trendai.trendai.exception.ResourceNotFoundException;
import com.trendai.trendai.repository.CategoryRepository;
import com.trendai.trendai.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductResponse createProduct(CreateProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // DTO -> Entity (Manual Mapping)
        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setColor(request.getColor());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setCreatedAt(LocalDateTime.now());
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        // Entity -> DTO (Manual Mapping)
        ProductResponse response = new ProductResponse();

        response.setId(savedProduct.getId());
        response.setName(savedProduct.getName());
        response.setDescription(savedProduct.getDescription());
        response.setBrand(savedProduct.getBrand());
        response.setColor(savedProduct.getColor());
        response.setPrice(savedProduct.getPrice());
        response.setStock(savedProduct.getStock());
        response.setImageUrl(savedProduct.getImageUrl());
        response.setCreatedAt(savedProduct.getCreatedAt());
        response.setCategoryName(savedProduct.getCategory().getName());

        return response;
    }

    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll().stream().map(product -> {

            ProductResponse response = new ProductResponse();

            response.setId(product.getId());
            response.setName(product.getName());
            response.setDescription(product.getDescription());
            response.setBrand(product.getBrand());
            response.setColor(product.getColor());
            response.setPrice(product.getPrice());
            response.setStock(product.getStock());
            response.setImageUrl(product.getImageUrl());
            response.setCreatedAt(product.getCreatedAt());
            response.setCategoryName(product.getCategory().getName());

            return response;

        }).toList();
    }

    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setBrand(product.getBrand());
        response.setColor(product.getColor());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setImageUrl(product.getImageUrl());
        response.setCreatedAt(product.getCreatedAt());
        response.setCategoryName(product.getCategory().getName());

        return response;
    }
}