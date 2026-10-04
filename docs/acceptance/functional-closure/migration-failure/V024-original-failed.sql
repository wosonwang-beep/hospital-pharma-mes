-- DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 explicitly approved 2026-10-04. Append-only V024.

ALTER TABLE proc_operation_def ADD ipc_definitions_json LONGTEXT NOT NULL DEFAULT ('[]') CHECK(JSON_VALID(ipc_definitions_json));

CREATE TABLE wms_material_lot_inventory_decision (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 material_lot_id BIGINT NOT NULL,
 FOREIGN KEY(material_lot_id) REFERENCES wms_material_lot(id),
 previous_decision_id BIGINT NULL,
 FOREIGN KEY(previous_decision_id) REFERENCES wms_material_lot_inventory_decision(id),
 action VARCHAR(30) NOT NULL,
 previous_inventory_status VARCHAR(30) NOT NULL,
 resulting_inventory_status VARCHAR(30) NOT NULL,
 reason VARCHAR(1000) NOT NULL,
 decided_by BIGINT NOT NULL,
 FOREIGN KEY(decided_by) REFERENCES sys_user(id),
 decided_at DATETIME(3) NOT NULL,
 signature_id BIGINT NOT NULL,
 FOREIGN KEY(signature_id) REFERENCES gxp_signature(id),
 signature_evidence_json LONGTEXT NOT NULL,
 CHECK(JSON_VALID(signature_evidence_json)),
 KEY ix_wms_material_lot_inventory_decision_root(org_id,material_lot_id,id),
 UNIQUE KEY uk_inventory_predecessor(org_id,previous_decision_id),
 CHECK(action IN ('FREEZE','UNFREEZE')),
 CHECK((action='FREEZE' AND previous_inventory_status='AVAILABLE' AND resulting_inventory_status='FROZEN') OR (action='UNFREEZE' AND previous_inventory_status='FROZEN' AND resulting_inventory_status='AVAILABLE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mes_clearance_record (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 operation_execution_id BIGINT NOT NULL,
 FOREIGN KEY(operation_execution_id) REFERENCES mes_operation_execution(id),
 equipment_usage_id BIGINT NULL,
 FOREIGN KEY(equipment_usage_id) REFERENCES mes_equipment_usage(id),
 previous_record_id BIGINT NULL,
 FOREIGN KEY(previous_record_id) REFERENCES mes_clearance_record(id),
 outcome VARCHAR(30) NOT NULL,
 reason VARCHAR(1000) NOT NULL,
 performed_by BIGINT NOT NULL,
 FOREIGN KEY(performed_by) REFERENCES sys_user(id),
 performed_at DATETIME(3) NOT NULL,
 signature_id BIGINT NOT NULL,
 FOREIGN KEY(signature_id) REFERENCES gxp_signature(id),
 signature_evidence_json LONGTEXT NOT NULL,
 CHECK(JSON_VALID(signature_evidence_json)),
 KEY ix_mes_clearance_record_root(org_id,operation_execution_id,id),
 UNIQUE KEY uk_clearance_predecessor(org_id,previous_record_id),
 CHECK(outcome IN ('PASS','FAIL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mes_clearance_review (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 clearance_record_id BIGINT NOT NULL,
 FOREIGN KEY(clearance_record_id) REFERENCES mes_clearance_record(id),
 decision VARCHAR(30) NOT NULL,
 reason VARCHAR(1000) NOT NULL,
 reviewed_by BIGINT NOT NULL,
 FOREIGN KEY(reviewed_by) REFERENCES sys_user(id),
 reviewed_at DATETIME(3) NOT NULL,
 signature_id BIGINT NOT NULL,
 FOREIGN KEY(signature_id) REFERENCES gxp_signature(id),
 signature_evidence_json LONGTEXT NOT NULL,
 CHECK(JSON_VALID(signature_evidence_json)),
 KEY ix_mes_clearance_review_root(org_id,clearance_record_id,id),
 UNIQUE KEY uk_clearance_review(org_id,clearance_record_id),
 CHECK(decision IN ('APPROVED','REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qms_ipc_instance (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 operation_execution_id BIGINT NOT NULL,
 FOREIGN KEY(operation_execution_id) REFERENCES mes_operation_execution(id),
 ipc_code VARCHAR(64) NOT NULL,
 status VARCHAR(30) NOT NULL,
 result VARCHAR(30) NULL,
 completed_at DATETIME(3) NULL,
 definition_snapshot_json LONGTEXT NOT NULL,
 CHECK(JSON_VALID(definition_snapshot_json)),
 current_result_revision_id BIGINT NULL,
 KEY ix_qms_ipc_instance_root(org_id,operation_execution_id,id),
 UNIQUE KEY uk_ipc_operation_code(org_id,operation_execution_id,ipc_code),
 CHECK(status IN ('PENDING','TESTING','COMPLETED')),
 CHECK(result IS NULL OR result IN ('PASS','FAIL','INVALID'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qms_ipc_result_revision (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 ipc_instance_id BIGINT NOT NULL,
 FOREIGN KEY(ipc_instance_id) REFERENCES qms_ipc_instance(id),
 revision_no INT NOT NULL,
 previous_revision_id BIGINT NULL,
 FOREIGN KEY(previous_revision_id) REFERENCES qms_ipc_result_revision(id),
 result_numeric DECIMAL(24,8) NULL,
 result_text VARCHAR(1000) NULL,
 result_conclusion VARCHAR(30) NOT NULL,
 definition_snapshot_json LONGTEXT NOT NULL,
 CHECK(JSON_VALID(definition_snapshot_json)),
 reason_for_change VARCHAR(1000) NULL,
 recorded_by BIGINT NOT NULL,
 FOREIGN KEY(recorded_by) REFERENCES sys_user(id),
 recorded_at DATETIME(3) NOT NULL,
 signature_id BIGINT NOT NULL,
 FOREIGN KEY(signature_id) REFERENCES gxp_signature(id),
 signature_evidence_json LONGTEXT NOT NULL,
 CHECK(JSON_VALID(signature_evidence_json)),
 KEY ix_qms_ipc_result_revision_root(org_id,ipc_instance_id,id),
 UNIQUE KEY uk_ipc_revision(org_id,ipc_instance_id,revision_no),
 UNIQUE KEY uk_ipc_predecessor(org_id,previous_revision_id),
 CHECK(result_conclusion IN ('PASS','FAIL','INVALID')),
 CHECK(revision_no>0),
 CHECK((revision_no=1 AND previous_revision_id IS NULL) OR (revision_no>1 AND previous_revision_id IS NOT NULL AND reason_for_change IS NOT NULL)),
 CHECK((result_numeric IS NULL) <> (result_text IS NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qms_ipc_review (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 ipc_result_revision_id BIGINT NOT NULL,
 FOREIGN KEY(ipc_result_revision_id) REFERENCES qms_ipc_result_revision(id),
 disposition VARCHAR(30) NOT NULL,
 reason VARCHAR(1000) NOT NULL,
 reviewed_by BIGINT NOT NULL,
 FOREIGN KEY(reviewed_by) REFERENCES sys_user(id),
 reviewed_at DATETIME(3) NOT NULL,
 signature_id BIGINT NOT NULL,
 FOREIGN KEY(signature_id) REFERENCES gxp_signature(id),
 signature_evidence_json LONGTEXT NOT NULL,
 CHECK(JSON_VALID(signature_evidence_json)),
 KEY ix_qms_ipc_review_root(org_id,ipc_result_revision_id,id),
 UNIQUE KEY uk_ipc_review(org_id,ipc_result_revision_id),
 CHECK(disposition IN ('CONFIRMED','INVALIDATED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE qms_ipc_instance ADD FOREIGN KEY(current_result_revision_id) REFERENCES qms_ipc_result_revision(id);

DELIMITER $$

CREATE TRIGGER bu_wms_material_lot_inventory_decision BEFORE UPDATE ON wms_material_lot_inventory_decision FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable controlled fact'$$

CREATE TRIGGER bd_wms_material_lot_inventory_decision BEFORE DELETE ON wms_material_lot_inventory_decision FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Controlled fact deletion prohibited'$$

CREATE TRIGGER bu_mes_clearance_record BEFORE UPDATE ON mes_clearance_record FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable controlled fact'$$

CREATE TRIGGER bd_mes_clearance_record BEFORE DELETE ON mes_clearance_record FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Controlled fact deletion prohibited'$$

CREATE TRIGGER bu_mes_clearance_review BEFORE UPDATE ON mes_clearance_review FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable controlled fact'$$

CREATE TRIGGER bd_mes_clearance_review BEFORE DELETE ON mes_clearance_review FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Controlled fact deletion prohibited'$$

CREATE TRIGGER bd_qms_ipc_instance BEFORE DELETE ON qms_ipc_instance FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Controlled fact deletion prohibited'$$

CREATE TRIGGER bu_qms_ipc_result_revision BEFORE UPDATE ON qms_ipc_result_revision FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable controlled fact'$$

CREATE TRIGGER bd_qms_ipc_result_revision BEFORE DELETE ON qms_ipc_result_revision FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Controlled fact deletion prohibited'$$

CREATE TRIGGER bu_qms_ipc_review BEFORE UPDATE ON qms_ipc_review FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable controlled fact'$$

CREATE TRIGGER bd_qms_ipc_review BEFORE DELETE ON qms_ipc_review FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Controlled fact deletion prohibited'$$

DELIMITER ;

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qa:material-inventory:freeze','ACTION','qa:material-inventory:freeze',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qa:material-inventory:freeze');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qa:material-inventory:unfreeze','ACTION','qa:material-inventory:unfreeze',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qa:material-inventory:unfreeze');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:ipc:view','ACTION','qms:ipc:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:ipc:view');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:ipc:record','ACTION','qms:ipc:record',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:ipc:record');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:ipc:review','ACTION','qms:ipc:review',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:ipc:review');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'mes:clearance:record','ACTION','mes:clearance:record',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='mes:clearance:record');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'mes:clearance:review','ACTION','mes:clearance:review',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='mes:clearance:review');
