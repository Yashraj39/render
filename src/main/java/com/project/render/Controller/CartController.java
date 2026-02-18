package com.project.render.Controller;

import com.project.render.Entity.BookingCart;
import com.project.render.Service.BookingCartService;
import com.project.render.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private BookingCartService bookingCartService;

    @Autowired
    private UserService userService;

    // Add service to cart
    @PostMapping("/add")
    public BookingCart addToCart(@RequestParam String userId,
                                 @RequestParam String salonId,
                                 @RequestParam String serviceId,
                                 @RequestParam(required = false) String customerName) {

        if (customerName == null || customerName.isEmpty()) {
            customerName = userService.getUserByUserID(userId);
        }

        return bookingCartService.addServiceToCart(userId, salonId, serviceId, customerName);
    }

    // Get cart
    @GetMapping("/get")
    public BookingCart getCart(
            @RequestParam String userId,
            @RequestParam String salonId,
            @RequestParam(required = false) String customerName
    ) {
        if (customerName == null || customerName.isEmpty()) {
            customerName = userService.getUserByUserID(userId);
        }
        return bookingCartService.getCart(userId, salonId, customerName);
    }

    // Clear cart
    @DeleteMapping("/clear")
    public void clearCart(
            @RequestParam String userId,
            @RequestParam String salonId,
            @RequestParam(required = false) String customerName
    ) {
        if (customerName == null || customerName.isEmpty()) {
            customerName = userService.getUserByUserID(userId);
        }
        bookingCartService.clearCart(userId, salonId, customerName);
    }

    // Get cart count
    @GetMapping("/cart-count")
    public int getCartCount(
            @RequestParam String userId,
            @RequestParam String salonId,
            @RequestParam(required = false) String customerName
    ) {
        if (customerName == null || customerName.isEmpty()) {
            customerName = userService.getUserByUserID(userId);
        }
        return bookingCartService.getCartCount(userId, salonId, customerName);
    }

    // Get all pending carts (all customerNames)
    @GetMapping("/navbar-cart")
    public List<Map<String, Object>> getNavbarCart(@RequestParam String userId) {
        return bookingCartService.getUserPendingCarts(userId);
    }
}
