package com.example.shoppecommerce.DTO;

public class AddressDTO {
    private Long id;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phone;
    private String email;

    public AddressDTO(Long id, String addressLine1, String addressLine2, String city, String state,
                      String postalCode, String country, String phone, String email) {
        this.id = id;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.phone = phone;
        this.email = email;
    }

    // Getters, setters
}