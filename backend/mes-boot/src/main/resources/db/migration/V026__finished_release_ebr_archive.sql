-- Approved DCP-MES-012-013-CONTRACT-001, MES-013. Highest success verified25.
-- Append only: no executed migration/history/configuration rewrite.
ALTER TABLE qms_release_decision ADD CONSTRAINT ck_finished_release_target
 CHECK(release_scope<>'FINISHED_PRODUCT' OR
  (finished_lot_id IS NOT NULL AND inspection_report_id IS NULL
   AND decision_source='USER_QA' AND decision IN ('RELEASED','REJECTED')
   AND release_basis='FULL_INSPECTION'));

CREATE TABLE ebr_pdf_manifest (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 main_batch_id BIGINT NOT NULL, generation_version INT NOT NULL CHECK(generation_version>0),
 definition_hash CHAR(64) NOT NULL, record_digest CHAR(64) NOT NULL,
 file_id BIGINT NOT NULL, file_hash CHAR(64) NOT NULL,
 generated_by BIGINT NOT NULL, generated_at DATETIME(3) NOT NULL,
 archive_kind VARCHAR(30) NOT NULL CHECK(archive_kind IN ('REVIEW_COPY','FINAL')),
 release_decision_id BIGINT NULL,
 CONSTRAINT fk_pdf_batch FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id),
 CONSTRAINT fk_pdf_file FOREIGN KEY(file_id) REFERENCES gxp_attachment(id),
 CONSTRAINT fk_pdf_generator FOREIGN KEY(generated_by) REFERENCES sys_user(id),
 CONSTRAINT fk_pdf_creator FOREIGN KEY(created_by) REFERENCES sys_user(id),
 CONSTRAINT fk_pdf_decision FOREIGN KEY(release_decision_id) REFERENCES qms_release_decision(id),
 UNIQUE KEY uk_pdf_generation(org_id,main_batch_id,generation_version),
 UNIQUE KEY uk_pdf_source_digest(org_id,main_batch_id,archive_kind,record_digest),
 CHECK((archive_kind='REVIEW_COPY' AND release_decision_id IS NULL)
    OR (archive_kind='FINAL' AND release_decision_id IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TRIGGER bu_ebr_pdf_manifest BEFORE UPDATE ON ebr_pdf_manifest FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable eBR archive';
CREATE TRIGGER bd_ebr_pdf_manifest BEFORE DELETE ON ebr_pdf_manifest FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable eBR archive';

INSERT INTO sys_permission(permission_code,permission_type,display_name,enabled)
 SELECT p.code,'ACTION',p.code,TRUE FROM (
  SELECT 'qa:batch-review' AS code UNION ALL SELECT 'qa:release' UNION ALL SELECT 'ebr:pdf:generate'
 ) p WHERE NOT EXISTS(SELECT 1 FROM sys_permission old WHERE old.permission_code=p.code);
INSERT INTO sys_role_permission(role_id,permission_id)
 SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON
  (r.role_code='SYSTEM_ADMIN' AND p.permission_code='qa:batch-review')
  OR (r.role_code='QA_APPROVER' AND p.permission_code IN ('qa:batch-review','qa:release','ebr:pdf:generate'))
  OR (r.role_code='QC_REVIEWER' AND p.permission_code='qa:batch-review')
 WHERE NOT EXISTS(SELECT 1 FROM sys_role_permission old WHERE old.role_id=r.id AND old.permission_id=p.id);
