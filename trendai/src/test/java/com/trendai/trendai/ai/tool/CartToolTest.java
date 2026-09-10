package com.trendai.trendai.ai.tool;

import com.trendai.trendai.ai.tool.dto.CartToolResult;
import com.trendai.trendai.entity.Cart;
import com.trendai.trendai.entity.CartItem;
import com.trendai.trendai.entity.CartStatus;
import com.trendai.trendai.entity.Product;
import com.trendai.trendai.entity.User;
import com.trendai.trendai.exception.ResourceNotFoundException;
import com.trendai.trendai.repository.CartItemRepository;
import com.trendai.trendai.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartToolTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartTool cartTool;

    @Test
    void shouldReturnActiveCartWithItemsAndGrandTotal() {

        User user = new User();
        user.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product1 = new Product();
        product1.setId(100L);
        product1.setName("Laptop");

        CartItem item1 = new CartItem();
        item1.setId(1000L);
        item1.setCart(cart);
        item1.setProduct(product1);
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("500.00"));

        Product product2 = new Product();
        product2.setId(200L);
        product2.setName("Mouse");

        CartItem item2 = new CartItem();
        item2.setId(2000L);
        item2.setCart(cart);
        item2.setProduct(product2);
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("50.00"));

        when(cartRepository.findByUserIdAndStatus(
                1L,
                CartStatus.ACTIVE
        )).thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(item1, item2));

        CartToolResult result = cartTool.getActiveCart(1L);

        assertEquals(10L, result.cartId());
        assertEquals(1L, result.userId());
        assertEquals(2, result.items().size());

        assertEquals(
                new BigDecimal("1000.00"),
                result.items().get(0).lineTotal()
        );

        assertEquals(
                new BigDecimal("50.00"),
                result.items().get(1).lineTotal()
        );

        assertEquals(
                new BigDecimal("1050.00"),
                result.grandTotal()
        );

        assertEquals(
                100L,
                result.items().get(0).productId()
        );

        assertEquals(
                "Laptop",
                result.items().get(0).productName()
        );

        assertEquals(
                2,
                result.items().get(0).quantity()
        );

        assertEquals(
                new BigDecimal("500.00"),
                result.items().get(0).unitPrice()
        );

        verify(cartRepository)
                .findByUserIdAndStatus(1L, CartStatus.ACTIVE);

        verify(cartItemRepository)
                .findByCartId(10L);

        verifyNoMoreInteractions(cartRepository, cartItemRepository);
    }

    @Test
    void shouldReturnEmptyCartWhenCartHasNoItems() {

        User user = new User();
        user.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.findByUserIdAndStatus(
                1L,
                CartStatus.ACTIVE
        )).thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of());

        CartToolResult result = cartTool.getActiveCart(1L);

        assertEquals(10L, result.cartId());
        assertEquals(1L, result.userId());
        assertTrue(result.items().isEmpty());
        assertEquals(
                BigDecimal.ZERO,
                result.grandTotal()
        );

        verify(cartRepository)
                .findByUserIdAndStatus(1L, CartStatus.ACTIVE);

        verify(cartItemRepository)
                .findByCartId(10L);
    }

    @Test
    void shouldThrowExceptionWhenActiveCartDoesNotExist() {

        when(cartRepository.findByUserIdAndStatus(
                1L,
                CartStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartTool.getActiveCart(1L)
        );

        verify(cartRepository)
                .findByUserIdAndStatus(1L, CartStatus.ACTIVE);

        verifyNoInteractions(cartItemRepository);
    }

    @Test
    void shouldRejectNullUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cartTool.getActiveCart(null)
        );

        verifyNoInteractions(
                cartRepository,
                cartItemRepository
        );
    }

    @Test
    void shouldRejectZeroUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cartTool.getActiveCart(0L)
        );

        verifyNoInteractions(
                cartRepository,
                cartItemRepository
        );
    }

    @Test
    void shouldRejectNegativeUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cartTool.getActiveCart(-1L)
        );

        verifyNoInteractions(
                cartRepository,
                cartItemRepository
        );
    }
}