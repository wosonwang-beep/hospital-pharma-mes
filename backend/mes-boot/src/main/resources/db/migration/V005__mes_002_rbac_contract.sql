ALTER TABLE sys_permission ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER enabled;

CREATE TABLE sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    org_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    menu_code VARCHAR(64) NOT NULL,
    menu_name VARCHAR(100) NOT NULL,
    route_path VARCHAR(255) NULL,
    sort_no INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_by BIGINT NOT NULL,
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    version_no BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_menu_org_code (org_id, menu_code),
    KEY ix_sys_menu_org_status_sort (org_id, status, sort_no),
    KEY ix_sys_menu_parent (parent_id),
    CONSTRAINT fk_sys_menu_parent FOREIGN KEY (parent_id) REFERENCES sys_menu (id),
    CONSTRAINT ck_sys_menu_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_sys_menu_sort CHECK (sort_no >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_role_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    org_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu_pair (role_id, menu_id),
    KEY ix_sys_role_menu_org_role (org_id, role_id),
    KEY ix_sys_role_menu_menu (menu_id),
    CONSTRAINT fk_sys_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_sys_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_permission (permission_code, permission_type, display_name, menu_route, enabled)
SELECT 'iam:user:view', 'MENU', 'View users', '/admin/users', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'iam:user:view');
INSERT INTO sys_permission (permission_code, permission_type, display_name, enabled)
SELECT 'iam:user:create', 'ACTION', 'Create users', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'iam:user:create');
INSERT INTO sys_permission (permission_code, permission_type, display_name, enabled)
SELECT 'iam:user:update', 'ACTION', 'Update users', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'iam:user:update');

INSERT INTO sys_permission (permission_code, permission_type, display_name, menu_route, enabled)
SELECT 'iam:role:view', 'MENU', 'View roles', '/admin/roles', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'iam:role:view');
INSERT INTO sys_permission (permission_code, permission_type, display_name, enabled)
SELECT 'iam:role:create', 'ACTION', 'Create roles', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'iam:role:create');
INSERT INTO sys_permission (permission_code, permission_type, display_name, enabled)
SELECT 'iam:role:update', 'ACTION', 'Update roles', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'iam:role:update');

INSERT INTO sys_permission (permission_code, permission_type, display_name, enabled)
SELECT 'iam:permission:view', 'ACTION', 'View permissions', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'iam:permission:view');
INSERT INTO sys_permission (permission_code, permission_type, display_name, enabled)
SELECT 'iam:permission:create', 'ACTION', 'Create permissions', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'iam:permission:create');
INSERT INTO sys_permission (permission_code, permission_type, display_name, enabled)
SELECT 'iam:permission:update', 'ACTION', 'Update permissions', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'iam:permission:update');

INSERT INTO sys_permission (permission_code, permission_type, display_name, enabled)
SELECT 'iam:menu:view', 'ACTION', 'View menus', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'iam:menu:view');
INSERT INTO sys_permission (permission_code, permission_type, display_name, enabled)
SELECT 'iam:menu:create', 'ACTION', 'Create menus', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'iam:menu:create');
INSERT INTO sys_permission (permission_code, permission_type, display_name, enabled)
SELECT 'iam:menu:update', 'ACTION', 'Update menus', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'iam:menu:update');

INSERT INTO sys_menu (org_id, parent_id, menu_code, menu_name, route_path, sort_no, status, created_by, updated_by)
SELECT 1, NULL, 'iam:user:view', 'User administration', '/admin/users', 100, 'ACTIVE', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE org_id = 1 AND menu_code = 'iam:user:view');
INSERT INTO sys_menu (org_id, parent_id, menu_code, menu_name, route_path, sort_no, status, created_by, updated_by)
SELECT 1, NULL, 'iam:role:view', 'Role administration', '/admin/roles', 110, 'ACTIVE', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE org_id = 1 AND menu_code = 'iam:role:view');

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
    'iam:user:view', 'iam:user:create', 'iam:user:update',
    'iam:role:view', 'iam:role:create', 'iam:role:update',
    'iam:permission:view', 'iam:permission:create', 'iam:permission:update',
    'iam:menu:view', 'iam:menu:create', 'iam:menu:update'
)
WHERE r.role_code = 'SYSTEM_ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_permission rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

INSERT INTO sys_role_menu (org_id, role_id, menu_id, created_by)
SELECT m.org_id, r.id, m.id, 1
FROM sys_role r
JOIN sys_menu m ON m.org_id = 1 AND m.menu_code IN ('iam:user:view', 'iam:role:view')
WHERE r.role_code = 'SYSTEM_ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );
