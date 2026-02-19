    package com.project.render.Service;
    
    import com.project.render.DTO.AvailableSlotResponse;
    import com.project.render.DTO.ConfirmBookingRequest;
    import com.project.render.Entity.Barber;
    import com.project.render.Entity.Booking;
    import com.project.render.Entity.BookingCart;
    import com.project.render.Repository.BarberRepository;
    import com.project.render.Repository.BookingCartRepository;
    import com.project.render.Repository.BookingRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    
    import java.time.LocalDate;
    import java.time.LocalTime;
    import java.util.ArrayList;
    import java.util.List;
    
    @Service
    public class BookingService {
    
        @Autowired
        private BookingRepository bookingRepository;
    
        @Autowired
        private BookingCartRepository bookingCartRepository;
    
        @Autowired
        private BarberRepository barberRepository;
    
        // 1️⃣ Get Available Slots
        public List<AvailableSlotResponse> getAvailableSlots(
                String userId,
                String salonId,
                String barberId,
                String customerName,
                LocalDate date
        ) {
    
            BookingCart cart = getCart(userId, salonId, customerName);
            Barber barber = getBarber(barberId);
    
            validateBarberAvailability(barber, date);
    
            int requiredMinutes = cart.getTotalTime();
    
            List<Booking> existingBookings =
                    bookingRepository.findByBarberIdAndBookingDate(barberId, date);
    
            System.out.println("Required Minutes: " + requiredMinutes);
            System.out.println("Start Time: " + barber.getWorkingStartTime());
            System.out.println("End Time: " + barber.getWorkingEndTime());
    
    
            return generateAvailableSlots(barber, existingBookings, requiredMinutes);
        }
    
        // 2️⃣ Confirm Booking
        public Booking confirmBooking(ConfirmBookingRequest request) {
    
            BookingCart cart = getCart(
                    request.getUserId(),
                    request.getSalonId(),
                    request.getCustomerName()
            );
    
            Booking booking = Booking.builder()
                    .userId(request.getUserId())
                    .salonId(request.getSalonId())
                    .barberId(request.getBarberId())
                    .customerName(request.getCustomerName())
                    .bookingDate(request.getBookingDate())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .services(cart.getItems())
                    .totalPrice(cart.getTotalPrice())
                    .totalTime(cart.getTotalTime())
                    .status("CONFIRMED")
                    .build();
    
            bookingRepository.save(booking);

            markCartItemsAsBooked(cart);
    
            return booking;
        }
    
        // ===============================
        // 🔹 Private Helper Methods
        // ===============================
    
        private BookingCart getCart(String userId, String salonId, String customerName) {

            System.out.println("Searching Cart For:");
            System.out.println("UserId: " + userId);
            System.out.println("SalonId: " + salonId);
            System.out.println("CustomerName: '" + customerName + "'");

            return bookingCartRepository
                    .findByUserIdAndSalonIdAndCustomerName(userId, salonId, customerName)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
        }
    
        private Barber getBarber(String barberId) {
            return barberRepository
                    .findById(barberId)
                    .orElseThrow(() -> new RuntimeException("Barber not found"));
        }
    
        private void validateBarberAvailability(Barber barber, LocalDate date) {
    
            if (barber.getLeaves() != null &&
                    barber.getLeaves().contains(date)) {
                throw new RuntimeException("Barber is on leave");
            }
        }
    
        private void markCartItemsAsBooked(BookingCart cart) {
    
            cart.getItems().forEach(item -> item.setActive(true));
    
            bookingCartRepository.save(cart);
        }
    
        private List<AvailableSlotResponse> generateAvailableSlots(
                Barber barber,
                List<Booking> existingBookings,
                int requiredMinutes
        ) {
    
            List<AvailableSlotResponse> availableSlots = new ArrayList<>();
    
            LocalTime current = barber.getWorkingStartTime();
    
            while (current.plusMinutes(requiredMinutes)
                    .isBefore(barber.getWorkingEndTime().plusSeconds(1))) {
    
                LocalTime end = current.plusMinutes(requiredMinutes);
    
                if (isLunchTime(barber, current, end)) {
                    current = barber.getLunchEnd();
                    continue;
                }
    
                if (!isOverlapping(existingBookings, current, end)) {
                    availableSlots.add(new AvailableSlotResponse(current, end));
                }
    
                current = current.plusMinutes(15); // 15 min step
            }
    
            return availableSlots;
        }
    
        private boolean isLunchTime(Barber barber, LocalTime start, LocalTime end) {
    
            return start.isBefore(barber.getLunchEnd()) &&
                    end.isAfter(barber.getLunchStart());
        }
    
        private boolean isOverlapping(
                List<Booking> bookings,
                LocalTime newStart,
                LocalTime newEnd
        ) {
    
            for (Booking booking : bookings) {
    
                if (newStart.isBefore(booking.getEndTime()) &&
                        newEnd.isAfter(booking.getStartTime())) {
                    return true;
                }
            }
    
            return false;
        }
    }
    
