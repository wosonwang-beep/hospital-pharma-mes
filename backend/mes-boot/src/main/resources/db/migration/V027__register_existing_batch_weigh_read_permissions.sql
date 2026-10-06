-- Register two read permissions already defined by the frozen production API.
-- Registration only: preserve every existing permission and role assignment.
INSERT INTO sys_permission(permission_code, permission_type, display_name, menu_route, enabled)
SELECT p.code, 'ACTION', p.code, NULL, TRUE
FROM (
    SELECT 'production:batch:view' AS code
    UNION ALL SELECT 'mes:weigh:view'
) p
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission existing WHERE existing.permission_code = p.code
);
