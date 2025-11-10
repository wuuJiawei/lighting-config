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
