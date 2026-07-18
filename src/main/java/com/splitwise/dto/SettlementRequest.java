package com.splitwise.dto;

import lombok.Data;

import java.util.List;

@Data
public class SettlementRequest {

    private List<Long> friendIds;

}
