package com.project.render.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Document(collection = "booking")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

    @Id
    private String id;

    private String userId;
    private String salonId;
    private String barberId;

    private String customerName;

    private LocalDate bookingDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private List<CartItem> services;

    private int totalPrice;
    private int totalTime;

    private String status; // CONFIRMED / CANCELLED
}


