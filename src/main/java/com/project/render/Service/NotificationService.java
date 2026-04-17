package com.project.render.Service;

import com.project.render.Entity.Notification;
import com.project.render.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
                .message("Your booking " + bookingInfo + " has been cancelled by the salon owner. Reason: " + reason + ". Your refund will be processed within 3 working days.")
                .type("BOOKING_CANCELLED")
                .audience("USER")
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(notification);
    }

    public void createNotification(String userId, String title, String message, String type, String audience) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .audience(audience)
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(notification);
        System.out.println("notification saved");
    }

    public List<Notification> getNotificationsByAudience(String userId, String audience) {
        return notificationRepository.findByUserIdAndAudienceOrderByCreatedAtDesc(userId, audience);
    }

    public Notification markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    public List<Notification> markAllAsReadByAudience(String userId, String audience) {
        List<Notification> notifications =
                notificationRepository.findByUserIdAndAudienceOrderByCreatedAtDesc(userId, audience);

        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }

        return notificationRepository.saveAll(notifications);
    }

    public void deleteNotification(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notificationRepository.delete(notification);
    }
}