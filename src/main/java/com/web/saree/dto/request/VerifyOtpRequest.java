package com.web.saree.dto.request;


import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String phoneNumber;
    private String otp;

}
