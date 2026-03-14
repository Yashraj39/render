package com.project.render.Controller;

import com.project.render.Entity.Notification;
import com.project.render.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin("*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable String userId) {
        return notificationService.getUserNotifications(userId);
    }

    @PutMapping("/read/{notificationId}")
    public Notification markAsRead(@PathVariable String notificationId) {
        return notificationService.markAsRead(notificationId);
    }

    @PutMapping("/read-all/{userId}")
    public List<Notification> markAllAsRead(@PathVariable String userId) {
        return notificationService.markAllAsRead(userId);
    }

    @DeleteMapping("/{notificationId}")
    public String deleteNotification(@PathVariable String notificationId) {
        notificationService.deleteNotification(notificationId);
        return "Notification deleted successfully";
    }
}