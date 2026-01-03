package com.project.render.Service;

import com.project.render.Entity.BookingCart;
import com.project.render.Entity.CartItem;
import com.project.render.Repository.BookingCartRepository;
import com.project.render.Repository.ServiceCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class BookingCartService {

    @Autowired
    private BookingCartRepository bookingCartRepository;

    @Autowired
    private ServiceCrudRepository serviceCrudRepository;

    public BookingCart addServiceToCart(String userId,String salonId,String serviceId){

        BookingCart cart = bookingCartRepository
                .findByUserIdAndSalonId(userId,salonId)
                .orElse(
                        BookingCart.builder()
                                .userId(userId)
                                .salonId(salonId)
                                .items(new ArrayList<>())
                                .totalPrice(0)
                                .totalTime(0)
                                .build()
                );

        com.project.render.Entity.Service service = serviceCrudRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        CartItem item = CartItem.builder()
                .serviceId(service.getId())
                .serviceName(service.getName())
                .price(service.getPrice())
                .time(service.getTime())
                .imageUrl(service.getImageUrl())
                .build();

        cart.getItems().add(item);

        cart.setTotalPrice(cart.getTotalPrice() + service.getPrice());
        cart.setTotalTime(cart.getTotalTime() + service.getTime());

        return bookingCartRepository.save(cart);
    }

    public BookingCart getCart(String userId, String salonId) {
        return bookingCartRepository
                .findByUserIdAndSalonId(userId, salonId)
                .orElseThrow(() -> new RuntimeException("Cart empty"));
    }

    public void clearCart(String userId, String salonId) {
        bookingCartRepository.findByUserIdAndSalonId(userId, salonId)
                .ifPresent(bookingCartRepository::delete);
    }

}
