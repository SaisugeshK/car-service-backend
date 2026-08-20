package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.ProductBarcodeRequestDTO;
import com.example.InventoryManagementSystem.dto.ProductBarcodeResponseDTO;
import com.example.InventoryManagementSystem.service.ProductBarcodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/product-barcodes")
@RequiredArgsConstructor
public class ProductBarcodeController {

    private final ProductBarcodeService service;

    @PostMapping
    public ProductBarcodeResponseDTO create(@RequestBody ProductBarcodeRequestDTO request) {
        return service.createBarcode(request);
    }

    @GetMapping
    public List<ProductBarcodeResponseDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/product/{productId}")
    public List<ProductBarcodeResponseDTO> getByProduct(@PathVariable Long productId) {
        return service.getByProductId(productId);
    }

    @GetMapping("/scan/{barcode}")
    public ProductBarcodeResponseDTO scan(@PathVariable String barcode) {
        return service.getByBarcode(barcode);
    }

    // 🟣 PUT - UPDATE BARCODE
    @PutMapping("/{id}")
    public ProductBarcodeResponseDTO update(
            @PathVariable Long id,
            @RequestBody ProductBarcodeRequestDTO request) {
        return service.updateBarcode(id, request);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.deleteBarcode(id);
        return "Deleted Successfully";
    }
}