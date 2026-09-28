package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.ExpenseRepository;
import com.example.InventoryManagementSystem.dto.ExpenseRequestDTO;
import com.example.InventoryManagementSystem.dto.ExpenseResponseDTO;
import com.example.InventoryManagementSystem.exception.AccessDeniedException;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Expense;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

// Access rule, applied to every method: a SUPER_ADMIN sees and manages every expense; anyone else
// (EMPLOYEE) sees and manages only the expenses they created themselves — never anyone else's,
// and never a total across the business (they only ever receive their own rows).
@Service
@RequiredArgsConstructor
public class ExpenseService {

    public static final Set<String> CATEGORIES = Set.of("UTILITIES", "RENT", "MAINTENANCE", "SUPPLIES", "MISCELLANEOUS");
    public static final Set<String> PAYMENT_METHODS = Set.of("CASH", "CARD", "UPI", "BANK_TRANSFER");

    private static final Map<String, String> RECEIPT_TYPES = Map.of(
            "application/pdf", ".pdf",
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final ExpenseRepository repository;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;

    @Value("${app.expense-receipt-dir:uploads/expense-receipts}")
    private String receiptDir;

    public record StoredReceipt(byte[] data, String contentType, String fileName) {}

    public List<ExpenseResponseDTO> list() {
        List<Expense> rows = currentUserService.isSuperAdmin()
                ? repository.findAllByOrderByExpenseDateDescExpenseIdDesc()
                : repository.findByCreatedByUserIdOrderByExpenseDateDescExpenseIdDesc(requireCurrentUserId());
        return rows.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ExpenseResponseDTO get(Long id) {
        return toDto(requireAccessible(id));
    }

    @Transactional
    public ExpenseResponseDTO create(ExpenseRequestDTO dto) {
        Expense expense = new Expense();
        apply(expense, dto);
        expense.setCreatedByUserId(requireCurrentUserId());
        expense.setCreatedByName(currentUserService.getCurrentUsername());
        expense.setCreatedByRole(currentUserService.isSuperAdmin() ? "SUPER_ADMIN" : "EMPLOYEE");
        Expense saved = repository.save(expense);
        auditLogService.record("EXPENSE_CREATED", "EXPENSE", saved.getExpenseId(),
                "Expense '" + saved.getTitle() + "' (" + saved.getAmount() + ") recorded by " + saved.getCreatedByName() + ".");
        return toDto(saved);
    }

    @Transactional
    public ExpenseResponseDTO update(Long id, ExpenseRequestDTO dto) {
        Expense expense = requireAccessible(id);
        apply(expense, dto);
        Expense saved = repository.save(expense);
        auditLogService.record("EXPENSE_UPDATED", "EXPENSE", saved.getExpenseId(),
                "Expense '" + saved.getTitle() + "' (" + saved.getAmount() + ") updated by " + currentUserService.getCurrentUsername() + ".");
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        Expense expense = requireAccessible(id);
        deleteReceiptFile(expense);
        repository.delete(expense);
        auditLogService.record("EXPENSE_DELETED", "EXPENSE", id,
                "Expense '" + expense.getTitle() + "' (" + expense.getAmount() + ") deleted by " + currentUserService.getCurrentUsername() + ".");
    }

    // Replaces any receipt already attached.
    @Transactional
    public ExpenseResponseDTO uploadReceipt(Long id, MultipartFile file) {
        Expense expense = requireAccessible(id);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded");
        }
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
        String extension = RECEIPT_TYPES.get(contentType);
        if (extension == null) {
            throw new IllegalArgumentException("Receipt must be a PDF, JPEG, PNG or WEBP file");
        }
        try {
            Path dir = Paths.get(receiptDir);
            Files.createDirectories(dir);
            String storedName = UUID.randomUUID() + extension;
            Files.copy(file.getInputStream(), dir.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
            deleteReceiptFile(expense);
            expense.setReceiptStoredFileName(storedName);
            expense.setReceiptOriginalFileName(file.getOriginalFilename());
            expense.setReceiptContentType(contentType);
            expense.setReceiptFileSize(file.getSize());
            return toDto(repository.save(expense));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the receipt", e);
        }
    }

    public StoredReceipt loadReceipt(Long id) {
        Expense expense = requireAccessible(id);
        if (expense.getReceiptStoredFileName() == null) {
            throw new ResourceNotFoundException("No receipt attached to this expense");
        }
        try {
            byte[] data = Files.readAllBytes(Paths.get(receiptDir).resolve(expense.getReceiptStoredFileName()));
            return new StoredReceipt(data, expense.getReceiptContentType(), expense.getReceiptOriginalFileName());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the stored receipt", e);
        }
    }

    @Transactional
    public ExpenseResponseDTO removeReceipt(Long id) {
        Expense expense = requireAccessible(id);
        deleteReceiptFile(expense);
        expense.setReceiptStoredFileName(null);
        expense.setReceiptOriginalFileName(null);
        expense.setReceiptContentType(null);
        expense.setReceiptFileSize(null);
        return toDto(repository.save(expense));
    }

    private void apply(Expense expense, ExpenseRequestDTO dto) {
        String category = dto.getCategory().trim().toUpperCase();
        if (!CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Category must be one of " + CATEGORIES);
        }
        String method = dto.getPaymentMethod().trim().toUpperCase();
        if (!PAYMENT_METHODS.contains(method)) {
            throw new IllegalArgumentException("Payment method must be one of " + PAYMENT_METHODS);
        }
        expense.setTitle(dto.getTitle().trim());
        expense.setCategory(category);
        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setPaymentMethod(method);
        expense.setExpenseDate(dto.getExpenseDate());
    }

    private Long requireCurrentUserId() {
        Long userId = currentUserService.getCurrentUserId();
        if (userId == null) throw new AccessDeniedException("Not authenticated");
        return userId;
    }

    private Expense requireAccessible(Long id) {
        Expense expense = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        if (!currentUserService.isSuperAdmin() && !Objects.equals(expense.getCreatedByUserId(), requireCurrentUserId())) {
            throw new AccessDeniedException("You can only access expenses you created");
        }
        return expense;
    }

    private void deleteReceiptFile(Expense expense) {
        if (expense.getReceiptStoredFileName() == null) return;
        try {
            Files.deleteIfExists(Paths.get(receiptDir).resolve(expense.getReceiptStoredFileName()));
        } catch (IOException ignored) {
            // an orphaned file on disk is harmless; never fail the user's action over it
        }
    }

    private ExpenseResponseDTO toDto(Expense e) {
        ExpenseResponseDTO dto = new ExpenseResponseDTO();
        dto.setExpenseId(e.getExpenseId());
        dto.setTitle(e.getTitle());
        dto.setCategory(e.getCategory());
        dto.setDescription(e.getDescription());
        dto.setAmount(e.getAmount());
        dto.setPaymentMethod(e.getPaymentMethod());
        dto.setExpenseDate(e.getExpenseDate());
        dto.setHasReceipt(e.getReceiptStoredFileName() != null);
        dto.setReceiptFileName(e.getReceiptOriginalFileName());
        dto.setReceiptContentType(e.getReceiptContentType());
        dto.setCreatedByUserId(e.getCreatedByUserId());
        dto.setCreatedByName(e.getCreatedByName());
        dto.setCreatedByRole(e.getCreatedByRole());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
