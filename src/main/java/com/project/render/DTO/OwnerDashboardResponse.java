package com.project.render.DTO;

import lombok.Data;
import java.util.List;

@Data
public class OwnerDashboardResponse {

    private int todayBookings;
    private int confirmedBookings;
    private int todayRevenue;
    private int activeBarbers;

    private List<RevenueData> revenueLast7Days;

    @Data
    public static class RevenueData {
        private String date;
        private int revenue;
    }
}