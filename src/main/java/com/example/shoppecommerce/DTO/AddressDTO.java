package com.example.shoppecommerce.DTO;

import lombok.Data;

@Data
public class AddressDTO {
    private String addressLine1;
    private String addressLine2;
    private String phone;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
