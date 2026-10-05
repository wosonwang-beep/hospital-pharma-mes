-- MES-012: DCP-MES-012-013-CONTRACT-001, reviewed v1.0.16. Prior migrations remain immutable.
CREATE TABLE qms_production_plan (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 main_batch_id BIGINT NOT NULL, finished_material_id BIGINT NOT NULL, qc_specification_version_id BIGINT NOT NULL,
 plan_json LONGTEXT NOT NULL CHECK(JSON_VALID(plan_json)), specification_snapshot_json LONGTEXT NULL CHECK(specification_snapshot_json IS NULL OR JSON_VALID(specification_snapshot_json)),
 content_hash CHAR(64) NULL, status VARCHAR(30) NOT NULL CHECK(status IN ('DRAFT','APPROVED')),
 approved_by BIGINT NULL, approved_at DATETIME(3) NULL, signature_id BIGINT NULL, signature_evidence_json LONGTEXT NULL CHECK(signature_evidence_json IS NULL OR JSON_VALID(signature_evidence_json)),
 UNIQUE KEY uk_production_plan(org_id,main_batch_id),
 FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id), FOREIGN KEY(finished_material_id) REFERENCES md_material(id),
 FOREIGN KEY(qc_specification_version_id) REFERENCES qc_specification_version(id), FOREIGN KEY(approved_by) REFERENCES sys_user(id), FOREIGN KEY(signature_id) REFERENCES gxp_signature(id),
 CHECK((signature_id IS NULL)=(signature_evidence_json IS NULL)),
 CHECK(status <> 'APPROVED' OR (approved_by IS NOT NULL AND approved_at IS NOT NULL AND signature_id IS NOT NULL AND content_hash IS NOT NULL AND specification_snapshot_json IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE prd_main_batch ADD COLUMN finished_lot_id BIGINT NULL,
 ADD CONSTRAINT fk_batch_finished_lot FOREIGN KEY(finished_lot_id) REFERENCES md_material_lot(id),
 ADD UNIQUE KEY uk_batch_finished_lot(org_id,finished_lot_id);
ALTER TABLE mes_quantity_event ADD COLUMN reversal_of_id BIGINT NULL, ADD COLUMN reason VARCHAR(1000) NULL,
 ADD COLUMN signature_id BIGINT NULL, ADD COLUMN signature_evidence_json LONGTEXT NULL,
 ADD CONSTRAINT fk_quantity_reversal FOREIGN KEY(reversal_of_id) REFERENCES mes_quantity_event(id),
 ADD CONSTRAINT fk_quantity_signature FOREIGN KEY(signature_id) REFERENCES gxp_signature(id),
 ADD UNIQUE KEY uk_quantity_reversal(org_id,reversal_of_id),
 ADD CHECK(signature_evidence_json IS NULL OR JSON_VALID(signature_evidence_json)),
 ADD CHECK((signature_id IS NULL)=(signature_evidence_json IS NULL));

CREATE TABLE mes_balance_rule (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 process_snapshot_id BIGINT NOT NULL, main_batch_id BIGINT NOT NULL, balance_code VARCHAR(80) NOT NULL,
 basis VARCHAR(30) NOT NULL CHECK(basis IN ('BATCH','OPERATION','PACKAGING')), operation_execution_id BIGINT NULL, material_id BIGINT NULL, unit_id BIGINT NOT NULL,
 formula_expr LONGTEXT NOT NULL CHECK(JSON_VALID(formula_expr)), tolerance_low DECIMAL(24,8) NOT NULL, tolerance_high DECIMAL(24,8) NOT NULL,
 rule_version INT NOT NULL DEFAULT 1 CHECK(rule_version>0), check_point VARCHAR(30) NOT NULL CHECK(check_point IN ('OPERATION_COMPLETE','BATCH_COMPLETE','QA_RELEASE')),
 UNIQUE KEY uk_balance_rule(org_id,main_batch_id,balance_code,operation_execution_id), KEY ix_balance_rule_batch(org_id,main_batch_id,id),
 FOREIGN KEY(process_snapshot_id) REFERENCES prd_process_snapshot(id), FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id),
 FOREIGN KEY(operation_execution_id) REFERENCES mes_operation_execution(id), FOREIGN KEY(material_id) REFERENCES md_material(id), FOREIGN KEY(unit_id) REFERENCES md_unit(id),
 CHECK(tolerance_low<=tolerance_high), CHECK(basis<>'OPERATION' OR operation_execution_id IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mes_balance_result (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 main_batch_id BIGINT NOT NULL, balance_rule_id BIGINT NOT NULL, calculation_version INT NOT NULL CHECK(calculation_version>0),
 expected_value DECIMAL(24,8) NOT NULL CHECK(expected_value>0), actual_value DECIMAL(24,8) NOT NULL, difference_value DECIMAL(24,8) NOT NULL, difference_pct DECIMAL(24,8) NOT NULL,
 status VARCHAR(20) NOT NULL CHECK(status IN ('PASS','FAIL')), calculated_at DATETIME(3) NOT NULL, calculated_by BIGINT NOT NULL,
 input_snapshot_json LONGTEXT NOT NULL CHECK(JSON_VALID(input_snapshot_json)), input_digest CHAR(64) NOT NULL, rule_snapshot_json LONGTEXT NOT NULL CHECK(JSON_VALID(rule_snapshot_json)),
 UNIQUE KEY uk_balance_calculation(org_id,balance_rule_id,calculation_version), KEY ix_balance_result_batch(org_id,main_batch_id,id),
 FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id), FOREIGN KEY(balance_rule_id) REFERENCES mes_balance_rule(id), FOREIGN KEY(calculated_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mes_balance_investigation (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 balance_result_id BIGINT NOT NULL, status VARCHAR(30) NOT NULL CHECK(status IN ('OPEN','INVESTIGATING','APPROVED_EXCEPTION','RECALCULATE_REQUIRED')),
 investigation_text TEXT NULL, decision VARCHAR(30) NULL CHECK(decision IS NULL OR decision IN ('ACCEPT_EXCEPTION','REQUIRE_RECALCULATION')),
 input_digest CHAR(64) NOT NULL, investigated_by BIGINT NULL, approved_by BIGINT NULL, approved_at DATETIME(3) NULL, signature_id BIGINT NULL,
 signature_evidence_json LONGTEXT NULL CHECK(signature_evidence_json IS NULL OR JSON_VALID(signature_evidence_json)),
 UNIQUE KEY uk_balance_investigation(org_id,balance_result_id), FOREIGN KEY(balance_result_id) REFERENCES mes_balance_result(id),
 FOREIGN KEY(investigated_by) REFERENCES sys_user(id), FOREIGN KEY(approved_by) REFERENCES sys_user(id), FOREIGN KEY(signature_id) REFERENCES gxp_signature(id),
 CHECK((signature_id IS NULL)=(signature_evidence_json IS NULL)),
 CHECK(status NOT IN ('APPROVED_EXCEPTION','RECALCULATE_REQUIRED') OR (signature_id IS NOT NULL AND approved_by IS NOT NULL AND approved_at IS NOT NULL AND decision IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE qms_sample ADD COLUMN production_plan_id BIGINT NULL, ADD COLUMN qc_specification_version_id BIGINT NULL, ADD COLUMN source_ref VARCHAR(100) NULL,
 ADD CONSTRAINT fk_sample_production_plan FOREIGN KEY(production_plan_id) REFERENCES qms_production_plan(id),
 ADD CONSTRAINT fk_sample_production_spec FOREIGN KEY(qc_specification_version_id) REFERENCES qc_specification_version(id),
 ADD CHECK(sample_scope<>'PRODUCTION' OR (production_plan_id IS NOT NULL AND qc_specification_version_id IS NOT NULL AND main_batch_id IS NOT NULL AND quantity IS NOT NULL AND unit_id IS NOT NULL AND source_ref IS NOT NULL AND sampling_task_id IS NULL AND sampling_detail_id IS NULL AND inspection_request_item_id IS NULL));
CREATE TABLE qms_production_test_instance (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 sample_id BIGINT NOT NULL, production_plan_id BIGINT NOT NULL, qc_specification_item_id BIGINT NOT NULL,
 specification_snapshot_json LONGTEXT NOT NULL CHECK(JSON_VALID(specification_snapshot_json)), test_code VARCHAR(100) NOT NULL, attempt_no INT NOT NULL CHECK(attempt_no>0),
 previous_instance_id BIGINT NULL, approved_investigation_id BIGINT NULL, status VARCHAR(30) NOT NULL CHECK(status IN ('READY','TESTING','COMPLETED')),
 current_result_revision_id BIGINT NULL, instrument_id BIGINT NULL, performed_by BIGINT NULL,
 UNIQUE KEY uk_production_test_attempt(org_id,sample_id,qc_specification_item_id,attempt_no),
 FOREIGN KEY(sample_id) REFERENCES qms_sample(id), FOREIGN KEY(production_plan_id) REFERENCES qms_production_plan(id),
 FOREIGN KEY(qc_specification_item_id) REFERENCES qc_specification_item(id), FOREIGN KEY(previous_instance_id) REFERENCES qms_production_test_instance(id),
 FOREIGN KEY(approved_investigation_id) REFERENCES qms_deviation(id), FOREIGN KEY(instrument_id) REFERENCES md_equipment(id), FOREIGN KEY(performed_by) REFERENCES sys_user(id),
 CHECK((attempt_no=1 AND previous_instance_id IS NULL AND approved_investigation_id IS NULL) OR (attempt_no>1 AND previous_instance_id IS NOT NULL AND approved_investigation_id IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
ALTER TABLE qms_test_result_revision MODIFY COLUMN inspection_item_id BIGINT NULL, MODIFY COLUMN test_execution_id BIGINT NULL,
 ADD COLUMN production_test_instance_id BIGINT NULL,
 ADD CONSTRAINT fk_result_production_test FOREIGN KEY(production_test_instance_id) REFERENCES qms_production_test_instance(id),
 ADD UNIQUE KEY uk_production_result_revision(org_id,production_test_instance_id,revision_no),
 ADD CONSTRAINT ck_test_result_scope CHECK((production_test_instance_id IS NULL AND inspection_item_id IS NOT NULL AND test_execution_id IS NOT NULL) OR (production_test_instance_id IS NOT NULL AND inspection_item_id IS NULL AND test_execution_id IS NULL));
ALTER TABLE qms_production_test_instance ADD CONSTRAINT fk_production_test_current_result FOREIGN KEY(current_result_revision_id) REFERENCES qms_test_result_revision(id);
CREATE TABLE qms_production_test_review (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 result_revision_id BIGINT NOT NULL, disposition VARCHAR(30) NOT NULL CHECK(disposition IN ('CONFIRMED','INVALIDATED')), reason VARCHAR(1000) NOT NULL,
 reviewed_by BIGINT NOT NULL, reviewed_at DATETIME(3) NOT NULL, signature_id BIGINT NOT NULL, signature_evidence_json LONGTEXT NOT NULL CHECK(JSON_VALID(signature_evidence_json)),
 UNIQUE KEY uk_production_test_review(org_id,result_revision_id), FOREIGN KEY(result_revision_id) REFERENCES qms_test_result_revision(id),
 FOREIGN KEY(reviewed_by) REFERENCES sys_user(id), FOREIGN KEY(signature_id) REFERENCES gxp_signature(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Resolve the old anonymous incoming OOS check by its actual clause, not a guessed MariaDB name.
SET @mes012_oos_check = (SELECT constraint_name FROM information_schema.check_constraints WHERE constraint_schema=DATABASE() AND table_name='qms_deviation' AND check_clause LIKE '%investigation_kind%' AND check_clause LIKE '%test_execution_id%' AND check_clause NOT LIKE '%production_test_instance_id%' LIMIT 1);
SET @mes012_drop_check = IF(@mes012_oos_check IS NULL,'SELECT 1',CONCAT('ALTER TABLE qms_deviation DROP CONSTRAINT `',REPLACE(@mes012_oos_check,'`','``'),'`'));
PREPARE mes012_check_stmt FROM @mes012_drop_check;
EXECUTE mes012_check_stmt;
DEALLOCATE PREPARE mes012_check_stmt;
ALTER TABLE qms_deviation ADD COLUMN production_test_instance_id BIGINT NULL,
 ADD CONSTRAINT fk_deviation_production_test FOREIGN KEY(production_test_instance_id) REFERENCES qms_production_test_instance(id),
 ADD CONSTRAINT ck_deviation_oos_scope CHECK(investigation_kind<>'OOS' OR (original_result_revision_id IS NOT NULL AND ((investigation_scope='INCOMING_MATERIAL' AND test_execution_id IS NOT NULL AND production_test_instance_id IS NULL) OR (investigation_scope='PRODUCTION' AND test_execution_id IS NULL AND production_test_instance_id IS NOT NULL)))),
 ADD CHECK(investigation_scope<>'PRODUCTION' OR main_batch_id IS NOT NULL);
CREATE TABLE qms_capa (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 capa_no VARCHAR(80) NOT NULL, source_type VARCHAR(30) NOT NULL CHECK(source_type='PRODUCTION_DEVIATION'), source_id BIGINT NOT NULL,
 status VARCHAR(30) NOT NULL CHECK(status IN ('OPEN','IN_PROGRESS','COMPLETED','VERIFIED')), action_text TEXT NOT NULL, completion_text TEXT NULL, completed_by BIGINT NULL, completed_at DATETIME(3) NULL,
 UNIQUE KEY uk_capa_no(org_id,capa_no), FOREIGN KEY(source_id) REFERENCES qms_deviation(id), FOREIGN KEY(completed_by) REFERENCES sys_user(id),
 CHECK(status NOT IN ('COMPLETED','VERIFIED') OR (completion_text IS NOT NULL AND completed_by IS NOT NULL AND completed_at IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE qms_capa_review (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 capa_id BIGINT NOT NULL, review_no INT NOT NULL CHECK(review_no>0), decision VARCHAR(30) NOT NULL CHECK(decision IN ('VERIFIED','REOPEN')),
 reason VARCHAR(1000) NOT NULL, reviewed_by BIGINT NOT NULL, reviewed_at DATETIME(3) NOT NULL, signature_id BIGINT NOT NULL, signature_evidence_json LONGTEXT NOT NULL CHECK(JSON_VALID(signature_evidence_json)),
 UNIQUE KEY uk_capa_review(org_id,capa_id,review_no), FOREIGN KEY(capa_id) REFERENCES qms_capa(id), FOREIGN KEY(reviewed_by) REFERENCES sys_user(id), FOREIGN KEY(signature_id) REFERENCES gxp_signature(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TRIGGER bu_mes_balance_rule BEFORE UPDATE ON mes_balance_rule FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable production evidence';
CREATE TRIGGER bd_mes_balance_rule BEFORE DELETE ON mes_balance_rule FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable production evidence';
CREATE TRIGGER bu_mes_balance_result BEFORE UPDATE ON mes_balance_result FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable production evidence';
CREATE TRIGGER bd_mes_balance_result BEFORE DELETE ON mes_balance_result FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable production evidence';
CREATE TRIGGER bu_qms_production_test_review BEFORE UPDATE ON qms_production_test_review FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable production evidence';
CREATE TRIGGER bd_qms_production_test_review BEFORE DELETE ON qms_production_test_review FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable production evidence';
CREATE TRIGGER bu_qms_capa_review BEFORE UPDATE ON qms_capa_review FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable production evidence';
CREATE TRIGGER bd_qms_capa_review BEFORE DELETE ON qms_capa_review FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable production evidence';
CREATE TRIGGER bd_qms_production_plan BEFORE DELETE ON qms_production_plan FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited';
CREATE TRIGGER bd_mes_balance_investigation BEFORE DELETE ON mes_balance_investigation FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited';
CREATE TRIGGER bd_qms_production_test_instance BEFORE DELETE ON qms_production_test_instance FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited';
CREATE TRIGGER bd_qms_capa BEFORE DELETE ON qms_capa FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited';

INSERT INTO sys_permission(permission_code,permission_type,display_name,enabled)
 SELECT p.code,'ACTION',p.code,TRUE FROM (
 SELECT 'qms:plan:view' AS code UNION ALL SELECT 'qms:plan:create' UNION ALL SELECT 'qms:plan:update' UNION ALL SELECT 'qms:plan:approve'
 UNION ALL SELECT 'qms:sample:view' UNION ALL SELECT 'qms:sample:create' UNION ALL SELECT 'qms:sample:receive'
 UNION ALL SELECT 'qms:test:record' UNION ALL SELECT 'qms:test:view' UNION ALL SELECT 'qms:test:review' UNION ALL SELECT 'qms:test:retest'
 UNION ALL SELECT 'qms:capa:create' UNION ALL SELECT 'qms:capa:view' UNION ALL SELECT 'qms:capa:update' UNION ALL SELECT 'qms:capa:verify'
 UNION ALL SELECT 'balance:view' UNION ALL SELECT 'balance:investigate' UNION ALL SELECT 'balance:approve'
 UNION ALL SELECT 'mes:quantity:record' UNION ALL SELECT 'mes:quantity:reverse') p
 WHERE NOT EXISTS(SELECT 1 FROM sys_permission existing WHERE existing.permission_code=p.code);
INSERT INTO sys_role_permission(role_id,permission_id)
 SELECT r.id,p.id FROM sys_role r CROSS JOIN sys_permission p
 WHERE ((r.role_code='SYSTEM_ADMIN' AND p.permission_code IN ('qms:plan:view','qms:sample:view','qms:test:view','qms:capa:view','balance:view'))
 OR (r.role_code='PRODUCTION_OPERATOR' AND p.permission_code IN ('qms:plan:view','qms:plan:create','qms:plan:update','balance:view','mes:quantity:record','mes:quantity:reverse'))
 OR (r.role_code='QC_OPERATOR' AND p.permission_code IN ('qms:plan:view','qms:sample:view','qms:sample:create','qms:sample:receive','qms:test:view','qms:test:record','qms:test:retest','qms:capa:view','qms:capa:create','qms:capa:update','balance:view','balance:investigate'))
 OR (r.role_code='QC_REVIEWER' AND p.permission_code IN ('qms:test:view','qms:test:review','qms:sample:view','qms:plan:view','qms:capa:view','balance:view'))
 OR (r.role_code='QA_APPROVER' AND p.permission_code IN ('qms:plan:view','qms:plan:approve','qms:capa:view','qms:capa:verify','balance:view','balance:approve')))
 AND NOT EXISTS(SELECT 1 FROM sys_role_permission existing WHERE existing.role_id=r.id AND existing.permission_id=p.id);
