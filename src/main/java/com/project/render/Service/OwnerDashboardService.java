package com.project.render.Service;

import com.project.render.DTO.OwnerDashboardResponse;
import com.project.render.Entity.Booking;
import com.project.render.Repository.BarberRepository;
import com.project.render.Repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OwnerDashboardService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BarberRepository barberRepository;

    public OwnerDashboardResponse getDashboard(String salonId) {

        OwnerDashboardResponse res = new OwnerDashboardResponse();

        LocalDate today = LocalDate.now();

        List<Booking> todayBookings =
                bookingRepository.findBySalonIdAndBookingDate(salonId, today);

        res.setTodayBookings(todayBookings.size());

        int confirmed = (int) todayBookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()))
                .count();

        res.setConfirmedBookings(confirmed);

        int todayRevenue = todayBookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()))
                .mapToInt(Booking::getTotalPrice)
                .sum();

        res.setTodayRevenue(todayRevenue);

        int activeBarbers =
                barberRepository.findBySalonIdAndActiveTrue(salonId).size();

        res.setActiveBarbers(activeBarbers);

        List<OwnerDashboardResponse.RevenueData> chart = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {

            LocalDate date = today.minusDays(i);

            List<Booking> bookings =
                    bookingRepository.findBySalonIdAndBookingDate(salonId, date);

            int revenue = bookings.stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()))
                    .mapToInt(Booking::getTotalPrice)
                    .sum();

            OwnerDashboardResponse.RevenueData data =
                    new OwnerDashboardResponse.RevenueData();

            data.setDate(date.toString());
            data.setRevenue(revenue);

            chart.add(data);
        }

        res.setRevenueLast7Days(chart);

        return res;
    }
}