package com.example.InventoryManagementSystem.util;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Single source of truth for invoice math — every screen that needs a total (POS, Invoice
 * completion, payment reconciliation, printable invoice, reports) must go through this class
 * rather than recomputing the formula locally, so totals never drift between screens.
 *
 * Mirrored on the frontend by src/utils/invoiceCalculations.js — keep the two in sync.
 */
public final class InvoiceCalculator {

    private InvoiceCalculator() {
    }

    @Getter
    public static class LineInput {
        private final String itemType; // SERVICE / PRODUCT
        private final BigDecimal unitPrice;
        private final Integer quantity;
        private final BigDecimal discount;
        private final BigDecimal taxPercentage;

        public LineInput(String itemType, BigDecimal unitPrice, Integer quantity, BigDecimal discount, BigDecimal taxPercentage) {
            this.itemType = itemType;
            this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
            this.quantity = quantity != null ? quantity : 0;
            this.discount = discount != null ? discount : BigDecimal.ZERO;
            this.taxPercentage = taxPercentage != null ? taxPercentage : BigDecimal.ZERO;
        }
    }

    @Getter
    public static class LineResult {
        private final String itemType;
        private final BigDecimal grossAmount;
        private final BigDecimal discount;
        private final BigDecimal taxableAmount;
        private final BigDecimal taxPercentage;
        private final BigDecimal taxAmount;
        private final BigDecimal totalAmount;

        LineResult(String itemType, BigDecimal grossAmount, BigDecimal discount, BigDecimal taxableAmount,
                   BigDecimal taxPercentage, BigDecimal taxAmount, BigDecimal totalAmount) {
            this.itemType = itemType;
            this.grossAmount = grossAmount;
            this.discount = discount;
            this.taxableAmount = taxableAmount;
            this.taxPercentage = taxPercentage;
            this.taxAmount = taxAmount;
            this.totalAmount = totalAmount;
        }
    }

    @Getter
    public static class InvoiceTotals {
        private final List<LineResult> lines;
        private final BigDecimal serviceSubtotal;
        private final BigDecimal productSubtotal;
        private final BigDecimal subtotal;
        private final BigDecimal lineDiscountTotal;
        private final BigDecimal additionalDiscount;
        private final BigDecimal discountAmount;
        private final BigDecimal taxAmount;
        private final BigDecimal cgstAmount;
        private final BigDecimal sgstAmount;
        private final BigDecimal grandTotal;

        InvoiceTotals(List<LineResult> lines, BigDecimal serviceSubtotal, BigDecimal productSubtotal,
                      BigDecimal subtotal, BigDecimal lineDiscountTotal, BigDecimal additionalDiscount,
                      BigDecimal discountAmount, BigDecimal taxAmount, BigDecimal cgstAmount,
                      BigDecimal sgstAmount, BigDecimal grandTotal) {
            this.lines = lines;
            this.serviceSubtotal = serviceSubtotal;
            this.productSubtotal = productSubtotal;
            this.subtotal = subtotal;
            this.lineDiscountTotal = lineDiscountTotal;
            this.additionalDiscount = additionalDiscount;
            this.discountAmount = discountAmount;
            this.taxAmount = taxAmount;
            this.cgstAmount = cgstAmount;
            this.sgstAmount = sgstAmount;
            this.grandTotal = grandTotal;
        }
    }

    public static LineResult calculateLine(LineInput in) {
        BigDecimal gross = in.getUnitPrice().multiply(BigDecimal.valueOf(in.getQuantity()));
        BigDecimal taxable = gross.subtract(in.getDiscount());
        if (taxable.compareTo(BigDecimal.ZERO) < 0) {
            taxable = BigDecimal.ZERO;
        }
        // Rounded to paisa here, not left to whatever scale BigDecimal.divide happens to pick —
        // otherwise a create-response can show a sub-paisa total (e.g. 323.9964) that silently
        // differs from what numeric(38,2)/numeric(12,2) actually persists (324.00), which is a
        // real money-display bug on the confirmation the caller sees before any refetch.
        BigDecimal tax = taxable.multiply(in.getTaxPercentage())
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal total = taxable.add(tax);
        return new LineResult(in.getItemType(), gross, in.getDiscount(), taxable, in.getTaxPercentage(), tax, total);
    }

    /**
     * additionalDiscount is applied at the invoice level, on top of grand total (matches the
     * proven Sales module formula: line discounts reduce the taxable base per line, the
     * additional/overall discount is subtracted after tax).
     */
    public static InvoiceTotals calculate(List<LineInput> items, BigDecimal additionalDiscount) {

        BigDecimal discount = additionalDiscount != null ? additionalDiscount : BigDecimal.ZERO;

        List<LineResult> results = new ArrayList<>();
        BigDecimal serviceSubtotal = BigDecimal.ZERO;
        BigDecimal productSubtotal = BigDecimal.ZERO;
        BigDecimal lineDiscountTotal = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        for (LineInput in : items) {
            LineResult r = calculateLine(in);
            results.add(r);

            if ("SERVICE".equals(in.getItemType())) {
                serviceSubtotal = serviceSubtotal.add(r.getGrossAmount());
            } else {
                productSubtotal = productSubtotal.add(r.getGrossAmount());
            }
            lineDiscountTotal = lineDiscountTotal.add(r.getDiscount());
            taxAmount = taxAmount.add(r.getTaxAmount());
        }

        BigDecimal subtotal = serviceSubtotal.add(productSubtotal);
        BigDecimal discountAmount = lineDiscountTotal.add(discount);
        BigDecimal cgst = taxAmount.divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal sgst = taxAmount.subtract(cgst);

        BigDecimal grandTotal = subtotal.subtract(lineDiscountTotal).add(taxAmount).subtract(discount);
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {
            grandTotal = BigDecimal.ZERO;
        }

        return new InvoiceTotals(results, serviceSubtotal, productSubtotal, subtotal, lineDiscountTotal,
                discount, discountAmount, taxAmount, cgst, sgst, grandTotal);
    }

    /** Same PAID/PARTIAL/UNPAID rule everywhere a payment status needs deriving. */
    public static String derivePaymentStatus(BigDecimal grandTotal, BigDecimal paidAmount) {
        BigDecimal total = grandTotal != null ? grandTotal : BigDecimal.ZERO;
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        if (paid.compareTo(total) >= 0) {
            return "PAID";
        } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
            return "PARTIAL";
        } else {
            return "UNPAID";
        }
    }
}
