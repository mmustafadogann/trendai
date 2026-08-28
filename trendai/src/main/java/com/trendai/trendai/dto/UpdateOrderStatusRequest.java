package com.trendai.trendai.dto;

import com.trendai.trendai.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {

    private OrderStatus status;
}