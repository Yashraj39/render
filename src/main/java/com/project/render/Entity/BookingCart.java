package com.project.render.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingCart {

    @Id
    private String id;

    private String userId;
    private String salonId;

    private List<CartItem> items;

    private int totalPrice;
    private int totalTime;

}
