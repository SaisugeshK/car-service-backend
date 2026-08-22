package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.PublicCompanyProfileDTO;
import com.example.InventoryManagementSystem.service.SettingsLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// Everything under /api/public/** is permitAll() in SecurityConfig — reachable before login (the
// login page itself is the caller). This controller is the one place that decides what a visitor
// with no token gets to see; it must never grow into a passthrough for the full settings table
// (address/GSTIN/email stay behind the authenticated GET /api/settings instead — see
// PublicCompanyProfileDTO's own comment for why).
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final SettingsLookupService settingsLookupService;

    @GetMapping("/company-profile")
    public PublicCompanyProfileDTO getCompanyProfile() {
        return new PublicCompanyProfileDTO(
                settingsLookupService.get("company_name", null),
                settingsLookupService.get("company_tagline", null),
                settingsLookupService.get("company_logo", null),
                settingsLookupService.get("company_phone", null),
                settingsLookupService.get("company_whatsapp", null)
        );
    }
}
