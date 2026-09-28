-- Regular-customer tracking: visits table + visit stats on customers + one vehicle per plate.
--
-- NOT auto-executed. With ddl-auto=update (dev) Hibernate adds the table/columns on the next start
-- (but NOT the unique index below — run that part by hand in dev too). The prod profile runs
-- ddl-auto=validate, so run this whole script against prod BEFORE deploying this backend.
--
-- On startup the backend itself (VisitService.backfillFromJobCards) turns every existing job card
-- into a visit and recomputes each customer's total_visits / last_visit_date / regular_status —
-- nothing needs backfilling here.
--
-- Safe to re-run.

CREATE TABLE IF NOT EXISTS visits (
    visit_id            BIGSERIAL PRIMARY KEY,
    customer_id         BIGINT       NOT NULL,
    vehicle_id          BIGINT,
    visit_date_time     TIMESTAMP WITH TIME ZONE NOT NULL,
    purpose             VARCHAR(30)  NOT NULL,   -- SERVICE / REPAIR / INQUIRY / INSURANCE_RENEWAL / OTHER
    notes               TEXT,
    handled_by_user_id  BIGINT,
    handled_by_name     VARCHAR(255),
    source              VARCHAR(20)  NOT NULL,   -- MANUAL / JOB_CARD
    job_card_id         BIGINT UNIQUE,
    created_at          TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_visits_customer   ON visits (customer_id, visit_date_time);
CREATE INDEX IF NOT EXISTS idx_visits_handled_by ON visits (handled_by_user_id);

ALTER TABLE customers ADD COLUMN IF NOT EXISTS total_visits    INTEGER DEFAULT 0;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS last_visit_date TIMESTAMP WITH TIME ZONE;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS regular_status  VARCHAR(20) DEFAULT 'NEW';

-- One vehicle record per registration plate, ignoring case, spaces and dashes. Fails if duplicate
-- plates already exist — find them first with:
--   SELECT UPPER(REPLACE(REPLACE(registration_number,' ',''),'-','')) r, COUNT(*)
--   FROM vehicles WHERE registration_number IS NOT NULL GROUP BY 1 HAVING COUNT(*) > 1;
CREATE UNIQUE INDEX IF NOT EXISTS uq_vehicles_registration_normalized
    ON vehicles (UPPER(REPLACE(REPLACE(registration_number, ' ', ''), '-', '')))
    WHERE registration_number IS NOT NULL AND registration_number <> '';
