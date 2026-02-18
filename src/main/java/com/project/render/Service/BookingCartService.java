package com.project.render.Service;

import com.project.render.Entity.Barber;
import com.project.render.Entity.BookingCart;
import com.project.render.Entity.CartItem;
import com.project.render.Repository.BarberRepository;
import com.project.render.Repository.BookingCartRepository;
import com.project.render.Repository.ServiceCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BookingCartService {

    @Autowired
    private BookingCartRepository bookingCartRepository;

    @Autowired
    private ServiceCrudRepository serviceCrudRepository;

    @Autowired
    private BarberRepository barberRepository;

    // Add service to cart (supports multiple customer names)
    public BookingCart addServiceToCart(String userId, String salonId, String serviceId, String bookedBy, String customerName) {

        BookingCart cart = bookingCartRepository
                .findByUserIdAndSalonIdAndCustomerName(userId, salonId, customerName)
                .orElse(
                        BookingCart.builder()
                                .userId(userId)
                                .bookedBy(bookedBy)
                                .customerName(customerName)
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

        BookingCart cart = bookingCartRepository
                .findByUserIdAndSalonIdAndCustomerName(userId, salonId, customerName)
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
        Optional<BookingCart> bookingCart = bookingCartRepository
                .findByUserIdAndSalonIdAndCustomerName(userId, salonId, customerName);

        bookingCart.ifPresent(cart -> {
            cart.getItems().removeIf(item -> !item.isActive());

            int totalPrice = cart.getItems().stream().mapToInt(CartItem::getPrice).sum();
            int totalTime = cart.getItems().stream().mapToInt(CartItem::getTime).sum();

            cart.setTotalPrice(totalPrice);
            cart.setTotalTime(totalTime);

            bookingCartRepository.save(cart);
        });
    }

    // Show available times (unchanged)
    public String showAvailableTimes(String barberId, String serviceId) {
        com.project.render.Entity.Service service = serviceCrudRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        return "times";
    }

    // Get cart count (specific customer)
    public int getCartCount(String userId, String salonId, String customerName) {
        BookingCart cart = bookingCartRepository
                .findByUserIdAndSalonIdAndCustomerName(userId, salonId, customerName)
                .orElse(null);

        if (cart == null) return 0;

        return (int) cart.getItems().stream().filter(item -> !item.isActive()).count();
    }

    // Get all pending carts for user (all customerNames)
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

                    return map;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
