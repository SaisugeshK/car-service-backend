-- Reference schema script for the Car Service Center billing redesign (Invoice/InvoiceItem
-- becomes the primary billing engine; Sales/SalesItem is left as a frozen legacy module).
--
-- NOT auto-executed. spring.jpa.hibernate.ddl-auto=update already applies these same changes
-- automatically on next backend boot (this project has no Flyway/Liquibase). Run manually in
-- pgAdmin only if you want to apply ahead of time or keep a written record next to the entity
-- classes in super-market-inv/src/main/java/.../model/.
--
-- Safe to re-run: every statement is idempotent (IF NOT EXISTS).

-- New: vehicles — a customer can have multiple vehicles; invoices reference one.
CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id          BIGSERIAL PRIMARY KEY,
    customer_id         BIGINT NOT NULL,
    vehicle_model       VARCHAR(255) NOT NULL,
    registration_number VARCHAR(255),
    odometer            INTEGER,
    vehicle_type        VARCHAR(255),
    fuel_type           VARCHAR(255),
    year                INTEGER,
    created_at          TIMESTAMPTZ,
    updated_at          TIMESTAMPTZ
);

-- New: service_master — the car-service catalog (labour items), distinct from Product and
-- never stock-tracked. Named "service_master" (not "services"/"service") to read clearly
-- next to this schema's many *_service tables and avoid any ambiguity with the app layer.
CREATE TABLE IF NOT EXISTS service_master (
    service_id     BIGSERIAL PRIMARY KEY,
    service_code   VARCHAR(255) UNIQUE,
    service_name   VARCHAR(255) NOT NULL,
    description    TEXT,
    default_price  NUMERIC(12, 2),
    gst_percentage NUMERIC(5, 2) DEFAULT 0,
    status         VARCHAR(255) DEFAULT 'active',
    created_at     TIMESTAMPTZ,
    updated_at     TIMESTAMPTZ
);

-- One-time migration: copy existing itemType='SERVICE' products into the new service catalog.
-- Originals are left untouched. Safe to skip if you don't have any such rows, or already ran it.
INSERT INTO service_master (service_code, service_name, default_price, gst_percentage, status)
SELECT p.sku, TRIM(p.product_name), p.selling_price,
       COALESCE((SELECT MAX(pt.tax_percentage) FROM product_taxes pt WHERE pt.product_id = p.product_id), 18),
       'active'
FROM products p
WHERE p.item_type = 'SERVICE'
  AND NOT EXISTS (SELECT 1 FROM service_master sm WHERE sm.service_code = p.sku);

-- invoices: vehicle link, business/lifecycle fields, CGST/SGST split, updated_at.
ALTER TABLE invoices
    ADD COLUMN IF NOT EXISTS vehicle_id       BIGINT,
    ADD COLUMN IF NOT EXISTS odometer_reading INTEGER,
    ADD COLUMN IF NOT EXISTS invoice_date     TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS cgst_amount      NUMERIC(12, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS sgst_amount      NUMERIC(12, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS status           VARCHAR(20) DEFAULT 'COMPLETED',
    ADD COLUMN IF NOT EXISTS updated_at       TIMESTAMPTZ;

-- invoice_items: SERVICE/PRODUCT split (mirrors Invoice — one invoice, many mixed items).
ALTER TABLE invoice_items
    ADD COLUMN IF NOT EXISTS item_type   VARCHAR(20),
    ADD COLUMN IF NOT EXISTS service_id  INTEGER,
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS created_at  TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS updated_at  TIMESTAMPTZ;
ALTER TABLE invoice_items ALTER COLUMN product_id DROP NOT NULL;

-- hold_invoices: structured cart snapshot instead of a bare free-text field, plus denormalized
-- columns so the "Held Bills" list doesn't need to parse JSON just to render.
ALTER TABLE hold_invoices
    ADD COLUMN IF NOT EXISTS customer_id BIGINT,
    ADD COLUMN IF NOT EXISTS vehicle_id  BIGINT,
    ADD COLUMN IF NOT EXISTS status      VARCHAR(20) DEFAULT 'HELD';
ALTER TABLE hold_invoices ALTER COLUMN data TYPE TEXT;

-- sales_returns / sales_return_items: can now target an Invoice-based transaction (the current
-- primary billing engine) as well as the legacy Sales one.
ALTER TABLE sales_returns
    ADD COLUMN IF NOT EXISTS invoice_id      BIGINT,
    ADD COLUMN IF NOT EXISTS invoice_item_id BIGINT;
ALTER TABLE sales_return_items
    ADD COLUMN IF NOT EXISTS invoice_id      BIGINT,
    ADD COLUMN IF NOT EXISTS invoice_item_id BIGINT;
