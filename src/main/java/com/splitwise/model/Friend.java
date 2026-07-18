package com.splitwise.model;

import jakarta.persistence.*;
import lombok.Data;
import org.apache.tomcat.util.descriptor.LocalResolver;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Table(name="friends")
@Data
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    @Column(nullable = false)
    private String status = "active";

    private Double balance = 0.0;

    @Column(name = "deleted_by_user_id")
    private Long deletedByUserId;

    @Column(name = "deleted at")
    private LocalDateTime deletedAt;

    @Column(name = "hard_delete_at")
    private LocalDateTime hardDeleteAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    public boolean isDeleted() {
        return "deleted".equals(status);
    }

    private boolean hasUnreadMessage = false;

}