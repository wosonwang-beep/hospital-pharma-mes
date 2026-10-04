CREATE TABLE md_material (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 material_code VARCHAR(50) NOT NULL,
 material_name VARCHAR(200) NOT NULL,
 generic_name VARCHAR(200) NULL,
 english_name VARCHAR(200) NULL,
 alias_name VARCHAR(200) NULL,
 material_type VARCHAR(30) NOT NULL,
 specification VARCHAR(200) NULL,
 grade_purity VARCHAR(100) NULL,
 appearance VARCHAR(500) NULL,
 base_unit_id BIGINT NOT NULL,
 CONSTRAINT fk_md_material_base_unit_id FOREIGN KEY (base_unit_id) REFERENCES md_unit(id),
 pack_spec VARCHAR(200) NULL,
 pack_unit_id BIGINT NULL,
 CONSTRAINT fk_md_material_pack_unit_id FOREIGN KEY (pack_unit_id) REFERENCES md_unit(id),
 manufacturer_name VARCHAR(200) NULL,
 quality_standard_code VARCHAR(100) NULL,
 storage_condition VARCHAR(500) NULL,
 shelf_life_days INT NULL,
 retest_period_days INT NULL,
 lot_controlled TINYINT(1) NOT NULL,
 sampling_required TINYINT(1) NOT NULL,
 inspection_required TINYINT(1) NOT NULL,
 release_required TINYINT(1) NOT NULL,
 weighing_required TINYINT(1) NOT NULL,
 critical_material TINYINT(1) NOT NULL,
 weighing_precision DECIMAL(18,6) NULL,
 weighing_tolerance_pct DECIMAL(9,6) NULL,
 special_control_type VARCHAR(50) NULL,
 status VARCHAR(20) NOT NULL,
 effective_from DATETIME(3) NULL,
 effective_to DATETIME(3) NULL,
 remark VARCHAR(1000) NULL,
 UNIQUE KEY uk_material_code(org_id,material_code),
 CONSTRAINT ck_material_state CHECK(status IN ('DRAFT','APPROVED','INACTIVE')),
 CONSTRAINT ck_material_period CHECK(effective_from IS NULL OR effective_to IS NULL OR effective_from<=effective_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE md_material_version (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 material_id BIGINT NOT NULL,
 CONSTRAINT fk_md_material_version_material_id FOREIGN KEY (material_id) REFERENCES md_material(id),
 version_no_business INT NOT NULL,
 status VARCHAR(20) NOT NULL,
 effective_from DATETIME(3) NULL,
 effective_to DATETIME(3) NULL,
 content_hash VARCHAR(128) NOT NULL,
 approved_by BIGINT NULL,
 CONSTRAINT fk_md_material_version_approved_by FOREIGN KEY (approved_by) REFERENCES sys_user(id),
 approved_at DATETIME(3) NULL,
 requires_incoming_inspection TINYINT(1) NOT NULL DEFAULT 1,
 UNIQUE KEY uk_material_business_version(org_id,material_id,version_no_business),
 CONSTRAINT ck_material_version_state CHECK(status IN ('DRAFT','SUBMITTED','APPROVED')),
 CONSTRAINT ck_material_version_period CHECK(effective_from IS NULL OR effective_to IS NULL OR effective_from<=effective_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE md_material_quality_spec (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 material_version_id BIGINT NOT NULL,
 CONSTRAINT fk_md_material_quality_spec_material_version_id FOREIGN KEY (material_version_id) REFERENCES md_material_version(id),
 standard_code VARCHAR(100) NOT NULL,
 sampling_required TINYINT(1) NOT NULL,
 inspection_required TINYINT(1) NOT NULL,
 release_required TINYINT(1) NOT NULL,
 spec_json LONGTEXT NOT NULL,
 UNIQUE KEY uk_md_material_quality_spec_version(org_id,material_version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE md_material_storage_rule (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 material_version_id BIGINT NOT NULL,
 CONSTRAINT fk_md_material_storage_rule_material_version_id FOREIGN KEY (material_version_id) REFERENCES md_material_version(id),
 storage_condition VARCHAR(500) NULL,
 temperature_min DECIMAL(9,3) NULL,
 temperature_max DECIMAL(9,3) NULL,
 humidity_min DECIMAL(9,3) NULL,
 humidity_max DECIMAL(9,3) NULL,
 shelf_life_days INT NULL,
 retest_period_days INT NULL,
 fefo_required TINYINT(1) NOT NULL,
 UNIQUE KEY uk_md_material_storage_rule_version(org_id,material_version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE md_material_production_rule (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 material_version_id BIGINT NOT NULL,
 CONSTRAINT fk_md_material_production_rule_material_version_id FOREIGN KEY (material_version_id) REFERENCES md_material_version(id),
 weighing_required TINYINT(1) NOT NULL,
 critical_material TINYINT(1) NOT NULL,
 weighing_precision DECIMAL(18,6) NULL,
 weighing_tolerance_pct DECIMAL(9,6) NULL,
 special_control_type VARCHAR(50) NULL,
 UNIQUE KEY uk_md_material_production_rule_version(org_id,material_version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE md_unit_conversion ADD CONSTRAINT fk_conversion_material FOREIGN KEY(material_id) REFERENCES md_material(id);
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:material:view','MENU','物料主数据-view','/master/materials',TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:material:view');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:material:create','ACTION','物料主数据-create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:material:create');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:material:update','ACTION','物料主数据-update',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:material:update');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:material:submit','ACTION','物料主数据-submit',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:material:submit');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:material:approve','ACTION','物料主数据-approve',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:material:approve');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:material:disable','ACTION','物料主数据-disable',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:material:disable');
INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'master:material:view','物料主数据','/master/materials',204,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='master:material:view');
INSERT INTO sys_role_permission(role_id,permission_id) SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON p.permission_code IN ('master:material:view','master:material:create','master:material:update','master:material:submit','master:material:approve','master:material:disable') WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_permission rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
INSERT INTO sys_role_menu(org_id,role_id,menu_id,created_by) SELECT m.org_id,r.id,m.id,1 FROM sys_role r JOIN sys_menu m ON m.menu_code='master:material:view' WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_menu rm WHERE rm.org_id=m.org_id AND rm.role_id=r.id AND rm.menu_id=m.id);
