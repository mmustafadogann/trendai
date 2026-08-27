package com.trendai.trendai.controller;

import com.trendai.trendai.dto.OrderResponse;
import com.trendai.trendai.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{cartId}/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @PathVariable Long cartId
    ) {
        return ResponseEntity.ok(
                orderService.checkout(cartId)
        );
    }
}