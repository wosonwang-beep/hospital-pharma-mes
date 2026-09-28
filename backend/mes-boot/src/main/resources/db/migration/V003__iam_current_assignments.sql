DELETE FROM sys_user_role WHERE revoked_at IS NOT NULL;
DELETE FROM sys_role_permission WHERE revoked_at IS NOT NULL;

DELETE older FROM sys_user_role older
JOIN sys_user_role newer ON older.user_id = newer.user_id
    AND older.role_id = newer.role_id AND older.id > newer.id;
DELETE older FROM sys_role_permission older
JOIN sys_role_permission newer ON older.role_id = newer.role_id
    AND older.permission_id = newer.permission_id AND older.id > newer.id;

ALTER TABLE sys_user_role
    ADD UNIQUE KEY uk_sys_user_role_pair (user_id, role_id),
    ADD KEY ix_sys_user_role_role_current (role_id);
ALTER TABLE sys_user_role
    DROP FOREIGN KEY fk_sys_user_role_grantor,
    DROP FOREIGN KEY fk_sys_user_role_revoker,
    DROP INDEX ix_sys_user_role_user,
    DROP INDEX ix_sys_user_role_role,
    DROP COLUMN granted_at,
    DROP COLUMN granted_by,
    DROP COLUMN revoked_at,
    DROP COLUMN revoked_by;

ALTER TABLE sys_role_permission
    ADD UNIQUE KEY uk_sys_role_permission_pair (role_id, permission_id),
    ADD KEY ix_sys_role_permission_permission_current (permission_id);
ALTER TABLE sys_role_permission
    DROP FOREIGN KEY fk_sys_role_permission_grantor,
    DROP FOREIGN KEY fk_sys_role_permission_revoker,
    DROP INDEX ix_sys_role_permission_role,
    DROP INDEX ix_sys_role_permission_permission,
    DROP COLUMN granted_at,
    DROP COLUMN granted_by,
    DROP COLUMN revoked_at,
    DROP COLUMN revoked_by;

ALTER TABLE sys_security_event
    ADD COLUMN target_permission_id BIGINT NULL AFTER target_role_id,
    ADD KEY ix_sys_security_event_permission (target_permission_id, occurred_at),
    ADD CONSTRAINT fk_sys_security_event_permission FOREIGN KEY (target_permission_id)
        REFERENCES sys_permission (id);

INSERT INTO sys_permission (permission_code, permission_type, display_name, menu_route) VALUES
    ('menu:iam:users', 'MENU', 'Employee administration', '/admin/users'),
    ('menu:iam:roles', 'MENU', 'Role administration', '/admin/roles');

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r CROSS JOIN sys_permission p
WHERE r.role_code = 'SYSTEM_ADMIN'
    AND p.permission_code IN ('menu:iam:users', 'menu:iam:roles');
