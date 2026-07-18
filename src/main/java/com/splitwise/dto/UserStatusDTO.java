package com.splitwise.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserStatusDTO {

    private Long userId;
    private boolean online;

}
