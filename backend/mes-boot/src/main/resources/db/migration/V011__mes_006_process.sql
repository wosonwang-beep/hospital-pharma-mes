CREATE TABLE md_product (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 product_code VARCHAR(64) NOT NULL,
 product_name VARCHAR(200) NOT NULL,
 dosage_form VARCHAR(100) NULL,
 specification VARCHAR(200) NULL,
 base_unit_id BIGINT NOT NULL,
 status VARCHAR(20) NOT NULL CHECK(status IN ('ACTIVE','INACTIVE')),
 UNIQUE KEY uk_product_code(org_id,product_code),
 KEY idx_product_status(org_id,status),
 FOREIGN KEY(base_unit_id) REFERENCES md_unit(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE proc_package (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 product_id BIGINT NOT NULL,
 package_code VARCHAR(64) NOT NULL,
 status VARCHAR(20) NOT NULL CHECK(status IN ('ACTIVE','INACTIVE')),
 UNIQUE KEY uk_package_code(org_id,package_code),
 KEY idx_package_product(org_id,product_id),
 FOREIGN KEY(product_id) REFERENCES md_product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE proc_package_version (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 package_id BIGINT NOT NULL,
 version INT NOT NULL,
 status VARCHAR(20) NOT NULL CHECK(status IN ('DRAFT','SUBMITTED','APPROVED','EFFECTIVE')),
 content_hash VARCHAR(128) NULL,
 effective_from DATETIME(3) NULL,
 UNIQUE KEY uk_process_version(org_id,package_id,version),
 draft_package_id BIGINT GENERATED ALWAYS AS (CASE WHEN status='DRAFT' THEN package_id ELSE NULL END) PERSISTENT,
 UNIQUE KEY uk_process_draft(org_id,draft_package_id),
 KEY idx_process_state(org_id,status,effective_from),
 FOREIGN KEY(package_id) REFERENCES proc_package(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE proc_formula_version (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 package_version_id BIGINT NOT NULL,
 formula_code VARCHAR(64) NOT NULL,
 version INT NOT NULL,
 batch_basis_qty DECIMAL(18,6) NOT NULL CHECK(batch_basis_qty>0),
 unit_id BIGINT NOT NULL,
 UNIQUE KEY uk_formula_package_version(org_id,package_version_id),
 FOREIGN KEY(package_version_id) REFERENCES proc_package_version(id),
 FOREIGN KEY(unit_id) REFERENCES md_unit(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE proc_formula_item (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 formula_version_id BIGINT NOT NULL,
 line_no INT NOT NULL CHECK(line_no>0),
 material_id BIGINT NOT NULL,
 required_qty DECIMAL(18,6) NOT NULL CHECK(required_qty>0),
 unit_id BIGINT NOT NULL,
 overage_pct DECIMAL(9,6) NULL CHECK(overage_pct>=0),
 critical BOOLEAN NOT NULL,
 UNIQUE KEY uk_formula_line(org_id,formula_version_id,line_no),
 FOREIGN KEY(formula_version_id) REFERENCES proc_formula_version(id),
 FOREIGN KEY(material_id) REFERENCES md_material(id),
 FOREIGN KEY(unit_id) REFERENCES md_unit(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE proc_route_version (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 package_version_id BIGINT NOT NULL,
 route_code VARCHAR(64) NOT NULL,
 version INT NOT NULL,
 UNIQUE KEY uk_route_package_version(org_id,package_version_id),
 FOREIGN KEY(package_version_id) REFERENCES proc_package_version(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE proc_operation_def (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 route_version_id BIGINT NOT NULL,
 operation_code VARCHAR(64) NOT NULL,
 operation_name VARCHAR(200) NOT NULL,
 sequence_no INT NOT NULL CHECK(sequence_no>0),
 required_role VARCHAR(64) NULL,
 completion_rule TEXT NULL CHECK(completion_rule IS NULL OR JSON_VALID(completion_rule)),
 predecessor_codes_json LONGTEXT NOT NULL CHECK(JSON_VALID(predecessor_codes_json)),
 required_equipment_type VARCHAR(64) NULL,
 clearance_required BOOLEAN NOT NULL DEFAULT FALSE,
 UNIQUE KEY uk_route_operation(org_id,route_version_id,operation_code),
 KEY idx_operation_sequence(org_id,route_version_id,sequence_no),
 FOREIGN KEY(route_version_id) REFERENCES proc_route_version(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE proc_parameter_def (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 operation_def_id BIGINT NOT NULL,
 parameter_code VARCHAR(64) NOT NULL,
 parameter_name VARCHAR(200) NOT NULL,
 acquisition_mode VARCHAR(20) NOT NULL CHECK(acquisition_mode IN ('MANUAL','AUTO','HYBRID')),
 unit_id BIGINT NULL,
 lower_limit DECIMAL(18,6) NULL,
 upper_limit DECIMAL(18,6) NULL,
 UNIQUE KEY uk_operation_parameter(org_id,operation_def_id,parameter_code),
 FOREIGN KEY(operation_def_id) REFERENCES proc_operation_def(id),
 FOREIGN KEY(unit_id) REFERENCES md_unit(id),
 CHECK(lower_limit IS NULL OR upper_limit IS NULL OR lower_limit<=upper_limit)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:product:view','ACTION','master:product:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:product:view');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:product:create','ACTION','master:product:create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:product:create');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'master:product:update','ACTION','master:product:update',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='master:product:update');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'process:package:view','ACTION','process:package:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='process:package:view');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'process:package:create','ACTION','process:package:create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='process:package:create');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'process:package:update','ACTION','process:package:update',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='process:package:update');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'process:package:edit','ACTION','process:package:edit',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='process:package:edit');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'process:package:submit','ACTION','process:package:submit',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='process:package:submit');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'process:package:approve','ACTION','process:package:approve',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='process:package:approve');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'process:package:publish','ACTION','process:package:publish',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='process:package:publish');
INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'master:product:view','产品','/process/products',206,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='master:product:view');
INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'process:package:view','工艺包','/process/packages',207,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='process:package:view');
INSERT INTO sys_role_permission(role_id,permission_id) SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON p.permission_code IN ('master:product:view','master:product:create','master:product:update','process:package:view','process:package:create','process:package:update','process:package:edit','process:package:submit','process:package:approve','process:package:publish') WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_permission rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
INSERT INTO sys_role_menu(org_id,role_id,menu_id,created_by) SELECT m.org_id,r.id,m.id,1 FROM sys_role r JOIN sys_menu m ON m.menu_code IN ('master:product:view','process:package:view') WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_menu rm WHERE rm.org_id=m.org_id AND rm.role_id=r.id AND rm.menu_id=m.id);