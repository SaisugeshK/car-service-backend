-- Expenses module (outgoing non-revenue costs + receipt attachments).
--
-- NOT auto-executed. With spring.jpa.hibernate.ddl-auto=update (dev) Hibernate creates this table
-- on the next start. The prod profile runs ddl-auto=validate, so run this against the prod DB
-- BEFORE deploying the backend that contains the Expense entity, or startup fails.
--
-- Safe to re-run: CREATE TABLE / INDEX IF NOT EXISTS.

CREATE TABLE IF NOT EXISTS expenses (
    expense_id                  BIGSERIAL PRIMARY KEY,
    title                       VARCHAR(150)  NOT NULL,
    category                    VARCHAR(30)   NOT NULL,   -- UTILITIES / RENT / MAINTENANCE / SUPPLIES / MISCELLANEOUS
    description                 TEXT,
    amount                      NUMERIC(12,2) NOT NULL,
    payment_method              VARCHAR(20)   NOT NULL,   -- CASH / CARD / UPI / BANK_TRANSFER
    expense_date                DATE          NOT NULL,
    receipt_stored_file_name    VARCHAR(255),
    receipt_original_file_name  VARCHAR(255),
    receipt_content_type        VARCHAR(255),
    receipt_file_size           BIGINT,
    created_by_user_id          BIGINT        NOT NULL,
    created_by_name             VARCHAR(255),
    created_by_role             VARCHAR(30),
    created_at                  TIMESTAMP WITH TIME ZONE,
    updated_at                  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_expenses_created_by ON expenses (created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_expenses_date ON expenses (expense_date);
