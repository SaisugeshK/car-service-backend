package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.SalesReturnItemRequestDTO;
import com.example.InventoryManagementSystem.dto.SalesReturnItemResponseDTO;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.SalesReturnItem;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.SalesReturnItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesReturnItemServiceImpl implements SalesReturnItemService {

    private final SalesReturnItemRepository salesReturnItemRepository;
    private final ProductRepository productRepository;

    // CREATE
    @Override
    public SalesReturnItemResponseDTO createItem(SalesReturnItemRequestDTO dto) {

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // FIX: prevent null price crash
        BigDecimal price = product.getSellingPrice();
        if (price == null) {
            throw new RuntimeException("Product selling price is NULL");
        }

        BigDecimal total = price.multiply(BigDecimal.valueOf(dto.getQuantity()));

        SalesReturnItem item = new SalesReturnItem();
        item.setSalesReturnId(dto.getSalesReturnId());
        item.setInvoiceId(dto.getInvoiceId());
        item.setInvoiceItemId(dto.getInvoiceItemId());
        item.setProductId(dto.getProductId());
        item.setQuantity(dto.getQuantity());
        item.setPrice(price);
        item.setTotal(total);

        // A returned product goes back into stock.
        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        product.setStockQuantity(currentStock + dto.getQuantity());
        productRepository.save(product);

        return mapToDTO(salesReturnItemRepository.save(item));
    }

    // GET ALL
    @Override
    public List<SalesReturnItemResponseDTO> getAll() {
        return salesReturnItemRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // GET BY RETURN ID
    @Override
    public List<SalesReturnItemResponseDTO> getByReturnId(Long salesReturnId) {
        return salesReturnItemRepository.findBySalesReturnId(salesReturnId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Override
    public SalesReturnItemResponseDTO updateItem(Long id, SalesReturnItemRequestDTO dto) {

        SalesReturnItem item = salesReturnItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // Undo the restock this item previously applied, against its original product/quantity,
        // before applying the new one below — otherwise editing a return record (or switching
        // which product it refers to) would double-count stock.
        Product oldProduct = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        int oldStock = oldProduct.getStockQuantity() != null ? oldProduct.getStockQuantity() : 0;
        oldProduct.setStockQuantity(oldStock - item.getQuantity());
        productRepository.save(oldProduct);

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        BigDecimal price = product.getSellingPrice();
        if (price == null) {
            throw new RuntimeException("Product selling price is NULL");
        }

        item.setSalesReturnId(dto.getSalesReturnId());
        item.setInvoiceId(dto.getInvoiceId());
        item.setInvoiceItemId(dto.getInvoiceItemId());
        item.setProductId(dto.getProductId());
        item.setQuantity(dto.getQuantity());
        item.setPrice(price);
        item.setTotal(price.multiply(BigDecimal.valueOf(dto.getQuantity())));

        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        product.setStockQuantity(currentStock + dto.getQuantity());
        productRepository.save(product);

        return mapToDTO(salesReturnItemRepository.save(item));
    }

    // DELETE
    @Override
    public void deleteItem(Long id) {
        SalesReturnItem item = salesReturnItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // Deleting a return record means it should no longer be counted as restocked.
        productRepository.findById(item.getProductId()).ifPresent(product -> {
            int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            product.setStockQuantity(currentStock - item.getQuantity());
            productRepository.save(product);
        });

        salesReturnItemRepository.delete(item);
    }

    // MAPPER
    private SalesReturnItemResponseDTO mapToDTO(SalesReturnItem item) {

        SalesReturnItemResponseDTO dto = new SalesReturnItemResponseDTO();

        dto.setSalesReturnItemId(item.getSalesReturnItemId());
        dto.setSalesReturnId(item.getSalesReturnId());
        dto.setInvoiceId(item.getInvoiceId());
        dto.setInvoiceItemId(item.getInvoiceItemId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setTotal(item.getTotal());

        return dto;
    }
}