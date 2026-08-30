package com.example.InventoryManagementSystem.exception;

// HRM/payroll — thrown when an authenticated EMPLOYEE requests a payroll record that isn't
// their own (e.g. GET /api/payroll/{someoneElsesId}). Deliberately its own type rather than
// reusing IllegalArgumentException/RuntimeException: those both map to 400 in
// GlobalExceptionHandler, and "you don't own this record" must surface as 403, not 400 — the
// concrete fix for the spec's "Employee A requests Employee B's payroll ID -> 403" requirement.
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
