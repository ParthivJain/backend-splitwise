package com.splitwise.dto;

import lombok.Data;

import java.util.List;

@Data
public class SettlementResponseDTO {

    private List<SettlementSuggestionDTO> suggestions;
    private int totalTransactions;
    private String message;

}
