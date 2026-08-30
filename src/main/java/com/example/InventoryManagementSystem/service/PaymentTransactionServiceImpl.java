package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.*;
import com.example.InventoryManagementSystem.model.Customer;
import com.example.InventoryManagementSystem.model.Invoice;
import com.example.InventoryManagementSystem.model.PaymentTransaction;
import com.example.InventoryManagementSystem.model.User;
import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.InvoiceRepository;
import com.example.InventoryManagementSystem.Repository.PaymentTransactionRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.util.InvoiceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    private final PaymentTransactionRepository repo;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final NotificationEventService notificationEventService;
    private final AuditLogService auditLogService;

    // Recording a payment must reconcile it against the invoice — otherwise paidAmount/
    // balanceAmount/paymentStatus on the invoice drift from reality (this was previously
    // fully disconnected: creating a payment never touched the invoice at all).
    @Override
    @Transactional
    public PaymentTransactionResponseDTO create(PaymentTransactionRequestDTO dto) {

        Invoice invoice = invoiceRepository.findById(dto.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + dto.getInvoiceId()));

        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if ("CANCELLED".equals(invoice.getStatus())) {
            throw new IllegalArgumentException("Cannot record a payment against a cancelled invoice");
        }
        rejectIfDuplicate(dto.getInvoiceId(), dto.getAmount(), dto.getPaymentMethod(), dto.getTransactionReference(), null);
        rejectIfOverpaying(invoice, dto.getAmount());

        PaymentTransaction p = PaymentTransaction.builder()
                .invoiceId(dto.getInvoiceId())
                .paymentMethod(dto.getPaymentMethod())
                .transactionReference(dto.getTransactionReference())
                .amount(dto.getAmount())
                .receivedByUserId(dto.getReceivedByUserId())
                .notes(dto.getNotes())
                .build();

        PaymentTransaction saved = repo.save(p);

        applyToInvoice(invoice, dto.getAmount());

        notificationEventService.raise("PAYMENT", "Payment received",
                "Payment of " + dto.getAmount() + " received for invoice " + invoice.getInvoiceNumber() + ".",
                "PAYMENT", saved.getTransactionId());
        auditLogService.record("PAYMENT_RECEIVED", "PAYMENT", saved.getTransactionId(),
                "Payment of " + dto.getAmount() + " (" + dto.getPaymentMethod() + ") received for invoice " + invoice.getInvoiceNumber() + ".");

        return map(saved);
    }

    @Override
    public List<PaymentTransactionResponseDTO> getAll() {
        return repo.findAll().stream().map(this::map).toList();
    }

    @Override
    public PaymentTransactionResponseDTO getById(Long id) {
        PaymentTransaction p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return map(p);
    }

    @Override
    @Transactional
    public PaymentTransactionResponseDTO update(Long id, PaymentTransactionRequestDTO dto) {

        PaymentTransaction p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        Invoice targetInvoice = invoiceRepository.findById(dto.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + dto.getInvoiceId()));
        if ("CANCELLED".equals(targetInvoice.getStatus())) {
            throw new IllegalArgumentException("Cannot record a payment against a cancelled invoice");
        }
        rejectIfDuplicate(dto.getInvoiceId(), dto.getAmount(), dto.getPaymentMethod(), dto.getTransactionReference(), id);

        // Reverse this payment's old amount off its old invoice, then re-apply the new amount
        // to the (possibly different) invoice — keeps every affected invoice's paidAmount correct.
        invoiceRepository.findById(p.getInvoiceId()).ifPresent(oldInvoice -> applyToInvoice(oldInvoice, p.getAmount().negate()));

        Invoice invoice = targetInvoice;
        rejectIfOverpaying(invoice, dto.getAmount());

        p.setInvoiceId(dto.getInvoiceId());
        p.setPaymentMethod(dto.getPaymentMethod());
        p.setTransactionReference(dto.getTransactionReference());
        p.setAmount(dto.getAmount());
        p.setReceivedByUserId(dto.getReceivedByUserId());
        p.setNotes(dto.getNotes());

        repo.save(p);

        applyToInvoice(invoice, dto.getAmount());

        return map(p);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PaymentTransaction p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        invoiceRepository.findById(p.getInvoiceId()).ifPresent(invoice -> applyToInvoice(invoice, p.getAmount().negate()));

        repo.delete(p);
    }

    // A real transaction reference (UPI ref, cheque number, bank UTR) is unique by nature — reused
    // on the same invoice is never legitimate, so that check applies regardless of timing. Cash
    // (no reference) has no such signal, so an identical amount+method within a short window is
    // treated as an accidental double-submit rather than two genuine separate payments.
    private static final int DUPLICATE_WINDOW_SECONDS = 30;

    private void rejectIfDuplicate(Long invoiceId, BigDecimal amount, String paymentMethod, String transactionReference, Long excludeId) {
        List<PaymentTransaction> existing = repo.findByInvoiceId(invoiceId).stream()
                .filter(p -> excludeId == null || !excludeId.equals(p.getTransactionId()))
                .toList();

        if (transactionReference != null && !transactionReference.isBlank()) {
            boolean referenceReused = existing.stream()
                    .anyMatch(p -> transactionReference.equals(p.getTransactionReference()));
            if (referenceReused) {
                throw new IllegalArgumentException("Duplicate payment: transaction reference '" + transactionReference
                        + "' has already been recorded against this invoice");
            }
            return;
        }

        OffsetDateTime cutoff = OffsetDateTime.now().minusSeconds(DUPLICATE_WINDOW_SECONDS);
        boolean recentDuplicate = existing.stream().anyMatch(p ->
                (p.getTransactionReference() == null || p.getTransactionReference().isBlank())
                        && Objects.equals(paymentMethod, p.getPaymentMethod())
                        && amount.compareTo(p.getAmount()) == 0
                        && p.getPaymentDate() != null && p.getPaymentDate().isAfter(cutoff));
        if (recentDuplicate) {
            throw new IllegalArgumentException("Duplicate payment: an identical payment was just recorded against this invoice — check the payment history before retrying");
        }
    }

    // A payment can never exceed what's actually owed — otherwise balanceAmount goes negative and
    // the invoice silently reports "PAID" while the ledger is wrong (no credit-note/refund feature
    // exists to explain a negative balance; it just means someone fat-fingered an extra digit).
    private void rejectIfOverpaying(Invoice invoice, BigDecimal amount) {
        BigDecimal grandTotal = invoice.getGrandTotal() != null ? invoice.getGrandTotal() : BigDecimal.ZERO;
        BigDecimal alreadyPaid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal outstanding = grandTotal.subtract(alreadyPaid);
        if (amount.compareTo(outstanding) > 0) {
            throw new IllegalArgumentException("Payment amount (" + amount + ") exceeds the outstanding balance ("
                    + outstanding + ") for invoice " + invoice.getInvoiceNumber());
        }
    }

    /** Adds delta (positive to record a payment, negative to reverse one) and recomputes status. */
    private void applyToInvoice(Invoice invoice, BigDecimal delta) {
        BigDecimal current = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal newPaid = current.add(delta);
        if (newPaid.compareTo(BigDecimal.ZERO) < 0) {
            newPaid = BigDecimal.ZERO;
        }
        invoice.setPaidAmount(newPaid);
        invoice.setBalanceAmount(invoice.getGrandTotal().subtract(newPaid));
        invoice.setPaymentStatus(InvoiceCalculator.derivePaymentStatus(invoice.getGrandTotal(), newPaid));
        try {
            // @Version on Invoice (added after concurrency testing) — two simultaneous payments
            // against the same invoice both used to read the same paidAmount before either
            // committed, silently losing one payment's contribution. saveAndFlush (not save)
            // forces the version check to run synchronously here rather than deferred to
            // transaction commit, where it surfaced as an opaque "Could not commit JPA
            // transaction" 400 instead of this clean message. The caller's @Transactional method
            // rolls back its own new PaymentTransaction row too, so nothing is left half-applied.
            invoiceRepository.saveAndFlush(invoice);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException lostRace) {
            throw new IllegalArgumentException("This invoice was just updated by another payment — please retry");
        }
    }

    private PaymentTransactionResponseDTO map(PaymentTransaction p) {
        PaymentTransactionResponseDTO.PaymentTransactionResponseDTOBuilder dto = PaymentTransactionResponseDTO.builder()
                .transactionId(p.getTransactionId())
                .invoiceId(p.getInvoiceId())
                .paymentMethod(p.getPaymentMethod())
                .transactionReference(p.getTransactionReference())
                .amount(p.getAmount())
                .paymentDate(p.getPaymentDate())
                .receivedByUserId(p.getReceivedByUserId())
                .notes(p.getNotes());

        invoiceRepository.findById(p.getInvoiceId()).ifPresent(invoice -> {
            dto.invoiceNumber(invoice.getInvoiceNumber());
            if (invoice.getCustomerId() != null) {
                dto.customerId(invoice.getCustomerId().longValue());
                customerRepository.findById(invoice.getCustomerId().longValue())
                        .ifPresent(c -> dto.customerName(c.getCustomerName()));
            }
        });

        if (p.getReceivedByUserId() != null) {
            userRepository.findById(p.getReceivedByUserId()).ifPresent(u -> dto.receivedByName(displayName(u)));
        }

        return dto.build();
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        return user.getUsername();
    }
}
