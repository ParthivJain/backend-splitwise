package com.splitwise.dto;

import lombok.Data;

@Data
public class OtpVarifyDTO {

    private String email;
    private String otp;

}
