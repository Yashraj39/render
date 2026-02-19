package com.project.render.Controller;

import com.project.render.DTO.AvailableSlotResponse;
import com.project.render.DTO.ConfirmBookingRequest;
import com.project.render.Entity.Booking;
import com.project.render.Service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/available-slots")
    public List<AvailableSlotResponse> getAvailableSlots(
            @RequestParam String userId,
            @RequestParam String salonId,
            @RequestParam String barberId,
            @RequestParam String customerName,
            @RequestParam LocalDate date
    ) {

        return bookingService.getAvailableSlots(
                userId, salonId, barberId, customerName, date
        );
    }

    @PostMapping("/confirm")
    public Booking confirmBooking(
            @RequestBody ConfirmBookingRequest request
    ) {

        return bookingService.confirmBooking(request);
    }
}

