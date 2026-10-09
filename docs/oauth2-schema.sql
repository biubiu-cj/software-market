-- =============================================
-- OAuth2 Provider Tables
-- =============================================

-- Registered third-party applications
CREATE TABLE IF NOT EXISTS oauth2_app (
    app_id       VARCHAR(64)  NOT NULL COMMENT 'Application ID (client_id)',
    app_secret   VARCHAR(128) NOT NULL COMMENT 'Application secret',
    app_name     VARCHAR(128) NOT NULL COMMENT 'Display name',
    app_icon     VARCHAR(512) DEFAULT '' COMMENT 'Icon URL',
    description  VARCHAR(512) DEFAULT '' COMMENT 'Description',
    redirect_uri VARCHAR(512) NOT NULL COMMENT 'Allowed redirect URI prefix',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2 registered applications';

-- One-time authorization codes
CREATE TABLE IF NOT EXISTS oauth2_code (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    code         VARCHAR(128) NOT NULL COMMENT 'Authorization code (UUID)',
    app_id       VARCHAR(64)  NOT NULL COMMENT 'FK to oauth2_app',
    user_id      INT          NOT NULL COMMENT 'FK to user',
    scope        VARCHAR(256) NOT NULL COMMENT 'Authorized scope',
    redirect_uri VARCHAR(512) NOT NULL COMMENT 'Original redirect URI',
    state        VARCHAR(256) DEFAULT '' COMMENT 'Original state parameter',
    is_used      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '0=unused, 1=used',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at   DATETIME     NOT NULL COMMENT 'Code expiry time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    INDEX idx_app_user (app_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2 authorization codes';

-- Authorization records (history of which user authorized which app)
CREATE TABLE IF NOT EXISTS oauth2_record (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     INT          NOT NULL COMMENT 'FK to user',
    app_id      VARCHAR(64)  NOT NULL COMMENT 'FK to oauth2_app',
    scope       VARCHAR(256) NOT NULL COMMENT 'Authorized scope',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_user (user_id),
    INDEX idx_app (app_id),
    UNIQUE KEY uk_user_app (user_id, app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2 authorization records';

-- Seed test application
INSERT IGNORE INTO oauth2_app (app_id, app_secret, app_name, app_icon, description, redirect_uri)
VALUES (
    'test-app-001',
    'test-secret-123',
    'Test OAuth2 App',
    'https://img.icons8.com/color/48/test.png',
    'A test application for OAuth2 integration.',
    'https://example.com/oauth2/callback'
);
