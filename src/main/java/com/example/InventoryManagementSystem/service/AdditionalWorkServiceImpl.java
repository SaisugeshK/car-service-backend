package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.AdditionalWorkItemRepository;
import com.example.InventoryManagementSystem.Repository.AdditionalWorkRequestRepository;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.ProductTaxRepository;
import com.example.InventoryManagementSystem.Repository.ServiceMasterRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.dto.AdditionalWorkItemResponseDTO;
import com.example.InventoryManagementSystem.dto.AdditionalWorkLineItemRequestDTO;
import com.example.InventoryManagementSystem.dto.AdditionalWorkRequestDTO;
import com.example.InventoryManagementSystem.dto.AdditionalWorkResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.AdditionalWorkItem;
import com.example.InventoryManagementSystem.model.AdditionalWorkRequest;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.ServiceMaster;
import com.example.InventoryManagementSystem.model.User;
import com.example.InventoryManagementSystem.util.InvoiceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Work a technician finds mid-service, outside the original estimate. Its own approve/reject
// cycle — same SERVICE/PRODUCT line shape and the same shared InvoiceCalculator as Estimate, but
// keyed to a job card directly (no separate "job card" concept to convert into, it's already on
// one). Only APPROVED requests are ever eligible for billing — see JobCardServiceImpl.generateInvoice
// and InvoiceServiceImpl.validateAgainstJobCard.
@Service
@RequiredArgsConstructor
public class AdditionalWorkServiceImpl implements AdditionalWorkService {

    private final AdditionalWorkRequestRepository requestRepository;
    private final AdditionalWorkItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final ServiceMasterRepository serviceMasterRepository;
    private final ProductTaxRepository productTaxRepository;
    private final UserRepository userRepository;
    private final JobCardService jobCardService;
    private final NotificationEventService notificationEventService;
    private final AuditLogService auditLogService;

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
    public AdditionalWorkResponseDTO create(AdditionalWorkRequestDTO dto) {

        List<ResolvedLine> resolved = new ArrayList<>();
        for (AdditionalWorkLineItemRequestDTO line : dto.getItems()) {
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
                // No stock check / no stock deduction here — nothing is committed until this
                // request is approved and actually invoiced.
            }

            BigDecimal gross = r.unitPrice.multiply(r.quantity);
            if (r.discount.compareTo(gross) > 0) {
                throw new IllegalArgumentException("Discount (" + r.discount + ") cannot exceed the line amount ("
                        + gross + ") for '" + r.itemName + "'");
            }

            resolved.add(r);
        }

        List<InvoiceCalculator.LineInput> calcInputs = resolved.stream()
                .map(r -> new InvoiceCalculator.LineInput(r.itemType, r.unitPrice, r.quantity.intValue(), r.discount, r.taxPercentage))
                .collect(Collectors.toList());
        InvoiceCalculator.InvoiceTotals totals = InvoiceCalculator.calculate(calcInputs, BigDecimal.ZERO);

        AdditionalWorkRequest request = new AdditionalWorkRequest();
        request.setJobCardId(dto.getJobCardId());
        request.setRequestedByUserId(dto.getRequestedByUserId());
        request.setNotes(dto.getNotes());
        request.setSubtotal(totals.getSubtotal());
        request.setDiscountAmount(totals.getDiscountAmount());
        request.setTaxAmount(totals.getTaxAmount());
        request.setGrandTotal(totals.getGrandTotal());
        request.setStatus("PENDING");

        AdditionalWorkRequest saved = requestRepository.save(request);

        List<AdditionalWorkItem> savedItems = new ArrayList<>();
        for (int i = 0; i < resolved.size(); i++) {
            ResolvedLine r = resolved.get(i);
            InvoiceCalculator.LineResult lr = totals.getLines().get(i);

            AdditionalWorkItem item = new AdditionalWorkItem();
            item.setAdditionalWorkRequestId(saved.getAdditionalWorkRequestId());
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

            savedItems.add(itemRepository.save(item));
        }

        // Customer needs to decide before work continues — surfaces on the job card timeline too.
        jobCardService.updateStatus(dto.getJobCardId(), "ADDITIONAL_APPROVAL_REQUIRED");

        notificationEventService.raise("ADDITIONAL_APPROVAL", "Additional work needs approval",
                "Additional work (" + saved.getGrandTotal() + ") requested on job card #" + dto.getJobCardId() + ".",
                "ADDITIONAL_WORK", saved.getAdditionalWorkRequestId());
        auditLogService.record("ADDITIONAL_WORK_REQUESTED", "ADDITIONAL_WORK", saved.getAdditionalWorkRequestId(),
                "Additional work (" + saved.getGrandTotal() + ") requested on job card #" + dto.getJobCardId() + ".");

