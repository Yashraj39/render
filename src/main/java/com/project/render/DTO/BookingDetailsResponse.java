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
public class BookingDetailsResponse {

    private String bookingId;
    private String userId;
    private String customerName;

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String bookingStatus;
    private String paymentStatus;

    private int totalPrice;
    private int totalTime;
    private int serviceCount;

    private SalonInfo salon;
    private BarberInfo barber;
    private List<ServiceInfo> services;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SalonInfo {
        private String salonId;
        private String name;
        private String city;
        private String address;
        private String contact;
        private String salonEmail;
        private String imageUrl;
        private String interiorImageUrl;
        private String exteriorImageUrl;
        private String mapLink;
        private LocalTime opentime;
        private LocalTime closetime;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BarberInfo {
        private String barberId;
        private String name;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ServiceInfo {
        private String categoryId;
        private String serviceId;
        private String serviceName;
        private int price;
        private int time;
        private String imageUrl;
    }
}