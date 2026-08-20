package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.ProductTaxRequestDTO;
import com.example.InventoryManagementSystem.dto.ProductTaxResponseDTO;
import com.example.InventoryManagementSystem.service.ProductTaxService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-taxes")
@RequiredArgsConstructor
public class ProductTaxController {

    private final ProductTaxService service;

    // CREATE
    @PostMapping
    public ProductTaxResponseDTO create(@RequestBody ProductTaxRequestDTO request) {
        return service.createTax(request);
    }

    // GET ALL
    @GetMapping
    public List<ProductTaxResponseDTO> getAll() {
        return service.getAllTaxes();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ProductTaxResponseDTO getById(@PathVariable Long id) {
        return service.getTaxById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ProductTaxResponseDTO update(@PathVariable Long id,
                                        @RequestBody ProductTaxRequestDTO request) {
        return service.updateTax(id, request);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        return service.deleteTax(id);
    }
}