        return mapToDto(saved, savedItems);
    }

    @Override
    public List<AdditionalWorkResponseDTO> getByJobCard(Long jobCardId) {
        return requestRepository.findByJobCardIdOrderByRequestedAtDesc(jobCardId).stream()
                .map(r -> mapToDto(r, itemRepository.findByAdditionalWorkRequestId(r.getAdditionalWorkRequestId())))
                .collect(Collectors.toList());
    }

    // Phase 23 — Reports needs a global view (counts/value by status) that per-job-card lookup
    // can't give it. Nothing before this called for "all additional work requests" as a concept.
    @Override
    public List<AdditionalWorkResponseDTO> getAll() {
        return requestRepository.findAll().stream()
                .map(r -> mapToDto(r, itemRepository.findByAdditionalWorkRequestId(r.getAdditionalWorkRequestId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdditionalWorkResponseDTO approve(Long id, String decidedBy) {
        AdditionalWorkRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Additional work request not found with id: " + id));
        // Phase 34 — billing safety: same reasoning as Estimate approve/reject. Without this, a
        // request the customer already REJECTED could be silently flipped to APPROVED with no
        // fresh consent, and generateInvoice() bills whatever is APPROVED at that moment.
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("This additional work request is already "
                    + request.getStatus() + " — only a PENDING request can be approved");
        }
        request.setStatus("APPROVED");
        request.setDecidedAt(OffsetDateTime.now());
        request.setDecidedBy(decidedBy);
        AdditionalWorkRequest saved = requestRepository.save(request);

        // Approved — work resumes. Billing picks this up automatically the next time the job
        // card's invoice is generated (see JobCardServiceImpl.generateInvoice).
        jobCardService.updateStatus(saved.getJobCardId(), "IN_PROGRESS");

        auditLogService.record("ADDITIONAL_WORK_APPROVED", "ADDITIONAL_WORK", saved.getAdditionalWorkRequestId(),
                "Additional work request #" + saved.getAdditionalWorkRequestId() + " approved.");

        return mapToDto(saved, itemRepository.findByAdditionalWorkRequestId(id));
    }

    @Override
    @Transactional
    public AdditionalWorkResponseDTO reject(Long id, String decidedBy) {
        AdditionalWorkRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Additional work request not found with id: " + id));
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("This additional work request is already "
                    + request.getStatus() + " — only a PENDING request can be rejected");
        }
        request.setStatus("REJECTED");
        request.setDecidedAt(OffsetDateTime.now());
        request.setDecidedBy(decidedBy);
        AdditionalWorkRequest saved = requestRepository.save(request);

        // Rejected — never billed, but the job itself still continues.
        jobCardService.updateStatus(saved.getJobCardId(), "IN_PROGRESS");

        auditLogService.record("ADDITIONAL_WORK_REJECTED", "ADDITIONAL_WORK", saved.getAdditionalWorkRequestId(),
                "Additional work request #" + saved.getAdditionalWorkRequestId() + " rejected.");

        return mapToDto(saved, itemRepository.findByAdditionalWorkRequestId(id));
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

    private AdditionalWorkResponseDTO mapToDto(AdditionalWorkRequest request, List<AdditionalWorkItem> items) {
        AdditionalWorkResponseDTO dto = new AdditionalWorkResponseDTO();
        dto.setAdditionalWorkRequestId(request.getAdditionalWorkRequestId());
        dto.setJobCardId(request.getJobCardId());
        dto.setRequestedByUserId(request.getRequestedByUserId());
        if (request.getRequestedByUserId() != null) {
            userRepository.findById(request.getRequestedByUserId())
                    .ifPresent(u -> dto.setRequestedByName(displayName(u)));
        }
        dto.setNotes(request.getNotes());
        dto.setSubtotal(request.getSubtotal());
        dto.setDiscountAmount(request.getDiscountAmount());
        dto.setTaxAmount(request.getTaxAmount());
        dto.setGrandTotal(request.getGrandTotal());
        dto.setStatus(request.getStatus());
        dto.setRequestedAt(request.getRequestedAt());
        dto.setDecidedAt(request.getDecidedAt());
        dto.setDecidedBy(request.getDecidedBy());
        dto.setItems(items.stream().map(this::mapItemToDto).collect(Collectors.toList()));
        return dto;
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        return user.getUsername();
    }

    private AdditionalWorkItemResponseDTO mapItemToDto(AdditionalWorkItem item) {
        AdditionalWorkItemResponseDTO dto = new AdditionalWorkItemResponseDTO();
        dto.setAdditionalWorkItemId(item.getAdditionalWorkItemId());
        dto.setAdditionalWorkRequestId(item.getAdditionalWorkRequestId());
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
