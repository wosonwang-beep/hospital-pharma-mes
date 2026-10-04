-- DCP-MES-007-008-SEQUENCING-001; append-only physical V013.
-- main_batch_id foreign keys deferred to LG-009A; all dependent writes fail closed.
CREATE TABLE wms_warehouse (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 warehouse_code VARCHAR(64) NOT NULL,
 warehouse_name VARCHAR(200) NOT NULL,
 warehouse_type VARCHAR(30) NOT NULL,
 status VARCHAR(20) NOT NULL,
 CHECK(status IN ('ACTIVE','INACTIVE')),
 UNIQUE KEY uk_warehouse(org_id,warehouse_code),
 KEY idx_warehouse_type(org_id,warehouse_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE wms_location (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 warehouse_id BIGINT NOT NULL,
 FOREIGN KEY(warehouse_id) REFERENCES wms_warehouse(id),
 location_code VARCHAR(64) NOT NULL,
 location_name VARCHAR(200) NULL,
 status VARCHAR(20) NOT NULL,
 CHECK(status IN ('ACTIVE','INACTIVE')),
 UNIQUE KEY uk_location(org_id,warehouse_id,location_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE wms_container (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 container_code VARCHAR(100) NOT NULL,
 container_type VARCHAR(30) NULL,
 status VARCHAR(20) NOT NULL,
 CHECK(status IN ('ACTIVE','INACTIVE')),
 UNIQUE KEY uk_container(org_id,container_code),
 KEY idx_container_status(org_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE wms_material_receipt (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 receipt_no VARCHAR(80) NOT NULL,
 supplier_id BIGINT NOT NULL,
 FOREIGN KEY(supplier_id) REFERENCES md_supplier(id),
 purchase_order_no VARCHAR(100) NULL,
 delivery_note_no VARCHAR(100) NULL,
 warehouse_id BIGINT NOT NULL,
 FOREIGN KEY(warehouse_id) REFERENCES wms_warehouse(id),
 transport_check_passed TINYINT(1) NOT NULL,
 record_status VARCHAR(20) NOT NULL,
 received_by BIGINT NOT NULL,
 received_at DATETIME(3) NOT NULL,
 confirmed_by BIGINT NULL,
 confirmed_at DATETIME(3) NULL,
 UNIQUE KEY uk_receipt(org_id,receipt_no),
 CHECK(record_status IN ('DRAFT','APPROVED')),
 KEY idx_receipt_status(org_id,record_status),
 KEY idx_receipt_supplier(org_id,supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE wms_material_receipt_item (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 receipt_id BIGINT NOT NULL,
 FOREIGN KEY(receipt_id) REFERENCES wms_material_receipt(id),
 material_id BIGINT NOT NULL,
 FOREIGN KEY(material_id) REFERENCES md_material(id),
 lot_no VARCHAR(100) NOT NULL,
 supplier_lot_no VARCHAR(100) NULL,
 manufacturer_lot_no VARCHAR(100) NULL,
 manufacture_date DATE NULL,
 expiry_date DATE NULL,
 retest_date DATE NULL,
 received_qty DECIMAL(18,6) NOT NULL,
 unit_id BIGINT NOT NULL,
 FOREIGN KEY(unit_id) REFERENCES md_unit(id),
 package_spec VARCHAR(200) NULL,
 package_count INT NOT NULL,
 location_id BIGINT NOT NULL,
 FOREIGN KEY(location_id) REFERENCES wms_location(id),
 container_id BIGINT NULL,
 FOREIGN KEY(container_id) REFERENCES wms_container(id),
 package_check_passed TINYINT(1) NOT NULL,
 seal_check_passed TINYINT(1) NOT NULL,
 label_check_passed TINYINT(1) NOT NULL,
 damage_check_passed TINYINT(1) NOT NULL,
 contamination_check_passed TINYINT(1) NOT NULL,
 material_snapshot_json LONGTEXT NOT NULL,
 requires_incoming_inspection_snapshot TINYINT(1) NOT NULL,
 CHECK(received_qty>0 AND package_count>0),
 KEY idx_receipt_items(org_id,receipt_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE md_material_lot (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 material_id BIGINT NOT NULL,
 FOREIGN KEY(material_id) REFERENCES md_material(id),
 lot_no VARCHAR(100) NOT NULL,
 supplier_lot_no VARCHAR(100) NULL,
 manufacture_date DATE NULL,
 expiry_date DATE NULL,
 retest_date DATE NULL,
 receipt_item_id BIGINT NULL,
 FOREIGN KEY(receipt_item_id) REFERENCES wms_material_receipt_item(id),
 material_snapshot_json LONGTEXT NOT NULL,
 requires_incoming_inspection_snapshot TINYINT(1) NOT NULL,
 quality_status VARCHAR(30) NOT NULL,
 inventory_status VARCHAR(30) NOT NULL,
 UNIQUE KEY uk_materiallot(org_id,lot_no),
 UNIQUE KEY uk_lot_receipt_item(receipt_item_id),
 CHECK(inventory_status IN ('BLOCKED','AVAILABLE','FROZEN')),
 CHECK(quality_status IN ('QUARANTINE','PENDING_SAMPLING','SAMPLING','SAMPLED','TESTING','PENDING_QC_REVIEW','QC_PASSED','QC_FAILED','PENDING_QA_RELEASE','PENDING_DISPOSITION','RELEASED','REJECTED')),
 KEY idx_lot_quality(org_id,quality_status),
 KEY idx_lot_expiry(org_id,expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE wms_inventory_ledger (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 material_lot_id BIGINT NOT NULL,
 FOREIGN KEY(material_lot_id) REFERENCES md_material_lot(id),
 location_id BIGINT NOT NULL,
 FOREIGN KEY(location_id) REFERENCES wms_location(id),
 container_id BIGINT NULL,
 FOREIGN KEY(container_id) REFERENCES wms_container(id),
 event_type VARCHAR(30) NOT NULL,
 delta_qty DECIMAL(18,6) NOT NULL,
 unit_id BIGINT NOT NULL,
 FOREIGN KEY(unit_id) REFERENCES md_unit(id),
 source_type VARCHAR(50) NOT NULL,
 source_ref VARCHAR(100) NOT NULL,
 idempotency_key VARCHAR(100) NOT NULL,
 occurred_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_ledger(org_id,idempotency_key),
 CHECK(delta_qty<>0),
 KEY idx_ledger_lot(org_id,material_lot_id,location_id,container_id),
 KEY idx_ledger_event(event_type),
 KEY idx_ledger_source(source_ref),
 KEY idx_ledger_time(occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE wms_reservation (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 main_batch_id BIGINT NOT NULL,
 formula_item_id BIGINT NOT NULL,
 FOREIGN KEY(formula_item_id) REFERENCES proc_formula_item(id),
 material_lot_id BIGINT NULL,
 FOREIGN KEY(material_lot_id) REFERENCES md_material_lot(id),
 reserved_qty DECIMAL(18,6) NOT NULL,
 unit_id BIGINT NOT NULL,
 FOREIGN KEY(unit_id) REFERENCES md_unit(id),
 status VARCHAR(20) NOT NULL,
 CHECK(status IN ('ACTIVE','CONSUMED','RELEASED')),
 CHECK(reserved_qty>0),
 KEY idx_reservation_batch(org_id,main_batch_id),
 KEY idx_reservation_status(org_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE wms_material_issue (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 main_batch_id BIGINT NOT NULL,
 issue_no VARCHAR(80) NOT NULL,
 status VARCHAR(20) NOT NULL,
 issued_at DATETIME(3) NULL,
 UNIQUE KEY uk_materialissue(org_id,issue_no),
 CHECK(status IN ('DRAFT','CONFIRMED','CLOSED')),
 KEY idx_issue_batch(org_id,main_batch_id),
 KEY idx_issue_status(org_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE wms_material_issue_item (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 issue_id BIGINT NOT NULL,
 FOREIGN KEY(issue_id) REFERENCES wms_material_issue(id),
 material_lot_id BIGINT NOT NULL,
 FOREIGN KEY(material_lot_id) REFERENCES md_material_lot(id),
 formula_item_id BIGINT NULL,
 FOREIGN KEY(formula_item_id) REFERENCES proc_formula_item(id),
 issued_qty DECIMAL(18,6) NOT NULL,
 unit_id BIGINT NOT NULL,
 FOREIGN KEY(unit_id) REFERENCES md_unit(id),
 CHECK(issued_qty>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TRIGGER trg_wms_ledger_no_update BEFORE UPDATE ON wms_inventory_ledger FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='wms_inventory_ledger is append-only';
CREATE TRIGGER trg_wms_ledger_no_delete BEFORE DELETE ON wms_inventory_ledger FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='wms_inventory_ledger is append-only';

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:inventory:adjust','ACTION','wms:inventory:adjust',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:inventory:adjust');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:inventory:create','ACTION','wms:inventory:create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:inventory:create');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:inventory:move','ACTION','wms:inventory:move',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:inventory:move');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:inventory:update','ACTION','wms:inventory:update',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:inventory:update');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:inventory:view','ACTION','wms:inventory:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:inventory:view');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:issue:confirm','ACTION','wms:issue:confirm',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:issue:confirm');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:issue:create','ACTION','wms:issue:create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:issue:create');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:issue:return','ACTION','wms:issue:return',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:issue:return');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:issue:update','ACTION','wms:issue:update',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:issue:update');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:issue:view','ACTION','wms:issue:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:issue:view');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:receipt:confirm','ACTION','wms:receipt:confirm',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:receipt:confirm');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:receipt:create','ACTION','wms:receipt:create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:receipt:create');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:receipt:update','ACTION','wms:receipt:update',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:receipt:update');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:receipt:view','ACTION','wms:receipt:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:receipt:view');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:reservation:create','ACTION','wms:reservation:create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:reservation:create');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:reservation:view','ACTION','wms:reservation:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:reservation:view');
INSERT INTO sys_role_permission(role_id,permission_id) SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON p.permission_code IN ('wms:inventory:adjust','wms:inventory:create','wms:inventory:move','wms:inventory:update','wms:inventory:view','wms:issue:confirm','wms:issue:create','wms:issue:return','wms:issue:update','wms:issue:view','wms:receipt:confirm','wms:receipt:create','wms:receipt:update','wms:receipt:view','wms:reservation:create','wms:reservation:view') WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_permission rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'wms:receipt:view','物料收货','/wms/receipts',220,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='wms:receipt:view');
INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'wms:issue:view','发退料','/wms/issues',222,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='wms:issue:view');
INSERT INTO sys_role_menu(org_id,role_id,menu_id,created_by) SELECT m.org_id,r.id,m.id,1 FROM sys_role r JOIN sys_menu m ON m.menu_code IN ('wms:receipt:view','wms:issue:view') WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_menu rm WHERE rm.org_id=m.org_id AND rm.role_id=r.id AND rm.menu_id=m.id);
