package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// Summary of one generation run (spec §19) — returned to the admin UI and logged server-side,
// so "click Generate" gives an honest account of what happened instead of a bare 200 OK.
@Data
public class PayrollGenerationResultDTO {

    private Integer payPeriodMonth;
    private Integer payPeriodYear;
    private int employeesFound;
    private int generatedCount;
    private int alreadyExistedCount;
    private int failedCount;
    private List<String> failures = new ArrayList<>();
}
