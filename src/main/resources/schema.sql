-- Distribution table (simplified - customer_id is just a string, no FK)
CREATE TABLE IF NOT EXISTS distribution (
    distribution_id VARCHAR(255) PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    bucket_id VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    disabled_at TIMESTAMP,
    disable_reason VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_distribution_customer ON distribution(customer_id);
CREATE INDEX IF NOT EXISTS idx_distribution_status ON distribution(status);

-- Usage snapshot table (denormalized with customer_id for fast queries)
CREATE TABLE IF NOT EXISTS usage_snapshot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    distribution_id VARCHAR(255) NOT NULL,
    customer_id VARCHAR(255) NOT NULL,
    snapshot_time TIMESTAMP NOT NULL,
    data_transfer_gb DOUBLE NOT NULL,
    source VARCHAR(50) NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_customer_time ON usage_snapshot(customer_id, snapshot_time);
CREATE INDEX IF NOT EXISTS idx_dist_time_source ON usage_snapshot(distribution_id, snapshot_time, source);

-- Billing event table (idempotency via unique constraint)
CREATE TABLE IF NOT EXISTS billing_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    traffic_usage_gb DOUBLE NOT NULL,
    sent_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT uk_billing UNIQUE (customer_id, period_start, period_end)
);

CREATE INDEX IF NOT EXISTS idx_billing_customer ON billing_event(customer_id);

-- ShedLock table for distributed locking (HA support)
CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);
