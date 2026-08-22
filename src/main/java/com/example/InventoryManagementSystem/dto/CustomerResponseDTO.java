package com.example.InventoryManagementSystem.dto;



import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CustomerResponseDTO {
    private Long customerId;
    private String customerName;
    private String phone;
    private String whatsappNumber;
    private String alternateMobile;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String gstin;
    private String notes;
    private String status;
    private OffsetDateTime lastServiceDate;
    private OffsetDateTime createdAt;
}