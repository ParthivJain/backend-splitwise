package com.splitwise.dto;

import com.splitwise.model.Transaction;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long id;
    private Long userId;
    private Long friendId;
    private Double amount;
    private String reason;
    private String type;
    private String status;
    private LocalDateTime transactionDate;
    private Boolean isDeleted;

    public static TransactionDTO fromEntity(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUser().getId());
        dto.setFriendId(transaction.getFriend().getId());
        dto.setAmount(transaction.getAmount());
        dto.setReason(transaction.getReason());
        dto.setType(transaction.getType());
        dto.setStatus(transaction.getStatus());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setIsDeleted(transaction.getIsDeleted());
        return dto;
    }
}