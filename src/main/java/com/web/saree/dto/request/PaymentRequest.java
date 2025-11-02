package com.web.saree.dto.request;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty; // 💡 नया Import


@Data
public class PaymentRequest {
    private Double amount;
    @JsonProperty("shippingAddressId")
    private Long shippingAddressId;
    private String paymentMethod;
}
