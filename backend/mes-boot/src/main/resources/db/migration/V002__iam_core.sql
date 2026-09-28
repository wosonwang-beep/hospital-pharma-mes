CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    login_name VARCHAR(128) NOT NULL,
    login_name_normalized VARCHAR(128) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_count INT NOT NULL DEFAULT 0,
    locked_until DATETIME(6) NULL,
    must_change_password BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_login_normalized (login_name_normalized),
    CONSTRAINT ck_sys_user_failure_count CHECK (failed_login_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_code VARCHAR(128) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT,
    permission_code VARCHAR(160) NOT NULL,
    permission_type VARCHAR(16) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    menu_route VARCHAR(255) NULL,
    parent_permission_id BIGINT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_permission_code (permission_code),
    KEY ix_sys_permission_parent (parent_permission_id),
    CONSTRAINT fk_sys_permission_parent FOREIGN KEY (parent_permission_id) REFERENCES sys_permission (id),
    CONSTRAINT ck_sys_permission_type CHECK (permission_type IN ('MENU', 'ACTION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    granted_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    granted_by BIGINT NULL,
    revoked_at DATETIME(6) NULL,
    revoked_by BIGINT NULL,
    PRIMARY KEY (id),
    KEY ix_sys_user_role_user (user_id, revoked_at),
    KEY ix_sys_user_role_role (role_id, revoked_at),
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_sys_user_role_grantor FOREIGN KEY (granted_by) REFERENCES sys_user (id),
    CONSTRAINT fk_sys_user_role_revoker FOREIGN KEY (revoked_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    granted_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    granted_by BIGINT NULL,
    revoked_at DATETIME(6) NULL,
    revoked_by BIGINT NULL,
    PRIMARY KEY (id),
    KEY ix_sys_role_permission_role (role_id, revoked_at),
    KEY ix_sys_role_permission_permission (permission_id, revoked_at),
    CONSTRAINT fk_sys_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_sys_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (id),
    CONSTRAINT fk_sys_role_permission_grantor FOREIGN KEY (granted_by) REFERENCES sys_user (id),
    CONSTRAINT fk_sys_role_permission_revoker FOREIGN KEY (revoked_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_security_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_type VARCHAR(64) NOT NULL,
    outcome VARCHAR(32) NOT NULL,
    actor_user_id BIGINT NULL,
    target_user_id BIGINT NULL,
    target_role_id BIGINT NULL,
    trace_id VARCHAR(64) NULL,
    request_context VARCHAR(255) NULL,
    occurred_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY ix_sys_security_event_actor (actor_user_id, occurred_at),
    KEY ix_sys_security_event_target (target_user_id, occurred_at),
    KEY ix_sys_security_event_type (event_type, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_role (role_code, display_name) VALUES
    ('SYSTEM_ADMIN', 'System administrator');

INSERT INTO sys_permission (permission_code, permission_type, display_name, menu_route) VALUES
    ('menu:home', 'MENU', 'Home', '/'),
    ('action:iam:user.manage', 'ACTION', 'Manage employee accounts', NULL),
    ('action:iam:role.manage', 'ACTION', 'Manage roles and permissions', NULL);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r CROSS JOIN sys_permission p
WHERE r.role_code = 'SYSTEM_ADMIN';
