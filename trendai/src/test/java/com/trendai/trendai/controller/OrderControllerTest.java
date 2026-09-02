package com.trendai.trendai.controller;

import com.trendai.trendai.dto.OrderResponse;
import com.trendai.trendai.entity.OrderStatus;
import com.trendai.trendai.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @Test
    void updateStatus_shouldReturn400WhenStatusIsMissing() throws Exception {

        mockMvc.perform(
                        patch("/api/orders/10/status")
                                .contentType("application/json")
                                .content("{}")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void getUserOrders_shouldReturn400WhenPageIsNegative() throws Exception {

        mockMvc.perform(
                        get("/api/users/1/orders")
                                .param("page", "-1")
                                .param("size", "10")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void getUserOrders_shouldReturn400WhenSizeIsZero() throws Exception {

        mockMvc.perform(
                        get("/api/users/1/orders")
                                .param("page", "0")
                                .param("size", "0")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void getUserOrders_shouldReturn400WhenSizeIsGreaterThan100() throws Exception {

        mockMvc.perform(
                        get("/api/users/1/orders")
                                .param("page", "0")
                                .param("size", "101")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void getUserOrders_shouldAcceptValidPaginationParameters() throws Exception {

        when(orderService.getUserOrders(
                eq(1L),
                any(Pageable.class)
        )).thenReturn(
                new PageImpl<>(List.of())
        );

        mockMvc.perform(
                        get("/api/users/1/orders")
                                .param("page", "0")
                                .param("size", "100")
                )
                .andExpect(status().isOk());

        verify(orderService).getUserOrders(
                eq(1L),
                any(Pageable.class)
        );
    }
}