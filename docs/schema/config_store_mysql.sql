-- lighting-config schema for MySQL 8.0+
CREATE TABLE IF NOT EXISTS config_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant VARCHAR(64) NOT NULL DEFAULT 'default',
    namespace VARCHAR(128) NOT NULL,
    app_id VARCHAR(128) NOT NULL,
    `key` VARCHAR(512) NOT NULL,
    content_type VARCHAR(32) NOT NULL DEFAULT 'TEXT',
    value LONGTEXT NOT NULL,
    version BIGINT NOT NULL,
    tags JSON NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_config (tenant, namespace, app_id, `key`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS revision (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant VARCHAR(64) NOT NULL,
    namespace VARCHAR(128) NOT NULL,
    app_id VARCHAR(128) NOT NULL,
    `key` VARCHAR(512) NOT NULL,
    version BIGINT NOT NULL,
    op VARCHAR(32) NOT NULL,
    operator VARCHAR(128) NOT NULL,
    diff JSON NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_revision_lookup (tenant, namespace, app_id, `key`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cache_miss_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant VARCHAR(64) NOT NULL,
    namespace VARCHAR(128) NOT NULL,
    app_id VARCHAR(128) NOT NULL,
    selector VARCHAR(512) NOT NULL,
    miss_count BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cache_miss_tenant (tenant, created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS config_edit_lock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant VARCHAR(64) NOT NULL,
    namespace VARCHAR(128) NOT NULL,
    app_id VARCHAR(128) NOT NULL,
    `key` VARCHAR(512) NOT NULL,
    owner_id VARCHAR(128) NOT NULL,
    owner_name VARCHAR(128) NULL,
    expires_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_config_edit_lock (tenant, namespace, app_id, `key`)
) ENGINE=InnoDB;

-- Sample seed data for demos
INSERT INTO config_item (tenant, namespace, app_id, `key`, content_type, value, version, tags, enabled)
VALUES
    ('default', 'prod', 'order-service', 'feature.pay.v2', 'TEXT', 'true', 3, JSON_OBJECT('env','prod','owner','fintech'), 1),
    ('default', 'prod', 'order-service', 'datasource.read', 'JSON', '{"url":"jdbc:mysql://mysql:3306/order","user":"order_ro"}', 2, JSON_OBJECT('tier','critical'), 1),
    ('default', 'beta', 'search-service', 'feature.ai.ranking', 'TEXT', 'false', 1, JSON_OBJECT('experiment','A/B'), 1)
ON DUPLICATE KEY UPDATE value = VALUES(value);

INSERT INTO revision (tenant, namespace, app_id, `key`, version, op, operator, diff)
VALUES
    ('default', 'prod', 'order-service', 'feature.pay.v2', 3, 'UPSERT', 'system', '{"old":"false","new":"true"}'),
    ('default', 'prod', 'order-service', 'datasource.read', 2, 'UPSERT', 'dba', '{"old":"jdbc:mysql://old","new":"jdbc:mysql://mysql"}')
ON DUPLICATE KEY UPDATE diff = VALUES(diff);
