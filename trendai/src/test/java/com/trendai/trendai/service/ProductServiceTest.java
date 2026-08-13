package com.trendai.trendai.service;

import com.trendai.trendai.dto.CreateProductRequest;
import com.trendai.trendai.dto.ProductPageResponse;
import com.trendai.trendai.dto.ProductResponse;
import com.trendai.trendai.dto.UpdateProductRequest;
import com.trendai.trendai.entity.Category;
import com.trendai.trendai.entity.Product;
import com.trendai.trendai.exception.ResourceNotFoundException;
import com.trendai.trendai.mapper.ProductMapper;
import com.trendai.trendai.repository.CategoryRepository;
import com.trendai.trendai.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        category.setActive(true);

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

        when(categoryRepository.findByIdAndActiveTrue(2L))
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

        when(productRepository.findByIdAndActiveTrue(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(999L, request)
        );
    }

    @Test
    void testDeleteProductNotFound() {

        when(productRepository.findByIdAndActiveTrue(999L))
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
        category.setActive(true);

        Product product = new Product();
        product.setId(10L);
        product.setName("Old Product");
        product.setCategory(category);
        product.setActive(true);

        ProductResponse response = new ProductResponse();
        response.setId(10L);
        response.setName("Updated Product");
        response.setPrice(new BigDecimal("1500"));
        response.setStock(20);
        response.setActive(true);
        response.setCategoryId(2L);

        when(productRepository.findByIdAndActiveTrue(10L))
                .thenReturn(Optional.of(product));

        when(categoryRepository.findByIdAndActiveTrue(2L))
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

        when(productRepository.findByIdAndActiveTrue(10L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(10L);

        assertEquals(false, product.getActive());

        verify(productRepository).save(product);
        verify(productRepository, org.mockito.Mockito.never())
                .delete(product);
    }

    @Test
    void testGetProductByIdInactiveProduct() {

        when(productRepository.findByIdAndActiveTrue(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(10L)
        );
    }

    @Test
    void testSearchProducts() {

        Category category = new Category();
        category.setId(2L);
        category.setName("Elektronik");
        category.setActive(true);

        Product product1 = new Product();
        product1.setId(10L);
        product1.setName("iPhone");
        product1.setPrice(new BigDecimal("85000"));
        product1.setStock(10);
        product1.setActive(true);
        product1.setCategory(category);

        Product product2 = new Product();
        product2.setId(11L);
        product2.setName("MacBook");
        product2.setPrice(new BigDecimal("120000"));
        product2.setStock(5);
        product2.setActive(true);
        product2.setCategory(category);

        ProductResponse response1 = new ProductResponse();
        response1.setId(10L);
        response1.setName("iPhone");
        response1.setPrice(new BigDecimal("85000"));
        response1.setStock(10);
        response1.setActive(true);
        response1.setCategoryId(2L);
        response1.setCategoryName("Elektronik");

        ProductResponse response2 = new ProductResponse();
        response2.setId(11L);
        response2.setName("MacBook");
        response2.setPrice(new BigDecimal("120000"));
        response2.setStock(5);
        response2.setActive(true);
        response2.setCategoryId(2L);
        response2.setCategoryName("Elektronik");

        PageImpl<Product> productPage =
                new PageImpl<>(
                        List.of(product1, product2),
                        PageRequest.of(
                                0,
                                2,
                                Sort.by(Sort.Direction.ASC, "price")
                        ),
                        5
                );

        when(productRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Product>>any(),
                any(Pageable.class)
        )).thenReturn(productPage);

        when(productMapper.toResponse(product1))
                .thenReturn(response1);

        when(productMapper.toResponse(product2))
                .thenReturn(response2);

        ProductPageResponse result = productService.searchProducts(
                0,
                2,
                "iphone",
                2L,
                "Apple",
                new BigDecimal("50000"),
                new BigDecimal("150000"),
                "price,asc"
        );

        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals(true, result.isFirst());
        assertEquals(false, result.isLast());

        assertEquals(
                "iPhone",
                result.getContent().get(0).getName()
        );

        assertEquals(
                new BigDecimal("85000"),
                result.getContent().get(0).getPrice()
        );
    }

    @Test
    void testSearchProductsReturnsOnlyActiveProducts() {

        Category category = new Category();
        category.setId(2L);
        category.setName("Elektronik");
        category.setActive(true);

        Product activeProduct = new Product();
        activeProduct.setId(10L);
        activeProduct.setName("Active Product");
        activeProduct.setPrice(new BigDecimal("1000"));
        activeProduct.setActive(true);
        activeProduct.setCategory(category);

        ProductResponse response = new ProductResponse();
        response.setId(10L);
        response.setName("Active Product");
        response.setPrice(new BigDecimal("1000"));
        response.setActive(true);
        response.setCategoryId(2L);
        response.setCategoryName("Elektronik");

        PageImpl<Product> productPage =
                new PageImpl<>(
                        List.of(activeProduct),
                        PageRequest.of(0, 10),
                        1
                );

        when(productRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Product>>any(),
                any(Pageable.class)
        )).thenReturn(productPage);

        when(productMapper.toResponse(activeProduct))
                .thenReturn(response);

        ProductPageResponse result = productService.searchProducts(
                0,
                10,
                null,
                null,
                null,
                null,
                null,
                "id,asc"
        );

        assertEquals(1, result.getContent().size());
        assertEquals(true, result.getContent().get(0).getActive());
        assertEquals(
                "Active Product",
                result.getContent().get(0).getName()
        );
    }

    @Test
    void testUpdateInactiveProduct() {

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Updated Product");
        request.setDescription("Updated description");
        request.setBrand("Updated Brand");
        request.setColor("White");
        request.setPrice(new BigDecimal("1500"));
        request.setStock(20);
        request.setImageUrl("https://example.com/new.jpg");
        request.setCategoryId(2L);

        when(productRepository.findByIdAndActiveTrue(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(10L, request)
        );
    }

    @Test
    void testDeleteInactiveProductAgain() {

        when(productRepository.findByIdAndActiveTrue(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct(10L)
        );
    }

    @Test
    void testSearchDoesNotIncludeInactiveProduct() {

        PageImpl<Product> emptyPage =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(0, 10),
                        0
                );

        when(productRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Product>>any(),
                any(Pageable.class)
        )).thenReturn(emptyPage);

        ProductPageResponse result = productService.searchProducts(
                0,
                10,
                null,
                null,
                null,
                null,
                null,
                "id,asc"
        );

        assertEquals(0, result.getContent().size());
    }
}