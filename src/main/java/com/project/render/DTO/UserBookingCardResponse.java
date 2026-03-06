package com.project.render.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserBookingCardResponse {

    private String bookingId;

    private String salonId;
    private String salonName;
    private String salonImageUrl;

    private String customerName;

    private int serviceCount;
    private int totalPrice;
    private int totalTime;

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String bookingStatus;   // CONFIRMED / CANCELLED / UPCOMING / COMPLETED
    private String paymentStatus;   // PAID / UNPAID
}