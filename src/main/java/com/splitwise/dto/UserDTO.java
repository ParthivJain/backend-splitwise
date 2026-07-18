package com.splitwise.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;
    private String name;
    private String username;
    private String email;
    private String profilePic;

}
