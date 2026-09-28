package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.VisitRepository;
import com.example.InventoryManagementSystem.model.Customer;
import com.example.InventoryManagementSystem.model.Visit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

// Keeps customers.total_visits / last_visit_date / regular_status in step with the visits table.
// Recomputed from the visit rows themselves (never incremented blindly), so it's always exact —
// after an insert, a delete, or when a visit simply ages out of the 60-day window (the nightly
// run below handles that last case; no DB trigger needed).
//
//   NEW        — at most one visit ever (first visit only)
//   REGULAR    — 3+ visits in the last 60 days, OR a visit in each of the last 6 calendar months
//   OCCASIONAL — any other returning customer (2+ visits in total)
@Service
@RequiredArgsConstructor
public class CustomerVisitStatsService {

    static final int REGULAR_WINDOW_DAYS = 60;
    static final int REGULAR_MIN_VISITS_IN_WINDOW = 3;
    static final int MONTHLY_STREAK_MONTHS = 6;

    private final CustomerRepository customerRepository;
    private final VisitRepository visitRepository;

    @Value("${app.timezone:Asia/Kolkata}")
    private String appTimezone;

    @Transactional
    public void recompute(Long customerId) {
        if (customerId == null) return;
        customerRepository.findById(customerId).ifPresent(c ->
                apply(c, visitRepository.findByCustomerIdOrderByVisitDateTimeDesc(customerId), OffsetDateTime.now()));
    }

    // Nightly, so a customer whose last visits drop out of the 60-day window moves from Regular
    // to Occasional without anyone having to log anything.
    @Scheduled(cron = "0 30 0 * * *", zone = "${app.timezone:Asia/Kolkata}")
    @Transactional
    public void recomputeAll() {
        OffsetDateTime now = OffsetDateTime.now();
        Map<Long, List<Visit>> byCustomer = visitRepository.findAll().stream()
                .collect(Collectors.groupingBy(Visit::getCustomerId));
        for (Customer c : customerRepository.findAll()) {
            apply(c, byCustomer.getOrDefault(c.getCustomerId(), List.of()), now);
        }
    }

    private void apply(Customer customer, List<Visit> visits, OffsetDateTime now) {
        int total = visits.size();
        OffsetDateTime last = visits.stream().map(Visit::getVisitDateTime).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
        String status = statusFor(visits, now);
        if (Objects.equals(customer.getTotalVisits(), total) && Objects.equals(customer.getLastVisitDate(), last)
                && Objects.equals(customer.getRegularStatus(), status)) {
            return; // nothing changed — skip the write
        }
        customer.setTotalVisits(total);
        customer.setLastVisitDate(last);
        customer.setRegularStatus(status);
        customerRepository.save(customer);
    }

    String statusFor(List<Visit> visits, OffsetDateTime now) {
        if (visits.size() <= 1) return "NEW";
        OffsetDateTime windowStart = now.minusDays(REGULAR_WINDOW_DAYS);
        long inWindow = visits.stream().filter(v -> v.getVisitDateTime() != null && !v.getVisitDateTime().isBefore(windowStart)).count();
        if (inWindow >= REGULAR_MIN_VISITS_IN_WINDOW) return "REGULAR";

        ZoneId zone = ZoneId.of(appTimezone);
        YearMonth thisMonth = YearMonth.from(now.atZoneSameInstant(zone));
        Set<YearMonth> visitedMonths = visits.stream()
                .filter(v -> v.getVisitDateTime() != null)
                .map(v -> YearMonth.from(v.getVisitDateTime().atZoneSameInstant(zone)))
                .collect(Collectors.toSet());
        boolean everyMonth = true;
        for (int i = 0; i < MONTHLY_STREAK_MONTHS; i++) {
            if (!visitedMonths.contains(thisMonth.minusMonths(i))) {
                everyMonth = false;
                break;
            }
        }
        return everyMonth ? "REGULAR" : "OCCASIONAL";
    }
}
