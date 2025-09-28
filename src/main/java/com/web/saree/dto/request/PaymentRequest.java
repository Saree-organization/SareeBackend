package com.web.saree.dto.request;

import lombok.Data;

@Data
public class PaymentRequest {
    private Double amount;
    private Long shippingAddressId;
}
