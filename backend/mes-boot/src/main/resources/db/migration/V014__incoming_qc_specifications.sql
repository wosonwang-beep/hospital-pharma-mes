-- DCP-INCOMING-QUALITY-GAPS-001 / v1.0.11: independent immutable QC standards.
CREATE TABLE qc_specification (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0 CHECK (version_no >= 0),
 material_id BIGINT NOT NULL,
 specification_code VARCHAR(100) NOT NULL CHECK (CHAR_LENGTH(TRIM(specification_code)) > 0),
 specification_name VARCHAR(200) NOT NULL CHECK (CHAR_LENGTH(TRIM(specification_name)) > 0),
 CONSTRAINT fk_qc_spec_material FOREIGN KEY(material_id) REFERENCES md_material(id),
 UNIQUE KEY uk_qc_spec_code(org_id,specification_code),
 KEY ix_qc_spec_material(org_id,material_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE qc_specification_version (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0 CHECK (version_no >= 0),
 specification_id BIGINT NOT NULL,
 version_no_business INT NOT NULL CHECK (version_no_business > 0),
 status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','APPROVED','RETIRED')),
 content_hash CHAR(64) NULL CHECK (content_hash IS NULL OR content_hash REGEXP '^[a-f0-9]{64}$'),
 approved_by BIGINT NULL, approved_at DATETIME(3) NULL, approval_signature_id BIGINT NULL,
 approval_reason VARCHAR(1000) NULL CHECK (approval_reason IS NULL OR CHAR_LENGTH(TRIM(approval_reason))>0),
 retired_by BIGINT NULL, retired_at DATETIME(3) NULL, retirement_signature_id BIGINT NULL,
 retirement_reason VARCHAR(1000) NULL CHECK (retirement_reason IS NULL OR CHAR_LENGTH(TRIM(retirement_reason))>0),
 CONSTRAINT fk_qc_version_specification FOREIGN KEY(specification_id) REFERENCES qc_specification(id),
 CONSTRAINT fk_qc_version_approver FOREIGN KEY(approved_by) REFERENCES sys_user(id),
 CONSTRAINT fk_qc_version_approval_signature FOREIGN KEY(approval_signature_id) REFERENCES gxp_signature(id),
 CONSTRAINT fk_qc_version_retirer FOREIGN KEY(retired_by) REFERENCES sys_user(id),
 CONSTRAINT fk_qc_version_retirement_signature FOREIGN KEY(retirement_signature_id) REFERENCES gxp_signature(id),
 UNIQUE KEY uk_qc_version_business(org_id,specification_id,version_no_business),
 KEY ix_qc_version_select(org_id,specification_id,status,id),
 CONSTRAINT ck_qc_version_evidence CHECK (
  (status='DRAFT' AND content_hash IS NULL AND approved_by IS NULL AND approved_at IS NULL AND approval_signature_id IS NULL AND approval_reason IS NULL AND retired_by IS NULL AND retired_at IS NULL AND retirement_signature_id IS NULL AND retirement_reason IS NULL)
  OR (status='APPROVED' AND content_hash IS NOT NULL AND approved_by IS NOT NULL AND approved_at IS NOT NULL AND approval_signature_id IS NOT NULL AND approval_reason IS NOT NULL AND retired_by IS NULL AND retired_at IS NULL AND retirement_signature_id IS NULL AND retirement_reason IS NULL)
  OR (status='RETIRED' AND content_hash IS NOT NULL AND approved_by IS NOT NULL AND approved_at IS NOT NULL AND approval_signature_id IS NOT NULL AND approval_reason IS NOT NULL AND retired_by IS NOT NULL AND retired_at IS NOT NULL AND retirement_signature_id IS NOT NULL AND retirement_reason IS NOT NULL)
 )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE qc_specification_item (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0 CHECK (version_no >= 0),
 specification_version_id BIGINT NOT NULL,
 item_code VARCHAR(100) NOT NULL CHECK (CHAR_LENGTH(TRIM(item_code))>0),
 item_name VARCHAR(200) NOT NULL CHECK (CHAR_LENGTH(TRIM(item_name))>0),
 required TINYINT(1) NOT NULL CHECK (required IN (0,1)),
 result_type VARCHAR(20) NOT NULL CHECK (result_type IN ('NUMERIC','TEXT')),
 lower_limit DECIMAL(18,6) NULL, upper_limit DECIMAL(18,6) NULL,
 unit_id BIGINT NULL, text_acceptance_criteria VARCHAR(1000) NULL,
 method_code VARCHAR(100) NOT NULL CHECK (CHAR_LENGTH(TRIM(method_code))>0),
 method_version VARCHAR(50) NOT NULL CHECK (CHAR_LENGTH(TRIM(method_version))>0),
 active TINYINT(1) NOT NULL DEFAULT 1 CHECK (active IN (0,1)),
 CONSTRAINT fk_qc_item_version FOREIGN KEY(specification_version_id) REFERENCES qc_specification_version(id),
 CONSTRAINT fk_qc_item_unit FOREIGN KEY(unit_id) REFERENCES md_unit(id),
 UNIQUE KEY uk_qc_item_code(org_id,specification_version_id,item_code),
 KEY ix_qc_item_version(org_id,specification_version_id,active,id),
 CONSTRAINT ck_qc_item_shape CHECK (
  (result_type='NUMERIC' AND (lower_limit IS NOT NULL OR upper_limit IS NOT NULL) AND unit_id IS NOT NULL AND text_acceptance_criteria IS NULL AND (lower_limit IS NULL OR upper_limit IS NULL OR lower_limit<=upper_limit))
  OR (result_type='TEXT' AND lower_limit IS NULL AND upper_limit IS NULL AND unit_id IS NULL AND text_acceptance_criteria IS NOT NULL AND CHAR_LENGTH(TRIM(text_acceptance_criteria))>0)
 )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELIMITER $$
CREATE TRIGGER qc_specification_no_update BEFORE UPDATE ON qc_specification FOR EACH ROW
BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC specification identity is immutable'; END$$
CREATE TRIGGER qc_specification_no_delete BEFORE DELETE ON qc_specification FOR EACH ROW
BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC specification evidence cannot be deleted'; END$$
CREATE TRIGGER qc_specification_org_insert BEFORE INSERT ON qc_specification FOR EACH ROW
BEGIN
 IF NOT EXISTS(SELECT 1 FROM md_material WHERE id=NEW.material_id AND org_id=NEW.org_id) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC material organization mismatch';
 END IF;
END$$
CREATE TRIGGER qc_version_insert BEFORE INSERT ON qc_specification_version FOR EACH ROW
BEGIN
 IF NEW.status<>'DRAFT' OR NOT EXISTS(SELECT 1 FROM qc_specification WHERE id=NEW.specification_id AND org_id=NEW.org_id) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC version must start as same-organization DRAFT';
 END IF;
END$$
CREATE TRIGGER qc_version_update BEFORE UPDATE ON qc_specification_version FOR EACH ROW
BEGIN
 IF NEW.id<>OLD.id OR NEW.org_id<>OLD.org_id OR NEW.specification_id<>OLD.specification_id OR NEW.version_no_business<>OLD.version_no_business OR NEW.created_by<>OLD.created_by OR NEW.created_at<>OLD.created_at OR NEW.version_no<>OLD.version_no+1 THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC version identity or optimistic token changed';
 END IF;
 IF OLD.status='RETIRED' OR (OLD.status='DRAFT' AND NEW.status NOT IN ('DRAFT','APPROVED')) OR (OLD.status='APPROVED' AND NEW.status<>'RETIRED') THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC version transition rejected';
 END IF;
 IF OLD.status='APPROVED' AND (NOT(NEW.content_hash<=>OLD.content_hash) OR NOT(NEW.approved_by<=>OLD.approved_by) OR NOT(NEW.approved_at<=>OLD.approved_at) OR NOT(NEW.approval_signature_id<=>OLD.approval_signature_id) OR NOT(NEW.approval_reason<=>OLD.approval_reason)) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC approved evidence is immutable';
 END IF;
 IF NEW.status='APPROVED' AND (NEW.approved_by=NEW.created_by OR NOT EXISTS(SELECT 1 FROM qc_specification_item WHERE specification_version_id=NEW.id AND org_id=NEW.org_id AND active=1)) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC approval requires items and independent approver';
 END IF;
END$$
CREATE TRIGGER qc_version_no_delete BEFORE DELETE ON qc_specification_version FOR EACH ROW
BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC version evidence cannot be deleted'; END$$
CREATE TRIGGER qc_item_insert BEFORE INSERT ON qc_specification_item FOR EACH ROW
BEGIN
 IF NOT EXISTS(SELECT 1 FROM qc_specification_version WHERE id=NEW.specification_version_id AND org_id=NEW.org_id AND status='DRAFT') THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC items require a same-organization DRAFT version';
 END IF;
 IF NEW.unit_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM md_unit WHERE id=NEW.unit_id AND org_id=NEW.org_id) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC unit organization mismatch';
 END IF;
END$$
CREATE TRIGGER qc_item_update BEFORE UPDATE ON qc_specification_item FOR EACH ROW
BEGIN
 IF NEW.id<>OLD.id OR NEW.org_id<>OLD.org_id OR NEW.specification_version_id<>OLD.specification_version_id OR BINARY NEW.item_code<>BINARY OLD.item_code OR NEW.created_by<>OLD.created_by OR NEW.created_at<>OLD.created_at OR NEW.version_no<>OLD.version_no+1 THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC item identity or optimistic token changed';
 END IF;
 IF NOT EXISTS(SELECT 1 FROM qc_specification_version WHERE id=OLD.specification_version_id AND org_id=OLD.org_id AND status='DRAFT') THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC approved items are immutable';
 END IF;
 IF NEW.unit_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM md_unit WHERE id=NEW.unit_id AND org_id=NEW.org_id) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC unit organization mismatch';
 END IF;
END$$
CREATE TRIGGER qc_item_no_delete BEFORE DELETE ON qc_specification_item FOR EACH ROW
BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC item evidence cannot be deleted'; END$$
DELIMITER ;
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:specification:view','ACTION','qms:specification:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:specification:view');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:specification:create','ACTION','qms:specification:create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:specification:create');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:specification:edit','ACTION','qms:specification:edit',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:specification:edit');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:specification:approve','ACTION','qms:specification:approve',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:specification:approve');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:specification:retire','ACTION','qms:specification:retire',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:specification:retire');
INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'qms:specification:view','QC质量标准','/quality/specifications',230,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='qms:specification:view');
-- Role grants intentionally use existing IAM assignment workflow, never implicit QC grants.
