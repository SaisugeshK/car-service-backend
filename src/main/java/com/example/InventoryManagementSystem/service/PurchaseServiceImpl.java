package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseItemRepository;
import com.example.InventoryManagementSystem.Repository.PurchaseRepository;
import com.example.InventoryManagementSystem.Repository.SupplierRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.dto.PurchaseItemResponseDto;
import com.example.InventoryManagementSystem.dto.PurchaseLineItemRequestDto;
import com.example.InventoryManagementSystem.dto.PurchaseRequestDto;
import com.example.InventoryManagementSystem.dto.PurchaseResponseDto;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.Purchase;
import com.example.InventoryManagementSystem.model.PurchaseItem;
import com.example.InventoryManagementSystem.model.Supplier;
import com.example.InventoryManagementSystem.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // CREATE PURCHASE
    @Override
    @Transactional
    public PurchaseResponseDto createPurchase(PurchaseRequestDto dto) {

        if (dto.getSupplierId() == null) {
            throw new IllegalArgumentException("supplierId is required");
        }

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + dto.getSupplierId()));

        Purchase purchase = new Purchase();
        purchase.setSupplierId(supplier);
        purchase.setTotalAmount(dto.getTotalAmount());
        purchase.setTax(dto.getTax());
        purchase.setPaymentStatus(dto.getPaymentStatus());

        if (dto.getCreatedBy() != null) {
            User createdBy = userRepository.findById(dto.getCreatedBy())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getCreatedBy()));
            purchase.setCreatedBy(createdBy);
        }

        // Auto-generate the invoice number when the caller didn't supply one — a billing/purchasing
        // system shouldn't require a person to type a unique reference by hand.
        if (dto.getInvoiceNumber() == null || dto.getInvoiceNumber().isBlank()) {
            Purchase saved = purchaseRepository.save(purchase);
            purchase.setInvoiceNumber("PUR-" + saved.getPurchaseId() + "-" + System.currentTimeMillis());
        } else {
            purchase.setInvoiceNumber(dto.getInvoiceNumber());
        }

        Purchase saved = purchaseRepository.save(purchase);

        List<PurchaseItem> savedItems = new ArrayList<>();
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (PurchaseLineItemRequestDto line : dto.getItems()) {

                Product product = productRepository.findById(line.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + line.getProductId()));

                // A purchase brings stock in.
                int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                product.setStockQuantity(currentStock + line.getQuantity());
                productRepository.save(product);

                BigDecimal price = line.getPurchasePrice() != null ? line.getPurchasePrice() : BigDecimal.ZERO;
                BigDecimal tax = line.getTaxAmount() != null ? line.getTaxAmount() : BigDecimal.ZERO;
                BigDecimal total = price.multiply(BigDecimal.valueOf(line.getQuantity())).add(tax);

                PurchaseItem item = PurchaseItem.builder()
                        .purchase(saved)
                        .product(product)
                        .quantity(line.getQuantity())
                        .purchasePrice(price)
                        .taxAmount(tax)
                        .total(total)
                        .build();

                savedItems.add(purchaseItemRepository.save(item));
            }
        }

        return mapToDto(saved, savedItems);
    }

    // GET ALL PURCHASES
    @Override
    public List<PurchaseResponseDto> getAllPurchases() {

        return purchaseRepository.findAll()
                .stream()
                .map(purchase -> mapToDto(purchase, purchaseItemRepository.findByPurchase_PurchaseId(purchase.getPurchaseId())))
                .toList();
    }

    // GET PURCHASE BY ID
    @Override
    public PurchaseResponseDto getPurchaseById(Long id) {

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));

        return mapToDto(purchase, purchaseItemRepository.findByPurchase_PurchaseId(id));
    }

    // UPDATE PURCHASE
    @Override
    @Transactional
    public PurchaseResponseDto updatePurchase(Long id, PurchaseRequestDto dto) {

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));

        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + dto.getSupplierId()));
            purchase.setSupplierId(supplier);
        }

        // Only overwrite the invoice number if a real one was supplied — never blank out the
        // auto-generated one just because the field wasn't part of this edit's payload.
        if (dto.getInvoiceNumber() != null && !dto.getInvoiceNumber().isBlank()) {
            purchase.setInvoiceNumber(dto.getInvoiceNumber());
        }

        if (dto.getTotalAmount() != null) purchase.setTotalAmount(dto.getTotalAmount());
        if (dto.getTax() != null) purchase.setTax(dto.getTax());
        if (dto.getPaymentStatus() != null) purchase.setPaymentStatus(dto.getPaymentStatus());

        if (dto.getCreatedBy() != null) {
            User createdBy = userRepository.findById(dto.getCreatedBy())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getCreatedBy()));
            purchase.setCreatedBy(createdBy);
        }

        Purchase updated = purchaseRepository.save(purchase);

        return mapToDto(updated, purchaseItemRepository.findByPurchase_PurchaseId(id));
    }

    // DELETE PURCHASE
    @Override
    public void deletePurchase(Long id) {

        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id: " + id));

        purchaseRepository.delete(purchase);
    }

    // MAP ENTITY TO DTO
    private PurchaseResponseDto mapToDto(Purchase purchase, List<PurchaseItem> items) {

        PurchaseResponseDto dto = new PurchaseResponseDto();
        dto.setPurchaseId(purchase.getPurchaseId());
        dto.setSupplierName(purchase.getSupplierId() != null ? purchase.getSupplierId().getSupplierName() : null);
        dto.setInvoiceNumber(purchase.getInvoiceNumber());
        dto.setPurchaseDate(purchase.getPurchaseDate());
        dto.setTotalAmount(purchase.getTotalAmount());
        dto.setTax(purchase.getTax());
        dto.setPaymentStatus(purchase.getPaymentStatus());
        dto.setCreatedBy(purchase.getCreatedBy() != null ? purchase.getCreatedBy().getUsername() : null);

        if (items != null) {
            dto.setItems(items.stream().map(this::mapItemToDto).collect(Collectors.toList()));
        }

        return dto;
    }

    private PurchaseItemResponseDto mapItemToDto(PurchaseItem item) {
        return PurchaseItemResponseDto.builder()
                .purchaseItemId(item.getPurchaseItemId())
                .purchaseId(item.getPurchase().getPurchaseId())
                .invoiceNumber(item.getPurchase().getInvoiceNumber())
                .productId(item.getProduct().getProductId())
                .productName(item.getProduct().getProductName())
                .quantity(item.getQuantity())
                .purchasePrice(item.getPurchasePrice())
                .taxAmount(item.getTaxAmount())
                .total(item.getTotal())
                .build();
    }
}
