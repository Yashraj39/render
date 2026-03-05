package com.project.render.Controller;

import com.project.render.DTO.CreateOrderRequest;
import com.project.render.DTO.CreateOrderResponse;
import com.project.render.DTO.VerifyAndConfirmRequest;
import com.project.render.Entity.Booking;
import com.project.render.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-order")
    public CreateOrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        return paymentService.createOrder(request);
    }

    @PostMapping("/verify-and-confirm")
    public Booking verifyAndConfirm(@RequestBody VerifyAndConfirmRequest request) {
        return paymentService.verifyAndConfirm(request);
    }
}