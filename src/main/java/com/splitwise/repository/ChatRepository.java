package com.splitwise.repository;

import com.splitwise.model.ChatMessage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long>{

    List<ChatMessage> findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByCreatedAtAsc(
            Long senderId, Long receiverId, Long receiverId2, Long senderId2
    );

    @Modifying
    @Transactional
    @Query("""
    UPDATE ChatMessage c
    SET c.status = 'read'
    WHERE c.senderId = :senderId
      AND c.receiverId = :receiverId
      AND c.status = 'sent'
""")
    int markMessagesAsRead(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId
    );

}
