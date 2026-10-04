-- v1.0.11 actual execution/consumption facts. Existing history is unchanged.
CREATE TABLE mes_weighing_record (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 execution_unit_id BIGINT NOT NULL, material_lot_id BIGINT NOT NULL, bom_item_id BIGINT NOT NULL,
 target_qty DECIMAL(18,6) NOT NULL CHECK(target_qty>0), actual_qty DECIMAL(18,6) NOT NULL CHECK(actual_qty>0), unit_id BIGINT NOT NULL,
 weighed_by BIGINT NOT NULL, verified_by BIGINT NULL, status VARCHAR(20) NOT NULL CHECK(status IN ('DRAFT','CONFIRMED','VERIFIED')),
 scale_equipment_id BIGINT NOT NULL, equipment_evidence_json LONGTEXT NOT NULL CHECK(JSON_VALID(equipment_evidence_json)),
 gate_evidence_json LONGTEXT NOT NULL CHECK(JSON_VALID(gate_evidence_json)), signature_evidence_json LONGTEXT NOT NULL CHECK(JSON_VALID(signature_evidence_json)),
 KEY idx_weighing_execution(org_id,execution_unit_id),
 FOREIGN KEY(execution_unit_id) REFERENCES prd_execution_unit(id),FOREIGN KEY(material_lot_id) REFERENCES md_material_lot(id),FOREIGN KEY(bom_item_id) REFERENCES proc_formula_item(id),
 FOREIGN KEY(unit_id) REFERENCES md_unit(id),FOREIGN KEY(weighed_by) REFERENCES sys_user(id),FOREIGN KEY(verified_by) REFERENCES sys_user(id),FOREIGN KEY(scale_equipment_id) REFERENCES md_equipment(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mes_material_charge (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 execution_unit_id BIGINT NOT NULL, operation_execution_id BIGINT NOT NULL, material_lot_id BIGINT NOT NULL, weighing_record_id BIGINT NULL,
 charged_qty DECIMAL(18,6) NOT NULL CHECK(charged_qty>0), unit_id BIGINT NOT NULL, charged_by BIGINT NOT NULL, verified_by BIGINT NULL, charged_at DATETIME(3) NOT NULL,
 status VARCHAR(20) NOT NULL CHECK(status IN ('CONFIRMED','REVERSED')),
 gate_evidence_json LONGTEXT NOT NULL CHECK(JSON_VALID(gate_evidence_json)), signature_evidence_json LONGTEXT NOT NULL CHECK(JSON_VALID(signature_evidence_json)),
 KEY idx_charge_execution(org_id,execution_unit_id),KEY idx_charge_lot(org_id,material_lot_id), KEY idx_charge_time(org_id,charged_at),
 FOREIGN KEY(execution_unit_id) REFERENCES prd_execution_unit(id),FOREIGN KEY(operation_execution_id) REFERENCES mes_operation_execution(id),FOREIGN KEY(material_lot_id) REFERENCES md_material_lot(id),
 FOREIGN KEY(weighing_record_id) REFERENCES mes_weighing_record(id),FOREIGN KEY(unit_id) REFERENCES md_unit(id),FOREIGN KEY(charged_by) REFERENCES sys_user(id),FOREIGN KEY(verified_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mes_quantity_event (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 main_batch_id BIGINT NOT NULL, execution_unit_id BIGINT NULL, operation_execution_id BIGINT NULL, event_type VARCHAR(30) NOT NULL,
 material_lot_id BIGINT NULL, amount DECIMAL(18,6) NOT NULL, unit_id BIGINT NOT NULL, source_type VARCHAR(50) NOT NULL, source_ref VARCHAR(100) NOT NULL, occurred_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_quantity_source(org_id,event_type,source_type,source_ref),KEY idx_quantity_batch(org_id,main_batch_id),
 FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id),FOREIGN KEY(execution_unit_id) REFERENCES prd_execution_unit(id),FOREIGN KEY(operation_execution_id) REFERENCES mes_operation_execution(id),FOREIGN KEY(material_lot_id) REFERENCES md_material_lot(id),FOREIGN KEY(unit_id) REFERENCES md_unit(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mes_genealogy (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 main_batch_id BIGINT NOT NULL,input_material_lot_id BIGINT NOT NULL,charge_id BIGINT NOT NULL,output_lot_id BIGINT NULL,relation_type VARCHAR(30) NOT NULL,
 UNIQUE KEY uk_genealogy_charge(org_id,charge_id),KEY idx_genealogy_input(org_id,input_material_lot_id),
 FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id),FOREIGN KEY(input_material_lot_id) REFERENCES md_material_lot(id),FOREIGN KEY(charge_id) REFERENCES mes_material_charge(id),FOREIGN KEY(output_lot_id) REFERENCES md_material_lot(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TRIGGER trg_mes_quantity_event_update BEFORE UPDATE ON mes_quantity_event FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Immutable regulated evidence';
CREATE TRIGGER trg_mes_quantity_event_delete BEFORE DELETE ON mes_quantity_event FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Immutable regulated evidence';
CREATE TRIGGER trg_mes_genealogy_update BEFORE UPDATE ON mes_genealogy FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Immutable regulated evidence';
CREATE TRIGGER trg_mes_genealogy_delete BEFORE DELETE ON mes_genealogy FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Immutable regulated evidence';
