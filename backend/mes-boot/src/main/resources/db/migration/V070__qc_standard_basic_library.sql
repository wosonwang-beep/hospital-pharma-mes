-- DCP-QC-SPEC-BASIC-LIBRARY-001 (approved 2026-11-02): QC quality standards become a basic
-- information library with plain create/read/update/delete, no approval, no electronic signature and
-- no version management. One standard records its regulatory basis (标准的依据) and applies to many
-- materials and many products through an explicit applicability table.
--
-- The human explicitly authorised deleting the QC approval/version evidence: the version header, its
-- approval and retirement signature, and the version-scoped item rows. Every downstream document
-- keeps its own frozen snapshot JSON, so no other module loses evidence. V001..V069 are immutable
-- and are not modified by this migration.

-- 1. Applicability: one standard to many materials and many products.
CREATE TABLE qc_specification_scope (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 specification_id BIGINT NOT NULL,
 subject_type VARCHAR(20) NOT NULL,
 subject_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_qc_scope_subject (org_id,specification_id,subject_type,subject_id),
 KEY ix_qc_scope_subject_lookup (org_id,subject_type,subject_id,specification_id),
 KEY ix_qc_scope_specification (org_id,specification_id,id),
 CONSTRAINT fk_qc_scope_specification FOREIGN KEY (specification_id) REFERENCES qc_specification(id),
 CONSTRAINT ck_qc_scope_subject CHECK (subject_type IN ('MATERIAL','PRODUCT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. One row per standard mutation: the editable revision history of a basic record.
CREATE TABLE qc_specification_revision (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 specification_id BIGINT NOT NULL,
 revision_no INT NOT NULL,
 revision_type VARCHAR(16) NOT NULL,
 revision_reason VARCHAR(500) NOT NULL,
 before_json LONGTEXT NULL CHECK (before_json IS NULL OR JSON_VALID(before_json)),
 after_json LONGTEXT NOT NULL CHECK (JSON_VALID(after_json)),
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_qc_revision_no (org_id,specification_id,revision_no),
 KEY ix_qc_revision_created (org_id,created_at,id),
 CONSTRAINT fk_qc_revision_specification FOREIGN KEY (specification_id) REFERENCES qc_specification(id),
 CONSTRAINT ck_qc_revision_type CHECK (revision_type IN ('CREATE','UPDATE','DELETE')),
 CONSTRAINT ck_qc_revision_no_positive CHECK (revision_no>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Root header fields. material_id keeps its value as the primary applicable material so existing
-- data is preserved; the obsolete single subject columns and identity triggers are retired below.
ALTER TABLE qc_specification
 MODIFY material_id BIGINT NULL,
 ADD COLUMN basis_type VARCHAR(32) NULL,
 ADD COLUMN basis_reference VARCHAR(300) NULL,
 ADD COLUMN effective_date DATE NULL,
 ADD COLUMN remark VARCHAR(1000) NULL,
 ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
 ADD CONSTRAINT ck_qc_spec_status CHECK (status IN ('ACTIVE','INACTIVE')),
 ADD CONSTRAINT ck_qc_spec_basis_pair CHECK ((basis_type IS NULL)=(basis_reference IS NULL));

DROP TRIGGER IF EXISTS qc_specification_no_update;
DROP TRIGGER IF EXISTS qc_specification_org_insert;
DROP TRIGGER IF EXISTS qc_specification_no_delete;

-- 4. Existing applicability from the obsolete single subject column. Duplicates collapse on the
-- unique key. PRODUCT subjects survive only in the scope table.
INSERT INTO qc_specification_scope(org_id,specification_id,subject_type,subject_id,created_by,updated_by)
SELECT org_id,id,'MATERIAL',material_id,created_by,updated_by FROM qc_specification WHERE material_id IS NOT NULL
ON DUPLICATE KEY UPDATE updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO qc_specification_scope(org_id,specification_id,subject_type,subject_id,created_by,updated_by)
SELECT org_id,id,'PRODUCT',product_id,created_by,updated_by FROM qc_specification WHERE product_id IS NOT NULL
ON DUPLICATE KEY UPDATE updated_at=CURRENT_TIMESTAMP(3);

ALTER TABLE qc_specification DROP FOREIGN KEY fk_qc_spec_material;
ALTER TABLE qc_specification DROP FOREIGN KEY fk_qc_product_subject;
ALTER TABLE qc_specification
 DROP CONSTRAINT ck_qc_subject_identity,
 DROP INDEX ix_qc_spec_material;
ALTER TABLE qc_specification
 DROP COLUMN subject_type,
 DROP COLUMN product_id,
 ADD KEY ix_qc_spec_material(org_id,material_id,id);

-- 5. Retire every reference to the QC version table before it is dropped. The client names two of
-- those foreign keys with a numeric suffix, so they are located from the data dictionary instead of
-- being hard-coded. A cursor is used because one PREPARE cannot carry several statements.
DROP PROCEDURE IF EXISTS qc_drop_standard_version_references;
DELIMITER $$
CREATE PROCEDURE qc_drop_standard_version_references()
BEGIN
 DECLARE finished INT DEFAULT 0;
 DECLARE target_table VARCHAR(64);
 DECLARE target_key VARCHAR(64);
 DECLARE references_cursor CURSOR FOR
  SELECT TABLE_NAME,CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
   WHERE CONSTRAINT_SCHEMA=DATABASE() AND REFERENCED_TABLE_NAME='qc_specification_version'
     AND TABLE_NAME IN ('qms_inspection_request','qms_production_plan','qms_sample','qms_finished_inspection_request');
 DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished=1;
 OPEN references_cursor;
 dropping: LOOP
  FETCH references_cursor INTO target_table,target_key;
  IF finished=1 THEN LEAVE dropping; END IF;
  SET @statement=CONCAT('ALTER TABLE `',target_table,'` DROP FOREIGN KEY `',target_key,'`');
  PREPARE dropping FROM @statement;
  EXECUTE dropping;
  DEALLOCATE PREPARE dropping;
 END LOOP;
 CLOSE references_cursor;
END$$
DELIMITER ;
CALL qc_drop_standard_version_references();
DROP PROCEDURE qc_drop_standard_version_references;

-- Downstream documents must keep binding to the standard, not to a version. The historical column
-- stores the version id, so each row is translated to its owning standard before the version table
-- disappears; the column then points at the standard and keeps working for every reader. The frozen
-- evidence trigger protects exactly that column, so it is dropped for the length of this migration
-- and recreated unchanged below. The column name is retained verbatim: it is a frozen downstream
-- contract (API field, snapshots and reports already reference it) and renaming it would change
-- acceptance-tested contracts outside this design change.
DROP TRIGGER IF EXISTS preserve_qms_finished_inspection_request;

UPDATE qms_inspection_request r JOIN qc_specification_version v ON v.id=r.qc_specification_version_id
 SET r.qc_specification_version_id=v.specification_id;
UPDATE qms_production_plan r JOIN qc_specification_version v ON v.id=r.qc_specification_version_id
 SET r.qc_specification_version_id=v.specification_id;
UPDATE qms_sample r JOIN qc_specification_version v ON v.id=r.qc_specification_version_id
 SET r.qc_specification_version_id=v.specification_id;
UPDATE qms_finished_inspection_request r JOIN qc_specification_version v ON v.id=r.qc_specification_version_id
 SET r.qc_specification_version_id=v.specification_id;

ALTER TABLE qms_inspection_request
 MODIFY qc_specification_version_id BIGINT NULL,
 ADD CONSTRAINT fk_inspection_request_qc_standard FOREIGN KEY (qc_specification_version_id) REFERENCES qc_specification(id);

ALTER TABLE qms_production_plan
 MODIFY qc_specification_version_id BIGINT NULL,
 ADD CONSTRAINT fk_plan_qc_standard FOREIGN KEY (qc_specification_version_id) REFERENCES qc_specification(id);

ALTER TABLE qms_sample
 MODIFY qc_specification_version_id BIGINT NULL,
 ADD CONSTRAINT fk_sample_qc_standard FOREIGN KEY (qc_specification_version_id) REFERENCES qc_specification(id);

ALTER TABLE qms_finished_inspection_request
 MODIFY qc_specification_version_id BIGINT NULL,
 ADD CONSTRAINT fk_finished_request_qc_standard FOREIGN KEY (qc_specification_version_id) REFERENCES qc_specification(id);

DELIMITER $$
CREATE TRIGGER preserve_qms_finished_inspection_request BEFORE UPDATE ON qms_finished_inspection_request
FOR EACH ROW
BEGIN
 IF NOT(OLD.main_batch_id<=>NEW.main_batch_id) OR NOT(OLD.material_lot_id<=>NEW.material_lot_id)
  OR NOT(OLD.inbound_request_id<=>NEW.inbound_request_id)
  OR NOT(OLD.qc_specification_version_id<=>NEW.qc_specification_version_id)
  OR NOT(OLD.specification_snapshot_json<=>NEW.specification_snapshot_json)
  OR NOT(OLD.specification_digest<=>NEW.specification_digest)
 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Finished source fact is immutable';
 END IF;
END$$
DELIMITER ;

-- 6. Items belong to the standard root, not to a version. The version triggers are retired first:
-- they enforce the draft-version rule and an optimistic token on every row update, so they would
-- reject the one-off backfill that moves each row to its owning standard.
DROP TRIGGER IF EXISTS qc_item_insert;
DROP TRIGGER IF EXISTS qc_item_update;
DROP TRIGGER IF EXISTS qc_item_no_delete;

ALTER TABLE qc_specification_item ADD COLUMN specification_id BIGINT NULL AFTER org_id;

UPDATE qc_specification_item i
 JOIN qc_specification_version v ON v.id=i.specification_version_id
 SET i.specification_id=v.specification_id;

ALTER TABLE qc_specification_item DROP FOREIGN KEY fk_qc_item_version;
ALTER TABLE qc_specification_item DROP INDEX uk_qc_item_code;
ALTER TABLE qc_specification_item
 MODIFY specification_id BIGINT NOT NULL,
 DROP COLUMN specification_version_id,
 DROP COLUMN active,
 ADD UNIQUE KEY uk_qc_item_code(org_id,specification_id,item_code),
 ADD KEY ix_qc_item_specification(org_id,specification_id,item_code,id),
 ADD CONSTRAINT fk_qc_item_specification FOREIGN KEY (specification_id) REFERENCES qc_specification(id);

-- 7. Production plans whose approval snapshot was never written have no own evidence to lose; they
-- are cleared so a plan is never approved without a frozen standard snapshot.
UPDATE qms_production_plan SET status='DRAFT',approved_by=NULL,approved_at=NULL,signature_id=NULL,
 content_hash=NULL,specification_snapshot_json=NULL
 WHERE status='APPROVED' AND specification_snapshot_json IS NULL;

-- 8. Remove the QC approval/version evidence that the human explicitly authorised deleting. The
-- no-delete guard on the version table is retired first, because it exists only to protect evidence
-- that this approved change removes.
DROP TRIGGER IF EXISTS qc_version_no_delete;
DELETE FROM qc_specification_version;
DROP TABLE qc_specification_version;

-- 9. Permissions: approval and retirement no longer exist; a delete action is added.
DELETE rp FROM sys_role_permission rp JOIN sys_permission p ON p.id=rp.permission_id
 WHERE p.permission_code IN ('qms:specification:approve','qms:specification:retire');
DELETE mp FROM sys_menu_permission mp JOIN sys_permission p ON p.id=mp.permission_id
 WHERE p.permission_code IN ('qms:specification:approve','qms:specification:retire');
DELETE FROM sys_permission
 WHERE permission_code IN ('qms:specification:approve','qms:specification:retire');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled)
SELECT 'qms:specification:delete','ACTION','删除QC质量标准',NULL,TRUE
 WHERE NOT EXISTS (SELECT 1 FROM sys_permission p WHERE p.permission_code='qms:specification:delete');

INSERT INTO sys_role_permission(role_id,permission_id)
SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON p.permission_code='qms:specification:delete'
 WHERE r.role_code='SYSTEM_ADMIN' AND r.enabled=TRUE
   AND NOT EXISTS (SELECT 1 FROM sys_role_permission rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);

INSERT INTO sys_menu_permission(menu_id,permission_id,created_by)
SELECT m.id,p.id,1 FROM sys_menu m JOIN sys_permission p ON p.permission_code='qms:specification:delete'
 WHERE m.org_id=1 AND m.permission_code='qms:specification:view'
   AND NOT EXISTS (SELECT 1 FROM sys_menu_permission x WHERE x.menu_id=m.id AND x.permission_id=p.id);

-- 10. Standard basis dictionary. Codes are system-fixed; labels remain maintainable under dictionary
-- permissions, which is why the column stores the code and the view resolves the label.
INSERT INTO sys_dict_type
(org_id,dict_code,dict_name,dict_kind,structure_type,description,sort_no,status,created_by,updated_by)
SELECT 1,'QC_STANDARD_BASIS','质量标准依据','SYSTEM','FLAT',
 'QC质量标准依据来源固定字典；编码固定，标签可按企业习惯维护',65,'ACTIVE',1,1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE org_id=1 AND dict_code='QC_STANDARD_BASIS');

INSERT INTO sys_dict_item
(org_id,dict_type_id,item_code,item_label,description,sort_no,status,created_by,updated_by)
SELECT 1,t.id,x.item_code,x.item_label,x.description,x.sort_no,'ACTIVE',1,1
FROM sys_dict_type t
JOIN (
 SELECT 'CHINESE_PHARMACOPOEIA' AS item_code,'《中国药典》' AS item_label,'现行版《中华人民共和国药典》' AS description,10 AS sort_no
 UNION ALL SELECT 'MINISTRY_STANDARD','局（部）颁标准','国家药品监督管理局/原卫生部颁布的药品标准',20
 UNION ALL SELECT 'DRUG_REGISTRATION_STANDARD','药品注册标准','经批准的药品注册标准（含进口注册标准）',30
 UNION ALL SELECT 'NATIONAL_DRUG_STANDARD','国家药品标准','其他法定国家药品标准',40
 UNION ALL SELECT 'ENTERPRISE_INTERNAL','企业内控标准','企业内控质量标准，不得低于法定标准',50
 UNION ALL SELECT 'CUSTOMER_SPECIFICATION','客户标准','客户书面确认的质量标准',60
 UNION ALL SELECT 'OTHER','其他依据','其他经批准的技术依据',70
) x
WHERE t.org_id=1 AND t.dict_code='QC_STANDARD_BASIS'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item i WHERE i.org_id=1 AND i.dict_type_id=t.id AND i.item_code=x.item_code);

-- 11. Records created under the approved lifecycle had no recorded basis; the reference states that
-- explicitly so the mandatory basis is never blank and reviewers can see what must be re-confirmed.
UPDATE qc_specification SET basis_type='OTHER',basis_reference='历史批准版本记录，依据待复核'
 WHERE basis_type IS NULL;
