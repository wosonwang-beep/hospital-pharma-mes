-- MES-003 / DCP-MES-003-R2-001. Append-only physical V006 after DEV V005.
-- material_id physical FK belongs to LG-004; no early material table.
CREATE TABLE md_organization (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 parent_id BIGINT NULL, org_code VARCHAR(64) NOT NULL, org_name VARCHAR(200) NOT NULL,
 org_type VARCHAR(30) NOT NULL, status VARCHAR(20) NOT NULL,
 UNIQUE KEY uk_org_code (org_id,org_code), KEY idx_org_parent (org_id,parent_id), KEY idx_org_type (org_id,org_type),
 CONSTRAINT fk_org_parent FOREIGN KEY (parent_id) REFERENCES md_organization(id),
 CONSTRAINT ck_org_type CHECK (org_type IN ('ENTERPRISE','FACTORY','WORKSHOP','LINE')),
 CONSTRAINT ck_org_status CHECK (status IN ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE md_unit (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 unit_code VARCHAR(32) NOT NULL, unit_name VARCHAR(64) NOT NULL, dimension VARCHAR(30) NOT NULL, scale INT NOT NULL,
 UNIQUE KEY uk_unit_code (org_id,unit_code), KEY idx_unit_dimension (org_id,dimension), CONSTRAINT ck_unit_scale CHECK (scale BETWEEN 0 AND 12)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE md_unit_conversion (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 from_unit_id BIGINT NOT NULL, to_unit_id BIGINT NOT NULL, factor DECIMAL(24,12) NOT NULL, material_id BIGINT NULL,
 UNIQUE KEY uk_conversion_pair (org_id,from_unit_id,to_unit_id,material_id), KEY idx_conversion_material (org_id,material_id),
 CONSTRAINT fk_conversion_from FOREIGN KEY (from_unit_id) REFERENCES md_unit(id),
 CONSTRAINT fk_conversion_to FOREIGN KEY (to_unit_id) REFERENCES md_unit(id),
 CONSTRAINT ck_conversion_factor CHECK (factor>0), CONSTRAINT ck_conversion_distinct CHECK (from_unit_id<>to_unit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE md_equipment (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 equipment_code VARCHAR(64) NOT NULL, equipment_name VARCHAR(200) NOT NULL, equipment_type VARCHAR(64) NOT NULL,
 status VARCHAR(30) NOT NULL, calibration_due_date DATE NULL, location VARCHAR(200) NULL,
 UNIQUE KEY uk_equipment_code (org_id,equipment_code), KEY idx_equipment_type (org_id,equipment_type), KEY idx_equipment_status (org_id,status), KEY idx_equipment_due (org_id,calibration_due_date),
 CONSTRAINT ck_equipment_status CHECK (status IN ('ACTIVE','MAINTENANCE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE md_qualification (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 user_id BIGINT NOT NULL, qualification_code VARCHAR(64) NOT NULL, valid_from DATE NULL, valid_to DATE NULL, status VARCHAR(20) NOT NULL,
 KEY idx_qualification_user (org_id,user_id,qualification_code), KEY idx_qualification_status (org_id,status),
 CONSTRAINT fk_qualification_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
 CONSTRAINT ck_qualification_period CHECK (valid_from IS NULL OR valid_to IS NULL OR valid_from<=valid_to),
 CONSTRAINT ck_qualification_status CHECK (status IN ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELIMITER $$
CREATE TRIGGER md_conversion_generic_insert BEFORE INSERT ON md_unit_conversion FOR EACH ROW
BEGIN
 IF NEW.material_id IS NULL AND EXISTS (SELECT 1 FROM md_unit_conversion WHERE org_id=NEW.org_id AND from_unit_id=NEW.from_unit_id AND to_unit_id=NEW.to_unit_id AND material_id IS NULL) THEN
 SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Duplicate generic conversion';
 END IF;
END$$
CREATE TRIGGER md_conversion_generic_update BEFORE UPDATE ON md_unit_conversion FOR EACH ROW
BEGIN
 IF NEW.material_id IS NULL AND EXISTS (SELECT 1 FROM md_unit_conversion WHERE id<>NEW.id AND org_id=NEW.org_id AND from_unit_id=NEW.from_unit_id AND to_unit_id=NEW.to_unit_id AND material_id IS NULL) THEN
 SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Duplicate generic conversion';
 END IF;
END$$
DELIMITER ;
INSERT INTO sys_permission (permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:org:view','MENU','组织-view','/master/organizations',TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:org:view');
INSERT INTO sys_permission (permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:org:create','ACTION','组织-create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:org:create');
INSERT INTO sys_permission (permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:org:update','ACTION','组织-update',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:org:update');
INSERT INTO sys_menu (org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'master:org:view','组织','/master/organizations',200,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='master:org:view');
INSERT INTO sys_permission (permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:uom:view','MENU','单位与换算-view','/master/units',TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:uom:view');
INSERT INTO sys_permission (permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:uom:create','ACTION','单位与换算-create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:uom:create');
INSERT INTO sys_permission (permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:uom:update','ACTION','单位与换算-update',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:uom:update');
INSERT INTO sys_menu (org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'master:uom:view','单位与换算','/master/units',201,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='master:uom:view');
INSERT INTO sys_permission (permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:equipment:view','MENU','设备-view','/master/equipment',TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:equipment:view');
INSERT INTO sys_permission (permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:equipment:create','ACTION','设备-create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:equipment:create');
INSERT INTO sys_permission (permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:equipment:update','ACTION','设备-update',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:equipment:update');
INSERT INTO sys_menu (org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'master:equipment:view','设备','/master/equipment',202,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='master:equipment:view');
INSERT INTO sys_permission (permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:qualification:view','MENU','人员资格-view','/master/qualifications',TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:qualification:view');
INSERT INTO sys_permission (permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:qualification:create','ACTION','人员资格-create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:qualification:create');
INSERT INTO sys_permission (permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:qualification:update','ACTION','人员资格-update',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:qualification:update');
INSERT INTO sys_menu (org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'master:qualification:view','人员资格','/master/qualifications',203,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='master:qualification:view');
INSERT INTO sys_role_permission(role_id,permission_id)
SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON p.permission_code IN (
 'master:org:view','master:org:create','master:org:update','master:uom:view','master:uom:create','master:uom:update',
 'master:equipment:view','master:equipment:create','master:equipment:update','master:qualification:view','master:qualification:create','master:qualification:update')
WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_permission rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
INSERT INTO sys_role_menu(org_id,role_id,menu_id,created_by)
SELECT m.org_id,r.id,m.id,1 FROM sys_role r JOIN sys_menu m ON m.menu_code IN ('master:org:view','master:uom:view','master:equipment:view','master:qualification:view')
WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_menu rm WHERE rm.org_id=m.org_id AND rm.role_id=r.id AND rm.menu_id=m.id);
