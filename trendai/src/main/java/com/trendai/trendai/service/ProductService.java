package com.trendai.trendai.service;

import com.trendai.trendai.dto.CreateProductRequest;
import com.trendai.trendai.dto.ProductPageResponse;
import com.trendai.trendai.dto.ProductResponse;
import com.trendai.trendai.dto.UpdateProductRequest;
import com.trendai.trendai.entity.Category;
import com.trendai.trendai.entity.Product;
import com.trendai.trendai.exception.BadRequestException;
import com.trendai.trendai.exception.ResourceNotFoundException;
import com.trendai.trendai.mapper.ProductMapper;
import com.trendai.trendai.repository.CategoryRepository;
import com.trendai.trendai.repository.ProductRepository;
import com.trendai.trendai.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class ProductService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "name",
            "price",
            "stock",
            "brand",
            "color"
    );

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductMapper productMapper) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    public ProductResponse createProduct(CreateProductRequest request) {

        Category category = categoryRepository
                .findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        Product product = productMapper.toEntity(request);

        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    public ProductPageResponse searchProducts(
            int page,
            int size,
            String keyword,
            Long categoryId,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort) {

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new BadRequestException(
                    "Minimum price cannot be greater than maximum price"
            );
        }

        String[] sortParts = sort.split(",", -1);

        if (sortParts.length != 2) {
            throw new BadRequestException(
                    "Sort must be in the format field,direction"
            );
        }

        String sortBy = sortParts[0];

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Invalid sort field");
        }

        String sortDirection = sortParts[1].toLowerCase();

        if (!sortDirection.equals("asc")
                && !sortDirection.equals("desc")) {

            throw new BadRequestException("Invalid sort direction");
        }

        Sort.Direction direction =
                sortDirection.equals("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(direction, sortBy)
        );

        var specification = ProductSpecification.isActive();

        if (keyword != null && !keyword.isBlank()) {
            specification = specification.and(
                    ProductSpecification.hasKeyword(keyword)
            );
        }

        if (categoryId != null) {
            specification = specification.and(
                    ProductSpecification.hasCategoryId(categoryId)
            );
        }

        if (brand != null && !brand.isBlank()) {
            specification = specification.and(
                    ProductSpecification.hasBrand(brand)
            );
        }

        if (minPrice != null) {
            specification = specification.and(
                    ProductSpecification.hasMinPrice(minPrice)
            );
        }

        if (maxPrice != null) {
            specification = specification.and(
                    ProductSpecification.hasMaxPrice(maxPrice)
            );
        }

        Page<Product> productPage =
                productRepository.findAll(specification, pageable);

        ProductPageResponse response = new ProductPageResponse();

        response.setContent(
                productPage.getContent()
                        .stream()
                        .map(productMapper::toResponse)
                        .toList()
        );

        response.setPage(productPage.getNumber());
        response.setSize(productPage.getSize());
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());
        response.setFirst(productPage.isFirst());
        response.setLast(productPage.isLast());

        return response;
    }

    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        return productMapper.toResponse(product);
    }

    public ProductResponse updateProduct(
            Long id,
            UpdateProductRequest request) {

        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        Category category = categoryRepository
                .findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        productMapper.updateEntity(product, request);

        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    public void deleteProduct(Long id) {

        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        product.setActive(false);

        productRepository.save(product);
    }
}