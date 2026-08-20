package com.example.InventoryManagementSystem.dto;



import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CustomerResponseDTO {
    private Long customerId;
    private String customerName;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String gstin;
    private String notes;
    private String status;
    private OffsetDateTime createdAt;
}