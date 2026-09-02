package com.trendai.trendai.service;

import com.trendai.trendai.entity.Cart;
import com.trendai.trendai.entity.CartItem;
import com.trendai.trendai.entity.CartStatus;
import com.trendai.trendai.entity.Category;
import com.trendai.trendai.entity.Product;
import com.trendai.trendai.entity.User;
import com.trendai.trendai.exception.BusinessException;
import com.trendai.trendai.repository.CartItemRepository;
import com.trendai.trendai.repository.CartRepository;
import com.trendai.trendai.repository.CategoryRepository;
import com.trendai.trendai.repository.OrderRepository;
import com.trendai.trendai.repository.ProductRepository;
import com.trendai.trendai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void checkoutFailureShouldNotPersistAnyChanges() {

        User user = new User();
        user.setEmail("integration@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);

        user = userRepository.save(user);

        Category category = new Category();
        category.setName("Integration Test Category");
        category.setDescription("Test category");
        category.setActive(true);

        category = categoryRepository.save(category);

        Product product1 = new Product();
        product1.setName("Product 1");
        product1.setPrice(new BigDecimal("100.00"));
        product1.setStock(10);
        product1.setActive(true);
        product1.setCategory(category);

        Product product2 = new Product();
        product2.setName("Product 2");
        product2.setPrice(new BigDecimal("200.00"));
        product2.setStock(1);
        product2.setActive(true);
        product2.setCategory(category);

        product1 = productRepository.save(product1);
        product2 = productRepository.save(product2);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        cart = cartRepository.save(cart);

        CartItem item1 = new CartItem();
        item1.setCart(cart);
        item1.setProduct(product1);
        item1.setQuantity(2);
        item1.setUnitPrice(product1.getPrice());

        CartItem item2 = new CartItem();
        item2.setCart(cart);
        item2.setProduct(product2);
        item2.setQuantity(5);
        item2.setUnitPrice(product2.getPrice());

        cartItemRepository.saveAll(List.of(item1, item2));

        Long cartId = cart.getId();
        Long userId = user.getId();
        Long product1Id = product1.getId();

        assertThrows(
                BusinessException.class,
                () -> orderService.checkout(cartId, userId)
        );

        assertEquals(
                0,
                orderRepository.count()
        );

        Product productAfterFailure =
                productRepository.findById(product1Id).orElseThrow();

        assertEquals(
                10,
                productAfterFailure.getStock()
        );

        Cart cartAfterFailure =
                cartRepository.findById(cartId).orElseThrow();

        assertEquals(
                CartStatus.ACTIVE,
                cartAfterFailure.getStatus()
        );
    }
}