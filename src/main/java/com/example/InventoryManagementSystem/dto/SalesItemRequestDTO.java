package com.example.InventoryManagementSystem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesItemRequestDTO {

    private Long saleId;
    private Long productId;
    private Integer quantity;
}