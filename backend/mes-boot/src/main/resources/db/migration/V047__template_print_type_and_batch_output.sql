-- User approved template-owned DOCUMENT/LIST and selected-record printing, 2026-10-09.
-- Existing versions are legacy DOCUMENT. Historical source bytes and artifacts remain unchanged.
ALTER TABLE mes_print_template_version ADD COLUMN print_type VARCHAR(16) NOT NULL DEFAULT 'DOCUMENT',
 ADD CONSTRAINT ck_print_template_type CHECK (print_type IN ('DOCUMENT','LIST'));
CREATE TABLE mes_print_batch (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
 business_type VARCHAR(60) NOT NULL, template_version_id BIGINT NOT NULL,
 print_type VARCHAR(16) NOT NULL, formal BOOLEAN NOT NULL,
 record_ids_json LONGTEXT NOT NULL, source_artifact_ids_json LONGTEXT NOT NULL,
 snapshot_json LONGTEXT NOT NULL, snapshot_hash CHAR(64) NOT NULL,
 pdf LONGBLOB NOT NULL, pdf_hash CHAR(64) NOT NULL,
 created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL,updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL,version_no BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT ck_print_batch_type CHECK (print_type IN ('DOCUMENT','LIST')),
 CONSTRAINT ck_print_batch_records CHECK (JSON_VALID(record_ids_json)),
 CONSTRAINT ck_print_batch_sources CHECK (JSON_VALID(source_artifact_ids_json)),
 CONSTRAINT ck_print_batch_snapshot CHECK (JSON_VALID(snapshot_json)),
 CONSTRAINT ck_print_batch_formal CHECK (print_type='DOCUMENT' OR formal=FALSE),
 CONSTRAINT fk_print_batch_template FOREIGN KEY(org_id,template_version_id) REFERENCES mes_print_template_version(org_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
