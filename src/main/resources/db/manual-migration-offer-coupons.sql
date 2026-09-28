-- Offer coupon codes, date+time validity, usage limits, and offer details stored on invoices.
--
-- NOT auto-executed. With spring.jpa.hibernate.ddl-auto=update (dev) Hibernate adds these
-- columns on the next start. The prod profile runs ddl-auto=validate, so run this against the
-- prod DB BEFORE deploying the backend that contains these entity changes, or startup fails.
--
-- Safe to re-run: ADD COLUMN IF NOT EXISTS / guarded UPDATEs.
--
-- Coupon codes for existing offers and start/end date-times copied from the old date-only
-- columns are also filled in by OfferServiceImpl.backfillLegacyOffers at startup; the UPDATEs
-- below just do it ahead of time.

ALTER TABLE offers ADD COLUMN IF NOT EXISTS coupon_code      VARCHAR(60);
ALTER TABLE offers ADD COLUMN IF NOT EXISTS start_date_time  TIMESTAMP WITH TIME ZONE;
ALTER TABLE offers ADD COLUMN IF NOT EXISTS end_date_time    TIMESTAMP WITH TIME ZONE;
ALTER TABLE offers ADD COLUMN IF NOT EXISTS usage_limit      INTEGER;

CREATE UNIQUE INDEX IF NOT EXISTS uq_offers_coupon_code ON offers (coupon_code);

-- Old date-only validity -> whole days in India time.
UPDATE offers SET start_date_time = (start_date::timestamp AT TIME ZONE 'Asia/Kolkata')
 WHERE start_date_time IS NULL AND start_date IS NOT NULL;
UPDATE offers SET end_date_time = ((end_date::timestamp + INTERVAL '23 hours 59 minutes 59 seconds') AT TIME ZONE 'Asia/Kolkata')
 WHERE end_date_time IS NULL AND end_date IS NOT NULL;

-- Name-based code plus the offer id, so it's unique without a lookup loop.
UPDATE offers SET coupon_code = COALESCE(NULLIF(UPPER(REGEXP_REPLACE(offer_name, '[^A-Za-z0-9]', '', 'g')), ''), 'OFFER') || offer_id
 WHERE coupon_code IS NULL;

ALTER TABLE invoices ADD COLUMN IF NOT EXISTS offer_id              BIGINT;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS offer_name            VARCHAR(255);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS coupon_code           VARCHAR(60);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS offer_discount_type   VARCHAR(20);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS offer_discount_value  NUMERIC(12,2);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS offer_discount_amount NUMERIC(12,2);

CREATE INDEX IF NOT EXISTS idx_invoices_offer_id ON invoices (offer_id);
