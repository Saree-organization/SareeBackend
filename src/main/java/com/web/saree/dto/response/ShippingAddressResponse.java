package com.web.saree.dto.response;

import lombok.Data;

@Data
public class ShippingAddressResponse {
    private Long id;
    private String fullName;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private String phone;


}
