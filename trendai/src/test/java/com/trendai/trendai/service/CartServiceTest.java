package com.trendai.trendai.service;

import com.trendai.trendai.dto.AddCartItemRequest;
import com.trendai.trendai.entity.Cart;
import com.trendai.trendai.entity.CartItem;
import com.trendai.trendai.entity.CartStatus;
import com.trendai.trendai.entity.Product;
import com.trendai.trendai.entity.User;
import com.trendai.trendai.repository.CartItemRepository;
import com.trendai.trendai.repository.CartRepository;
import com.trendai.trendai.repository.ProductRepository;
import com.trendai.trendai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.trendai.trendai.exception.BusinessException;
import com.trendai.trendai.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void testAddItemCreatesNewCartItem() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(20L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("1000"));
        product.setStock(10);
        product.setActive(true);

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(20L);
        request.setQuantity(2);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findByIdAndActiveTrue(20L))
                .thenReturn(Optional.of(product));

        when(cartItemRepository.findByCartIdAndProductId(10L, 20L))
                .thenReturn(Optional.empty());

        CartItem savedItem = new CartItem();
        savedItem.setId(100L);
        savedItem.setCart(cart);
        savedItem.setProduct(product);
        savedItem.setQuantity(2);
        savedItem.setUnitPrice(new BigDecimal("1000"));

        when(cartItemRepository.save(any(CartItem.class)))
                .thenReturn(savedItem);

        cartService.addItem(10L, request);

        ArgumentCaptor<CartItem> captor =
                ArgumentCaptor.forClass(CartItem.class);

        verify(cartItemRepository).save(captor.capture());

        CartItem createdItem = captor.getValue();

        assertEquals(cart, createdItem.getCart());
        assertEquals(product, createdItem.getProduct());
        assertEquals(2, createdItem.getQuantity());
        assertEquals(
                new BigDecimal("1000"),
                createdItem.getUnitPrice()
        );
    }

    @Test
    void testAddItemIncreasesExistingQuantity() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(20L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("1000"));
        product.setStock(10);
        product.setActive(true);

        CartItem existingItem = new CartItem();
        existingItem.setId(100L);
        existingItem.setCart(cart);
        existingItem.setProduct(product);
        existingItem.setQuantity(3);
        existingItem.setUnitPrice(new BigDecimal("1000"));

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(20L);
        request.setQuantity(2);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findByIdAndActiveTrue(20L))
                .thenReturn(Optional.of(product));

        when(cartItemRepository.findByCartIdAndProductId(10L, 20L))
                .thenReturn(Optional.of(existingItem));

        when(cartItemRepository.save(existingItem))
                .thenReturn(existingItem);

        cartService.addItem(10L, request);

        assertEquals(5, existingItem.getQuantity());

        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void testAddItemExceedsStock() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(20L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("1000"));
        product.setStock(5);
        product.setActive(true);

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(20L);
        request.setQuantity(6);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findByIdAndActiveTrue(20L))
                .thenReturn(Optional.of(product));

        when(cartItemRepository.findByCartIdAndProductId(10L, 20L))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.addItem(10L, request)
        );

        assertEquals("Insufficient stock", exception.getMessage());
    }

    @Test
    void testAddItemCartNotFound() {

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(20L);
        request.setQuantity(2);

        when(cartRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addItem(999L, request)
        );

        assertEquals("Cart not found", exception.getMessage());
    }

    @Test
    void testAddItemToInactiveCart() {

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setStatus(CartStatus.ORDERED);

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(20L);
        request.setQuantity(2);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.addItem(10L, request)
        );

        assertEquals("Cart is not active", exception.getMessage());
    }

    @Test
    void testAddItemProductNotFound() {

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setStatus(CartStatus.ACTIVE);

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(999L);
        request.setQuantity(2);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findByIdAndActiveTrue(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addItem(10L, request)
        );

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void testAddItemWithInvalidQuantity() {

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(20L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("1000"));
        product.setStock(10);
        product.setActive(true);

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(20L);
        request.setQuantity(0);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findByIdAndActiveTrue(20L))
                .thenReturn(Optional.of(product));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.addItem(10L, request)
        );

        assertEquals(
                "Quantity must be at least 1",
                exception.getMessage()
        );
    }

    @Test
    void testAddItemExistingQuantityExceedsStock() {

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(20L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("1000"));
        product.setStock(5);
        product.setActive(true);

        CartItem existingItem = new CartItem();
        existingItem.setId(100L);
        existingItem.setCart(cart);
        existingItem.setProduct(product);
        existingItem.setQuantity(3);
        existingItem.setUnitPrice(new BigDecimal("1000"));

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(20L);
        request.setQuantity(3);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findByIdAndActiveTrue(20L))
                .thenReturn(Optional.of(product));

        when(cartItemRepository.findByCartIdAndProductId(10L, 20L))
                .thenReturn(Optional.of(existingItem));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.addItem(10L, request)
        );

        assertEquals("Insufficient stock", exception.getMessage());
    }
}