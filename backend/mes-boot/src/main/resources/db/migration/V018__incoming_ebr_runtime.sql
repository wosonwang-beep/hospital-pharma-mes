-- v1.0.12 runtime evidence. V019 installs the actual operation-execution foreign key.
CREATE TABLE ebr_batch_snapshot (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 main_batch_id BIGINT NOT NULL,template_version_id BIGINT NOT NULL,definition_hash VARCHAR(128) NOT NULL,snapshot_json LONGTEXT NOT NULL CHECK(JSON_VALID(snapshot_json)),frozen_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_ebr_batch(main_batch_id),FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id),FOREIGN KEY(template_version_id) REFERENCES ebr_template_version(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE ebr_form_instance (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 operation_execution_id BIGINT NOT NULL,form_def_id BIGINT NOT NULL,status VARCHAR(20) NOT NULL CHECK(status IN ('DRAFT','SUBMITTED','VERIFIED','APPROVED')),submitted_at DATETIME(3) NULL,
 occurrence_no INT NOT NULL CHECK(occurrence_no>=1),revision INT NOT NULL DEFAULT 0 CHECK(revision>=0),submitted_by BIGINT NULL,
 CHECK((status='DRAFT' AND submitted_at IS NULL AND submitted_by IS NULL) OR (status<>'DRAFT' AND submitted_at IS NOT NULL AND submitted_by IS NOT NULL)),
 UNIQUE KEY uk_ebr_runtime_form(org_id,operation_execution_id,form_def_id,occurrence_no),KEY idx_ebr_form_state(org_id,status),FOREIGN KEY(form_def_id) REFERENCES ebr_form_def(id),FOREIGN KEY(submitted_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE ebr_field_value_revision (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 form_instance_id BIGINT NOT NULL,field_def_id BIGINT NOT NULL,revision_no INT NOT NULL CHECK(revision_no>=1),previous_revision_id BIGINT NULL,raw_value LONGTEXT NULL,derived_value LONGTEXT NULL,change_reason VARCHAR(1000) NULL,recorded_at DATETIME(3) NOT NULL,
 field_code VARCHAR(64) NOT NULL,occurrence_path VARCHAR(255) NOT NULL,raw_value_json LONGTEXT NULL CHECK(raw_value_json IS NULL OR JSON_VALID(raw_value_json)),normalized_value_json LONGTEXT NULL CHECK(normalized_value_json IS NULL OR JSON_VALID(normalized_value_json)),unit_id BIGINT NULL,source_type VARCHAR(20) NOT NULL CHECK(source_type IN ('MANUAL','BARCODE','INSTRUMENT','SYSTEM','DERIVED')),source_ref VARCHAR(128) NULL,recorded_by BIGINT NOT NULL,
 CHECK((revision_no=1 AND previous_revision_id IS NULL) OR (revision_no>1 AND previous_revision_id IS NOT NULL)),
 UNIQUE KEY uk_ebr_field_revision(org_id,form_instance_id,field_code,occurrence_path,revision_no),UNIQUE KEY uk_ebr_revision_successor(previous_revision_id),KEY idx_ebr_recorded(org_id,recorded_at),
 FOREIGN KEY(form_instance_id) REFERENCES ebr_form_instance(id),FOREIGN KEY(field_def_id) REFERENCES ebr_field_def(id),FOREIGN KEY(previous_revision_id) REFERENCES ebr_field_value_revision(id),FOREIGN KEY(unit_id) REFERENCES md_unit(id),FOREIGN KEY(recorded_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE ebr_rule_execution (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 form_instance_id BIGINT NOT NULL,rule_code VARCHAR(64) NOT NULL,rule_version VARCHAR(32) NOT NULL,trigger_point VARCHAR(30) NOT NULL CHECK(trigger_point IN ('ON_CHANGE','ON_SAVE','ON_SUBMIT','ON_OPERATION_COMPLETE','ON_BATCH_CLOSE')),passed TINYINT(1) NOT NULL CHECK(passed IN(0,1)),severity VARCHAR(20) NULL CHECK(severity IS NULL OR severity IN('BLOCK','WARN')),input_snapshot_json LONGTEXT NOT NULL CHECK(JSON_VALID(input_snapshot_json)),output_json LONGTEXT NULL CHECK(output_json IS NULL OR JSON_VALID(output_json)),engine_version VARCHAR(32) NOT NULL,executed_at DATETIME(3) NOT NULL,
 KEY idx_ebr_rule_code(org_id,rule_code),KEY idx_ebr_rule_form(org_id,form_instance_id),FOREIGN KEY(form_instance_id) REFERENCES ebr_form_instance(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE ebr_review_record (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 object_type VARCHAR(50) NOT NULL CHECK(object_type='EBR_FORM_INSTANCE'),object_id BIGINT NOT NULL,review_type VARCHAR(20) NOT NULL CHECK(review_type IN('VERIFY','APPROVE')),reviewer_id BIGINT NOT NULL,role_snapshot VARCHAR(100) NOT NULL,decision VARCHAR(20) NOT NULL CHECK(decision IN('PASS','REJECT')),comment VARCHAR(1000) NULL,reviewed_at DATETIME(3) NOT NULL,signature_id BIGINT NOT NULL,invalidated_at DATETIME(3) NULL,invalidation_reason VARCHAR(1000) NULL,
 form_revision INT NOT NULL CHECK(form_revision>=0),review_rule_id BIGINT NOT NULL,reason VARCHAR(1000) NOT NULL CHECK(CHAR_LENGTH(TRIM(reason))>0),signature_evidence_json LONGTEXT NOT NULL CHECK(JSON_VALID(signature_evidence_json)),
 CHECK((invalidated_at IS NULL AND invalidation_reason IS NULL) OR (invalidated_at IS NOT NULL AND CHAR_LENGTH(TRIM(invalidation_reason))>0)),
 UNIQUE KEY uk_ebr_review_revision(org_id,object_type,object_id,form_revision,review_rule_id),KEY idx_ebr_review_object(object_type,object_id),
 FOREIGN KEY(object_id) REFERENCES ebr_form_instance(id),FOREIGN KEY(reviewer_id) REFERENCES sys_user(id),FOREIGN KEY(review_rule_id) REFERENCES ebr_review_rule(id),FOREIGN KEY(signature_id) REFERENCES gxp_signature(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
DELIMITER $$
CREATE TRIGGER ebr_snapshot_insert BEFORE INSERT ON ebr_batch_snapshot FOR EACH ROW
BEGIN
 IF NOT EXISTS(SELECT 1 FROM prd_main_batch WHERE id=NEW.main_batch_id AND org_id=NEW.org_id) OR NOT EXISTS(SELECT 1 FROM ebr_template_version WHERE id=NEW.template_version_id AND org_id=NEW.org_id AND status='EFFECTIVE') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR snapshot source mismatch'; END IF;
END$$
CREATE TRIGGER ebr_form_insert BEFORE INSERT ON ebr_form_instance FOR EACH ROW
BEGIN
 IF NEW.status<>'DRAFT' OR NEW.revision<>0 OR NOT EXISTS(SELECT 1 FROM ebr_form_def WHERE id=NEW.form_def_id AND org_id=NEW.org_id) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR form source or initial state mismatch'; END IF;
END$$
CREATE TRIGGER ebr_form_update BEFORE UPDATE ON ebr_form_instance FOR EACH ROW
BEGIN
 IF NEW.id<>OLD.id OR NEW.org_id<>OLD.org_id OR NEW.operation_execution_id<>OLD.operation_execution_id OR NEW.form_def_id<>OLD.form_def_id OR NEW.occurrence_no<>OLD.occurrence_no OR NEW.created_at<>OLD.created_at OR NEW.created_by<>OLD.created_by OR NEW.version_no<>OLD.version_no+1 OR NEW.revision<OLD.revision OR NEW.revision>OLD.revision+1 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR form identity or optimistic revision mismatch'; END IF;
 IF NEW.revision>OLD.revision AND NEW.status<>'DRAFT' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR changed content must return to draft'; END IF;
END$$
CREATE TRIGGER ebr_value_insert BEFORE INSERT ON ebr_field_value_revision FOR EACH ROW
BEGIN
 IF NOT EXISTS(SELECT 1 FROM ebr_form_instance f JOIN ebr_field_def d ON d.form_def_id=f.form_def_id AND d.org_id=f.org_id WHERE f.id=NEW.form_instance_id AND f.org_id=NEW.org_id AND d.id=NEW.field_def_id AND BINARY d.field_code=BINARY NEW.field_code) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR field organization or source mismatch'; END IF;
 IF NEW.previous_revision_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM ebr_field_value_revision WHERE id=NEW.previous_revision_id AND org_id=NEW.org_id AND form_instance_id=NEW.form_instance_id AND field_def_id=NEW.field_def_id AND BINARY field_code=BINARY NEW.field_code AND BINARY occurrence_path=BINARY NEW.occurrence_path AND revision_no=NEW.revision_no-1) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR previous field revision mismatch'; END IF;
 IF NEW.unit_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM md_unit WHERE id=NEW.unit_id AND org_id=NEW.org_id) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR unit organization mismatch'; END IF;
END$$
CREATE TRIGGER ebr_rule_insert BEFORE INSERT ON ebr_rule_execution FOR EACH ROW
BEGIN
 IF NOT EXISTS(SELECT 1 FROM ebr_form_instance WHERE id=NEW.form_instance_id AND org_id=NEW.org_id) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR rule organization mismatch'; END IF;
END$$
CREATE TRIGGER ebr_review_insert BEFORE INSERT ON ebr_review_record FOR EACH ROW
BEGIN
 IF NOT EXISTS(SELECT 1 FROM ebr_form_instance f JOIN ebr_form_def d ON d.id=f.form_def_id AND d.org_id=f.org_id JOIN ebr_review_rule r ON r.template_version_id=d.template_version_id AND r.org_id=f.org_id WHERE f.id=NEW.object_id AND f.org_id=NEW.org_id AND f.revision=NEW.form_revision AND r.id=NEW.review_rule_id AND r.review_type=NEW.review_type AND r.required_role=NEW.role_snapshot) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR review frozen rule mismatch'; END IF;
 IF NOT EXISTS(SELECT 1 FROM gxp_signature WHERE id=NEW.signature_id AND org_id=NEW.org_id AND signer_id=NEW.reviewer_id AND object_type='EBR_REVIEW_RECORD' AND object_id=CONCAT(NEW.object_id,':',NEW.form_revision,':',NEW.review_rule_id) AND meaning=NEW.review_type AND status='VALID') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR review signature mismatch'; END IF;
END$$
CREATE TRIGGER ebr_review_update BEFORE UPDATE ON ebr_review_record FOR EACH ROW
BEGIN
 IF NEW.id<>OLD.id OR NEW.org_id<>OLD.org_id OR NEW.object_type<>OLD.object_type OR NEW.object_id<>OLD.object_id OR NEW.review_type<>OLD.review_type OR NEW.reviewer_id<>OLD.reviewer_id OR BINARY NEW.role_snapshot<>BINARY OLD.role_snapshot OR NEW.decision<>OLD.decision OR NOT(BINARY NEW.comment<=>BINARY OLD.comment) OR NEW.reviewed_at<>OLD.reviewed_at OR NEW.signature_id<>OLD.signature_id OR NEW.form_revision<>OLD.form_revision OR NEW.review_rule_id<>OLD.review_rule_id OR BINARY NEW.reason<>BINARY OLD.reason OR BINARY NEW.signature_evidence_json<>BINARY OLD.signature_evidence_json OR NEW.created_by<>OLD.created_by OR NEW.created_at<>OLD.created_at OR NEW.version_no<>OLD.version_no+1 OR OLD.invalidated_at IS NOT NULL OR NEW.invalidated_at IS NULL OR NEW.invalidation_reason IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR review is immutable except one-way invalidation'; END IF;
END$$
CREATE TRIGGER ebr_snapshot_no_update BEFORE UPDATE ON ebr_batch_snapshot FOR EACH ROW BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR snapshot immutable'; END$$
CREATE TRIGGER ebr_snapshot_no_delete BEFORE DELETE ON ebr_batch_snapshot FOR EACH ROW BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR snapshot retained'; END$$
CREATE TRIGGER ebr_form_no_delete BEFORE DELETE ON ebr_form_instance FOR EACH ROW BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR form retained'; END$$
CREATE TRIGGER ebr_value_no_update BEFORE UPDATE ON ebr_field_value_revision FOR EACH ROW BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR value revision immutable'; END$$
CREATE TRIGGER ebr_value_no_delete BEFORE DELETE ON ebr_field_value_revision FOR EACH ROW BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR value revision retained'; END$$
CREATE TRIGGER ebr_rule_no_update BEFORE UPDATE ON ebr_rule_execution FOR EACH ROW BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR rule execution immutable'; END$$
CREATE TRIGGER ebr_rule_no_delete BEFORE DELETE ON ebr_rule_execution FOR EACH ROW BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR rule execution retained'; END$$
CREATE TRIGGER ebr_review_no_delete BEFORE DELETE ON ebr_review_record FOR EACH ROW BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR review retained'; END$$
DELIMITER ;
