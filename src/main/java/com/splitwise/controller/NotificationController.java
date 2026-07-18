package com.splitwise.controller;

import com.splitwise.dto.NotificationDTO;
import com.splitwise.model.Notification;
import com.splitwise.model.User;
import com.splitwise.service.NotificationService;
import com.splitwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<NotificationDTO> getNotifications() {
        User currentUser = getCurrentUser();

        List<NotificationDTO> notifications = notificationService.getNotifications(currentUser);

        return notifications;
    }

    @DeleteMapping("/{id}")
    public void deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}