package com.project.render.Controller;

import com.project.render.Entity.BookingCart;
import com.project.render.Repository.BookingCartRepository;
import com.project.render.Service.BookingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private BookingCartService bookingCartService;

    @PostMapping("/add")
    public BookingCart addToCart(@RequestParam String userId,
                                 @RequestParam String salonId,
                                 @RequestParam String serviceId){
        return bookingCartService.addServiceToCart(userId, salonId, serviceId);
    }

    @GetMapping("/get")
    public BookingCart getCart(
            @RequestParam String userId,
            @RequestParam String salonId
    ) {
        return bookingCartService.getCart(userId, salonId);
    }

    @DeleteMapping("/clear")
    public void clearCart(
            @RequestParam String userId,
            @RequestParam String salonId
    ) {
        bookingCartService.clearCart(userId, salonId);
    }

    @GetMapping("/cart-count")
    public int getCartCount(
            @RequestParam String userId,
            @RequestParam String salonId
    ){
        return bookingCartService.getCartCount(userId,salonId);
    }

    @GetMapping("/navbar-cart")
    public List<Map<String, Object>> getNavbarCart(@RequestParam String userId) {
        return bookingCartService.getUserPendingCarts(userId);
    }


}
