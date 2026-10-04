CREATE TABLE md_supplier (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 supplier_code VARCHAR(64) NOT NULL,
 supplier_name VARCHAR(200) NOT NULL,
 qualification_status VARCHAR(30) NOT NULL,
 valid_to DATE NULL,
 UNIQUE KEY uk_supplier_code(org_id,supplier_code),
 CONSTRAINT ck_supplier_state CHECK(qualification_status IN ('UNAPPROVED','APPROVED','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE md_material_supplier (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 material_id BIGINT NOT NULL,
 CONSTRAINT fk_md_material_supplier_material_id FOREIGN KEY (material_id) REFERENCES md_material(id),
 supplier_id BIGINT NOT NULL,
 CONSTRAINT fk_md_material_supplier_supplier_id FOREIGN KEY (supplier_id) REFERENCES md_supplier(id),
 approved TINYINT(1) NOT NULL,
 valid_to DATE NULL,
 UNIQUE KEY uk_material_supplier(org_id,material_id,supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:supplier:view','MENU','供应商-view','/master/suppliers',TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:supplier:view');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:supplier:create','ACTION','供应商-create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:supplier:create');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:supplier:update','ACTION','供应商-update',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:supplier:update');
INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'master:supplier:view','供应商','/master/suppliers',205,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='master:supplier:view');
INSERT INTO sys_role_permission(role_id,permission_id) SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON p.permission_code IN ('master:supplier:view','master:supplier:create','master:supplier:update') WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_permission rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
INSERT INTO sys_role_menu(org_id,role_id,menu_id,created_by) SELECT m.org_id,r.id,m.id,1 FROM sys_role r JOIN sys_menu m ON m.menu_code='master:supplier:view' WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_menu rm WHERE rm.org_id=m.org_id AND rm.role_id=r.id AND rm.menu_id=m.id);
