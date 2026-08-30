package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.EstimateItemRepository;
import com.example.InventoryManagementSystem.Repository.EstimateRepository;
import com.example.InventoryManagementSystem.Repository.JobCardRepository;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.ProductTaxRepository;
import com.example.InventoryManagementSystem.Repository.ServiceMasterRepository;
import com.example.InventoryManagementSystem.Repository.VehicleRepository;
import com.example.InventoryManagementSystem.dto.EstimateItemResponseDTO;
import com.example.InventoryManagementSystem.dto.EstimateLineItemRequestDTO;
import com.example.InventoryManagementSystem.dto.EstimateRequestDTO;
import com.example.InventoryManagementSystem.dto.EstimateResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Estimate;
import com.example.InventoryManagementSystem.model.EstimateItem;
import com.example.InventoryManagementSystem.model.JobCard;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.ServiceMaster;
import com.example.InventoryManagementSystem.model.Vehicle;
import com.example.InventoryManagementSystem.util.InvoiceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final CustomerRepository customerRepository;
    private final NotificationEventService notificationEventService;
    private final AuditLogService auditLogService;
    private final SettingsLookupService settingsLookupService;
    private final JobCardRepository jobCardRepository;
    private final VehicleRepository vehicleRepository;
    private final ServicePricingResolver servicePricingResolver;

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
        String workCategory;
    }

    @Override
    @Transactional
    public EstimateResponseDTO createEstimate(EstimateRequestDTO dto) {
        String estimateNumber = settingsLookupService.get("estimate_prefix", "EST") + "-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
        return saveEstimateAndItems(dto, estimateNumber, null, 1);
    }

    @Override
    @Transactional
    public EstimateResponseDTO reviseEstimate(Long originalId, EstimateRequestDTO dto) {
        Estimate original = estimateRepository.findById(originalId)
                .orElseThrow(() -> new ResourceNotFoundException("Estimate not found with id: " + originalId));

        Long rootId = original.getRootEstimateId() != null ? original.getRootEstimateId() : original.getEstimateId();
        int nextRevision = getLineage(rootId).stream()
                .map(e -> e.getRevisionNumber() != null ? e.getRevisionNumber() : 1)
                .max(Integer::compareTo)
                .orElse(original.getRevisionNumber() != null ? original.getRevisionNumber() : 1) + 1;

        // original row is never modified — it stays exactly as the customer saw it.
        return saveEstimateAndItems(dto, original.getEstimateNumber(), rootId, nextRevision);
    }

    @Override
    public List<EstimateResponseDTO> getRevisions(Long estimateId) {
        Estimate estimate = estimateRepository.findById(estimateId)
                .orElseThrow(() -> new ResourceNotFoundException("Estimate not found with id: " + estimateId));
        Long rootId = estimate.getRootEstimateId() != null ? estimate.getRootEstimateId() : estimate.getEstimateId();
        return getLineage(rootId).stream()
                .sorted((a, b) -> {
                    int ra = a.getRevisionNumber() != null ? a.getRevisionNumber() : 1;
                    int rb = b.getRevisionNumber() != null ? b.getRevisionNumber() : 1;
                    return Integer.compare(ra, rb);
                })
                .map(e -> mapToDto(e, estimateItemRepository.findByEstimateId(e.getEstimateId())))
                .collect(Collectors.toList());
    }

    /** The root estimate itself plus every estimate that points at it as rootEstimateId. */
    private List<Estimate> getLineage(Long rootId) {
        List<Estimate> lineage = new ArrayList<>(estimateRepository.findByRootEstimateId(rootId));
        estimateRepository.findById(rootId).ifPresent(lineage::add);
        return lineage;
    }

    private EstimateResponseDTO saveEstimateAndItems(EstimateRequestDTO dto, String estimateNumber, Long rootEstimateId, int revisionNumber) {
        String vehicleSizeClass = resolveVehicleSizeClass(dto.getJobCardId());

        List<ResolvedLine> resolved = new ArrayList<>();
        for (EstimateLineItemRequestDTO line : dto.getItems()) {
            ResolvedLine r = new ResolvedLine();
            r.itemType = normalizeItemType(line.getItemType());
            r.quantity = line.getQuantity();
            r.discount = line.getDiscount() != null ? line.getDiscount() : BigDecimal.ZERO;
            r.description = line.getDescription();
            r.workCategory = (line.getWorkCategory() != null && !line.getWorkCategory().isBlank())
                    ? line.getWorkCategory().trim().toUpperCase() : "RECOMMENDED";

            if ("SERVICE".equals(r.itemType)) {
                if (line.getServiceId() == null) {
                    throw new IllegalArgumentException("serviceId is required for a SERVICE line");
                }
                ServiceMaster service = serviceMasterRepository.findById(line.getServiceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + line.getServiceId()));
                r.serviceId = service.getServiceId();
                r.itemName = service.getServiceName();
                // An explicit unitPrice from the advisor always wins. Otherwise the price is the
                // one set for this vehicle's size band on the service, or its base price.
                r.unitPrice = line.getUnitPrice() != null ? line.getUnitPrice()
                        : servicePricingResolver.priceFor(service.getServiceId(), vehicleSizeClass)
                                .setScale(2, RoundingMode.HALF_UP);
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
        InvoiceCalculator.InvoiceTotals totals = InvoiceCalculator.calculate(calcInputs, dto.getDiscountAmount());

        Estimate estimate = new Estimate();
        estimate.setEstimateNumber(estimateNumber);
        estimate.setJobCardId(dto.getJobCardId());
        estimate.setCustomerId(dto.getCustomerId());
        estimate.setSubtotal(totals.getSubtotal());
        estimate.setDiscountAmount(totals.getDiscountAmount());
        estimate.setTaxAmount(totals.getTaxAmount());
        estimate.setGrandTotal(totals.getGrandTotal());
        estimate.setNotes(dto.getNotes());
        estimate.setValidUntil(dto.getValidUntil());
        estimate.setStatus("PENDING");
        estimate.setRootEstimateId(rootEstimateId);
        estimate.setRevisionNumber(revisionNumber);

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
            item.setWorkCategory(r.workCategory);

            savedItems.add(estimateItemRepository.save(item));
        }

        notificationEventService.raise("PENDING_ESTIMATE", "Estimate pending approval",
                "Estimate " + saved.getEstimateNumber() + " (" + totals.getGrandTotal() + ") is waiting on customer approval.",
                "ESTIMATE", saved.getEstimateId());

        // "Estimate created" and "Estimate sent" are the same instant in this workflow — there's
        // no draft/send-later step, PENDING happens at creation — so one CREATED entry covers
        // both rather than logging two rows for one real event. A revision (rootEstimateId set)
        // is "Estimate changed" instead.
        boolean isRevision = rootEstimateId != null;
        auditLogService.record(isRevision ? "ESTIMATE_REVISED" : "ESTIMATE_CREATED", "ESTIMATE", saved.getEstimateId(),
                (isRevision ? "Estimate " + saved.getEstimateNumber() + " revised to REV " + revisionNumber
                        : "Estimate " + saved.getEstimateNumber() + " created") + " (" + totals.getGrandTotal() + ").");

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
        // Phase 34 — billing safety: a decision, once made, is final on this row. Without this,
        // an already-REJECTED estimate could be silently flipped to APPROVED (or an already-
        // APPROVED one re-approved, quietly overwriting approvedDate/approvedBy) with no fresh
        // customer consent — and generateInvoice() would happily bill whatever is APPROVED at
        // that moment. A genuine change goes through reviseEstimate, a new PENDING row, not this.
        if (!"PENDING".equals(estimate.getStatus())) {
            throw new IllegalArgumentException("Estimate " + estimate.getEstimateNumber()
                    + " is already " + estimate.getStatus() + " — only a PENDING estimate can be approved");
        }
        estimate.setStatus("APPROVED");
        estimate.setApprovedDate(OffsetDateTime.now());
        estimate.setApprovedBy(approvedBy);
        Estimate saved;
        try {
            saved = estimateRepository.save(estimate);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException lostRace) {
            throw new IllegalArgumentException("This estimate was just updated by someone else — reload and try again");
        }
        notificationEventService.raise("ESTIMATE_APPROVED", "Estimate approved",
                "Estimate " + saved.getEstimateNumber() + " was approved.", "ESTIMATE", saved.getEstimateId());
        auditLogService.record("ESTIMATE_APPROVED", "ESTIMATE", saved.getEstimateId(),
                "Estimate " + saved.getEstimateNumber() + " approved" + (approvedBy != null ? " by " + approvedBy : "") + ".");
        return mapToDto(saved, estimateItemRepository.findByEstimateId(id));
    }

    @Override
    public EstimateResponseDTO reject(Long id, String notes) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estimate not found with id: " + id));
        // Phase 34 — same reasoning as approve(): a decision is final on this row.
        if (!"PENDING".equals(estimate.getStatus())) {
            throw new IllegalArgumentException("Estimate " + estimate.getEstimateNumber()
                    + " is already " + estimate.getStatus() + " — only a PENDING estimate can be rejected");
        }
        estimate.setStatus("REJECTED");
        if (notes != null) estimate.setNotes(notes);
        Estimate saved = estimateRepository.save(estimate);
        notificationEventService.raise("ESTIMATE_REJECTED", "Estimate rejected",
                "Estimate " + saved.getEstimateNumber() + " was rejected by the customer.", "ESTIMATE", saved.getEstimateId());
        auditLogService.record("ESTIMATE_REJECTED", "ESTIMATE", saved.getEstimateId(),
                "Estimate " + saved.getEstimateNumber() + " rejected.");
        return mapToDto(saved, estimateItemRepository.findByEstimateId(id));
    }

    @Override
    public EstimateResponseDTO requestChanges(Long id, String notes) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estimate not found with id: " + id));
        if (!"PENDING".equals(estimate.getStatus())) {
            throw new IllegalArgumentException("Estimate " + estimate.getEstimateNumber()
                    + " is already " + estimate.getStatus() + " — changes can only be requested on a PENDING estimate");
        }
        estimate.setStatus("CHANGES_REQUESTED");
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

    /** Size-band code of the vehicle behind this estimate's job card; null when unknown. */
    private String resolveVehicleSizeClass(Long jobCardId) {
        if (jobCardId == null) return null;
        return jobCardRepository.findById(jobCardId)
                .map(JobCard::getVehicleId)
                .flatMap(vehicleRepository::findById)
                .map(Vehicle::getSizeClass)
                .orElse(null);
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
        customerRepository.findById(estimate.getCustomerId()).ifPresent(c -> dto.setCustomerName(c.getCustomerName()));
        dto.setValidUntil(estimate.getValidUntil());
        dto.setSubtotal(estimate.getSubtotal());
        dto.setDiscountAmount(estimate.getDiscountAmount());
        dto.setTaxAmount(estimate.getTaxAmount());
        dto.setGrandTotal(estimate.getGrandTotal());
        dto.setStatus(estimate.getStatus());
        dto.setRootEstimateId(estimate.getRootEstimateId());
        dto.setRevisionNumber(estimate.getRevisionNumber());
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
        dto.setWorkCategory(item.getWorkCategory());
        return dto;
    }
}
