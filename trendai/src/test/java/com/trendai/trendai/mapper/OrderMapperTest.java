package com.trendai.trendai.mapper;

import com.trendai.trendai.dto.OrderResponse;
import com.trendai.trendai.entity.Order;
import com.trendai.trendai.entity.OrderItem;
import com.trendai.trendai.entity.OrderStatus;
import com.trendai.trendai.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderMapperTest {

    private final OrderMapper orderMapper = new OrderMapper();

    @Test
    void testOrderToResponse() {

        User user = new User();
        user.setId(1L);

        Order order = new Order();
        order.setId(10L);
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new BigDecimal("300.00"));

        LocalDateTime now = LocalDateTime.now();

        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        OrderItem item = new OrderItem();
        item.setId(100L);
        item.setOrder(order);
        item.setProductId(2L);
        item.setProductName("Test Product");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("150.00"));
        item.setLineTotal(new BigDecimal("300.00"));

        order.setItems(List.of(item));

        OrderResponse response = orderMapper.toResponse(order);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(OrderStatus.CREATED, response.getStatus());
        assertEquals(
                new BigDecimal("300.00"),
                response.getTotalAmount()
        );

        assertEquals(1, response.getItems().size());

        assertEquals(
                2L,
                response.getItems().get(0).getProductId()
        );

        assertEquals(
                "Test Product",
                response.getItems().get(0).getProductName()
        );

        assertEquals(
                2,
                response.getItems().get(0).getQuantity()
        );

        assertEquals(
                new BigDecimal("150.00"),
                response.getItems().get(0).getUnitPrice()
        );

        assertEquals(
                new BigDecimal("300.00"),
                response.getItems().get(0).getLineTotal()
        );
    }
}