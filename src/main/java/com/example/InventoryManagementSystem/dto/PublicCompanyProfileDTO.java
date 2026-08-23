package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Deliberately narrow — this is the ONLY company-profile shape ever served to an unauthenticated
// caller (the login page). It must never grow an address/gstin/email/phone-of-owner field; those
// are legitimate on an invoice/receipt (authenticated, GET /api/settings) but have no business
// being public. Adding a field here is a conscious security decision, not a convenience.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicCompanyProfileDTO {
    private String companyName;
    private String tagline;
    private String logo;
    private String phone;
    private String whatsapp;
}
