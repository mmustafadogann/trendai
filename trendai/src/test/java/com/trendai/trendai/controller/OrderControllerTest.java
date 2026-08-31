package com.trendai.trendai.controller;

import com.trendai.trendai.dto.OrderResponse;
import com.trendai.trendai.entity.OrderStatus;
import com.trendai.trendai.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void checkout_shouldReturn201Created() throws Exception {

        OrderResponse response = new OrderResponse();

        response.setId(50L);
        response.setUserId(1L);
        response.setStatus(OrderStatus.CREATED);
        response.setTotalAmount(
                new BigDecimal("200.00")
        );

        when(orderService.checkout(10L, 1L))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/users/1/carts/10/checkout")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(200.00));

        verify(orderService)
                .checkout(10L, 1L);
    }
}