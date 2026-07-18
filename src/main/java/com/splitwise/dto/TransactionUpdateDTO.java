package com.splitwise.dto;

import lombok.Data;

@Data
public class TransactionUpdateDTO {

    private Double amount;
    private String reason;

}
