package com.trendai.trendai.repository;

import com.trendai.trendai.entity.Category;
import com.trendai.trendai.entity.Product;
import com.trendai.trendai.specification.ProductSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category electronics;

    @BeforeEach
    void setUp() {

        productRepository.deleteAll();
        categoryRepository.deleteAll();

        electronics = new Category();
        electronics.setName("Repository Test Electronics");
        electronics.setDescription("Repository test category");
        electronics.setActive(true);

        electronics = categoryRepository.save(electronics);
    }

    @Test
    void testFindByBrandCaseInsensitive() {

        Product product = createProduct(
                "iPhone",
                "Apple",
                new BigDecimal("50000"),
                true
        );

        productRepository.save(product);

        Page<Product> result = productRepository.findAll(
                ProductSpecification.isActive()
                        .and(ProductSpecification.hasBrand("apple")),
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Apple",
                result.getContent().get(0).getBrand()
        );
    }

    @Test
    void testFindByKeywordCaseInsensitive() {

        Product product = createProduct(
                "iPhone Telefon",
                "Apple",
                new BigDecimal("50000"),
                true
        );

        productRepository.save(product);

        Page<Product> result = productRepository.findAll(
                ProductSpecification.isActive()
                        .and(ProductSpecification.hasKeyword("IPHONE")),
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "iPhone Telefon",
                result.getContent().get(0).getName()
        );
    }

    @Test
    void testFindByMinAndMaxPrice() {

        productRepository.save(
                createProduct(
                        "Cheap Product",
                        "Apple",
                        new BigDecimal("5000"),
                        true
                )
        );

        productRepository.save(
                createProduct(
                        "Expensive Product",
                        "Apple",
                        new BigDecimal("15000"),
                        true
                )
        );

        Page<Product> result = productRepository.findAll(
                ProductSpecification.isActive()
                        .and(
                                ProductSpecification.hasMinPrice(
                                        new BigDecimal("5000")
                                )
                        )
                        .and(
                                ProductSpecification.hasMaxPrice(
                                        new BigDecimal("10000")
                                )
                        ),
                PageRequest.of(
                        0,
                        10,
                        Sort.by(Sort.Direction.ASC, "price")
                )
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                new BigDecimal("5000"),
                result.getContent().get(0).getPrice()
        );
    }

    @Test
    void testInactiveProductIsNotReturned() {

        Product activeProduct = createProduct(
                "Active Product",
                "Apple",
                new BigDecimal("5000"),
                true
        );

        Product inactiveProduct = createProduct(
                "Inactive Product",
                "Apple",
                new BigDecimal("5000"),
                false
        );

        productRepository.save(activeProduct);
        productRepository.save(inactiveProduct);

        Page<Product> result = productRepository.findAll(
                ProductSpecification.isActive(),
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Active Product",
                result.getContent().get(0).getName()
        );
    }

    @Test
    void testFindByCategory() {

        Product product = createProduct(
                "Category Product",
                "Apple",
                new BigDecimal("5000"),
                true
        );

        productRepository.save(product);

        Page<Product> result = productRepository.findAll(
                ProductSpecification.isActive()
                        .and(
                                ProductSpecification.hasCategoryId(
                                        electronics.getId()
                                )
                        ),
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                electronics.getId(),
                result.getContent().get(0).getCategory().getId()
        );
    }

    private Product createProduct(
            String name,
            String brand,
            BigDecimal price,
            boolean active) {

        Product product = new Product();

        product.setName(name);
        product.setDescription("Repository test product");
        product.setBrand(brand);
        product.setColor("Black");
        product.setPrice(price);
        product.setStock(10);
        product.setImageUrl("test.jpg");
        product.setActive(active);
        product.setCategory(electronics);

        return product;
    }
}