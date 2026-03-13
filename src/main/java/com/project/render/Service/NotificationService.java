package com.project.render.Service;

import com.project.render.Entity.Notification;
import com.project.render.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void createBookingCancelledNotification(String userId, String bookingId, String reason, String bookingInfo) {
        Notification notification = Notification.builder()
                .userId(userId)
                .bookingId(bookingId)
                .title("Booking Cancelled")
                .message("Your booking " + bookingInfo + " has been cancelled. Reason: " + reason)
                .type("BOOKING_CANCELLED")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}