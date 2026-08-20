package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.EstimateItemRepository;
import com.example.InventoryManagementSystem.Repository.EstimateRepository;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.ProductTaxRepository;
import com.example.InventoryManagementSystem.Repository.ServiceMasterRepository;
import com.example.InventoryManagementSystem.dto.EstimateItemResponseDTO;
import com.example.InventoryManagementSystem.dto.EstimateLineItemRequestDTO;
import com.example.InventoryManagementSystem.dto.EstimateRequestDTO;
import com.example.InventoryManagementSystem.dto.EstimateResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Estimate;
import com.example.InventoryManagementSystem.model.EstimateItem;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.ServiceMaster;
import com.example.InventoryManagementSystem.util.InvoiceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// An estimate is a pre-invoice quote — same SERVICE/PRODUCT line shape and the same shared
// InvoiceCalculator as Invoice, but it never touches stock and never takes payment. Its lines
// become real InvoiceLineItemRequestDTOs once approved + QC passes (see JobCardServiceImpl).
@Service
@RequiredArgsConstructor
public class EstimateServiceImpl implements EstimateService {

    private final EstimateRepository estimateRepository;
    private final EstimateItemRepository estimateItemRepository;
    private final ProductRepository productRepository;
    private final ServiceMasterRepository serviceMasterRepository;
    private final ProductTaxRepository productTaxRepository;

    private static class ResolvedLine {
        String itemType;
        String itemName;
        Long serviceId;
        Long productId;
        String description;
        BigDecimal quantity;
        BigDecimal unitPrice;
        BigDecimal discount;
        BigDecimal taxPercentage;
    }

