-- V040 System management departments. Additive only; do not modify existing IAM/business data.
CREATE TABLE sys_department (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 organization_id BIGINT NOT NULL,
 parent_id BIGINT NULL,
 department_code VARCHAR(64) NOT NULL,
 department_name VARCHAR(120) NOT NULL,
 leader_user_id BIGINT NULL,
 phone VARCHAR(40) NULL,
 description VARCHAR(500) NULL,
 sort_no INT NOT NULL DEFAULT 0,
 status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_department_org_code(org_id,department_code),
 UNIQUE KEY uk_department_org_id(org_id,id),
 KEY ix_department_tree(org_id,parent_id,sort_no),
 KEY ix_department_organization(org_id,organization_id),
 CONSTRAINT fk_department_organization FOREIGN KEY(organization_id) REFERENCES md_organization(id),
 CONSTRAINT fk_department_parent FOREIGN KEY(org_id,parent_id) REFERENCES sys_department(org_id,id),
 CONSTRAINT fk_department_leader FOREIGN KEY(leader_user_id) REFERENCES sys_user(id),
 CONSTRAINT ck_department_status CHECK(status IN ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_user_department (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 user_id BIGINT NOT NULL,
 department_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_user_department_per_org(org_id,user_id),
 KEY ix_user_department_lookup(org_id,department_id),
 CONSTRAINT fk_user_department_user FOREIGN KEY(user_id) REFERENCES sys_user(id),
 CONSTRAINT fk_user_department_department FOREIGN KEY(org_id,department_id) REFERENCES sys_department(org_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled)
SELECT p.code,p.kind,p.display_name,p.route,TRUE FROM (
 SELECT 'iam:department:view' code,'MENU' kind,'部门管理查看' display_name,'/admin/departments' route
 UNION ALL SELECT 'iam:department:create','ACTION','创建部门',NULL
 UNION ALL SELECT 'iam:department:update','ACTION','编辑部门及成员归属',NULL
) p WHERE NOT EXISTS(SELECT 1 FROM sys_permission existing WHERE existing.permission_code=p.code);

INSERT INTO sys_role_permission(role_id,permission_id)
SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON p.permission_code LIKE 'iam:department:%'
WHERE r.role_code='SYSTEM_ADMIN' AND r.enabled=TRUE
AND NOT EXISTS(SELECT 1 FROM sys_role_permission existing WHERE existing.role_id=r.id AND existing.permission_id=p.id);

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,
 created_by,updated_by,permission_code,required_permissions)
SELECT 1,m.id,'nav_departments','部门管理','/admin/departments',5,'ACTIVE',1,1,'iam:department:view',NULL
FROM sys_menu m WHERE m.org_id=1 AND m.menu_code='nav_system'
AND NOT EXISTS(SELECT 1 FROM sys_menu existing WHERE existing.org_id=1 AND existing.menu_code='nav_departments');

INSERT INTO sys_role_menu(org_id,role_id,menu_id,created_by)
SELECT m.org_id,r.id,m.id,1 FROM sys_role r CROSS JOIN sys_menu m
WHERE r.role_code='SYSTEM_ADMIN' AND r.enabled=TRUE AND m.menu_code='nav_departments' AND m.org_id=1
AND NOT EXISTS(SELECT 1 FROM sys_role_menu existing WHERE existing.role_id=r.id AND existing.menu_id=m.id AND existing.org_id=m.org_id);

INSERT INTO sys_menu_permission(menu_id,permission_id,created_by)
SELECT m.id,p.id,1 FROM sys_menu m JOIN sys_permission p ON p.permission_code LIKE 'iam:department:%'
WHERE m.org_id=1 AND m.menu_code='nav_departments'
AND NOT EXISTS(SELECT 1 FROM sys_menu_permission existing WHERE existing.menu_id=m.id AND existing.permission_id=p.id);
