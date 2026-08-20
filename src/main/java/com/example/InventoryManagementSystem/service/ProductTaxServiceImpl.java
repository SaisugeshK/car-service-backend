package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.ProductTaxRequestDTO;
import com.example.InventoryManagementSystem.dto.ProductTaxResponseDTO;
import com.example.InventoryManagementSystem.model.ProductTax;
import com.example.InventoryManagementSystem.Repository.ProductTaxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductTaxServiceImpl implements ProductTaxService {

    private final ProductTaxRepository repository;

    // ==============================
    // CREATE TAX
    // ==============================
    @Override
    public ProductTaxResponseDTO createTax(ProductTaxRequestDTO request) {

        ProductTax tax = new ProductTax();
        tax.setProductId(request.getProductId());
        tax.setTaxName(request.getTaxName());
        tax.setTaxPercentage(request.getTaxPercentage());
        tax.setCreatedAt(LocalDateTime.now());

        ProductTax savedTax = repository.save(tax);

        return mapToResponse(savedTax);
    }

    // ==============================
    // GET ALL TAXES
    // ==============================
    @Override
    public List<ProductTaxResponseDTO> getAllTaxes() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==============================
    // GET TAX BY ID
    // ==============================
    @Override
    public ProductTaxResponseDTO getTaxById(Long taxId) {

        ProductTax tax = repository.findById(taxId)
                .orElseThrow(() ->
                        new RuntimeException("Product Tax not found with ID: " + taxId));

        return mapToResponse(tax);
    }

    // ==============================
    // UPDATE TAX
    // ==============================
    @Override
    public ProductTaxResponseDTO updateTax(Long taxId,
                                           ProductTaxRequestDTO request) {

        ProductTax existingTax = repository.findById(taxId)
                .orElseThrow(() ->
                        new RuntimeException("Product Tax not found with ID: " + taxId));

        existingTax.setProductId(request.getProductId());
        existingTax.setTaxName(request.getTaxName());
        existingTax.setTaxPercentage(request.getTaxPercentage());

        ProductTax updatedTax = repository.save(existingTax);

        return mapToResponse(updatedTax);
    }

    // ==============================
    // DELETE TAX
    // ==============================
    @Override
    public String deleteTax(Long taxId) {

        ProductTax tax = repository.findById(taxId)
                .orElseThrow(() ->
                        new RuntimeException("Product Tax not found with ID: " + taxId));

        repository.delete(tax);

        return "Product Tax Deleted Successfully";
    }

    // ==============================
    // DTO MAPPING METHOD
    // ==============================
    private ProductTaxResponseDTO mapToResponse(ProductTax tax) {

        ProductTaxResponseDTO dto = new ProductTaxResponseDTO();

        dto.setTaxId(tax.getTaxId());
        dto.setProductId(tax.getProductId());
        dto.setTaxName(tax.getTaxName());
        dto.setTaxPercentage(tax.getTaxPercentage());
        dto.setCreatedAt(tax.getCreatedAt());

        return dto;
    }
}