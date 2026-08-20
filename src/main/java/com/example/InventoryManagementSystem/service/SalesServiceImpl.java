package com.example.InventoryManagementSystem.service;


import com.example.InventoryManagementSystem.dto.SalesItemResponseDTO;
import com.example.InventoryManagementSystem.dto.SalesLineItemRequestDTO;
import com.example.InventoryManagementSystem.dto.SalesRequestDTO;
import com.example.InventoryManagementSystem.dto.SalesResponseDTO;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.Sales;
import com.example.InventoryManagementSystem.model.SalesItem;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.ProductTaxRepository;
import com.example.InventoryManagementSystem.Repository.SalesItemRepository;
import com.example.InventoryManagementSystem.Repository.SalesRepository;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesServiceImpl implements SalesService {

    private final SalesRepository salesRepository;
    private final SalesItemRepository salesItemRepository;
    private final ProductRepository productRepository;
    private final ProductTaxRepository productTaxRepository;

    // CREATE SALE (full billing cascade: header + line items + stock + totals)
    @Override
    @Transactional
    public SalesResponseDTO createSale(SalesRequestDTO dto) {

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            // No items supplied: legacy header-only save, exactly what the generic Sales CRUD
            // screen (src/pages/Sales.jsx) sends. Real billing (POS) always sends items and
            // goes through the full cascade below.
            Sales sale = new Sales();
            sale.setCustomerId(dto.getCustomerId());
            sale.setCreatedBy(dto.getCreatedBy());
            sale.setPaymentStatus(dto.getPaymentStatus());
            sale.setPaymentMethod(dto.getPaymentMethod());
            sale.setVehicleModel(dto.getVehicleModel());
            sale.setRegistrationNumber(dto.getRegistrationNumber());
            sale.setOdometerReading(dto.getOdometerReading());
            sale.setTotalAmount(dto.getTotalAmount());
            sale.setSaleDate(LocalDateTime.now());

            if (dto.getInvoiceNumber() == null || dto.getInvoiceNumber().isBlank()) {
                Sales saved = salesRepository.save(sale);
                sale.setInvoiceNumber("INV-" + saved.getSaleId() + "-" + System.currentTimeMillis());
            } else {
                sale.setInvoiceNumber(dto.getInvoiceNumber());
            }

            return mapToDTO(salesRepository.save(sale), null);
        }

        // 1) Save the header first so we have a saleId to stamp on each line and to build
        //    a fallback invoice number from.
        Sales sale = new Sales();
        sale.setCustomerId(dto.getCustomerId());
        sale.setCreatedBy(dto.getCreatedBy());
        sale.setPaymentMethod(dto.getPaymentMethod());
        sale.setVehicleModel(dto.getVehicleModel());
        sale.setRegistrationNumber(dto.getRegistrationNumber());
        sale.setOdometerReading(dto.getOdometerReading());
        sale.setSaleDate(LocalDateTime.now());
        sale = salesRepository.save(sale);

        String invoiceNumber = dto.getInvoiceNumber();
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            invoiceNumber = "INV-" + sale.getSaleId() + "-" + System.currentTimeMillis();
        }
        sale.setInvoiceNumber(invoiceNumber);

        // 2) Process each line: resolve the product, stock-check/decrement PRODUCT lines only
        //    (SERVICE lines aren't stock-tracked), snapshot tax, persist the SalesItem.
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalLineDiscount = BigDecimal.ZERO;
        List<SalesItem> savedItems = new ArrayList<>();

        for (SalesLineItemRequestDTO line : dto.getItems()) {

            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + line.getProductId()));

            String itemType = product.getItemType() != null ? product.getItemType() : "PRODUCT";

            if ("PRODUCT".equals(itemType)) {
                Integer available = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                if (available < line.getQuantity()) {
                    throw new IllegalArgumentException(
                            "Insufficient stock for " + product.getProductName()
                                    + " (available: " + available + ", requested: " + line.getQuantity() + ")");
                }
                product.setStockQuantity(available - line.getQuantity());
                productRepository.save(product);
            }

            BigDecimal sellingPrice = product.getSellingPrice() != null ? product.getSellingPrice() : BigDecimal.ZERO;
            BigDecimal discount = line.getDiscount() != null ? line.getDiscount() : BigDecimal.ZERO;
            BigDecimal taxPercentage = productTaxRepository.findTopByProductId(product.getProductId())
                    .map(tax -> tax.getTaxPercentage() != null ? BigDecimal.valueOf(tax.getTaxPercentage()) : BigDecimal.ZERO)
                    .orElse(BigDecimal.ZERO);

            BigDecimal lineGross = sellingPrice.multiply(BigDecimal.valueOf(line.getQuantity()));
            BigDecimal lineSubtotal = lineGross.subtract(discount);
            if (lineSubtotal.compareTo(BigDecimal.ZERO) < 0) {
                lineSubtotal = BigDecimal.ZERO;
            }
            BigDecimal lineTax = lineSubtotal.multiply(taxPercentage).divide(BigDecimal.valueOf(100));
            BigDecimal lineTotal = lineSubtotal.add(lineTax);

            SalesItem item = new SalesItem();
            item.setSaleId(sale.getSaleId());
            item.setProductId(product.getProductId());
            item.setItemType(itemType);
            item.setQuantity(line.getQuantity());
            item.setSellingPrice(sellingPrice);
            item.setDiscount(discount);
            item.setTaxPercentage(taxPercentage);
            item.setTaxAmount(lineTax);
            item.setTotal(lineTotal);

            savedItems.add(salesItemRepository.save(item));

            subtotal = subtotal.add(lineGross);
            totalTax = totalTax.add(lineTax);
            totalLineDiscount = totalLineDiscount.add(discount);
        }

        // 3) Roll up totals: line discounts + an optional global discount from the request.
        BigDecimal globalDiscount = dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount = totalLineDiscount.add(globalDiscount);
        BigDecimal grandTotal = subtotal.subtract(totalLineDiscount).add(totalTax).subtract(globalDiscount);
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {
            grandTotal = BigDecimal.ZERO;
        }
        BigDecimal paidAmount = dto.getPaidAmount() != null ? dto.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal balanceAmount = grandTotal.subtract(paidAmount);

        String paymentStatus = dto.getPaymentStatus();
        if (paymentStatus == null || paymentStatus.isBlank()) {
            if (paidAmount.compareTo(grandTotal) >= 0) {
                paymentStatus = "PAID";
            } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                paymentStatus = "PARTIAL";
            } else {
                paymentStatus = "UNPAID";
            }
        }

        sale.setSubtotal(subtotal);
        sale.setDiscountAmount(discountAmount);
        sale.setTaxAmount(totalTax);
        sale.setGrandTotal(grandTotal);
        sale.setPaidAmount(paidAmount);
        sale.setBalanceAmount(balanceAmount);
        sale.setPaymentStatus(paymentStatus);
        sale.setTotalAmount(grandTotal);

        Sales saved = salesRepository.save(sale);

        return mapToDTO(saved, savedItems);
    }

    // GET BY ID
    @Override
    public SalesResponseDTO getSaleById(Long id) {

        Sales sale = salesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        return mapToDTO(sale, salesItemRepository.findBySaleId(id));
    }

    // GET ALL
    @Override
    public List<SalesResponseDTO> getAllSales() {

        return salesRepository.findAll()
                .stream()
                .map(sale -> mapToDTO(sale, salesItemRepository.findBySaleId(sale.getSaleId())))
                .collect(Collectors.toList());
    }

    // UPDATE (header fields only — matches the existing generic Sales CRUD screen, which never
    // re-submits line items; line items keep their own CRUD via SalesItemController)
    @Override
    public SalesResponseDTO updateSale(Long id, SalesRequestDTO dto) {

        Sales sale = salesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        sale.setCustomerId(dto.getCustomerId());
        sale.setCreatedBy(dto.getCreatedBy());
        // Only overwrite the invoice number if a real one was supplied — never blank out the
        // auto-generated one just because the field wasn't part of this edit's payload.
        if (dto.getInvoiceNumber() != null && !dto.getInvoiceNumber().isBlank()) {
            sale.setInvoiceNumber(dto.getInvoiceNumber());
        }
        sale.setPaymentStatus(dto.getPaymentStatus());
        sale.setTotalAmount(dto.getTotalAmount());

        Sales updated = salesRepository.save(sale);

        return mapToDTO(updated, salesItemRepository.findBySaleId(id));
    }

    // DELETE
    @Override
    public void deleteSale(Long id) {

        Sales sale = salesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        salesRepository.delete(sale);
    }

    // MAPPER METHODS
    private SalesResponseDTO mapToDTO(Sales sale, List<SalesItem> items) {

        SalesResponseDTO dto = new SalesResponseDTO();

        dto.setSaleId(sale.getSaleId());
        dto.setCustomerId(sale.getCustomerId());
        dto.setCreatedBy(sale.getCreatedBy());
        dto.setInvoiceNumber(sale.getInvoiceNumber());
        dto.setPaymentStatus(sale.getPaymentStatus());
        dto.setPaymentMethod(sale.getPaymentMethod());
        dto.setVehicleModel(sale.getVehicleModel());
        dto.setRegistrationNumber(sale.getRegistrationNumber());
        dto.setOdometerReading(sale.getOdometerReading());
        dto.setTotalAmount(sale.getTotalAmount());
        dto.setSubtotal(sale.getSubtotal());
        dto.setDiscountAmount(sale.getDiscountAmount());
        dto.setTaxAmount(sale.getTaxAmount());
        dto.setGrandTotal(sale.getGrandTotal());
        dto.setPaidAmount(sale.getPaidAmount());
        dto.setBalanceAmount(sale.getBalanceAmount());
        dto.setSaleDate(sale.getSaleDate());

        if (items != null) {
            dto.setItems(items.stream().map(this::mapItemToDTO).collect(Collectors.toList()));
        }

        return dto;
    }

    private SalesItemResponseDTO mapItemToDTO(SalesItem item) {

        SalesItemResponseDTO dto = new SalesItemResponseDTO();

        dto.setSaleItemId(item.getSaleItemId());
        dto.setSaleId(item.getSaleId());
        dto.setProductId(item.getProductId());
        dto.setItemType(item.getItemType());
        dto.setQuantity(item.getQuantity());
        dto.setSellingPrice(item.getSellingPrice());
        dto.setDiscount(item.getDiscount());
        dto.setTaxPercentage(item.getTaxPercentage());
        dto.setTaxAmount(item.getTaxAmount());
        dto.setTotal(item.getTotal());

        productRepository.findById(item.getProductId())
                .ifPresent(p -> dto.setProductName(p.getProductName()));

        return dto;
    }
}
