package com.splitwise.dto;

import lombok.Data;

@Data
public class SettlementSuggestionDTO {

    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private Double amount;

}
