package com.web.saree.dto.request;

import lombok.Data;

@Data
public class ExchangeRequestDTO {
    private Long orderItemId;
    private Long newVariantId;
    private String reason;
}