package com.trendai.trendai.ai.tool;

import com.trendai.trendai.ai.tool.dto.ProductDetailsResult;
import com.trendai.trendai.dto.ProductResponse;
import com.trendai.trendai.service.ProductService;
import com.trendai.trendai.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductDetailsToolTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductDetailsTool productDetailsTool;

    @Test
    void shouldReturnProductDetails() {

        ProductResponse product = createProductResponse();

        when(productService.getProductById(1L))
                .thenReturn(product);

        ProductDetailsResult result =
                productDetailsTool.getProductDetails(1L);

        assertEquals(1L, result.id());
        assertEquals("iPhone 15", result.name());
        assertEquals("Powerful smartphone", result.description());
        assertEquals("Apple", result.brand());
        assertEquals("Black", result.color());
        assertEquals(new BigDecimal("20000"), result.price());
        assertEquals(5, result.stock());
        assertEquals(true, result.active());
        assertEquals("Elektronik", result.categoryName());

        verify(productService).getProductById(1L);
    }

    @Test
    void shouldRejectNullProductId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productDetailsTool.getProductDetails(null)
        );

        verifyNoInteractions(productService);
    }

    @Test
    void shouldRejectZeroProductId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productDetailsTool.getProductDetails(0L)
        );

        verifyNoInteractions(productService);
    }

    @Test
    void shouldRejectNegativeProductId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productDetailsTool.getProductDetails(-1L)
        );

        verifyNoInteractions(productService);
    }

    @Test
    void shouldPropagateProductNotFoundException() {

        when(productService.getProductById(999L))
                .thenThrow(
                        new ResourceNotFoundException("Product not found")
                );

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productDetailsTool.getProductDetails(999L)
        );

        assertEquals("Product not found", exception.getMessage());

        verify(productService).getProductById(999L);
    }

    private ProductResponse createProductResponse() {

        ProductResponse product = new ProductResponse();

        product.setId(1L);
        product.setName("iPhone 15");
        product.setDescription("Powerful smartphone");
        product.setBrand("Apple");
        product.setColor("Black");
        product.setPrice(new BigDecimal("20000"));
        product.setStock(5);
        product.setActive(true);
        product.setCategoryName("Elektronik");

        return product;
    }
}