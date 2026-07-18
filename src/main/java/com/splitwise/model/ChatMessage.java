package com.splitwise.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String status = "sent";

    @Column(name = "is_deleted_by_sender")
    private boolean isDeletedBySender = false;

    @Column(name = "is_deleted_by_receiver")
    private boolean isDeletedByReceiver = false;

    @Column(name = "deleted_for_everyone")
    private boolean deletedForEveryone = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

}