    @Override
    @Transactional
    public EstimateResponseDTO createEstimate(EstimateRequestDTO dto) {

        List<ResolvedLine> resolved = new ArrayList<>();
        for (EstimateLineItemRequestDTO line : dto.getItems()) {
            ResolvedLine r = new ResolvedLine();
            r.itemType = normalizeItemType(line.getItemType());
            r.quantity = line.getQuantity();
            r.discount = line.getDiscount() != null ? line.getDiscount() : BigDecimal.ZERO;
            r.description = line.getDescription();

            if ("SERVICE".equals(r.itemType)) {
                if (line.getServiceId() == null) {
                    throw new IllegalArgumentException("serviceId is required for a SERVICE line");
                }
                ServiceMaster service = serviceMasterRepository.findById(line.getServiceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + line.getServiceId()));
                r.serviceId = service.getServiceId();
                r.itemName = service.getServiceName();
                r.unitPrice = line.getUnitPrice() != null ? line.getUnitPrice()
                        : (service.getDefaultPrice() != null ? service.getDefaultPrice() : BigDecimal.ZERO);
                r.taxPercentage = service.getGstPercentage() != null ? service.getGstPercentage() : BigDecimal.ZERO;
            } else {
                if (line.getProductId() == null) {
                    throw new IllegalArgumentException("productId is required for a PRODUCT line");
                }
                Product product = productRepository.findById(line.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + line.getProductId()));
                r.productId = product.getProductId();
                r.itemName = product.getProductName();
                r.unitPrice = line.getUnitPrice() != null ? line.getUnitPrice()
                        : (product.getSellingPrice() != null ? product.getSellingPrice() : BigDecimal.ZERO);
                r.taxPercentage = productTaxRepository.findTopByProductId(product.getProductId())
                        .map(tax -> tax.getTaxPercentage() != null ? BigDecimal.valueOf(tax.getTaxPercentage()) : BigDecimal.ZERO)
                        .orElse(BigDecimal.ZERO);
                // No stock check here — an estimate is a quote, not a commitment; stock is
                // validated for real when the job card actually generates the invoice.
            }
            resolved.add(r);
        }

        List<InvoiceCalculator.LineInput> calcInputs = resolved.stream()
                .map(r -> new InvoiceCalculator.LineInput(r.itemType, r.unitPrice, r.quantity.intValue(), r.discount, r.taxPercentage))
                .collect(Collectors.toList());
        InvoiceCalculator.InvoiceTotals totals = InvoiceCalculator.calculate(calcInputs, dto.getDiscountAmount());

        Estimate estimate = new Estimate();
        estimate.setEstimateNumber("EST-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000));
        estimate.setJobCardId(dto.getJobCardId());
        estimate.setCustomerId(dto.getCustomerId());
        estimate.setSubtotal(totals.getSubtotal());
        estimate.setDiscountAmount(totals.getDiscountAmount());
        estimate.setTaxAmount(totals.getTaxAmount());
        estimate.setGrandTotal(totals.getGrandTotal());
        estimate.setNotes(dto.getNotes());
        estimate.setStatus("PENDING");

        Estimate saved = estimateRepository.save(estimate);

        List<EstimateItem> savedItems = new ArrayList<>();
        for (int i = 0; i < resolved.size(); i++) {
            ResolvedLine r = resolved.get(i);
            InvoiceCalculator.LineResult lr = totals.getLines().get(i);

            EstimateItem item = new EstimateItem();
            item.setEstimateId(saved.getEstimateId());
            item.setItemType(r.itemType);
            item.setServiceId(r.serviceId);
            item.setProductId(r.productId);
            item.setDescription(r.description != null ? r.description : r.itemName);
            item.setQuantity(r.quantity);
            item.setUnitPrice(r.unitPrice);
            item.setDiscount(r.discount);
            item.setTaxPercentage(lr.getTaxPercentage());
            item.setTaxAmount(lr.getTaxAmount());
            item.setTotalAmount(lr.getTotalAmount());

            savedItems.add(estimateItemRepository.save(item));
        }

        return mapToDto(saved, savedItems);
    }

    @Override
    public EstimateResponseDTO getEstimateById(Long id) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estimate not found with id: " + id));
        return mapToDto(estimate, estimateItemRepository.findByEstimateId(id));
    }

    @Override
    public List<EstimateResponseDTO> getAllEstimates() {
        return estimateRepository.findAll().stream()
                .map(e -> mapToDto(e, estimateItemRepository.findByEstimateId(e.getEstimateId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<EstimateResponseDTO> getByJobCardId(Long jobCardId) {
        return estimateRepository.findByJobCardId(jobCardId).stream()
                .map(e -> mapToDto(e, estimateItemRepository.findByEstimateId(e.getEstimateId())))
                .collect(Collectors.toList());
    }

    @Override
    public EstimateResponseDTO approve(Long id, String approvedBy) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estimate not found with id: " + id));
        estimate.setStatus("APPROVED");
        estimate.setApprovedDate(OffsetDateTime.now());
        estimate.setApprovedBy(approvedBy);
        Estimate saved = estimateRepository.save(estimate);
        return mapToDto(saved, estimateItemRepository.findByEstimateId(id));
    }

    @Override
    public EstimateResponseDTO reject(Long id, String notes) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estimate not found with id: " + id));
        estimate.setStatus("REJECTED");
        if (notes != null) estimate.setNotes(notes);
        Estimate saved = estimateRepository.save(estimate);
        return mapToDto(saved, estimateItemRepository.findByEstimateId(id));
    }

    @Override
    public void deleteEstimate(Long id) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estimate not found with id: " + id));
        estimateRepository.delete(estimate);
    }

    private String normalizeItemType(String itemType) {
        if (itemType == null || itemType.isBlank()) {
            throw new IllegalArgumentException("itemType is required (SERVICE or PRODUCT)");
        }
        String upper = itemType.trim().toUpperCase();
        if (!upper.equals("SERVICE") && !upper.equals("PRODUCT")) {
            throw new IllegalArgumentException("itemType must be SERVICE or PRODUCT");
        }
        return upper;
    }

    private EstimateResponseDTO mapToDto(Estimate estimate, List<EstimateItem> items) {
        EstimateResponseDTO dto = new EstimateResponseDTO();
        dto.setEstimateId(estimate.getEstimateId());
        dto.setEstimateNumber(estimate.getEstimateNumber());
        dto.setJobCardId(estimate.getJobCardId());
        dto.setCustomerId(estimate.getCustomerId());
        dto.setSubtotal(estimate.getSubtotal());
        dto.setDiscountAmount(estimate.getDiscountAmount());
        dto.setTaxAmount(estimate.getTaxAmount());
        dto.setGrandTotal(estimate.getGrandTotal());
        dto.setStatus(estimate.getStatus());
        dto.setApprovedDate(estimate.getApprovedDate());
        dto.setApprovedBy(estimate.getApprovedBy());
        dto.setNotes(estimate.getNotes());
        dto.setCreatedAt(estimate.getCreatedAt());
        dto.setUpdatedAt(estimate.getUpdatedAt());
        dto.setItems(items.stream().map(this::mapItemToDto).collect(Collectors.toList()));
        return dto;
    }

    private EstimateItemResponseDTO mapItemToDto(EstimateItem item) {
        EstimateItemResponseDTO dto = new EstimateItemResponseDTO();
        dto.setEstimateItemId(item.getEstimateItemId());
        dto.setEstimateId(item.getEstimateId());
        dto.setItemType(item.getItemType());
        dto.setServiceId(item.getServiceId());
        dto.setProductId(item.getProductId());
        dto.setDescription(item.getDescription());
        dto.setItemName(item.getDescription());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscount(item.getDiscount());
        dto.setTaxPercentage(item.getTaxPercentage());
        dto.setTaxAmount(item.getTaxAmount());
        dto.setTotalAmount(item.getTotalAmount());
        return dto;
    }
}
