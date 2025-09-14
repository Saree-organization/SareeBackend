// File: com/web/saree/dto/request/VerifyOtpRequest.java

package com.web.saree.dto.request;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String email; // Changed from phoneNumber
    private String otp;
}