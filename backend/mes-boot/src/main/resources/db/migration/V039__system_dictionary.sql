-- Approved 2026-10-08 system dictionary foundation, additive and non-destructive.
-- No existing business rows, signed records or role grants are modified/deleted.
CREATE TABLE sys_dict_type (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 dict_code VARCHAR(64) NOT NULL, dict_name VARCHAR(100) NOT NULL,
 dict_kind VARCHAR(16) NOT NULL, structure_type VARCHAR(16) NOT NULL,
 description VARCHAR(500) NULL, sort_no INT NOT NULL DEFAULT 0,
 status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
 created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_dict_type_org_code(org_id,dict_code),
 UNIQUE KEY uk_dict_type_org_id(org_id,id),
 CONSTRAINT ck_dict_type_kind CHECK(dict_kind IN ('SYSTEM','BUSINESS')),
 CONSTRAINT ck_dict_type_structure CHECK(structure_type IN ('FLAT','TREE')),
 CONSTRAINT ck_dict_type_status CHECK(status IN ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sys_dict_item (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, dict_type_id BIGINT NOT NULL,
 parent_id BIGINT NULL, item_code VARCHAR(80) NOT NULL,
 item_label VARCHAR(120) NOT NULL, description VARCHAR(500) NULL,
 sort_no INT NOT NULL DEFAULT 0, status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
 is_default BOOLEAN NOT NULL DEFAULT FALSE,
 created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_dict_item_code(org_id,dict_type_id,item_code),
 UNIQUE KEY uk_dict_item_org_id(org_id,id),
 KEY ix_dict_item_type_order(org_id,dict_type_id,sort_no,id),
 CONSTRAINT fk_dict_item_type FOREIGN KEY(org_id,dict_type_id) REFERENCES sys_dict_type(org_id,id),
 CONSTRAINT fk_dict_item_parent FOREIGN KEY(org_id,parent_id) REFERENCES sys_dict_item(org_id,id),
 CONSTRAINT ck_dict_item_status CHECK(status IN ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled)
SELECT x.code,x.kind,x.label,x.route,TRUE FROM (
 SELECT 'iam:dict:view' code,'MENU' kind,'数据字典查看' label,'/admin/dictionaries' route
 UNION ALL SELECT 'iam:dict:create','ACTION','创建数据字典',NULL
 UNION ALL SELECT 'iam:dict:update','ACTION','管理字典类型及选项',NULL
) x WHERE NOT EXISTS(SELECT 1 FROM sys_permission p WHERE p.permission_code=x.code);

INSERT INTO sys_role_permission(role_id,permission_id)
SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON p.permission_code LIKE 'iam:dict:%'
WHERE r.role_code='SYSTEM_ADMIN' AND r.enabled=TRUE
  AND NOT EXISTS (SELECT 1 FROM sys_role_permission rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,
 created_by,updated_by,permission_code,required_permissions)
SELECT 1,m.id,'nav_dictionaries','数据字典','/admin/dictionaries',45,'ACTIVE',1,1,'iam:dict:view',NULL
FROM sys_menu m WHERE m.org_id=1 AND m.menu_code='nav_system'
AND NOT EXISTS(SELECT 1 FROM sys_menu t WHERE t.org_id=1 AND t.menu_code='nav_dictionaries');

INSERT INTO sys_role_menu(org_id,role_id,menu_id,created_by)
SELECT m.org_id,r.id,m.id,1 FROM sys_role r CROSS JOIN sys_menu m
WHERE r.role_code='SYSTEM_ADMIN' AND r.enabled=TRUE AND m.menu_code='nav_dictionaries' AND m.org_id=1
AND NOT EXISTS(SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.id AND rm.menu_id=m.id AND rm.org_id=m.org_id);

INSERT INTO sys_menu_permission(menu_id,permission_id,created_by)
SELECT m.id,p.id,1 FROM sys_menu m JOIN sys_permission p ON p.permission_code LIKE 'iam:dict:%'
WHERE m.org_id=1 AND m.menu_code='nav_dictionaries'
AND NOT EXISTS(SELECT 1 FROM sys_menu_permission x WHERE x.menu_id=m.id AND x.permission_id=p.id);

-- Dictionaries are seeded from existing committed master-data codes, never from demo UI arrays.
INSERT INTO sys_dict_type(org_id,dict_code,dict_name,dict_kind,structure_type,description,sort_no,status,created_by,updated_by)
VALUES (1,'EQUIPMENT_TYPE','设备类型','BUSINESS','FLAT','设备主数据类型维护',10,'ACTIVE',1,1),
(1,'MATERIAL_TYPE','物料类型','BUSINESS','FLAT','物料主数据类型维护',20,'ACTIVE',1,1),
(1,'ORGANIZATION_TYPE','组织类型','SYSTEM','FLAT','组织层级类型维护',30,'ACTIVE',1,1);

INSERT INTO sys_dict_item(org_id,dict_type_id,item_code,item_label,sort_no,status,created_by,updated_by)
SELECT 1,t.id,code,code,10,'ACTIVE',1,1 FROM sys_dict_type t
JOIN (SELECT DISTINCT equipment_type AS code FROM md_equipment WHERE org_id=1 AND equipment_type IS NOT NULL AND equipment_type<>'') c
WHERE t.org_id=1 AND t.dict_code='EQUIPMENT_TYPE';

INSERT INTO sys_dict_item(org_id,dict_type_id,item_code,item_label,sort_no,status,created_by,updated_by)
SELECT 1,t.id,code,code,10,'ACTIVE',1,1 FROM sys_dict_type t
JOIN (SELECT DISTINCT material_type AS code FROM md_material WHERE org_id=1 AND material_type IS NOT NULL AND material_type<>'') c
WHERE t.org_id=1 AND t.dict_code='MATERIAL_TYPE';

INSERT INTO sys_dict_item(org_id,dict_type_id,item_code,item_label,sort_no,status,created_by,updated_by)
SELECT 1,t.id,code,code,10,'ACTIVE',1,1 FROM sys_dict_type t
JOIN (SELECT DISTINCT org_type AS code FROM md_organization WHERE org_id=1 AND org_type IS NOT NULL AND org_type<>'') c
WHERE t.org_id=1 AND t.dict_code='ORGANIZATION_TYPE';
