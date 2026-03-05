package com.project.render.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateOrderResponse {
    private String orderId;
    private int amount; // paise
    private String currency;
    private String key;
}
