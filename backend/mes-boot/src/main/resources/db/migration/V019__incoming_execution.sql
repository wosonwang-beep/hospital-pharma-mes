-- v1.0.11 actual execution/consumption facts. Existing history is unchanged.
CREATE TABLE mes_operation_execution (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 execution_unit_id BIGINT NOT NULL, operation_def_id BIGINT NOT NULL, operation_seq INT NOT NULL,
 status VARCHAR(30) NOT NULL CHECK(status IN ('PENDING','READY','IN_PROGRESS','PAUSED','COMPLETED','BLOCKED')),
 started_at DATETIME(3) NULL, completed_at DATETIME(3) NULL, operator_id BIGINT NULL,
 gate_evidence_json LONGTEXT NOT NULL CHECK(JSON_VALID(gate_evidence_json)),
 UNIQUE KEY uk_operation_unit_definition(org_id,execution_unit_id,operation_def_id), KEY idx_operation_state(org_id,status),
 FOREIGN KEY(execution_unit_id) REFERENCES prd_execution_unit(id), FOREIGN KEY(operation_def_id) REFERENCES proc_operation_def(id), FOREIGN KEY(operator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mes_equipment_usage (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 execution_unit_id BIGINT NOT NULL, operation_execution_id BIGINT NOT NULL, equipment_id BIGINT NOT NULL,
 usage_role VARCHAR(50) NOT NULL, clearance_status VARCHAR(30) NULL, qualification_status VARCHAR(30) NULL, bound_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_operation_equipment(org_id,operation_execution_id,equipment_id),
 FOREIGN KEY(execution_unit_id) REFERENCES prd_execution_unit(id), FOREIGN KEY(operation_execution_id) REFERENCES mes_operation_execution(id), FOREIGN KEY(equipment_id) REFERENCES md_equipment(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mes_equipment_run (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 equipment_usage_id BIGINT NOT NULL, run_no VARCHAR(100) NOT NULL, status VARCHAR(30) NOT NULL CHECK(status IN ('RUNNING','ENDED')),
 started_at DATETIME(3) NOT NULL, ended_at DATETIME(3) NULL, source_message_id VARCHAR(100) NULL,
 UNIQUE KEY uk_equipment_run(org_id,run_no), KEY idx_equipment_run_state(org_id,status),KEY idx_equipment_run_message(org_id,source_message_id),
 FOREIGN KEY(equipment_usage_id) REFERENCES mes_equipment_usage(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mes_parameter_value (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 operation_execution_id BIGINT NOT NULL, parameter_def_id BIGINT NOT NULL, source_mode VARCHAR(30) NOT NULL CHECK(source_mode IN ('MANUAL','DEVICE')),
 raw_value DECIMAL(24,8) NULL, text_value VARCHAR(1000) NULL, unit_id BIGINT NULL, equipment_id BIGINT NULL,
 source_message_id VARCHAR(100) NULL, captured_at DATETIME(3) NOT NULL,
 CHECK((raw_value IS NOT NULL AND text_value IS NULL) OR (raw_value IS NULL AND text_value IS NOT NULL)),
 CHECK(source_mode<>'DEVICE' OR (equipment_id IS NOT NULL AND source_message_id IS NOT NULL)),
 UNIQUE KEY uk_parameter_device_message(org_id,equipment_id,parameter_def_id,source_message_id), KEY idx_parameter_source(org_id,source_mode),KEY idx_parameter_captured(org_id,captured_at),
 FOREIGN KEY(operation_execution_id) REFERENCES mes_operation_execution(id), FOREIGN KEY(parameter_def_id) REFERENCES proc_parameter_def(id), FOREIGN KEY(unit_id) REFERENCES md_unit(id), FOREIGN KEY(equipment_id) REFERENCES md_equipment(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE ebr_form_instance ADD CONSTRAINT fk_ebr_form_operation FOREIGN KEY(operation_execution_id) REFERENCES mes_operation_execution(id);
ALTER TABLE qms_deviation ADD CONSTRAINT fk_deviation_operation FOREIGN KEY(operation_execution_id) REFERENCES mes_operation_execution(id);
CREATE TRIGGER trg_mes_parameter_value_update BEFORE UPDATE ON mes_parameter_value FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Immutable regulated evidence';
CREATE TRIGGER trg_mes_parameter_value_delete BEFORE DELETE ON mes_parameter_value FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Immutable regulated evidence';
