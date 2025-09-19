package com.web.saree.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RazorpayOrderResponse {
    private String razorpayOrderId;
    private double amount;
}