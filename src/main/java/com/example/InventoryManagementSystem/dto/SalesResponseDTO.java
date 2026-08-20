package com.example.InventoryManagementSystem.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SalesResponseDTO {

    private Long saleId;
    private Long customerId;
    private Long createdBy;
    private Long getSalesReturnId;
    private String invoiceNumber;
    private String paymentStatus;
    private String paymentMethod;

    private String vehicleModel;
    private String registrationNumber;
    private Integer odometerReading;

    private BigDecimal totalAmount;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;

    private LocalDateTime saleDate;

    private List<SalesItemResponseDTO> items;
}
