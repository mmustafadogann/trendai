package com.trendai.trendai.service;

import com.trendai.trendai.dto.OrderResponse;
import com.trendai.trendai.entity.Cart;
import com.trendai.trendai.entity.CartItem;
import com.trendai.trendai.entity.CartStatus;
import com.trendai.trendai.entity.Order;
import com.trendai.trendai.entity.OrderItem;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            OrderMapper orderMapper
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Transactional
    public OrderResponse checkout(Long cartId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found"));

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new BusinessException("Cart is not active");
        }

        User user = cart.getUser();

        if (user == null || !user.isActive()) {
            throw new ResourceNotFoundException("User not found");
        }

        List<CartItem> cartItems =
                cartItemRepository.findByCartId(cartId);

        if (cartItems.isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<Product> products = new ArrayList<>();

        for (CartItem cartItem : cartItems) {

            Product product = productRepository
                    .findByIdAndActiveTrue(
                            cartItem.getProduct().getId()
                    )
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Product not found"));

            if (cartItem.getQuantity() > product.getStock()) {
                throw new BusinessException("Insufficient stock");
            }

            products.add(product);

            BigDecimal unitPrice = product.getPrice();

            BigDecimal lineTotal = unitPrice.multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())
            );

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setLineTotal(lineTotal);

            order.getItems().add(orderItem);

            totalAmount = totalAmount.add(lineTotal);
        }

        order.setTotalAmount(totalAmount);

        for (int i = 0; i < cartItems.size(); i++) {

            CartItem cartItem = cartItems.get(i);
            Product product = products.get(i);

            product.setStock(
                    product.getStock() - cartItem.getQuantity()
            );

            productRepository.save(product);
        }

        cart.setStatus(CartStatus.ORDERED);

        Order savedOrder = orderRepository.save(order);

        return orderMapper.toResponse(savedOrder);
    }
}