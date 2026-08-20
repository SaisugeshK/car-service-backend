package com.example.InventoryManagementSystem.dto;


import lombok.Data;

@Data
public class ProductBarcodeResponseDTO {

    private Long barcodeId;
    private Long productId;
    private String barcode;
}