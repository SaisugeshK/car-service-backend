package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class InvoiceResponseDTO {

    private Long invoiceId;
    private String invoiceNumber;
    private Long customerId;
    private String customerName;
    private String customerPhone;

    private Long vehicleId;
    private String vehicleModel;
    private String registrationNumber;
    private String vehicleCategory;
    private Integer odometer;

    private Long counterId;
    private OffsetDateTime invoiceDate;

    private BigDecimal serviceSubtotal;
    private BigDecimal productSubtotal;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal grandTotal;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;

    private String paymentMethod;
    private String paymentStatus;
    private String status;

    private Integer createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private List<InvoiceItemResponseDTO> items;
}
