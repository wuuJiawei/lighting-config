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

-- Sample seed data for demos
INSERT INTO config_item (tenant, namespace, app_id, key, content_type, value, version, tags, enabled)
VALUES
    ('default', 'prod', 'order-service', 'feature.pay.v2', 'TEXT', 'true', 3, '{"env":"prod","owner":"fintech"}'::jsonb, TRUE),
    ('default', 'prod', 'order-service', 'datasource.read', 'JSON', '{"url":"jdbc:postgresql://pg:5432/order","user":"order_ro"}', 2, '{"tier":"critical"}'::jsonb, TRUE),
    ('default', 'beta', 'search-service', 'feature.ai.ranking', 'TEXT', 'false', 1, '{"experiment":"A/B"}'::jsonb, TRUE)
ON CONFLICT (tenant, namespace, app_id, key) DO NOTHING;

INSERT INTO revision (tenant, namespace, app_id, key, version, op, operator, diff)
VALUES
    ('default', 'prod', 'order-service', 'feature.pay.v2', 3, 'UPSERT', 'system', '{"old":"false","new":"true"}'::jsonb),
    ('default', 'prod', 'order-service', 'datasource.read', 2, 'UPSERT', 'dba', '{"old":"jdbc:postgresql://old","new":"jdbc:postgresql://pg"}'::jsonb)
ON CONFLICT DO NOTHING;
