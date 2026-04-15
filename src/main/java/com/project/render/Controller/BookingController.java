package com.project.render.Controller;

import com.project.render.DTO.AvailableSlotResponse;
import com.project.render.DTO.BookingDetailsResponse;
import com.project.render.DTO.ConfirmBookingRequest;
import com.project.render.DTO.UserBookingCardResponse;
import com.project.render.Entity.Booking;
import com.project.render.Service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/user-bookings")
    public List<UserBookingCardResponse> getUserBookings(
            @RequestParam String userId,
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(defaultValue = "DESC") String sort
    ) {
        return bookingService.getUserBookings(userId, filter, sort);
    }

    @GetMapping("/details/{bookingId}")
    public BookingDetailsResponse getBookingDetails(
            @PathVariable String bookingId,
            @RequestParam String userId
    ) {
        return bookingService.getBookingDetails(bookingId, userId);
    }

    @GetMapping("/bill/{bookingId}")
    public ResponseEntity<byte[]> getBookingBill(
            @PathVariable String bookingId,
            @RequestParam String userId
    ) {
        byte[] pdfBytes = bookingService.generateBookingBillPdf(bookingId, userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=bill-" + bookingId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/{bookingId}/owner-cancel")
    public Booking ownerCancelBooking(
            @PathVariable String bookingId,
            @RequestParam String ownerId
    ) {
        return bookingService.ownerCancelBooking(bookingId, ownerId);
    }

    @PostMapping("/{bookingId}/user-cancel")
    public Booking userCancelBooking(
            @PathVariable String bookingId,
            @RequestParam String userId
    ) {
        return bookingService.userCancelBooking(bookingId, userId);
    }
}

