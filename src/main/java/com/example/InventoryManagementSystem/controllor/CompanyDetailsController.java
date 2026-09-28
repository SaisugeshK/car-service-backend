package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.service.SettingsLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

// Letterhead for printed documents (invoice/payslip PDFs). Open to every logged-in role — an
// EMPLOYEE downloading their own payslip needs the company address on it — but only these
// fixed keys, never the full settings table, which stays SUPER_ADMIN-only at /api/settings.
@RestController
@RequestMapping("/api/company-details")
@RequiredArgsConstructor
public class CompanyDetailsController {

    private static final String[] KEYS = {
            "company_name", "company_address", "company_phone", "company_whatsapp",
            "company_email", "company_gstin", "company_logo", "invoice_terms", "invoice_footer",
    };

    private final SettingsLookupService settingsLookupService;

    @GetMapping
    public Map<String, String> get() {
        Map<String, String> details = new LinkedHashMap<>();
        for (String key : KEYS) {
            details.put(key, settingsLookupService.get(key, null));
        }
        return details;
    }
}
