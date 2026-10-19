-- User-approved EBR navigation responsibility, 2026-10-13.
-- Supersedes only V044 navigation naming/visibility. No permission code, GxP state, signature, or historical data changes.

INSERT INTO sys_menu(
    org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,
    created_by,updated_by,permission_code,required_permissions
)
SELECT 1,
       (SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_production'),
       'nav_ebr','电子批记录',NULL,40,'ACTIVE',
       1,1,NULL,NULL
WHERE NOT EXISTS(
    SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='nav_ebr'
);

UPDATE sys_menu ebr
JOIN sys_menu production
  ON production.org_id=ebr.org_id AND production.menu_code='nav_production'
SET ebr.parent_id=production.id,
    ebr.menu_name='电子批记录',
    ebr.route_path=NULL,
    ebr.sort_no=40,
    ebr.status='ACTIVE',
    ebr.permission_code=NULL,
    ebr.required_permissions=NULL,
    ebr.version_no=ebr.version_no+1
WHERE ebr.org_id=1 AND ebr.menu_code='nav_ebr';

UPDATE sys_menu child
JOIN sys_menu parent
  ON parent.org_id=child.org_id AND parent.menu_code='nav_ebr'
SET child.parent_id=parent.id,
    child.menu_name='批记录目录模板',
    child.sort_no=10,
    child.status='ACTIVE',
    child.permission_code='ebr:template:view',
    child.required_permissions='process:package:view',
    child.version_no=child.version_no+1
WHERE child.org_id=1 AND child.route_path='/ebr/book-templates';

UPDATE sys_menu child
JOIN sys_menu parent
  ON parent.org_id=child.org_id AND parent.menu_code='nav_ebr'
SET child.parent_id=parent.id,
    child.menu_name='工序表单模板',
    child.sort_no=20,
    child.status='ACTIVE',
    child.permission_code='ebr:template:view',
    child.required_permissions='process:package:view',
    child.version_no=child.version_no+1
WHERE child.org_id=1 AND child.route_path='/ebr/templates';

INSERT INTO sys_menu(
    org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,
    created_by,updated_by,permission_code,required_permissions
)
SELECT 1,
       (SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_ebr'),
       'nav_ebr-batches','批记录实例','/ebr/batches',30,'ACTIVE',
       1,1,'ebr:form:view','production:batch:view,master:product:view'
WHERE NOT EXISTS(
    SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/ebr/batches'
);

UPDATE sys_menu child
JOIN sys_menu parent
  ON parent.org_id=child.org_id AND parent.menu_code='nav_ebr'
SET child.parent_id=parent.id,
    child.menu_name='批记录实例',
    child.sort_no=30,
    child.status='ACTIVE',
    child.permission_code='ebr:form:view',
    child.required_permissions='production:batch:view,master:product:view',
    child.version_no=child.version_no+1
WHERE child.org_id=1 AND child.route_path='/ebr/batches';

-- Keep Material Balance after the EBR directory group.
UPDATE sys_menu
SET sort_no=50,version_no=version_no+1
WHERE org_id=1 AND route_path='/production/balances' AND sort_no<>50;

-- Ensure menu-to-function relation for the new read-oriented batch-record instance entry.
INSERT INTO sys_menu_permission(menu_id,permission_id,created_by)
SELECT m.id,p.id,1
FROM sys_menu m
JOIN sys_permission p ON p.permission_code='ebr:form:view'
WHERE m.org_id=1 AND m.route_path='/ebr/batches'
  AND NOT EXISTS(
      SELECT 1 FROM sys_menu_permission mp
      WHERE mp.menu_id=m.id AND mp.permission_id=p.id
  );

-- Preserve role visibility according to existing effective permissions; no new permission is granted.
INSERT INTO sys_role_menu(org_id,role_id,menu_id,created_by)
SELECT 1,r.id,m.id,1
FROM sys_role r
JOIN sys_menu m ON m.org_id=1 AND m.route_path='/ebr/batches'
WHERE r.enabled=1
  AND EXISTS(
      SELECT 1
      FROM sys_role_permission rp
      JOIN sys_permission p ON p.id=rp.permission_id
      WHERE rp.role_id=r.id AND p.permission_code='ebr:form:view'
  )
  AND EXISTS(
      SELECT 1
      FROM sys_role_permission rp
      JOIN sys_permission p ON p.id=rp.permission_id
      WHERE rp.role_id=r.id AND p.permission_code='production:batch:view'
  )
  AND EXISTS(
      SELECT 1
      FROM sys_role_permission rp
      JOIN sys_permission p ON p.id=rp.permission_id
      WHERE rp.role_id=r.id AND p.permission_code='master:product:view'
  )
  AND NOT EXISTS(
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.org_id=1 AND rm.role_id=r.id AND rm.menu_id=m.id
  );

-- Rebuild visibility for the two pre-existing EBR menu items if roles were created while one item was inactive.
INSERT INTO sys_role_menu(org_id,role_id,menu_id,created_by)
SELECT 1,r.id,m.id,1
FROM sys_role r
JOIN sys_menu m ON m.org_id=1 AND m.route_path IN('/ebr/book-templates','/ebr/templates')
WHERE r.enabled=1
  AND EXISTS(
      SELECT 1
      FROM sys_role_permission rp
      JOIN sys_permission p ON p.id=rp.permission_id
      WHERE rp.role_id=r.id AND p.permission_code='ebr:template:view'
  )
  AND EXISTS(
      SELECT 1
      FROM sys_role_permission rp
      JOIN sys_permission p ON p.id=rp.permission_id
      WHERE rp.role_id=r.id AND p.permission_code='process:package:view'
  )
  AND NOT EXISTS(
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.org_id=1 AND rm.role_id=r.id AND rm.menu_id=m.id
  );
