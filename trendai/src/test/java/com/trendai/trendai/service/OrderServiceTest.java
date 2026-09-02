package com.trendai.trendai.service;

import com.trendai.trendai.dto.OrderResponse;
import com.trendai.trendai.entity.Cart;
import com.trendai.trendai.entity.CartItem;
import com.trendai.trendai.entity.CartStatus;
import com.trendai.trendai.entity.Order;
import com.trendai.trendai.entity.OrderStatus;
import com.trendai.trendai.entity.Product;
import com.trendai.trendai.entity.User;
import com.trendai.trendai.exception.BusinessException;
import com.trendai.trendai.exception.ResourceNotFoundException;
import com.trendai.trendai.mapper.OrderMapper;
import com.trendai.trendai.repository.CartItemRepository;
import com.trendai.trendai.repository.CartRepository;
import com.trendai.trendai.repository.OrderRepository;
import com.trendai.trendai.repository.ProductRepository;
import com.trendai.trendai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.trendai.trendai.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.trendai.trendai.dto.UpdateOrderStatusRequest;
import com.trendai.trendai.dto.UpdateOrderStatusRequest;
import com.trendai.trendai.exception.BusinessException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void testCheckoutSuccess() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(2L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);
        product.setActive(true);

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(new BigDecimal("100.00"));

        Order savedOrder = new Order();
        savedOrder.setId(50L);
        savedOrder.setUser(user);
        savedOrder.setStatus(OrderStatus.CREATED);
        savedOrder.setTotalAmount(new BigDecimal("200.00"));

        OrderResponse response = new OrderResponse();
        response.setId(50L);
        response.setUserId(1L);
        response.setStatus(OrderStatus.CREATED);
        response.setTotalAmount(new BigDecimal("200.00"));

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(cartItem));

        when(productRepository.findByIdAndActiveTrue(2L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        when(orderMapper.toResponse(savedOrder))
                .thenReturn(response);

        OrderResponse result =
                orderService.checkout(10L, 1L);

        assertEquals(50L, result.getId());

        assertEquals(
                new BigDecimal("200.00"),
                result.getTotalAmount()
        );

        assertEquals(
                OrderStatus.CREATED,
                result.getStatus()
        );

        assertEquals(
                8,
                product.getStock()
        );

        assertEquals(
                CartStatus.ORDERED,
                cart.getStatus()
        );

        verify(productRepository).save(product);
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toResponse(savedOrder);
    }

    @Test
    void testCheckoutEmptyCart() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.checkout(10L, 1L)
        );

        assertEquals(
                "Cart is empty",
                exception.getMessage()
        );

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void testCheckoutCartNotFound() {

        when(cartRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.checkout(999L, 1L)
        );

        assertEquals(
                "Cart not found",
                exception.getMessage()
        );

        verify(cartItemRepository, never())
                .findByCartId(anyLong());

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void testCheckoutCartDoesNotBelongToUser() {

        User cartOwner = new User();
        cartOwner.setId(1L);
        cartOwner.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(cartOwner);
        cart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.checkout(10L, 999L)
        );

        assertEquals(
                "Cart does not belong to this user",
                exception.getMessage()
        );

        verify(cartItemRepository, never())
                .findByCartId(anyLong());

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void testCheckoutInactiveProduct() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(2L);
        product.setName("Inactive Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);
        product.setActive(false);

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(new BigDecimal("100.00"));

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(cartItem));

        when(productRepository.findByIdAndActiveTrue(2L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.checkout(10L, 1L)
        );

        assertEquals(
                "Product not found",
                exception.getMessage()
        );

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void testCheckoutProductNotFound() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(999L);
        product.setName("Nonexistent Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);
        product.setActive(true);

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(new BigDecimal("100.00"));

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(cartItem));

        when(productRepository.findByIdAndActiveTrue(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.checkout(10L, 1L)
        );

        assertEquals(
                "Product not found",
                exception.getMessage()
        );

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void testCheckoutInsufficientStock() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(2L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(2);
        product.setActive(true);

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(5);
        cartItem.setUnitPrice(new BigDecimal("100.00"));

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(cartItem));

        when(productRepository.findByIdAndActiveTrue(2L))
                .thenReturn(Optional.of(product));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.checkout(10L, 1L)
        );

        assertEquals(
                "Insufficient stock",
                exception.getMessage()
        );

        // Stok değişmemeli
        assertEquals(2, product.getStock());

        // Sipariş oluşturulmamalı
        verify(orderRepository, never())
                .save(any(Order.class));

        // Ürün kaydedilmemeli
        verify(productRepository, never())
                .save(any(Product.class));

        // Sepet ORDERED olmamalı
        assertEquals(
                CartStatus.ACTIVE,
                cart.getStatus()
        );
    }

    @Test
    void testCheckoutMultipleProductsStockDecrease() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product1 = new Product();
        product1.setId(2L);
        product1.setName("Product 1");
        product1.setPrice(new BigDecimal("100.00"));
        product1.setStock(10);
        product1.setActive(true);

        Product product2 = new Product();
        product2.setId(3L);
        product2.setName("Product 2");
        product2.setPrice(new BigDecimal("200.00"));
        product2.setStock(20);
        product2.setActive(true);

        CartItem item1 = new CartItem();
        item1.setId(100L);
        item1.setCart(cart);
        item1.setProduct(product1);
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("100.00"));

        CartItem item2 = new CartItem();
        item2.setId(101L);
        item2.setCart(cart);
        item2.setProduct(product2);
        item2.setQuantity(3);
        item2.setUnitPrice(new BigDecimal("200.00"));

        Order savedOrder = new Order();
        savedOrder.setId(50L);
        savedOrder.setUser(user);
        savedOrder.setStatus(OrderStatus.CREATED);
        savedOrder.setTotalAmount(new BigDecimal("800.00"));

        OrderResponse response = new OrderResponse();
        response.setId(50L);
        response.setUserId(1L);
        response.setStatus(OrderStatus.CREATED);
        response.setTotalAmount(new BigDecimal("800.00"));

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(item1, item2));

        when(productRepository.findByIdAndActiveTrue(2L))
                .thenReturn(Optional.of(product1));

        when(productRepository.findByIdAndActiveTrue(3L))
                .thenReturn(Optional.of(product2));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        when(orderMapper.toResponse(savedOrder))
                .thenReturn(response);

        OrderResponse result =
                orderService.checkout(10L, 1L);

        // Product 1: 10 - 2 = 8
        assertEquals(8, product1.getStock());

        // Product 2: 20 - 3 = 17
        assertEquals(17, product2.getStock());

        assertEquals(
                new BigDecimal("800.00"),
                result.getTotalAmount()
        );

        assertEquals(
                CartStatus.ORDERED,
                cart.getStatus()
        );

        verify(productRepository).save(product1);
        verify(productRepository).save(product2);
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toResponse(savedOrder);
    }

    @Test
    void testCheckoutLineTotalCalculation() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(2L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("125.50"));
        product.setStock(10);
        product.setActive(true);

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(4);
        cartItem.setUnitPrice(new BigDecimal("999.99"));

        Order savedOrder = new Order();
        savedOrder.setId(50L);
        savedOrder.setUser(user);
        savedOrder.setStatus(OrderStatus.CREATED);
        savedOrder.setTotalAmount(new BigDecimal("502.00"));

        OrderResponse response = new OrderResponse();
        response.setId(50L);
        response.setUserId(1L);
        response.setStatus(OrderStatus.CREATED);
        response.setTotalAmount(new BigDecimal("502.00"));

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(cartItem));

        when(productRepository.findByIdAndActiveTrue(2L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);

                    assertEquals(
                            new BigDecimal("502.00"),
                            order.getItems().get(0).getLineTotal()
                    );

                    return savedOrder;
                });

        when(orderMapper.toResponse(savedOrder))
                .thenReturn(response);

        OrderResponse result =
                orderService.checkout(10L, 1L);

        assertEquals(
                new BigDecimal("502.00"),
                result.getTotalAmount()
        );

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testCheckoutTotalAmountCalculation() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product1 = new Product();
        product1.setId(2L);
        product1.setName("Product 1");
        product1.setPrice(new BigDecimal("100.00"));
        product1.setStock(10);
        product1.setActive(true);

        Product product2 = new Product();
        product2.setId(3L);
        product2.setName("Product 2");
        product2.setPrice(new BigDecimal("250.00"));
        product2.setStock(10);
        product2.setActive(true);

        CartItem item1 = new CartItem();
        item1.setId(100L);
        item1.setCart(cart);
        item1.setProduct(product1);
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("100.00"));

        CartItem item2 = new CartItem();
        item2.setId(101L);
        item2.setCart(cart);
        item2.setProduct(product2);
        item2.setQuantity(3);
        item2.setUnitPrice(new BigDecimal("250.00"));

        Order savedOrder = new Order();
        savedOrder.setId(50L);
        savedOrder.setUser(user);
        savedOrder.setStatus(OrderStatus.CREATED);
        savedOrder.setTotalAmount(new BigDecimal("950.00"));

        OrderResponse response = new OrderResponse();
        response.setId(50L);
        response.setUserId(1L);
        response.setStatus(OrderStatus.CREATED);
        response.setTotalAmount(new BigDecimal("950.00"));

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(item1, item2));

        when(productRepository.findByIdAndActiveTrue(2L))
                .thenReturn(Optional.of(product1));

        when(productRepository.findByIdAndActiveTrue(3L))
                .thenReturn(Optional.of(product2));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);

                    assertEquals(
                            new BigDecimal("950.00"),
                            order.getTotalAmount()
                    );

                    return savedOrder;
                });

        when(orderMapper.toResponse(savedOrder))
                .thenReturn(response);

        OrderResponse result =
                orderService.checkout(10L, 1L);

        assertEquals(
                new BigDecimal("950.00"),
                result.getTotalAmount()
        );

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testCheckoutOrderItemSnapshot() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(2L);
        product.setName("Original Product");
        product.setPrice(new BigDecimal("125.50"));
        product.setStock(10);
        product.setActive(true);

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(3);
        cartItem.setUnitPrice(new BigDecimal("999.99"));

        Order savedOrder = new Order();
        savedOrder.setId(50L);
        savedOrder.setUser(user);
        savedOrder.setStatus(OrderStatus.CREATED);
        savedOrder.setTotalAmount(new BigDecimal("376.50"));

        OrderResponse response = new OrderResponse();
        response.setId(50L);
        response.setUserId(1L);
        response.setStatus(OrderStatus.CREATED);
        response.setTotalAmount(new BigDecimal("376.50"));

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(cartItem));

        when(productRepository.findByIdAndActiveTrue(2L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {

                    Order order = invocation.getArgument(0);

                    assertEquals(1, order.getItems().size());

                    OrderItem orderItem =
                            order.getItems().get(0);

                    assertEquals(
                            2L,
                            orderItem.getProductId()
                    );

                    assertEquals(
                            "Original Product",
                            orderItem.getProductName()
                    );

                    assertEquals(
                            3,
                            orderItem.getQuantity()
                    );

                    assertEquals(
                            new BigDecimal("125.50"),
                            orderItem.getUnitPrice()
                    );

                    assertEquals(
                            new BigDecimal("376.50"),
                            orderItem.getLineTotal()
                    );

                    return savedOrder;
                });

        when(orderMapper.toResponse(savedOrder))
                .thenReturn(response);

        orderService.checkout(10L, 1L);

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testCheckoutChangesCartStatusToOrdered() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(2L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);
        product.setActive(true);

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(new BigDecimal("100.00"));

        Order savedOrder = new Order();
        savedOrder.setId(50L);
        savedOrder.setUser(user);
        savedOrder.setStatus(OrderStatus.CREATED);
        savedOrder.setTotalAmount(new BigDecimal("200.00"));

        OrderResponse response = new OrderResponse();
        response.setId(50L);
        response.setUserId(1L);
        response.setStatus(OrderStatus.CREATED);
        response.setTotalAmount(new BigDecimal("200.00"));

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(cartItem));

        when(productRepository.findByIdAndActiveTrue(2L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);

        when(orderMapper.toResponse(savedOrder))
                .thenReturn(response);

        orderService.checkout(10L, 1L);

        assertEquals(
                CartStatus.ORDERED,
                cart.getStatus()
        );
    }

    @Test
    void testCheckoutAlreadyOrderedCart() {

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
                () -> orderService.checkout(10L, 1L)
        );

        assertEquals(
                "Cart is not active",
                exception.getMessage()
        );

        verify(cartItemRepository, never())
                .findByCartId(anyLong());

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void testGetOrderSuccess() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Order order = new Order();
        order.setId(50L);
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new BigDecimal("300.00"));

        OrderResponse response = new OrderResponse();
        response.setId(50L);
        response.setUserId(1L);
        response.setStatus(OrderStatus.CREATED);
        response.setTotalAmount(new BigDecimal("300.00"));

        when(orderRepository.findById(50L))
                .thenReturn(Optional.of(order));

        when(orderMapper.toResponse(order))
                .thenReturn(response);

        OrderResponse result =
                orderService.getOrder(50L, 1L);

        assertEquals(50L, result.getId());

        assertEquals(
                1L,
                result.getUserId()
        );

        assertEquals(
                OrderStatus.CREATED,
                result.getStatus()
        );

        assertEquals(
                new BigDecimal("300.00"),
                result.getTotalAmount()
        );

        verify(orderRepository).findById(50L);
        verify(orderMapper).toResponse(order);
    }

    @Test
    void testGetOrderNotFound() {

        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrder(999L, 1L)
        );

        assertEquals(
                "Order not found",
                exception.getMessage()
        );

        verify(orderRepository).findById(999L);

        verify(orderMapper, never())
                .toResponse(any(Order.class));
    }

    @Test
    void testGetUserOrdersPagination() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Order order1 = new Order();
        order1.setId(50L);
        order1.setUser(user);
        order1.setStatus(OrderStatus.CREATED);
        order1.setTotalAmount(new BigDecimal("100.00"));

        Order order2 = new Order();
        order2.setId(51L);
        order2.setUser(user);
        order2.setStatus(OrderStatus.PREPARING);
        order2.setTotalAmount(new BigDecimal("200.00"));

        List<Order> orders = List.of(order1, order2);

        Page<Order> orderPage =
                new PageImpl<>(
                        orders,
                        PageRequest.of(0, 2),
                        5
                );

        OrderResponse response1 = new OrderResponse();
        response1.setId(50L);
        response1.setUserId(1L);
        response1.setStatus(OrderStatus.CREATED);
        response1.setTotalAmount(new BigDecimal("100.00"));

        OrderResponse response2 = new OrderResponse();
        response2.setId(51L);
        response2.setUserId(1L);
        response2.setStatus(OrderStatus.PREPARING);
        response2.setTotalAmount(new BigDecimal("200.00"));

        when(orderRepository.findAllByUserId(
                eq(1L),
                any(Pageable.class)
        )).thenReturn(orderPage);

        when(orderMapper.toResponse(order1))
                .thenReturn(response1);

        when(orderMapper.toResponse(order2))
                .thenReturn(response2);

        Page<OrderResponse> result =
                orderService.getUserOrders(
                        1L,
                        PageRequest.of(0, 2)
                );

        assertEquals(2, result.getContent().size());

        assertEquals(5, result.getTotalElements());

        assertEquals(3, result.getTotalPages());

        assertEquals(0, result.getNumber());

        assertEquals(2, result.getSize());

        assertEquals(
                50L,
                result.getContent().get(0).getId()
        );

        assertEquals(
                51L,
                result.getContent().get(1).getId()
        );

        verify(orderRepository).findAllByUserId(
                eq(1L),
                any(Pageable.class)
        );

        verify(orderMapper).toResponse(order1);
        verify(orderMapper).toResponse(order2);
    }

    @Test
    void testUpdateStatusCreatedToPreparing() {

        Order order = new Order();
        order.setId(50L);
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(50L))
                .thenReturn(Optional.of(order));

        Order savedOrder = new Order();
        savedOrder.setId(50L);
        savedOrder.setStatus(OrderStatus.PREPARING);

        OrderResponse response = new OrderResponse();
        response.setId(50L);
        response.setStatus(OrderStatus.PREPARING);

        when(orderRepository.save(order))
                .thenReturn(savedOrder);

        when(orderMapper.toResponse(savedOrder))
                .thenReturn(response);

        com.trendai.trendai.dto.UpdateOrderStatusRequest request =
                new com.trendai.trendai.dto.UpdateOrderStatusRequest();

        request.setStatus(OrderStatus.PREPARING);

        OrderResponse result =
                orderService.updateStatus(50L, request);

        assertEquals(
                OrderStatus.PREPARING,
                order.getStatus()
        );

        assertEquals(
                OrderStatus.PREPARING,
                result.getStatus()
        );

        verify(orderRepository).findById(50L);
        verify(orderRepository).save(order);
        verify(orderMapper).toResponse(savedOrder);
    }

    @Test
    void testUpdateStatusValidTransitions() {

        Order order = new Order();
        order.setId(50L);
        order.setStatus(OrderStatus.PREPARING);

        OrderResponse preparingResponse = new OrderResponse();
        preparingResponse.setId(50L);
        preparingResponse.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(50L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        when(orderMapper.toResponse(order))
                .thenReturn(preparingResponse);

        UpdateOrderStatusRequest request =
                new UpdateOrderStatusRequest();

        request.setStatus(OrderStatus.SHIPPED);

        OrderResponse result =
                orderService.updateStatus(50L, request);

        assertEquals(
                OrderStatus.SHIPPED,
                order.getStatus()
        );

        assertEquals(
                OrderStatus.SHIPPED,
                result.getStatus()
        );

        request.setStatus(OrderStatus.DELIVERED);

        OrderResponse deliveredResponse =
                new OrderResponse();

        deliveredResponse.setId(50L);
        deliveredResponse.setStatus(OrderStatus.DELIVERED);

        when(orderMapper.toResponse(order))
                .thenReturn(deliveredResponse);

        result = orderService.updateStatus(50L, request);

        assertEquals(
                OrderStatus.DELIVERED,
                order.getStatus()
        );

        assertEquals(
                OrderStatus.DELIVERED,
                result.getStatus()
        );

        verify(orderRepository, times(2))
                .findById(50L);

        verify(orderRepository, times(2))
                .save(order);

        verify(orderMapper, times(2))
                .toResponse(order);
    }

    @Test
    void testUpdateStatusInvalidTransitions() {

        // CREATED -> DELIVERED ❌
        Order order1 = new Order();
        order1.setId(50L);
        order1.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(50L))
                .thenReturn(Optional.of(order1));

        UpdateOrderStatusRequest request1 =
                new UpdateOrderStatusRequest();

        request1.setStatus(OrderStatus.DELIVERED);

        BusinessException exception1 = assertThrows(
                BusinessException.class,
                () -> orderService.updateStatus(50L, request1)
        );

        assertEquals(
                "Invalid order status transition",
                exception1.getMessage()
        );

        assertEquals(
                OrderStatus.CREATED,
                order1.getStatus()
        );

        verify(orderRepository, never())
                .save(any(Order.class));


        // DELIVERED -> CREATED ❌
        Order order2 = new Order();
        order2.setId(51L);
        order2.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(51L))
                .thenReturn(Optional.of(order2));

        UpdateOrderStatusRequest request2 =
                new UpdateOrderStatusRequest();

        request2.setStatus(OrderStatus.CREATED);

        BusinessException exception2 = assertThrows(
                BusinessException.class,
                () -> orderService.updateStatus(51L, request2)
        );

        assertEquals(
                "Invalid order status transition",
                exception2.getMessage()
        );

        assertEquals(
                OrderStatus.DELIVERED,
                order2.getStatus()
        );


        // CANCELLED -> SHIPPED ❌
        Order order3 = new Order();
        order3.setId(52L);
        order3.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(52L))
                .thenReturn(Optional.of(order3));

        UpdateOrderStatusRequest request3 =
                new UpdateOrderStatusRequest();

        request3.setStatus(OrderStatus.SHIPPED);

        BusinessException exception3 = assertThrows(
                BusinessException.class,
                () -> orderService.updateStatus(52L, request3)
        );

        assertEquals(
                "Invalid order status transition",
                exception3.getMessage()
        );

        assertEquals(
                OrderStatus.CANCELLED,
                order3.getStatus()
        );
    }

    @Test
    void testCheckoutOptimisticLockingFailure() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        Product product = new Product();
        product.setId(2L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(1);
        product.setActive(true);
        product.setVersion(1L);

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setUnitPrice(new BigDecimal("100.00"));

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(cartItem));

        when(productRepository.findByIdAndActiveTrue(2L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenThrow(
                        new org.springframework.orm.ObjectOptimisticLockingFailureException(
                                Product.class,
                                2L
                        )
                );

        assertThrows(
                org.springframework.orm.ObjectOptimisticLockingFailureException.class,
                () -> orderService.checkout(10L, 1L)
        );

        verify(productRepository).save(product);

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void testCheckoutCartOptimisticLockingFailure() {

        User user = new User();
        user.setId(1L);
        user.setActive(true);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setVersion(1L);

        Product product = new Product();
        product.setId(2L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);
        product.setActive(true);

        CartItem cartItem = new CartItem();
        cartItem.setId(100L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setUnitPrice(new BigDecimal("100.00"));

        when(cartRepository.findById(10L))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(cartItem));

        when(productRepository.findByIdAndActiveTrue(2L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        when(cartRepository.save(any(Cart.class)))
                .thenThrow(
                        new org.springframework.orm.ObjectOptimisticLockingFailureException(
                                Cart.class,
                                10L
                        )
                );

        assertThrows(
                org.springframework.orm.ObjectOptimisticLockingFailureException.class,
                () -> orderService.checkout(10L, 1L)
        );

        verify(cartRepository).save(cart);

        verify(orderRepository, never())
                .save(any(Order.class));
    }
}