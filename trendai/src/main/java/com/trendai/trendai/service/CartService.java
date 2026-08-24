package com.trendai.trendai.service;

import com.trendai.trendai.dto.CartItemResponse;
import com.trendai.trendai.dto.CartResponse;
import com.trendai.trendai.entity.Cart;
import com.trendai.trendai.entity.CartItem;
import com.trendai.trendai.entity.CartStatus;
import com.trendai.trendai.entity.User;
import com.trendai.trendai.exception.ResourceNotFoundException;
import com.trendai.trendai.repository.CartItemRepository;
import com.trendai.trendai.repository.CartRepository;
import com.trendai.trendai.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.trendai.trendai.dto.AddCartItemRequest;
import com.trendai.trendai.entity.Product;
import com.trendai.trendai.exception.BusinessException;
import com.trendai.trendai.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional
;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public CartResponse getOrCreateCart(Long userId) {

        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository
                .findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> createCart(user));

        return toResponse(cart);
    }
    @Transactional
    public CartResponse addItem(
            Long cartId,
            AddCartItemRequest request
    ) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found"));

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new BusinessException("Cart is not active");
        }

        Product product = productRepository
                .findByIdAndActiveTrue(request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new BusinessException("Quantity must be at least 1");
        }

        Optional<CartItem> existingItem =
                cartItemRepository.findByCartIdAndProductId(
                        cartId,
                        product.getId()
                );

        int newQuantity = request.getQuantity();

        if (existingItem.isPresent()) {
            newQuantity += existingItem.get().getQuantity();
        }

        if (newQuantity > product.getStock()) {
            throw new BusinessException("Insufficient stock");
        }

        if (existingItem.isPresent()) {

            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(newQuantity);

            cartItemRepository.save(cartItem);

        } else {

            CartItem cartItem = new CartItem();

            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUnitPrice(product.getPrice());

            cartItemRepository.save(cartItem);
        }

        return toResponse(cart);
    }

    private Cart createCart(User user) {

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        return cartRepository.save(cart);
    }

    private CartResponse toResponse(Cart cart) {

        List<CartItemResponse> items =
                cartItemRepository.findByCartId(cart.getId())
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartResponse response = new CartResponse();

        response.setId(cart.getId());
        response.setUserId(cart.getUser().getId());
        response.setStatus(cart.getStatus());
        response.setItems(items);
        response.setTotalAmount(totalAmount);

        return response;
    }

    private CartItemResponse toItemResponse(CartItem item) {

        BigDecimal totalPrice =
                item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));

        CartItemResponse response = new CartItemResponse();

        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setTotalPrice(totalPrice);

        return response;
    }
}