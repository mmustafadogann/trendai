package com.trendai.trendai.mapper;

import com.trendai.trendai.dto.CreateProductRequest;
import com.trendai.trendai.dto.UpdateProductRequest;
import com.trendai.trendai.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductMapperTest {

    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();
    }

    @Test
    void testToEntityTrimsTextFields() {

        CreateProductRequest request = new CreateProductRequest();

        request.setName("  Telefon  ");
        request.setDescription("  Akıllı telefon  ");
        request.setBrand("  Apple  ");
        request.setColor("  Black  ");
        request.setPrice(new BigDecimal("85000"));
        request.setStock(10);
        request.setImageUrl("  iphone.jpg  ");

        Product product = productMapper.toEntity(request);

        assertEquals("Telefon", product.getName());
        assertEquals("  Akıllı telefon  ", product.getDescription());
        assertEquals("Apple", product.getBrand());
        assertEquals("Black", product.getColor());
        assertEquals(new BigDecimal("85000"), product.getPrice());
        assertEquals(10, product.getStock());
        assertEquals("  iphone.jpg  ", product.getImageUrl());
    }

    @Test
    void testUpdateEntityTrimsTextFields() {

        Product product = new Product();

        UpdateProductRequest request = new UpdateProductRequest();

        request.setName("  iPhone 17  ");
        request.setDescription("  Updated description  ");
        request.setBrand("  Apple  ");
        request.setColor("  Silver  ");
        request.setPrice(new BigDecimal("90000"));
        request.setStock(20);
        request.setImageUrl("  iphone17.jpg  ");

        productMapper.updateEntity(product, request);

        assertEquals("iPhone 17", product.getName());
        assertEquals("  Updated description  ", product.getDescription());
        assertEquals("Apple", product.getBrand());
        assertEquals("Silver", product.getColor());
        assertEquals(new BigDecimal("90000"), product.getPrice());
        assertEquals(20, product.getStock());
        assertEquals("  iphone17.jpg  ", product.getImageUrl());
    }

    @Test
    void testToEntityHandlesNullTextFields() {

        CreateProductRequest request = new CreateProductRequest();

        request.setName("Telefon");
        request.setBrand(null);
        request.setColor(null);

        Product product = productMapper.toEntity(request);

        assertEquals("Telefon", product.getName());
        assertNull(product.getBrand());
        assertNull(product.getColor());
    }
}