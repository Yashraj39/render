package com.project.render.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "bookingCart")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingCart {

    @Id
    private String id;

    private String userId;
    private String customerName;
    private String salonId;

    private List<CartItem> items;

    private int totalPrice;
    private int totalTime;

}
