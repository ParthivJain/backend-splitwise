package com.splitwise.service;

import com.splitwise.dto.NotificationDTO;
import com.splitwise.model.Notification;
import com.splitwise.model.User;
import com.splitwise.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification sendNotification(User user, String type, String title, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);

        if ("friend_request".equals(type)) {
            notification.setActions(List.of("accept", "reject"));
        }

        if("transaction_add".equals(type)) {
            notification.setActions(List.of("add", "cancel"));
        }

        if("settlement".equals(type)){
            notification.setActions(List.of("settle", "cancel"));
        }

        return notificationRepository.save(notification);
    }

    public List<NotificationDTO> getNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    public void deleteNotificationByRequest(Long requestId) {
        List<Notification> notifications = notificationRepository.findAll();
        for (Notification n : notifications) {
            if (n.getActionData() != null && n.getActionData().contains(String.valueOf(requestId))) {
                notificationRepository.delete(n);
                break;
            }
        }
    }
}