package com.trendai.trendai.ai.tool;

import com.trendai.trendai.ai.tool.dto.ProductSearchResult;
import com.trendai.trendai.dto.ProductPageResponse;
import com.trendai.trendai.dto.ProductResponse;
import com.trendai.trendai.repository.CategoryRepository;
import com.trendai.trendai.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSearchToolTest {

    @Mock
    private ProductService productService;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductSearchTool productSearchTool;

    @Test
    void shouldSearchProductsWithFilters() {

        ProductResponse product = createProduct(
                1L,
                "iPhone 15",
                "Apple",
                new BigDecimal("20000"),
                5,
                "Elektronik"
        );

        ProductPageResponse pageResponse = createPageResponse(product);

        when(productService.searchProducts(
                0,
                10,
                "iphone",
                null,
                "Apple",
                new BigDecimal("10000"),
                new BigDecimal("25000"),
                "id,asc"
        )).thenReturn(pageResponse);

        List<ProductSearchResult> results = productSearchTool.searchProducts(
                "iphone",
                null,
                "Apple",
                new BigDecimal("10000"),
                new BigDecimal("25000"),
                "id,asc",
                10
        );

        assertEquals(1, results.size());

        ProductSearchResult result = results.get(0);

        assertEquals(1L, result.productId());
        assertEquals("iPhone 15", result.name());
        assertEquals("Apple", result.brand());
        assertEquals(new BigDecimal("20000"), result.price());
        assertEquals(5, result.stock());
        assertEquals("Elektronik", result.categoryName());

        verify(productService).searchProducts(
                0,
                10,
                "iphone",
                null,
                "Apple",
                new BigDecimal("10000"),
                new BigDecimal("25000"),
                "id,asc"
        );
    }

    @Test
    void shouldResolveCategoryNameToCategoryId() {

        var category = new com.trendai.trendai.entity.Category();
        category.setId(7L);
        category.setName("Elektronik");

        when(categoryRepository.findByNameIgnoreCaseAndActiveTrue("Elektronik"))
                .thenReturn(Optional.of(category));

        ProductPageResponse pageResponse = createPageResponse();

        when(productService.searchProducts(
                0,
                10,
                null,
                7L,
                null,
                null,
                null,
                "id,asc"
        )).thenReturn(pageResponse);

        List<ProductSearchResult> results = productSearchTool.searchProducts(
                null,
                "Elektronik",
                null,
                null,
                null,
                "id,asc",
                10
        );

        assertEquals(0, results.size());

        verify(categoryRepository)
                .findByNameIgnoreCaseAndActiveTrue("Elektronik");

        verify(productService).searchProducts(
                0,
                10,
                null,
                7L,
                null,
                null,
                null,
                "id,asc"
        );
    }

    @Test
    void shouldReturnAtMostTenResults() {

        List<ProductResponse> products =
                java.util.stream.IntStream.rangeClosed(1, 10)
                        .mapToObj(id -> createProduct(
                                (long) id,
                                "Product " + id,
                                "Brand",
                                new BigDecimal("100"),
                                5,
                                "Category"
                        ))
                        .toList();

        ProductPageResponse pageResponse = new ProductPageResponse();
        pageResponse.setContent(products);

        when(productService.searchProducts(
                0,
                10,
                null,
                null,
                null,
                null,
                null,
                "id,asc"
        )).thenReturn(pageResponse);

        List<ProductSearchResult> results = productSearchTool.searchProducts(
                null,
                null,
                null,
                null,
                null,
                "id,asc",
                10
        );

        assertEquals(10, results.size());
    }

    @Test
    void shouldUseRequestedLimit() {

        ProductResponse product1 = createProduct(
                1L,
                "Product 1",
                "Brand",
                new BigDecimal("100"),
                5,
                "Category"
        );

        ProductResponse product2 = createProduct(
                2L,
                "Product 2",
                "Brand",
                new BigDecimal("200"),
                5,
                "Category"
        );

        ProductResponse product3 = createProduct(
                3L,
                "Product 3",
                "Brand",
                new BigDecimal("300"),
                5,
                "Category"
        );

        ProductPageResponse pageResponse = createPageResponse(
                product1,
                product2,
                product3
        );

        when(productService.searchProducts(
                0,
                3,
                null,
                null,
                null,
                null,
                null,
                "price,asc"
        )).thenReturn(pageResponse);

        List<ProductSearchResult> results = productSearchTool.searchProducts(
                null,
                null,
                null,
                null,
                null,
                "price,asc",
                3
        );

        assertEquals(3, results.size());

        verify(productService).searchProducts(
                0,
                3,
                null,
                null,
                null,
                null,
                null,
                "price,asc"
        );
    }

    @Test
    void shouldUseDefaultLimitWhenLimitIsNull() {

        ProductPageResponse pageResponse = createPageResponse();

        when(productService.searchProducts(
                0,
                10,
                null,
                null,
                null,
                null,
                null,
                "id,asc"
        )).thenReturn(pageResponse);

        productSearchTool.searchProducts(
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        verify(productService).searchProducts(
                0,
                10,
                null,
                null,
                null,
                null,
                null,
                "id,asc"
        );
    }

    @Test
    void shouldRejectLimitAboveTen() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productSearchTool.searchProducts(
                        null,
                        null,
                        null,
                        null,
                        null,
                        "id,asc",
                        11
                )
        );

        verifyNoInteractions(productService);
    }

    @Test
    void shouldRejectLimitBelowOne() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productSearchTool.searchProducts(
                        null,
                        null,
                        null,
                        null,
                        null,
                        "id,asc",
                        0
                )
        );

        verifyNoInteractions(productService);
    }

    @Test
    void shouldRejectInvalidPriceRange() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productSearchTool.searchProducts(
                        null,
                        null,
                        null,
                        new BigDecimal("20000"),
                        new BigDecimal("10000"),
                        "id,asc",
                        10
                )
        );

        verifyNoInteractions(productService);
    }

    @Test
    void shouldRejectNegativePrices() {

        assertThrows(
                IllegalArgumentException.class,
                () -> productSearchTool.searchProducts(
                        null,
                        null,
                        null,
                        new BigDecimal("-1"),
                        null,
                        "id,asc",
                        10
                )
        );

        verifyNoInteractions(productService);
    }

    @Test
    void shouldRejectUnknownCategory() {

        when(categoryRepository.findByNameIgnoreCaseAndActiveTrue("Unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> productSearchTool.searchProducts(
                        null,
                        "Unknown",
                        null,
                        null,
                        null,
                        "id,asc",
                        10
                )
        );

        verifyNoInteractions(productService);
    }

    @Test
    void shouldAcceptCategoryIdWithoutRepositoryLookup() {

        ProductPageResponse pageResponse = createPageResponse();

        when(productService.searchProducts(
                0,
                10,
                null,
                7L,
                null,
                null,
                null,
                "id,asc"
        )).thenReturn(pageResponse);

        productSearchTool.searchProducts(
                null,
                "7",
                null,
                null,
                null,
                "id,asc",
                10
        );

        verify(productService).searchProducts(
                0,
                10,
                null,
                7L,
                null,
                null,
                null,
                "id,asc"
        );

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldNormalizeBlankValues() {

        ProductPageResponse pageResponse = createPageResponse();

        when(productService.searchProducts(
                0,
                10,
                null,
                null,
                null,
                null,
                null,
                "id,asc"
        )).thenReturn(pageResponse);

        productSearchTool.searchProducts(
                "   ",
                null,
                "   ",
                null,
                null,
                "   ",
                10
        );

        verify(productService).searchProducts(
                0,
                10,
                null,
                null,
                null,
                null,
                null,
                "id,asc"
        );
    }

    private ProductResponse createProduct(
            Long id,
            String name,
            String brand,
            BigDecimal price,
            Integer stock,
            String categoryName) {

        ProductResponse product = new ProductResponse();

        product.setId(id);
        product.setName(name);
        product.setBrand(brand);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategoryName(categoryName);

        return product;
    }

    private ProductPageResponse createPageResponse(
            ProductResponse... products) {

        ProductPageResponse response = new ProductPageResponse();

        response.setContent(List.of(products));

        return response;
    }

    @Test
    void shouldPassValidSortToProductService() {

        ProductPageResponse pageResponse = createPageResponse();

        when(productService.searchProducts(
                0,
                5,
                null,
                null,
                null,
                null,
                null,
                "price,desc"
        )).thenReturn(pageResponse);

        productSearchTool.searchProducts(
                null,
                null,
                null,
                null,
                null,
                "price,desc",
                5
        );

        verify(productService).searchProducts(
                0,
                5,
                null,
                null,
                null,
                null,
                null,
                "price,desc"
        );
    }

    @Test
    void shouldRejectInvalidSort() {

        when(productService.searchProducts(
                0,
                5,
                null,
                null,
                null,
                null,
                null,
                "hack,desc"
        )).thenThrow(
                new com.trendai.trendai.exception.BadRequestException(
                        "Invalid sort field"
                )
        );

        assertThrows(
                com.trendai.trendai.exception.BadRequestException.class,
                () -> productSearchTool.searchProducts(
                        null,
                        null,
                        null,
                        null,
                        null,
                        "hack,desc",
                        5
                )
        );
    }
}