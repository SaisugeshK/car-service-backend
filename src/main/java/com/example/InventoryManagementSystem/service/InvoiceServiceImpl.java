package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.InvoiceItemRepository;
import com.example.InventoryManagementSystem.Repository.InvoiceRepository;
import com.example.InventoryManagementSystem.Repository.PaymentTransactionRepository;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.ProductTaxRepository;
import com.example.InventoryManagementSystem.Repository.ServiceMasterRepository;
import com.example.InventoryManagementSystem.Repository.VehicleRepository;
import com.example.InventoryManagementSystem.dto.InvoiceItemResponseDTO;
import com.example.InventoryManagementSystem.dto.InvoiceLineItemRequestDTO;
import com.example.InventoryManagementSystem.dto.InvoiceRequestDTO;
import com.example.InventoryManagementSystem.dto.InvoiceResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Customer;
import com.example.InventoryManagementSystem.model.Invoice;
import com.example.InventoryManagementSystem.model.InvoiceItem;
import com.example.InventoryManagementSystem.model.PaymentTransaction;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.ServiceMaster;
import com.example.InventoryManagementSystem.model.Vehicle;
import com.example.InventoryManagementSystem.util.InvoiceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final ProductRepository productRepository;
    private final ServiceMasterRepository serviceMasterRepository;
    private final ProductTaxRepository productTaxRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    // Resolved line — what's known about a line after looking up its Service/Product but
    // before running the shared calculator.
    private static class ResolvedLine {
        String itemType;
        String itemName;
        Product product;
        ServiceMaster service;
        String description;
        String barcode;
        BigDecimal quantity;
        BigDecimal unitPrice;
        BigDecimal discount;
        BigDecimal taxPercentage;
    }

    // CREATE / COMPLETE INVOICE — the real billing cascade.
    @Override
    @Transactional
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO dto) {

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));

        Vehicle vehicle = null;
        if (dto.getVehicleId() != null) {
            vehicle = vehicleRepository.findById(dto.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + dto.getVehicleId()));
            if (!vehicle.getCustomerId().equals(dto.getCustomerId())) {
                throw new IllegalArgumentException("Selected vehicle does not belong to this customer");
            }
        }

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one service or product line is required");
        }

        // 1) Resolve every line (Service or Product) and validate stock for PRODUCT lines
        //    BEFORE any write happens — a bad line must never leave a half-built invoice.
        List<ResolvedLine> resolved = new ArrayList<>();
        for (InvoiceLineItemRequestDTO line : dto.getItems()) {

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
                r.service = service;
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
                r.product = product;
                r.itemName = product.getProductName();
                r.barcode = product.getBarcode();
                r.unitPrice = line.getUnitPrice() != null ? line.getUnitPrice()
                        : (product.getSellingPrice() != null ? product.getSellingPrice() : BigDecimal.ZERO);
                r.taxPercentage = productTaxRepository.findTopByProductId(product.getProductId())
                        .map(tax -> tax.getTaxPercentage() != null ? BigDecimal.valueOf(tax.getTaxPercentage()) : BigDecimal.ZERO)
                        .orElse(BigDecimal.ZERO);

                int available = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                int requested = r.quantity.intValue();
                if (available < requested) {
                    throw new IllegalArgumentException("Insufficient stock for " + product.getProductName()
                            + ". Available: " + available + ", Required: " + requested + ".");
                }
            }

            resolved.add(r);
        }

        // 2) Run the single shared calculator over the resolved lines.
        List<InvoiceCalculator.LineInput> calcInputs = resolved.stream()
                .map(r -> new InvoiceCalculator.LineInput(r.itemType, r.unitPrice, r.quantity.intValue(), r.discount, r.taxPercentage))
                .collect(Collectors.toList());
        InvoiceCalculator.InvoiceTotals totals = InvoiceCalculator.calculate(calcInputs, dto.getDiscountAmount());

        // 3) Decrement stock for PRODUCT lines now that everything has been validated.
        for (ResolvedLine r : resolved) {
            if (r.product != null) {
                int available = r.product.getStockQuantity() != null ? r.product.getStockQuantity() : 0;
                r.product.setStockQuantity(available - r.quantity.intValue());
                productRepository.save(r.product);
            }
        }

        // 4) Save the invoice header — invoice_number is NOT NULL, so it must be set before
        //    the one and only insert (unlike Sales/Purchase, which can save-then-patch).
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000));
        invoice.setCustomerId(dto.getCustomerId().intValue());
        invoice.setVehicleId(dto.getVehicleId());
        invoice.setOdometerReading(dto.getOdometerReading());
        invoice.setCounterId(dto.getCounterId() != null ? dto.getCounterId().intValue() : null);
        invoice.setPaymentMethod(dto.getPaymentMethod());
        invoice.setCreatedBy(dto.getCreatedBy());
        invoice.setSubtotal(totals.getSubtotal());
        invoice.setDiscountAmount(totals.getDiscountAmount());
        invoice.setTaxAmount(totals.getTaxAmount());
        invoice.setCgstAmount(totals.getCgstAmount());
        invoice.setSgstAmount(totals.getSgstAmount());
        invoice.setGrandTotal(totals.getGrandTotal());
        invoice.setPaidAmount(dto.getPaidAmount() != null ? dto.getPaidAmount() : BigDecimal.ZERO);
        invoice.setBalanceAmount(totals.getGrandTotal().subtract(invoice.getPaidAmount()));
        invoice.setPaymentStatus(dto.getPaymentStatus() != null && !dto.getPaymentStatus().isBlank()
                ? dto.getPaymentStatus()
                : InvoiceCalculator.derivePaymentStatus(totals.getGrandTotal(), invoice.getPaidAmount()));
        invoice.setStatus("COMPLETED");

        Invoice saved = invoiceRepository.save(invoice);

        // 5) Save the line items.
        List<InvoiceItem> savedItems = new ArrayList<>();
        for (int i = 0; i < resolved.size(); i++) {
            ResolvedLine r = resolved.get(i);
            InvoiceCalculator.LineResult lr = totals.getLines().get(i);

            InvoiceItem item = new InvoiceItem();
            item.setInvoiceId(saved.getInvoiceId().intValue());
            item.setItemType(r.itemType);
            item.setServiceId(r.service != null ? r.service.getServiceId().intValue() : null);
            item.setProductId(r.product != null ? r.product.getProductId().intValue() : null);
            item.setDescription(r.description != null ? r.description : r.itemName);
            item.setBarcode(r.barcode);
            item.setQuantity(r.quantity);
            item.setUnitPrice(r.unitPrice);
            item.setDiscount(r.discount);
            item.setTaxPercentage(lr.getTaxPercentage());
            item.setTaxAmount(lr.getTaxAmount());
            item.setTotalAmount(lr.getTotalAmount());

            savedItems.add(invoiceItemRepository.save(item));
        }

        // 6) Record the initial payment, if any, and keep the vehicle's odometer current.
        if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            PaymentTransaction payment = PaymentTransaction.builder()
                    .invoiceId(saved.getInvoiceId())
                    .paymentMethod(dto.getPaymentMethod())
                    .transactionReference(null)
                    .amount(invoice.getPaidAmount())
                    .paymentDate(OffsetDateTime.now())
                    .build();
            paymentTransactionRepository.save(payment);
        }

        if (vehicle != null && dto.getOdometerReading() != null) {
            vehicle.setOdometer(dto.getOdometerReading());
            vehicleRepository.save(vehicle);
        }

        return mapToDTO(saved, savedItems, customer, vehicle);
    }

    @Override
    public List<InvoiceResponseDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(invoice -> mapToDTO(invoice,
                        invoiceItemRepository.findByInvoiceId(invoice.getInvoiceId().intValue()),
                        invoice.getCustomerId() != null ? customerRepository.findById(invoice.getCustomerId().longValue()).orElse(null) : null,
                        invoice.getVehicleId() != null ? vehicleRepository.findById(invoice.getVehicleId()).orElse(null) : null))
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceResponseDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        Customer customer = invoice.getCustomerId() != null
                ? customerRepository.findById(invoice.getCustomerId().longValue()).orElse(null) : null;
        Vehicle vehicle = invoice.getVehicleId() != null
                ? vehicleRepository.findById(invoice.getVehicleId()).orElse(null) : null;

        return mapToDTO(invoice, invoiceItemRepository.findByInvoiceId(invoice.getInvoiceId().intValue()), customer, vehicle);
    }

    // Header-only edits (payment method, counter, notes-style corrections) — line items and
    // totals are not recomputed here; use cancelInvoice + a fresh invoice to correct a mistake.
    @Override
    public InvoiceResponseDTO updateInvoice(Long id, InvoiceRequestDTO dto) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        if (dto.getPaymentMethod() != null) invoice.setPaymentMethod(dto.getPaymentMethod());
        if (dto.getCounterId() != null) invoice.setCounterId(dto.getCounterId().intValue());

        Invoice updated = invoiceRepository.save(invoice);

        Customer customer = updated.getCustomerId() != null
                ? customerRepository.findById(updated.getCustomerId().longValue()).orElse(null) : null;
        Vehicle vehicle = updated.getVehicleId() != null
                ? vehicleRepository.findById(updated.getVehicleId()).orElse(null) : null;

        return mapToDTO(updated, invoiceItemRepository.findByInvoiceId(updated.getInvoiceId().intValue()), customer, vehicle);
    }

    @Override
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        invoiceRepository.delete(invoice);
    }

    // Proper reversal instead of a raw delete: restocks PRODUCT lines and marks the invoice void.
    @Override
    @Transactional
    public InvoiceResponseDTO cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        if ("CANCELLED".equals(invoice.getStatus())) {
            throw new IllegalArgumentException("Invoice is already cancelled");
        }

        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getInvoiceId().intValue());
        for (InvoiceItem item : items) {
            if ("PRODUCT".equals(item.getItemType()) && item.getProductId() != null) {
                productRepository.findById(item.getProductId().longValue()).ifPresent(product -> {
                    int current = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                    product.setStockQuantity(current + item.getQuantity().intValue());
                    productRepository.save(product);
                });
            }
        }

        invoice.setStatus("CANCELLED");
        Invoice saved = invoiceRepository.save(invoice);

        Customer customer = saved.getCustomerId() != null
                ? customerRepository.findById(saved.getCustomerId().longValue()).orElse(null) : null;
        Vehicle vehicle = saved.getVehicleId() != null
                ? vehicleRepository.findById(saved.getVehicleId()).orElse(null) : null;

        return mapToDTO(saved, items, customer, vehicle);
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

    private InvoiceResponseDTO mapToDTO(Invoice invoice, List<InvoiceItem> items, Customer customer, Vehicle vehicle) {

        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setInvoiceId(invoice.getInvoiceId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setCustomerId(invoice.getCustomerId() != null ? invoice.getCustomerId().longValue() : null);
        dto.setCustomerName(customer != null ? customer.getCustomerName() : null);
        dto.setCustomerPhone(customer != null ? customer.getPhone() : null);
        dto.setVehicleId(invoice.getVehicleId());
        dto.setVehicleModel(vehicle != null ? vehicle.getVehicleModel() : null);
        dto.setRegistrationNumber(vehicle != null ? vehicle.getRegistrationNumber() : null);
        dto.setOdometer(invoice.getOdometerReading());
        dto.setCounterId(invoice.getCounterId() != null ? invoice.getCounterId().longValue() : null);
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setDiscountAmount(invoice.getDiscountAmount());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setCgstAmount(invoice.getCgstAmount());
        dto.setSgstAmount(invoice.getSgstAmount());
        dto.setGrandTotal(invoice.getGrandTotal());
        dto.setPaidAmount(invoice.getPaidAmount());
        dto.setBalanceAmount(invoice.getBalanceAmount());
        dto.setPaymentMethod(invoice.getPaymentMethod());
        dto.setPaymentStatus(invoice.getPaymentStatus());
        dto.setStatus(invoice.getStatus());
        dto.setCreatedBy(invoice.getCreatedBy());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());

        BigDecimal serviceSubtotal = BigDecimal.ZERO;
        BigDecimal productSubtotal = BigDecimal.ZERO;
        List<InvoiceItemResponseDTO> itemDtos = new ArrayList<>();
        for (InvoiceItem item : items) {
            InvoiceItemResponseDTO itemDto = mapItemToDTO(item);
            itemDtos.add(itemDto);
            BigDecimal lineGross = item.getUnitPrice().multiply(item.getQuantity());
            if ("SERVICE".equals(item.getItemType())) {
                serviceSubtotal = serviceSubtotal.add(lineGross);
            } else {
                productSubtotal = productSubtotal.add(lineGross);
            }
        }
        dto.setServiceSubtotal(serviceSubtotal);
        dto.setProductSubtotal(productSubtotal);
        dto.setItems(itemDtos);

        return dto;
    }

    private InvoiceItemResponseDTO mapItemToDTO(InvoiceItem item) {
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
            serviceMasterRepository.findById(item.getServiceId().longValue())
                    .ifPresent(s -> dto.setItemName(s.getServiceName()));
        } else if (item.getProductId() != null) {
            productRepository.findById(item.getProductId().longValue())
                    .ifPresent(p -> dto.setItemName(p.getProductName()));
        }

        return dto;
    }
}
