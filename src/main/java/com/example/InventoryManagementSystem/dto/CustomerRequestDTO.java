package com.example.InventoryManagementSystem.dto;



import lombok.Data;

@Data
public class CustomerRequestDTO {
    private String customerName;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String gstin;
    private String notes;
    private String status;
}