-- SYSTEM_ADMIN full-access authorization and registration of permissions already required by runtime contracts.
-- Authorized during live MES review on 2026-10-07.
-- Append-only: do not remove or rewrite any existing role/permission assignment.

INSERT INTO sys_permission(permission_code, permission_type, display_name, menu_route, enabled)
SELECT p.code, 'ACTION', p.code, NULL, TRUE
FROM (
    SELECT 'ebr:form:view' AS code
    UNION ALL SELECT 'ebr:form:edit'
    UNION ALL SELECT 'ebr:form:submit'
    UNION ALL SELECT 'ebr:record:correct'
    UNION ALL SELECT 'ebr:review:verify'
    UNION ALL SELECT 'ebr:review:approve'
    UNION ALL SELECT 'mes:charge:create'
    UNION ALL SELECT 'mes:charge:reverse'
    UNION ALL SELECT 'mes:charge:view'
    UNION ALL SELECT 'mes:equipment:bind'
    UNION ALL SELECT 'mes:execution:view'
    UNION ALL SELECT 'mes:operation:view'
    UNION ALL SELECT 'mes:operation:start'
    UNION ALL SELECT 'mes:operation:pause'
    UNION ALL SELECT 'mes:operation:complete'
    UNION ALL SELECT 'mes:param:record'
    UNION ALL SELECT 'mes:weigh:create'
    UNION ALL SELECT 'mes:weigh:verify'
    UNION ALL SELECT 'production:batch:create'
    UNION ALL SELECT 'production:batch:update'
    UNION ALL SELECT 'production:batch:release'
    UNION ALL SELECT 'production:batch:start'
    UNION ALL SELECT 'production:batch:complete'
    UNION ALL SELECT 'production:order:create'
    UNION ALL SELECT 'production:order:update'
    UNION ALL SELECT 'production:order:view'
    UNION ALL SELECT 'production:subbatch:create'
    UNION ALL SELECT 'trace:view'
) p
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission existing WHERE existing.permission_code = p.code
);

-- SYSTEM_ADMIN is the platform super-administrator role: grant every enabled permission.
INSERT INTO sys_role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.enabled = TRUE
WHERE r.role_code = 'SYSTEM_ADMIN'
  AND r.enabled = TRUE
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_permission existing
      WHERE existing.role_id = r.id
        AND existing.permission_id = p.id
  );
