package com.project.render.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VerifyAndConfirmRequest {
    private String userId;
    private String salonId;
    private String barberId;
    private String customerName;

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
