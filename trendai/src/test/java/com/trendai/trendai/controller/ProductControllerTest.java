package com.trendai.trendai.controller;

import com.trendai.trendai.exception.BadRequestException;
import com.trendai.trendai.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void negativePageReturns400() throws Exception {

        mockMvc.perform(
                get("/api/products")
                        .param("page", "-1")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void zeroSizeReturns400() throws Exception {

        mockMvc.perform(
                get("/api/products")
                        .param("size", "0")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void sizeGreaterThan100Returns400() throws Exception {

        mockMvc.perform(
                get("/api/products")
                        .param("size", "101")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void negativeMinPriceReturns400() throws Exception {

        mockMvc.perform(
                get("/api/products")
                        .param("minPrice", "-1")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void negativeMaxPriceReturns400() throws Exception {

        mockMvc.perform(
                get("/api/products")
                        .param("maxPrice", "-1")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void minPriceGreaterThanMaxPriceReturns400() throws Exception {

        when(productService.searchProducts(
                anyInt(),
                anyInt(),
                any(),
                any(),
                any(),
                any(),
                any(),
                eq("id,asc")
        )).thenThrow(
                new BadRequestException(
                        "minPrice cannot be greater than maxPrice"
                )
        );

        mockMvc.perform(
                get("/api/products")
                        .param("minPrice", "10000")
                        .param("maxPrice", "5000")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void invalidSortFieldReturns400() throws Exception {

        when(productService.searchProducts(
                anyInt(),
                anyInt(),
                any(),
                any(),
                any(),
                any(),
                any(),
                eq("createdAt,asc")
        )).thenThrow(
                new BadRequestException("Invalid sort field")
        );

        mockMvc.perform(
                get("/api/products")
                        .param("sort", "createdAt,asc")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void invalidSortDirectionReturns400() throws Exception {

        when(productService.searchProducts(
                anyInt(),
                anyInt(),
                any(),
                any(),
                any(),
                any(),
                any(),
                eq("price,random")
        )).thenThrow(
                new BadRequestException("Invalid sort direction")
        );

        mockMvc.perform(
                get("/api/products")
                        .param("sort", "price,random")
        ).andExpect(status().isBadRequest());
    }
}