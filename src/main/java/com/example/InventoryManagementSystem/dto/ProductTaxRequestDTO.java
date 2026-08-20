package com.example.InventoryManagementSystem.dto;


import lombok.Data;

@Data
public class ProductTaxRequestDTO {

    private Long productId;
    private String taxName;
    private Double taxPercentage;
}