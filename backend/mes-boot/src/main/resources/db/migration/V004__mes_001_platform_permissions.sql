INSERT INTO sys_permission (permission_code, permission_type, display_name, menu_route)
SELECT 'audit:view', 'MENU', 'View GMP audit trail', '/audit'
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'audit:view');

INSERT INTO sys_permission (permission_code, permission_type, display_name, menu_route)
SELECT 'ebr:sign', 'ACTION', 'Apply electronic signature', NULL
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'ebr:sign');

INSERT INTO sys_permission (permission_code, permission_type, display_name, menu_route)
SELECT 'integration:view', 'MENU', 'View integration operations', '/integration/operations'
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'integration:view');

INSERT INTO sys_permission (permission_code, permission_type, display_name, menu_route)
SELECT 'integration:retry', 'ACTION', 'Retry integration message', NULL
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'integration:retry');

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN ('audit:view', 'ebr:sign', 'integration:view', 'integration:retry')
LEFT JOIN sys_role_permission rp ON rp.role_id = r.id AND rp.permission_id = p.id
WHERE r.role_code = 'SYSTEM_ADMIN' AND rp.id IS NULL;
