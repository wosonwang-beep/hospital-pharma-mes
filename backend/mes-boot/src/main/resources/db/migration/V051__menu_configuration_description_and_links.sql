-- Approved DCP-MENU-CONFIGURATION-001. Preserve all existing definitions and grants.
ALTER TABLE sys_permission ADD COLUMN permission_description VARCHAR(512) NULL;

-- Complete missing view links for the already initialized formal menu catalog.
INSERT INTO sys_menu_permission(menu_id,permission_id,created_by)
SELECT m.id,p.id,m.created_by FROM sys_menu m
JOIN sys_permission p ON p.permission_code=m.permission_code
WHERE m.org_id=1 AND m.route_path IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM sys_menu_permission x WHERE x.menu_id=m.id AND x.permission_id=p.id);

-- V045 already initializes these four permission definitions; no new role grants.
INSERT INTO sys_menu_permission(menu_id,permission_id,created_by)
SELECT m.id,p.id,m.created_by FROM sys_menu m JOIN sys_permission p
ON p.permission_code IN ('production:prescription:view','production:prescription:create','production:prescription:update','production:prescription:activate')
WHERE m.org_id=1 AND m.menu_code='nav_production-prescriptions'
AND NOT EXISTS (SELECT 1 FROM sys_menu_permission x WHERE x.menu_id=m.id AND x.permission_id=p.id);
