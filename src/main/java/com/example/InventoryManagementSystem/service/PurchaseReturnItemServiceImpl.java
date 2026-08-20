package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.PurchaseReturnItemRequestDTO;
import com.example.InventoryManagementSystem.dto.PurchaseReturnItemResponseDTO;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseReturnItemRepository;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.PurchaseReturnItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseReturnItemServiceImpl
        implements PurchaseReturnItemService {

    private final PurchaseReturnItemRepository repository;
    private final ProductRepository productRepository;

    // A purchase return sends stock back OUT to the supplier.
    private void adjustStock(Long productId, int delta) {
        if (productId == null) return;
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        int current = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        int updated = current + delta;
        if (updated < 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock for " + product.getProductName()
                            + " (available: " + current + ", requested return: " + (-delta) + ")");
        }
        product.setStockQuantity(updated);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public PurchaseReturnItemResponseDTO createPurchaseReturnItem(
            PurchaseReturnItemRequestDTO requestDTO) {

        BigDecimal total = requestDTO.getPrice()
                .multiply(BigDecimal.valueOf(requestDTO.getQuantity()));

        adjustStock(requestDTO.getProductId() == null ? null : requestDTO.getProductId().longValue(),
                -requestDTO.getQuantity());

        PurchaseReturnItem entity = PurchaseReturnItem.builder()
                .purchaseReturnId(requestDTO.getPurchaseReturnId())
                .productId(requestDTO.getProductId())
                .quantity(requestDTO.getQuantity())
                .price(requestDTO.getPrice())
                .total(total)
                .build();

        PurchaseReturnItem saved = repository.save(entity);

        return mapToResponse(saved);
    }

    @Override
    public PurchaseReturnItemResponseDTO getPurchaseReturnItemById(
            Integer id) {

        PurchaseReturnItem entity =
                repository.findById(id).orElse(null);

        if (entity == null) {
            return null;
        }

        return mapToResponse(entity);
    }

    @Override
    public List<PurchaseReturnItemResponseDTO>
    getAllPurchaseReturnItems() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PurchaseReturnItemResponseDTO updatePurchaseReturnItem(
            Integer id,
            PurchaseReturnItemRequestDTO requestDTO) {

        PurchaseReturnItem entity =
                repository.findById(id).orElse(null);

        if (entity == null) {
            return null;
        }

        // Restore stock for the old line, then re-deduct for the new one — same pattern used
        // throughout this codebase for stock-affecting edits (e.g. SalesItemServiceImpl).
        adjustStock(entity.getProductId() == null ? null : entity.getProductId().longValue(),
                entity.getQuantity());

        BigDecimal total = requestDTO.getPrice()
                .multiply(BigDecimal.valueOf(requestDTO.getQuantity()));

        adjustStock(requestDTO.getProductId() == null ? null : requestDTO.getProductId().longValue(),
                -requestDTO.getQuantity());

        entity.setPurchaseReturnId(requestDTO.getPurchaseReturnId());
        entity.setProductId(requestDTO.getProductId());
        entity.setQuantity(requestDTO.getQuantity());
        entity.setPrice(requestDTO.getPrice());
        entity.setTotal(total);

        PurchaseReturnItem updated = repository.save(entity);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deletePurchaseReturnItem(Integer id) {

        PurchaseReturnItem entity =
                repository.findById(id).orElse(null);

        if (entity != null) {
            adjustStock(entity.getProductId() == null ? null : entity.getProductId().longValue(),
                    entity.getQuantity());
            repository.delete(entity);
        }
    }

    private PurchaseReturnItemResponseDTO mapToResponse(
            PurchaseReturnItem entity) {

        return PurchaseReturnItemResponseDTO.builder()
                .purchaseReturnItemId(entity.getPurchaseReturnItemId())
                .purchaseReturnId(entity.getPurchaseReturnId())
                .productId(entity.getProductId())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .total(entity.getTotal())
                .build();
    }
}
