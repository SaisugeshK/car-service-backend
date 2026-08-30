-- Pre-deployment fix — REQUIRED one-time step before/at deploying this change to any database
-- that already has rows in invoices, estimates, or salary_payments.
--
-- Adding @Version to those three entities (found necessary via concurrency testing — see
-- SalaryPayment.java/Estimate.java/Invoice.java for why) makes Hibernate add a nullable `version`
-- column via ddl-auto=update. Every row that existed BEFORE this change gets version = NULL, and
-- Hibernate's optimistic-lock check throws a NullPointerException the first time anyone tries to
-- update one of those pre-existing rows ("Cannot invoke Long.longValue() because ... is null" —
-- exactly the bug this migration prevents, caught during this session's own concurrency testing
-- against the dev database).
--
-- Run this immediately after the column is created (i.e., right after the app boots once with
-- the new entity fields and ddl-auto=update, or before flipping ddl-auto to `validate` for
-- production) and before any real traffic hits these tables.

UPDATE invoices SET version = 0 WHERE version IS NULL;
UPDATE estimates SET version = 0 WHERE version IS NULL;
UPDATE salary_payments SET version = 0 WHERE version IS NULL;

ALTER TABLE invoices ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE invoices ALTER COLUMN version SET NOT NULL;

ALTER TABLE estimates ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE estimates ALTER COLUMN version SET NOT NULL;

ALTER TABLE salary_payments ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE salary_payments ALTER COLUMN version SET NOT NULL;

-- Verified executed against the dev database during this pre-deployment test pass: 2 invoices,
-- 2 estimates, 3 salary_payments rows backfilled; all three columns locked to NOT NULL DEFAULT 0
-- afterward. Confirmed the concurrency test suite passes cleanly (no NPE) once applied.
