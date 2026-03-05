package com.project.render.Service;

import com.project.render.DTO.CreateOrderRequest;
import com.project.render.DTO.CreateOrderResponse;
import com.project.render.DTO.VerifyAndConfirmRequest;
import com.project.render.Entity.Booking;
import com.project.render.Entity.BookingCart;
import com.project.render.Repository.BookingCartRepository;
import com.project.render.Repository.BookingRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class PaymentService {

    private final BookingCartRepository bookingCartRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    @Value("${razorpay.keyId}")
    private String keyId;

    @Value("${razorpay.keySecret}")
    private String keySecret;

    public PaymentService(
            BookingCartRepository bookingCartRepository,
            BookingRepository bookingRepository,
            BookingService bookingService
    ) {
        this.bookingCartRepository = bookingCartRepository;
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request) {

        BookingCart cart = bookingCartRepository
                .findByUserIdAndSalonIdAndCustomerName(
                        request.getUserId(),
                        request.getSalonId(),
                        request.getCustomerName().toLowerCase().trim()
                )
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        int amountPaise = cart.getTotalPrice() * 100;

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            JSONObject options = new JSONObject();
            options.put("amount", amountPaise);
            options.put("currency", "INR");
            options.put("receipt", "rcpt_" + System.currentTimeMillis());

            Order order = client.orders.create(options);

            return new CreateOrderResponse(
                    order.get("id"),
                    amountPaise,
                    "INR",
                    keyId
            );

        } catch (Exception e) {
            throw new RuntimeException("Razorpay order creation failed", e);
        }
    }

    public Booking verifyAndConfirm(VerifyAndConfirmRequest request) {

        boolean ok = verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!ok) {
            throw new RuntimeException("Payment verification failed");
        }

        // ✅ Now payment verified → create booking using your existing logic
        Booking booking = bookingService.confirmBooking(
                new com.project.render.DTO.ConfirmBookingRequest(
                        request.getUserId(),
                        request.getSalonId(),
                        request.getBarberId(),
                        request.getCustomerName(),
                        request.getBookingDate(),
                        request.getStartTime(),
                        request.getEndTime()
                )
        );

        // ✅ Update booking payment info
        booking.setPaymentStatus("PAID");
        booking.setRazorpayOrderId(request.getRazorpayOrderId());
        booking.setRazorpayPaymentId(request.getRazorpayPaymentId());
        booking.setRazorpaySignature(request.getRazorpaySignature());

        return bookingRepository.save(booking);
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }

            return hex.toString().equals(signature);

        } catch (Exception e) {
            return false;
        }
    }
}