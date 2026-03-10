package com.project.render.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OwnerBookingsPageResponse {
    private long totalBookings;
    private int currentPage;
    private int pageSize;
    private int totalPages;

    private List<OwnerBookingRowResponse> bookings;
    private List<OwnerBookingFilterOptionDto> salons;
    private List<OwnerBookingFilterOptionDto> barbers;
}