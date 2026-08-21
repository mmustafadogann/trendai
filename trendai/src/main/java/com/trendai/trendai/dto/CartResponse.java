package com.trendai.trendai.dto;

import com.trendai.trendai.entity.CartStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CartResponse {

    private Long id;
    private Long userId;
    private CartStatus status;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
}