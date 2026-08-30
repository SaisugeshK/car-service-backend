-- Reference schema script for explicit per-vehicle-size service pricing.
--
-- NOT auto-executed. spring.jpa.hibernate.ddl-auto=update creates this table on the next
-- backend start. Run manually only to apply ahead of time or keep a written record.
--
-- Safe to re-run: CREATE TABLE IF NOT EXISTS.

-- One explicit price for a service at one vehicle size band. A size with no row here falls
-- back to service_master.default_price when quoting an estimate (see ServicePricingResolver).
CREATE TABLE IF NOT EXISTS service_prices (
    id              BIGSERIAL PRIMARY KEY,
    service_id      BIGINT       NOT NULL,
    size_class_code VARCHAR(20)  NOT NULL,   -- SMALL/SEDAN/SUV/MUV/LUXURY (car), STANDARD/PREMIUM (bike)
    price           NUMERIC(12,2) NOT NULL,
    CONSTRAINT uq_service_prices_service_size UNIQUE (service_id, size_class_code)
);

-- The old size-multiplier feature is retired. Its table is no longer read or written and can
-- be dropped whenever convenient (vehicles.size_class stays — it is the join key here):
--   DROP TABLE IF EXISTS vehicle_size_classes;
