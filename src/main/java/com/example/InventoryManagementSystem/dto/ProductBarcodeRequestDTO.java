package com.example.InventoryManagementSystem.dto;


import lombok.Data;

@Data
public class ProductBarcodeRequestDTO {

    private Long productId;
    private String barcode;
}