package com.trendai.trendai.service;

import com.trendai.trendai.dto.CreateProductRequest;
import com.trendai.trendai.dto.ProductResponse;
import com.trendai.trendai.entity.Category;
import com.trendai.trendai.entity.Product;
import com.trendai.trendai.mapper.ProductMapper;
import com.trendai.trendai.repository.CategoryRepository;
import com.trendai.trendai.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.trendai.trendai.dto.UpdateProductRequest;
import com.trendai.trendai.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.trendai.trendai.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateProduct() {

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Test Product");
        request.setDescription("Test ürün");
        request.setBrand("Test Brand");
        request.setColor("Black");
        request.setPrice(new BigDecimal("1000"));
        request.setStock(10);
        request.setImageUrl("https://example.com/product.jpg");
        request.setCategoryId(2L);

        Category category = new Category();
        category.setId(2L);
        category.setName("Elektronik");

        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Test ürün");
        product.setBrand("Test Brand");
        product.setColor("Black");
        product.setPrice(new BigDecimal("1000"));
        product.setStock(10);
        product.setImageUrl("https://example.com/product.jpg");
        product.setCategory(category);

        Product savedProduct = new Product();
        savedProduct.setId(10L);
        savedProduct.setName("Test Product");
        savedProduct.setDescription("Test ürün");
        savedProduct.setBrand("Test Brand");
        savedProduct.setColor("Black");
        savedProduct.setPrice(new BigDecimal("1000"));
        savedProduct.setStock(10);
        savedProduct.setImageUrl("https://example.com/product.jpg");
        savedProduct.setActive(true);
        savedProduct.setCategory(category);

        ProductResponse response = new ProductResponse();
        response.setId(10L);
        response.setName("Test Product");
        response.setPrice(new BigDecimal("1000"));
        response.setStock(10);
        response.setActive(true);
        response.setCategoryId(2L);
        response.setCategoryName("Elektronik");

        when(categoryRepository.findById(2L))
                .thenReturn(Optional.of(category));

        when(productMapper.toEntity(request))
                .thenReturn(product);

        when(productRepository.save(product))
                .thenReturn(savedProduct);

        when(productMapper.toResponse(savedProduct))
                .thenReturn(response);

        ProductResponse result = productService.createProduct(request);

        assertEquals(10L, result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals(new BigDecimal("1000"), result.getPrice());
        assertEquals(10, result.getStock());
        assertEquals(true, result.getActive());
        assertEquals(2L, result.getCategoryId());
        assertEquals("Elektronik", result.getCategoryName());
    }

    @Test
    void testUpdateProductNotFound() {

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Updated Product");
        request.setDescription("Updated description");
        request.setBrand("Updated Brand");
        request.setColor("White");
        request.setPrice(new BigDecimal("1500"));
        request.setStock(20);
        request.setImageUrl("https://example.com/new.jpg");
        request.setCategoryId(2L);

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(999L, request)
        );
    }

    @Test
    void testDeleteProductNotFound() {

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct(999L)
        );
    }

    @Test
    void testUpdateProduct() {

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Updated Product");
        request.setDescription("Updated description");
        request.setBrand("Updated Brand");
        request.setColor("White");
        request.setPrice(new BigDecimal("1500"));
        request.setStock(20);
        request.setImageUrl("https://example.com/new.jpg");
        request.setCategoryId(2L);

        Category category = new Category();
        category.setId(2L);
        category.setName("Elektronik");

        Product product = new Product();
        product.setId(10L);
        product.setName("Old Product");
        product.setCategory(category);

        ProductResponse response = new ProductResponse();
        response.setId(10L);
        response.setName("Updated Product");
        response.setPrice(new BigDecimal("1500"));
        response.setStock(20);
        response.setActive(true);
        response.setCategoryId(2L);

        when(productRepository.findById(10L))
                .thenReturn(Optional.of(product));

        when(categoryRepository.findById(2L))
                .thenReturn(Optional.of(category));

        when(productRepository.save(product))
                .thenReturn(product);

        when(productMapper.toResponse(product))
                .thenReturn(response);

        ProductResponse result = productService.updateProduct(10L, request);

        assertEquals(10L, result.getId());
        assertEquals("Updated Product", result.getName());
        assertEquals(new BigDecimal("1500"), result.getPrice());
        assertEquals(20, result.getStock());
        assertEquals(2L, result.getCategoryId());
    }

    @Test
    void testDeleteProduct() {

        Product product = new Product();
        product.setId(10L);
        product.setName("Test Product");
        product.setActive(true);

        when(productRepository.findById(10L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(10L);

        assertEquals(false, product.getActive());

        org.mockito.Mockito.verify(productRepository)
                .save(product);

        org.mockito.Mockito.verify(productRepository, org.mockito.Mockito.never())
                .delete(product);
    }
}