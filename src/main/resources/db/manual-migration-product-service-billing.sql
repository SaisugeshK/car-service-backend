-- Reference schema script for the Product/Service + billing changes.
--
-- NOT auto-executed by the application. spring.jpa.hibernate.ddl-auto=update already applies
-- these same column additions automatically the next time the backend starts (this project has
-- no Flyway/Liquibase migration runner). Run this manually in pgAdmin only if you want to apply
-- the schema ahead of time, inspect it, or keep a written record of the change alongside the
-- entity classes in super-market-inv/src/main/java/.../model/.
--
-- Safe to re-run: every statement is idempotent (IF NOT EXISTS / guarded UPDATE).

-- products: distinguish stock-tracked physical goods from non-stock services
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS item_type VARCHAR(20) NOT NULL DEFAULT 'PRODUCT';

UPDATE products SET item_type = 'PRODUCT' WHERE item_type IS NULL;

-- sales: header-level billing totals (subtotal/tax/discount/grand total/payment) plus the
-- car-service billing context captured per visit (vehicle/registration/odometer)
ALTER TABLE sales
    ADD COLUMN IF NOT EXISTS payment_method     VARCHAR(30),
    ADD COLUMN IF NOT EXISTS subtotal           NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS discount_amount    NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS tax_amount         NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS grand_total        NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS paid_amount        NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS balance_amount     NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS vehicle_model      VARCHAR(255),
    ADD COLUMN IF NOT EXISTS registration_number VARCHAR(255),
    ADD COLUMN IF NOT EXISTS odometer_reading   INTEGER;

-- sales_items: per-line billing snapshot (type, discount, tax) at time of sale
ALTER TABLE sales_items
    ADD COLUMN IF NOT EXISTS item_type      VARCHAR(20),
    ADD COLUMN IF NOT EXISTS discount       NUMERIC(12, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS tax_percentage NUMERIC(5, 2)  DEFAULT 0,
    ADD COLUMN IF NOT EXISTS tax_amount     NUMERIC(12, 2) DEFAULT 0;
