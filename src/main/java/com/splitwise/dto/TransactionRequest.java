package com.splitwise.dto;

import lombok.Data;

@Data
public class TransactionRequest {
    private Long friendId;
    private Double amount;
    private String reason;
    private String type;
}
