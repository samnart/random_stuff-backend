package com.random_stuff.api.entity;
 
import jakarta.persistence.Embeddable;
import lombok.*;
 
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress {
 
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String region;
    private String notes;
}