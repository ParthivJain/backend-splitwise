package com.splitwise.dto;

import com.splitwise.model.Notification;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NotificationDTO {
    private Long id;
    private Long requestId;
    private String type;
    private String title;
    private String message;
    private List<String> actions;
    private LocalDateTime createdAt;

    public static NotificationDTO fromEntity(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setCreatedAt(notification.getCreatedAt());

        if ("friend_request".equals(notification.getType())) {
            dto.setActions(List.of("accept", "reject"));
        } else if("transaction_add".equals(notification.getType())) {
            dto.setActions(List.of("add", "cancel"));
        } else if("settlement".equals(notification.getType())){
            dto.setActions(List.of("settle", "cancel"));
        }
        else {
            dto.setActions(List.of());
        }
        return dto;
    }
}