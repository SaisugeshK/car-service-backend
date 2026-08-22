package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.ProductRequestDTO;
import com.example.InventoryManagementSystem.dto.ProductResponseDTO;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    // =======================
    // ENTITY → DTO MAPPER
    // =======================
    private ProductResponseDTO mapToDTO(Product p) {

        ProductResponseDTO dto = new ProductResponseDTO();

        dto.setProductId(p.getProductId());
        dto.setCategoryId(p.getCategoryId());
        dto.setItemType(p.getItemType());
        dto.setProductName(p.getProductName());
        dto.setBrand(p.getBrand());
        dto.setSku(p.getSku());
        dto.setBarcode(p.getBarcode());
        dto.setPurchasePrice(p.getPurchasePrice());
        dto.setSellingPrice(p.getSellingPrice());
        dto.setStockQuantity(p.getStockQuantity());
        dto.setMinimumStock(p.getMinimumStock());
        dto.setUnit(p.getUnit());
        dto.setVehicleType(p.getVehicleType());
        dto.setStatus(p.getStatus());
        dto.setCreatedAt(p.getCreatedAt());

        return dto;
    }

    // =======================
    // CREATE PRODUCT
    // =======================
    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {

        String itemType = normalizeItemType(dto.getItemType());

        // Stock is only meaningful for physical PRODUCT rows; SERVICE rows aren't stock-tracked.
        if ("PRODUCT".equals(itemType) && dto.getStockQuantity() != null && dto.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        Product p = new Product();

        p.setCategoryId(dto.getCategoryId());
        p.setItemType(itemType);
        p.setProductName(dto.getProductName());
        p.setBrand(dto.getBrand());
        p.setSku(dto.getSku());
        p.setBarcode(dto.getBarcode());
        p.setPurchasePrice(dto.getPurchasePrice());
        p.setSellingPrice(dto.getSellingPrice());
        p.setStockQuantity(dto.getStockQuantity());
        p.setMinimumStock(dto.getMinimumStock());
        p.setUnit(dto.getUnit());
        p.setVehicleType(dto.getVehicleType());
        p.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");

        return mapToDTO(repository.save(p));
    }

    private String normalizeItemType(String itemType) {
        if (itemType == null || itemType.isBlank()) {
            return "PRODUCT";
        }
        String upper = itemType.trim().toUpperCase();
        if (!upper.equals("PRODUCT") && !upper.equals("SERVICE")) {
            throw new IllegalArgumentException("itemType must be PRODUCT or SERVICE");
        }
        return upper;
    }

    // =======================
    // GET BY ID
    // =======================
    @Override
    public ProductResponseDTO getProductById(Long id) {

        Product p = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return mapToDTO(p);
    }

    // =======================
    // GET ALL
    // =======================
    @Override
    public List<ProductResponseDTO> getAllProducts() {

        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Server-side type filter — Catalog screens (Products vs Service Master) must never fetch the
    // other type at all, not just hide it client-side.
    @Override
    public List<ProductResponseDTO> getAllProducts(String itemType) {

        return repository.findByItemType(itemType)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // =======================
    // UPDATE PRODUCT
    // =======================
    @Override
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {

        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (dto.getCategoryId() != null)
            product.setCategoryId(dto.getCategoryId());

        if (dto.getItemType() != null)
            product.setItemType(normalizeItemType(dto.getItemType()));

        if (dto.getProductName() != null)
            product.setProductName(dto.getProductName());

        if (dto.getBrand() != null)
            product.setBrand(dto.getBrand());

        if (dto.getSku() != null)
            product.setSku(dto.getSku());

        if (dto.getBarcode() != null)
            product.setBarcode(dto.getBarcode());

        if (dto.getPurchasePrice() != null)
            product.setPurchasePrice(dto.getPurchasePrice());

        if (dto.getSellingPrice() != null)
            product.setSellingPrice(dto.getSellingPrice());

        if (dto.getStockQuantity() != null) {

            if (dto.getStockQuantity() < 0) {
                throw new IllegalArgumentException("Stock cannot be negative");
            }

            product.setStockQuantity(dto.getStockQuantity());
        }

        if (dto.getMinimumStock() != null)
            product.setMinimumStock(dto.getMinimumStock());

        if (dto.getUnit() != null)
            product.setUnit(dto.getUnit());

        if (dto.getVehicleType() != null)
            product.setVehicleType(dto.getVehicleType());

        if (dto.getStatus() != null)
            product.setStatus(dto.getStatus());

        return mapToDTO(repository.save(product));
    }

    // =======================
    // DELETE PRODUCT
    // =======================
    @Override
    @Transactional
    public void deleteProduct(Long id) {

        Product p = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        repository.delete(p);
    }
}