-- Reference schema script for the HRM / payroll module (Attendance, Leave, Overtime, Employee
-- Salary Config, Payroll Runs / Salary Payments).
--
-- NOT auto-executed. spring.jpa.hibernate.ddl-auto=update creates these same tables automatically
-- on the next backend start (this project has no Flyway/Liquibase). Run this manually in pgAdmin
-- only if you want to apply ahead of time, or if the target database runs with ddl-auto=validate
-- (production, SPRING_PROFILES_ACTIVE=prod) and these tables do not exist yet — validate fails
-- startup on a missing table, so the HRM tables must be present before the first prod boot.
--
-- Safe to re-run: every statement is idempotent (CREATE TABLE IF NOT EXISTS).
--
-- Column names/types match the JPA entities under
-- src/main/java/com/example/InventoryManagementSystem/model/ (Attendance, LeaveRequest,
-- OvertimeEntry, EmployeeSalaryConfig, SalaryPayment) under Spring Boot's default
-- CamelCaseToUnderscores naming strategy.

-- One row per employee per calendar day. status: PRESENT | ABSENT | WEEK_OFF | HOLIDAY
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id       BIGSERIAL PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    attendance_date     DATE         NOT NULL,
    status              VARCHAR(255) NOT NULL,
    marked_by_user_id   BIGINT,
    notes               TEXT,
    created_at          TIMESTAMPTZ,
    updated_at          TIMESTAMPTZ,
    CONSTRAINT uq_attendance_user_date UNIQUE (user_id, attendance_date)
);

-- Employee leave, spanning start_date..end_date inclusive.
-- leave_type: PAID | UNPAID     status: PENDING | APPROVED | REJECTED
CREATE TABLE IF NOT EXISTS leave_requests (
    leave_id                BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT       NOT NULL,
    leave_type              VARCHAR(255) NOT NULL,
    start_date              DATE         NOT NULL,
    end_date                DATE         NOT NULL,
    reason                  TEXT,
    status                  VARCHAR(255),
    approved_by_user_id     BIGINT,
    approved_at             TIMESTAMPTZ,
    created_at              TIMESTAMPTZ,
    updated_at              TIMESTAMPTZ
);

-- One row per employee per overtime shift. amount = hours * rate (computed server-side).
-- status: PENDING | APPROVED | REJECTED
CREATE TABLE IF NOT EXISTS overtime_entries (
    overtime_id             BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT         NOT NULL,
    work_date               DATE           NOT NULL,
    hours                   NUMERIC(5, 2)  NOT NULL,
    rate                    NUMERIC(10, 2) NOT NULL,
    amount                  NUMERIC(12, 2) NOT NULL,
    status                  VARCHAR(255),
    approved_by_user_id     BIGINT,
    approved_at             TIMESTAMPTZ,
    notes                   TEXT,
    created_at              TIMESTAMPTZ,
    updated_at              TIMESTAMPTZ
);

-- An employee's current pay terms — one active row per user_id (a raise is an update in place).
CREATE TABLE IF NOT EXISTS employee_salary_config (
    salary_config_id    BIGSERIAL PRIMARY KEY,
    user_id             BIGINT         NOT NULL UNIQUE,
    basic_pay           NUMERIC(12, 2) NOT NULL,
    hra                 NUMERIC(12, 2),
    other_allowances    NUMERIC(12, 2),
    deductions          NUMERIC(12, 2),
    effective_from      DATE           NOT NULL,
    active              BOOLEAN,
    notes               TEXT,
    created_at          TIMESTAMPTZ,
    updated_at          TIMESTAMPTZ
);

-- One frozen snapshot row per employee per calendar month. status: PENDING | PAID | CANCELLED
-- generation_source: SCHEDULED | MANUAL     payment_method: CASH | BANK_TRANSFER | UPI | CHEQUE | OTHER
CREATE TABLE IF NOT EXISTS salary_payments (
    salary_payment_id       BIGSERIAL PRIMARY KEY,
    version                 BIGINT,
    payment_number          VARCHAR(255) UNIQUE,
    user_id                 BIGINT  NOT NULL,
    salary_config_id        BIGINT,
    basic_pay               NUMERIC(12, 2),
    hra                     NUMERIC(12, 2),
    other_allowances        NUMERIC(12, 2),
    overtime_amount         NUMERIC(12, 2),
    deductions              NUMERIC(12, 2),
    attendance_deductions   NUMERIC(12, 2),
    leave_deductions        NUMERIC(12, 2),
    gross_pay               NUMERIC(12, 2),
    net_pay                 NUMERIC(12, 2),
    working_days            INTEGER,
    present_days            INTEGER,
    absent_days             INTEGER,
    paid_leave_days         INTEGER,
    unpaid_leave_days       INTEGER,
    pay_period_month        INTEGER NOT NULL,
    pay_period_year         INTEGER NOT NULL,
    status                  VARCHAR(255),
    generated_at            TIMESTAMPTZ,
    generated_by            BIGINT,
    generation_source       VARCHAR(255),
    paid_at                 TIMESTAMPTZ,
    paid_by_user_id         BIGINT,
    payment_method          VARCHAR(255),
    payment_reference       VARCHAR(255),
    cancelled_at            TIMESTAMPTZ,
    cancelled_by_user_id    BIGINT,
    cancellation_reason     TEXT,
    notes                   TEXT,
    CONSTRAINT uq_salary_payments_user_period UNIQUE (user_id, pay_period_month, pay_period_year)
);

-- Payroll eligibility (PayrollServiceImpl) skips any user whose boolean `active` is not TRUE.
-- Staff created through the Users screen before the resolveActive() fix have active = NULL even
-- though their status is 'ACTIVE', so payroll would silently skip them. Backfill from status.
UPDATE users SET active = TRUE
 WHERE active IS NULL AND (status IS NULL OR upper(status) <> 'INACTIVE');
UPDATE users SET active = FALSE
 WHERE active IS NULL AND upper(status) = 'INACTIVE';
