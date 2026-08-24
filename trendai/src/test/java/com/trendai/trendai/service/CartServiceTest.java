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
import com.trendai.trendai.dto.UpdateCartItemQuantityRequest;
import com.trendai.trendai.dto.CartResponse;

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

    @Test
    void testUpdateItemQuantity() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(20L);
        product.setStock(10);
        product.setPrice(new BigDecimal("1000"));

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(new BigDecimal("1000"));

        UpdateCartItemQuantityRequest request =
                new UpdateCartItemQuantityRequest();

        request.setQuantity(5);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findById(100L))
                .thenReturn(Optional.of(cartItem));

        when(cartItemRepository.save(cartItem))
                .thenReturn(cartItem);

        cartService.updateItemQuantity(10L, 100L, request);

        assertEquals(5, cartItem.getQuantity());

        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void testUpdateItemQuantityExceedsStock() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(20L);
        product.setStock(5);
        product.setPrice(new BigDecimal("1000"));

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(new BigDecimal("1000"));

        UpdateCartItemQuantityRequest request =
                new UpdateCartItemQuantityRequest();

        request.setQuantity(6);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findById(100L))
                .thenReturn(Optional.of(cartItem));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.updateItemQuantity(10L, 100L, request)
        );

        assertEquals("Insufficient stock", exception.getMessage());
    }

    @Test
    void testUpdateItemDoesNotBelongToCart() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Cart anotherCart = new Cart();
        anotherCart.setId(20L);
        anotherCart.setUser(user);
        anotherCart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(30L);
        product.setStock(10);
        product.setPrice(new BigDecimal("1000"));

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(anotherCart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(new BigDecimal("1000"));

        UpdateCartItemQuantityRequest request =
                new UpdateCartItemQuantityRequest();

        request.setQuantity(5);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findById(100L))
                .thenReturn(Optional.of(cartItem));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.updateItemQuantity(10L, 100L, request)
        );

        assertEquals(
                "Cart item does not belong to this cart",
                exception.getMessage()
        );
    }

    @Test
    void testUpdateItemCartNotFound() {

        UpdateCartItemQuantityRequest request =
                new UpdateCartItemQuantityRequest();

        request.setQuantity(5);

        when(cartRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.updateItemQuantity(999L, 100L, request)
        );

        assertEquals(
                "Cart not found",
                exception.getMessage()
        );
    }

    @Test
    void testUpdateItemQuantityInactiveCart() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ORDERED);

        UpdateCartItemQuantityRequest request =
                new UpdateCartItemQuantityRequest();

        request.setQuantity(5);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.updateItemQuantity(10L, 100L, request)
        );

        assertEquals(
                "Cart is not active",
                exception.getMessage()
        );
    }

    @Test
    void testDeleteItem() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(20L);
        product.setStock(10);

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findById(100L))
                .thenReturn(Optional.of(cartItem));

        cartService.deleteItem(10L, 100L);

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void testDeleteItemDoesNotBelongToCart() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Cart anotherCart = new Cart();
        anotherCart.setId(20L);
        anotherCart.setUser(user);
        anotherCart.setStatus(CartStatus.ACTIVE);

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(anotherCart);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findById(100L))
                .thenReturn(Optional.of(cartItem));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.deleteItem(10L, 100L)
        );

        assertEquals(
                "Cart item does not belong to this cart",
                exception.getMessage()
        );
    }

    @Test
    void testDeleteItemCartNotFound() {

        when(cartRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.deleteItem(999L, 100L)
        );

        assertEquals(
                "Cart not found",
                exception.getMessage()
        );
    }

    @Test
    void testDeleteItemNotFound() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.deleteItem(10L, 999L)
        );

        assertEquals(
                "Cart item not found",
                exception.getMessage()
        );
    }

    @Test
    void testDeleteItemFromInactiveCart() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ORDERED);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.deleteItem(10L, 100L)
        );

        assertEquals(
                "Cart is not active",
                exception.getMessage()
        );
    }

    @Test
    void testDeleteLastItemDoesNotDeleteCart() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(cart);
        cartItem.setQuantity(1);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findById(100L))
                .thenReturn(Optional.of(cartItem));

        cartService.deleteItem(10L, 100L);

        verify(cartItemRepository).delete(cartItem);

        org.mockito.Mockito.verify(
                cartRepository,
                org.mockito.Mockito.never()
        ).delete(cart);
    }

    @Test
    void testGetOrCreateCartCreatesNewCart() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        when(userRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserIdAndStatus(
                1L,
                CartStatus.ACTIVE
        )).thenReturn(Optional.empty());

        Cart savedCart = new Cart();
        savedCart.setId(10L);
        savedCart.setUser(user);
        savedCart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.save(any(Cart.class)))
                .thenReturn(savedCart);

        CartResponse response = new CartResponse();
        response.setId(10L);
        response.setUserId(1L);
        response.setStatus(CartStatus.ACTIVE);
        response.setTotalAmount(BigDecimal.ZERO);

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(java.util.Collections.emptyList());

        CartResponse result = cartService.getOrCreateCart(1L);

        assertEquals(10L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(CartStatus.ACTIVE, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void testGetOrCreateCartReturnsExistingActiveCart() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart existingCart = new Cart();
        existingCart.setId(10L);
        existingCart.setUser(user);
        existingCart.setStatus(CartStatus.ACTIVE);

        when(userRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserIdAndStatus(
                1L,
                CartStatus.ACTIVE
        )).thenReturn(Optional.of(existingCart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(java.util.Collections.emptyList());

        CartResponse result = cartService.getOrCreateCart(1L);

        assertEquals(10L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(CartStatus.ACTIVE, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());

        verify(cartRepository, org.mockito.Mockito.never())
                .save(any(Cart.class));
    }

    @Test
    void testAddInactiveProduct() {

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setStatus(CartStatus.ACTIVE);

        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(20L);
        request.setQuantity(2);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findByIdAndActiveTrue(20L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addItem(10L, request)
        );

        assertEquals(
                "Product not found",
                exception.getMessage()
        );

        verify(cartItemRepository,
                org.mockito.Mockito.never())
                .save(any(CartItem.class));
    }

    @Test
    void testAddNonExistingProductDoesNotSaveItem() {

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

        assertEquals(
                "Product not found",
                exception.getMessage()
        );

        verify(cartItemRepository,
                org.mockito.Mockito.never())
                .save(any(CartItem.class));
    }

    @Test
    void testCartTotalIsCalculatedCorrectly() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product1 = new Product();
        product1.setId(20L);
        product1.setName("Product 1");

        Product product2 = new Product();
        product2.setId(30L);
        product2.setName("Product 2");

        CartItem item1 = new CartItem();
        item1.setId(100L);
        item1.setCart(cart);
        item1.setProduct(product1);
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("100"));

        CartItem item2 = new CartItem();
        item2.setId(101L);
        item2.setCart(cart);
        item2.setProduct(product2);
        item2.setQuantity(3);
        item2.setUnitPrice(new BigDecimal("50"));

        when(userRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUserIdAndStatus(
                1L,
                CartStatus.ACTIVE
        )).thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(java.util.List.of(item1, item2));

        CartResponse result = cartService.getOrCreateCart(1L);

        assertEquals(
                new BigDecimal("350"),
                result.getTotalAmount()
        );

        assertEquals(2, result.getItems().size());

        verify(cartItemRepository).findByCartId(10L);
    }
}