package com.project.render.Service;

import com.project.render.DTO.AvailableSlotResponse;
import com.project.render.Entity.Barber;
import com.project.render.Entity.Booking;
import com.project.render.Entity.BookingCart;
import com.project.render.Entity.CartItem;
import com.project.render.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BookingCartService {

    @Autowired
    private BookingCartRepository bookingCartRepository;

    @Autowired
    private ServiceCrudRepository serviceCrudRepository;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // Add service to cart
    public BookingCart addServiceToCart(String userId, String salonId, String serviceId, String bookedBy, String customerName) {

        String normalizedCustomerName = customerName.toLowerCase().trim();
        BookingCart cart = bookingCartRepository
                .findByUserIdAndSalonIdAndCustomerName(userId, salonId, normalizedCustomerName)
                .orElse(
                        BookingCart.builder()
                                .userId(userId)
                                .bookedBy(bookedBy)
                                .customerName(normalizedCustomerName)
                                .salonId(salonId)
                                .items(new ArrayList<>())
                                .totalPrice(0)
                                .totalTime(0)
                                .build()
                );

        com.project.render.Entity.Service service = serviceCrudRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        boolean categoryAlreadyExists = cart.getItems().stream()
                .anyMatch(item ->
                        item.getCategoryId() != null &&
                                item.getCategoryId().equals(service.getCategoryId())
                );

        if (categoryAlreadyExists) {
            throw new RuntimeException("Service from this category already added");
        }

        CartItem item = CartItem.builder()
                .categoryId(service.getCategoryId())
                .serviceId(service.getId())
                .serviceName(service.getName())
                .price(service.getPrice())
                .time(service.getTime())
                .imageUrl(service.getImageUrl())
                .active(false)
                .build();

        cart.getItems().add(item);
        cart.setTotalPrice(cart.getTotalPrice() + service.getPrice());
        cart.setTotalTime(cart.getTotalTime() + service.getTime());

        return bookingCartRepository.save(cart);
    }

    // Get cart (specific customer)
    public BookingCart getCart(String userId, String salonId, String customerName) {

        String normalizedCustomerName = customerName.toLowerCase().trim();
        BookingCart cart = bookingCartRepository
                .findByUserIdAndSalonIdAndCustomerName(userId, salonId, normalizedCustomerName)
                .orElseThrow(() -> new RuntimeException("Cart empty"));

        List<CartItem> inactiveItems = cart.getItems().stream()
                .filter(item -> !item.isActive())
                .toList();

        int totalPrice = inactiveItems.stream().mapToInt(CartItem::getPrice).sum();
        int totalTime = inactiveItems.stream().mapToInt(CartItem::getTime).sum();

        cart.setItems(inactiveItems);
        cart.setTotalPrice(totalPrice);
        cart.setTotalTime(totalTime);

        return cart;
    }

    // Clear cart (specific customer)
    public void clearCart(String userId, String salonId, String customerName) {
        String normalizedCustomerName = customerName.toLowerCase().trim();
        Optional<BookingCart> bookingCart = bookingCartRepository
                .findByUserIdAndSalonIdAndCustomerName(userId, salonId, normalizedCustomerName);

        bookingCart.ifPresent(cart -> {
            cart.getItems().removeIf(item -> !item.isActive());

            int totalPrice = cart.getItems().stream().mapToInt(CartItem::getPrice).sum();
            int totalTime = cart.getItems().stream().mapToInt(CartItem::getTime).sum();

            cart.setTotalPrice(totalPrice);
            cart.setTotalTime(totalTime);

            bookingCartRepository.save(cart);
        });
    }

    // Show available time slots
    public List<AvailableSlotResponse> showAvailableTimes(
            String barberId,
            int requiredMinutes,
            LocalDate bookingDate
    ) {

        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        LocalTime workStart = barber.getWorkingStartTime();
        LocalTime workEnd = barber.getWorkingEndTime();
        LocalTime lunchStart = barber.getLunchStart();
        LocalTime lunchEnd = barber.getLunchEnd();

        List<Booking> bookings = bookingRepository
                .findByBarberIdAndBookingDateAndStatus(barberId, bookingDate, "CONFIRMED");

        List<AvailableSlotResponse> slots = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime currentSlotStart = bookingDate.atTime(workStart);

        if (bookingDate.equals(now.toLocalDate()) && now.toLocalTime().isAfter(workStart)) {
            int minute = now.getMinute();
            int rounded = ((minute + 14) / 15) * 15;
            currentSlotStart = now.withSecond(0).withNano(0).withMinute(0).plusMinutes(rounded);
        }

        while (!currentSlotStart.plusMinutes(requiredMinutes)
                .isAfter(bookingDate.atTime(workEnd))) {

            LocalDateTime currentSlotEnd = currentSlotStart.plusMinutes(requiredMinutes);

            LocalTime slotStart = currentSlotStart.toLocalTime();
            LocalTime slotEnd = currentSlotEnd.toLocalTime();

            final LocalDateTime slotStartTime = currentSlotStart;
            final LocalDateTime slotEndTime = currentSlotEnd;

            boolean overlaps = bookings.stream().anyMatch(b -> {
                LocalDateTime bookingStart = bookingDate.atTime(b.getStartTime());
                LocalDateTime bookingEnd = bookingDate.atTime(b.getEndTime());

                return !(bookingEnd.isBefore(slotStartTime) ||
                        bookingStart.isAfter(slotEndTime));
            });

            boolean inLunch = !(slotEnd.isBefore(lunchStart) || slotStart.isAfter(lunchEnd));
            boolean inPast = currentSlotEnd.isBefore(now);

            if (!overlaps && !inLunch && !inPast) {
                slots.add(new AvailableSlotResponse(slotStart, slotEnd));
            }

            currentSlotStart = currentSlotStart.plusMinutes(15);
        }

        return slots;
    }

    // Get cart count (specific customer)
    public int getCartCount(String userId, String salonId, String customerName) {
        String normalizedCustomerName = customerName.toLowerCase().trim();
        BookingCart cart = bookingCartRepository
                .findByUserIdAndSalonIdAndCustomerName(userId, salonId, normalizedCustomerName)
                .orElse(null);

        if (cart == null) return 0;

        return (int) cart.getItems().stream().filter(item -> !item.isActive()).count();
    }

    // Get all pending carts for user
    public List<Map<String, Object>> getUserPendingCarts(String userId) {

        List<BookingCart> carts = bookingCartRepository.findByUserId(userId);

        return carts.stream()
                .filter(cart -> cart.getItems() != null)
                .map(cart -> {
                    long count = cart.getItems().stream()
                            .filter(item -> !item.isActive())
                            .count();
                    if (count == 0) return null;

                    Map<String, Object> map = new HashMap<>();
                    map.put("salonId", cart.getSalonId());
                    map.put("customerName", cart.getCustomerName());
                    map.put("pendingCount", count);

                    salonRepository.findById(cart.getSalonId())
                            .ifPresent(salon -> map.put("salonName", salon.getName()));

                    return map;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
