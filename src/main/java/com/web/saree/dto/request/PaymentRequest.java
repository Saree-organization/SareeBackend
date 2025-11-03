package com.web.saree.dto.request;

import lombok.Data;
// import com.fasterxml.jackson.annotation.JsonProperty; // NOTE: @JsonProperty is usually not needed here unless API keys differ

@Data
public class PaymentRequest {
    private Double amount;

    // NOTE: If using standard camelCase in frontend/backend, @JsonProperty is often unnecessary.
    // Assuming frontend uses 'shippingAddressId' as camelCase.
    // @JsonProperty("shippingAddressId")
    private Long shippingAddressId;

    // 🎯 COD FIX: This field is crucial to determine if the order is COD or ONLINE.
    private String paymentMethod;
}