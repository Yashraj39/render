package com.project.render.DTO;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private String userId;
    private String salonId;
    private String customerName;
}
