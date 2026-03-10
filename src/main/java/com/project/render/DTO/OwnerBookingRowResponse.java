package com.project.render.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OwnerBookingRowResponse {
    private String bookingId;
    private String userId;
    private String customerName;
    private String customerEmail;

    private String salonId;
    private String salonName;
    private String city;

    private String barberId;
    private String barberName;

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private List<String> serviceNames;
    private Integer totalPrice;
    private Integer totalTime;

    private String status;
    private String paymentStatus;
}