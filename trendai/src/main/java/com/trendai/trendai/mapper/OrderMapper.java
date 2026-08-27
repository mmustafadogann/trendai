package com.trendai.trendai.mapper;

import com.trendai.trendai.dto.OrderItemResponse;
import com.trendai.trendai.dto.OrderResponse;
import com.trendai.trendai.entity.Order;
import com.trendai.trendai.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        response.setItems(items);

        return response;
    }

    private OrderItemResponse toItemResponse(OrderItem item) {

        OrderItemResponse response = new OrderItemResponse();

        response.setProductId(item.getProductId());
        response.setProductName(item.getProductName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setLineTotal(item.getLineTotal());

        return response;
    }
}