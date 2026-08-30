package com.example.InventoryManagementSystem.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

// HRM/payroll — nothing in this backend ran on a schedule before this; every existing
// automation (reminders, notifications) was triggered by a request, not the clock. This
// single annotation is all @Scheduled needs (Spring's scheduler is already on the classpath
// transitively via spring-boot-starter-web — no new Maven dependency). See
// PayrollServiceImpl.runMonthlyPayroll() for the one job that uses it.
@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Value("${app.timezone:Asia/Kolkata}")
    private String appTimezone;

    // Sets the JVM default so every un-zoned LocalDate/LocalDateTime.now() call elsewhere in the
    // app (not just the scheduler, which already pins its own zone explicitly) agrees with it —
    // otherwise "the 1st of the month" could disagree with the host's actual system timezone.
    @PostConstruct
    public void configureDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(appTimezone));
    }
}
