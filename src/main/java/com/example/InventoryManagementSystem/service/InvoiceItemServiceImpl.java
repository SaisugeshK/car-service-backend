package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.InvoiceItemRepository;
import com.example.InventoryManagementSystem.Repository.InvoiceRepository;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.ServiceMasterRepository;
import com.example.InventoryManagementSystem.dto.InvoiceItemRequestDTO;
import com.example.InventoryManagementSystem.dto.InvoiceItemResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.InvoiceItem;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.ServiceMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// Standalone manual line-item CRUD (coexists with the InvoiceServiceImpl cascade the same way
// SalesItemServiceImpl coexists with SalesServiceImpl) — for admin corrections to an existing
// invoice's items outside of the normal POS "complete invoice" flow.
@Service
@RequiredArgsConstructor
public class InvoiceItemServiceImpl implements InvoiceItemService {

    private final InvoiceItemRepository repository;
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final ServiceMasterRepository serviceMasterRepository;

    // A cancelled invoice already had all its PRODUCT lines restocked by
    // InvoiceServiceImpl.cancelInvoice — updating/deleting its items afterward must not
    // restock a second time.
    private boolean isCancelled(Integer invoiceId) {
        if (invoiceId == null) return false;
        return invoiceRepository.findById(invoiceId.longValue())
                .map(inv -> "CANCELLED".equals(inv.getStatus()))
                .orElse(false);
    }

    @Override
    public InvoiceItemResponseDTO createInvoiceItem(InvoiceItemRequestDTO dto) {

        InvoiceItem item = new InvoiceItem();
        item.setInvoiceId(dto.getInvoiceId());
        item.setItemType(dto.getItemType());
        item.setServiceId(dto.getServiceId());
        item.setProductId(dto.getProductId());
        item.setDescription(dto.getDescription());
        item.setBarcode(dto.getBarcode());
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(dto.getUnitPrice());
        item.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : java.math.BigDecimal.ZERO);
        item.setTaxPercentage(dto.getTaxPercentage() != null ? dto.getTaxPercentage() : java.math.BigDecimal.ZERO);

        java.math.BigDecimal taxable = dto.getUnitPrice().multiply(dto.getQuantity())
                .subtract(item.getDiscount());
        if (taxable.signum() < 0) taxable = java.math.BigDecimal.ZERO;
        item.setTaxAmount(taxable.multiply(item.getTaxPercentage()).divide(java.math.BigDecimal.valueOf(100)));
        item.setTotalAmount(taxable.add(item.getTaxAmount()));

        // Products are stock-tracked; services never are — mirrors the guard already proven
        // necessary in SalesItemServiceImpl (which lacks it and NPEs on service-type products).
        if ("PRODUCT".equals(dto.getItemType()) && dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId().longValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            int available = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            int requested = dto.getQuantity().intValue();
            if (available < requested) {
                throw new IllegalArgumentException("Insufficient stock for " + product.getProductName()
                        + ". Available: " + available + ", Required: " + requested + ".");
            }
            product.setStockQuantity(available - requested);
            productRepository.save(product);
        } else if ("SERVICE".equals(dto.getItemType()) && dto.getServiceId() != null) {
            serviceMasterRepository.findById(dto.getServiceId().longValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        }

        return mapToDTO(repository.save(item));
    }

    @Override
    public List<InvoiceItemResponseDTO> getAllInvoiceItems() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public InvoiceItemResponseDTO getInvoiceItemById(Long id) {
        InvoiceItem item = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice item not found"));
        return mapToDTO(item);
    }

    @Override
    public InvoiceItemResponseDTO updateInvoiceItem(Long id, InvoiceItemRequestDTO dto) {

        InvoiceItem item = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice item not found"));

        // Restore stock for the old quantity before applying the new one, if it was a product line.
        if ("PRODUCT".equals(item.getItemType()) && item.getProductId() != null && !isCancelled(item.getInvoiceId())) {
            productRepository.findById(item.getProductId().longValue()).ifPresent(p -> {
                int stock = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
                p.setStockQuantity(stock + item.getQuantity().intValue());
                productRepository.save(p);
            });
        }

        item.setInvoiceId(dto.getInvoiceId());
        item.setItemType(dto.getItemType());
        item.setServiceId(dto.getServiceId());
        item.setProductId(dto.getProductId());
        item.setDescription(dto.getDescription());
        item.setBarcode(dto.getBarcode());
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(dto.getUnitPrice());
        item.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : java.math.BigDecimal.ZERO);
        item.setTaxPercentage(dto.getTaxPercentage() != null ? dto.getTaxPercentage() : java.math.BigDecimal.ZERO);

        java.math.BigDecimal taxable = dto.getUnitPrice().multiply(dto.getQuantity()).subtract(item.getDiscount());
        if (taxable.signum() < 0) taxable = java.math.BigDecimal.ZERO;
        item.setTaxAmount(taxable.multiply(item.getTaxPercentage()).divide(java.math.BigDecimal.valueOf(100)));
        item.setTotalAmount(taxable.add(item.getTaxAmount()));

        if ("PRODUCT".equals(dto.getItemType()) && dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId().longValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            int available = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            int requested = dto.getQuantity().intValue();
            if (available < requested) {
                throw new IllegalArgumentException("Insufficient stock for " + product.getProductName()
                        + ". Available: " + available + ", Required: " + requested + ".");
            }
            product.setStockQuantity(available - requested);
            productRepository.save(product);
        }

        return mapToDTO(repository.save(item));
    }

    @Override
    public void deleteInvoiceItem(Long id) {
        InvoiceItem item = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice item not found"));

        if ("PRODUCT".equals(item.getItemType()) && item.getProductId() != null && !isCancelled(item.getInvoiceId())) {
            productRepository.findById(item.getProductId().longValue()).ifPresent(p -> {
                int stock = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
                p.setStockQuantity(stock + item.getQuantity().intValue());
                productRepository.save(p);
            });
        }

        repository.delete(item);
    }

    private InvoiceItemResponseDTO mapToDTO(InvoiceItem item) {
        InvoiceItemResponseDTO dto = new InvoiceItemResponseDTO();
        dto.setInvoiceItemId(item.getInvoiceItemId());
        dto.setInvoiceId(item.getInvoiceId());
        dto.setItemType(item.getItemType());
        dto.setServiceId(item.getServiceId());
        dto.setProductId(item.getProductId());
        dto.setDescription(item.getDescription());
        dto.setBarcode(item.getBarcode());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscount(item.getDiscount());
        dto.setTaxPercentage(item.getTaxPercentage());
        dto.setTaxAmount(item.getTaxAmount());
        dto.setTotalAmount(item.getTotalAmount());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());

        dto.setItemName(item.getDescription());
        if ("SERVICE".equals(item.getItemType()) && item.getServiceId() != null) {
            ServiceMaster s = serviceMasterRepository.findById(item.getServiceId().longValue()).orElse(null);
            if (s != null) dto.setItemName(s.getServiceName());
        } else if (item.getProductId() != null) {
            Product p = productRepository.findById(item.getProductId().longValue()).orElse(null);
            if (p != null) dto.setItemName(p.getProductName());
        }

        return dto;
    }
}
