-- lighting-config schema for PostgreSQL 13+
CREATE TABLE IF NOT EXISTS config_item (
    id BIGSERIAL PRIMARY KEY,
    tenant VARCHAR(64) NOT NULL DEFAULT 'default',
    namespace VARCHAR(128) NOT NULL,
    app_id VARCHAR(128) NOT NULL,
    key VARCHAR(512) NOT NULL,
    content_type VARCHAR(32) NOT NULL DEFAULT 'TEXT',
    value TEXT NOT NULL,
    version BIGINT NOT NULL,
    tags JSONB NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant, namespace, app_id, key)
);

CREATE TABLE IF NOT EXISTS revision (
    id BIGSERIAL PRIMARY KEY,
    tenant VARCHAR(64) NOT NULL,
    namespace VARCHAR(128) NOT NULL,
    app_id VARCHAR(128) NOT NULL,
    key VARCHAR(512) NOT NULL,
    version BIGINT NOT NULL,
    op VARCHAR(32) NOT NULL,
    operator VARCHAR(128) NOT NULL,
    diff JSONB NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_revision_lookup ON revision (tenant, namespace, app_id, key);
