package com.caprock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateOrderResponse {

    private String orderId;
    private Integer amount;
    private String currency;
    private String plan;
}
