package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.ProductTaxRequestDTO;
import com.example.InventoryManagementSystem.dto.ProductTaxResponseDTO;

import java.util.List;

public interface ProductTaxService {

    ProductTaxResponseDTO createTax(ProductTaxRequestDTO request);

    List<ProductTaxResponseDTO> getAllTaxes();

    ProductTaxResponseDTO getTaxById(Long taxId);

    ProductTaxResponseDTO updateTax(Long taxId,
                                    ProductTaxRequestDTO request);

    String deleteTax(Long taxId);
}