# Exact approved physical schema — v1.0.20

V030 after native29, V031 after verified30; no executed migration edits. Common scoped ProfileM metadata / optimistic mutable roots, append-only sampling/review. Existing Sample/TestResult remains sole raw facts. Historical ReleaseDecision report refs nullable with no backfill. Scope validation additionally enforced in application.

## V030__finished_goods_controlled_chain.sql

```sql
-- Approved DCP-FINISHED-GOODS-CHAIN-001. Native highest29/success29/failure0 verified2026-10-06.

-- Append-only; preserve prior executed migration and original facts.

CREATE TABLE wms_finished_inbound_request (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 request_no VARCHAR(80) NOT NULL,
 main_batch_id BIGINT NOT NULL,
 FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id),
 material_lot_id BIGINT NOT NULL,
 FOREIGN KEY(material_lot_id) REFERENCES md_material_lot(id),
 product_id BIGINT NOT NULL,
 FOREIGN KEY(product_id) REFERENCES md_product(id),
 quantity DECIMAL(18,6) NOT NULL CHECK(quantity>0),
 unit_id BIGINT NOT NULL,
 FOREIGN KEY(unit_id) REFERENCES md_unit(id),
 status VARCHAR(80) NOT NULL CHECK(status IN ('DRAFT','SUBMITTED','CONFIRMED','CANCELLED')),
 source_snapshot_json LONGTEXT NOT NULL CHECK(source_snapshot_json IS NULL OR JSON_VALID(source_snapshot_json)),
 source_digest CHAR(64) NOT NULL,
 submitted_by BIGINT NULL,
 FOREIGN KEY(submitted_by) REFERENCES sys_user(id),
 submitted_at DATETIME(3) NULL,
 confirmed_by BIGINT NULL,
 FOREIGN KEY(confirmed_by) REFERENCES sys_user(id),
 confirmed_at DATETIME(3) NULL,
 location_id BIGINT NULL,
 FOREIGN KEY(location_id) REFERENCES wms_location(id),
 ledger_id BIGINT NULL,
 FOREIGN KEY(ledger_id) REFERENCES wms_inventory_ledger(id),
 signature_id BIGINT NULL,
 FOREIGN KEY(signature_id) REFERENCES gxp_signature(id),
 signature_evidence_json LONGTEXT NULL CHECK(signature_evidence_json IS NULL OR JSON_VALID(signature_evidence_json)),
 reason VARCHAR(1000) NOT NULL,
 UNIQUE KEY uk_finished_inbound_requestrequest_no(org_id,request_no),
 UNIQUE KEY uk_finished_inbound_requestmain_batch_id(org_id,main_batch_id),
 CHECK((signature_id IS NULL)=(signature_evidence_json IS NULL)),
 CHECK(status<>'CONFIRMED' OR (confirmed_by IS NOT NULL AND confirmed_at IS NOT NULL AND signature_id IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qms_finished_inspection_request (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 inspection_request_no VARCHAR(80) NOT NULL,
 main_batch_id BIGINT NOT NULL,
 FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id),
 material_lot_id BIGINT NOT NULL,
 FOREIGN KEY(material_lot_id) REFERENCES md_material_lot(id),
 inbound_request_id BIGINT NOT NULL,
 FOREIGN KEY(inbound_request_id) REFERENCES wms_finished_inbound_request(id),
 qc_specification_version_id BIGINT NOT NULL,
 FOREIGN KEY(qc_specification_version_id) REFERENCES qc_specification_version(id),
 specification_snapshot_json LONGTEXT NOT NULL CHECK(specification_snapshot_json IS NULL OR JSON_VALID(specification_snapshot_json)),
 specification_digest CHAR(64) NOT NULL,
 status VARCHAR(80) NOT NULL CHECK(status IN ('DRAFT','SUBMITTED','ACCEPTED','COMPLETED')),
 submitted_by BIGINT NULL,
 FOREIGN KEY(submitted_by) REFERENCES sys_user(id),
 submitted_at DATETIME(3) NULL,
 accepted_by BIGINT NULL,
 FOREIGN KEY(accepted_by) REFERENCES sys_user(id),
 accepted_at DATETIME(3) NULL,
 reason VARCHAR(1000) NOT NULL,
 UNIQUE KEY uk_finished_inspection_requestinspection_request_no(org_id,inspection_request_no),
 UNIQUE KEY uk_finished_inspection_requestinbound_request_id(org_id,inbound_request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qms_finished_sampling_record (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 inspection_request_id BIGINT NOT NULL,
 FOREIGN KEY(inspection_request_id) REFERENCES qms_finished_inspection_request(id),
 sample_id BIGINT NOT NULL,
 FOREIGN KEY(sample_id) REFERENCES qms_sample(id),
 material_lot_id BIGINT NOT NULL,
 FOREIGN KEY(material_lot_id) REFERENCES md_material_lot(id),
 sampling_no VARCHAR(80) NOT NULL,
 sampling_location VARCHAR(200) NOT NULL,
 quantity DECIMAL(18,6) NOT NULL CHECK(quantity>0),
 unit_id BIGINT NOT NULL,
 FOREIGN KEY(unit_id) REFERENCES md_unit(id),
 sampling_method VARCHAR(1000) NOT NULL,
 sampled_by BIGINT NOT NULL,
 FOREIGN KEY(sampled_by) REFERENCES sys_user(id),
 sampled_at DATETIME(3) NOT NULL,
 signature_id BIGINT NULL,
 FOREIGN KEY(signature_id) REFERENCES gxp_signature(id),
 signature_evidence_json LONGTEXT NULL CHECK(signature_evidence_json IS NULL OR JSON_VALID(signature_evidence_json)),
 reason VARCHAR(1000) NOT NULL,
 UNIQUE KEY uk_finished_sampling_recordsampling_no(org_id,sampling_no),
 UNIQUE KEY uk_finished_sampling_recordsample_id(org_id,sample_id),
 CHECK((signature_id IS NULL)=(signature_evidence_json IS NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qms_finished_inspection_report (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 report_no VARCHAR(80) NOT NULL,
 inspection_request_id BIGINT NOT NULL,
 FOREIGN KEY(inspection_request_id) REFERENCES qms_finished_inspection_request(id),
 generation_no BIGINT NOT NULL,
 overall_conclusion VARCHAR(80) NOT NULL CHECK(overall_conclusion IN ('PASS','FAIL')),
 status VARCHAR(80) NOT NULL CHECK(status IN ('GENERATED','APPROVED')),
 evidence_digest CHAR(64) NOT NULL,
 summary_json LONGTEXT NOT NULL CHECK(summary_json IS NULL OR JSON_VALID(summary_json)),
 generated_by BIGINT NOT NULL,
 FOREIGN KEY(generated_by) REFERENCES sys_user(id),
 generated_at DATETIME(3) NOT NULL,
 approved_by BIGINT NULL,
 FOREIGN KEY(approved_by) REFERENCES sys_user(id),
 approved_at DATETIME(3) NULL,
 signature_id BIGINT NULL,
 FOREIGN KEY(signature_id) REFERENCES gxp_signature(id),
 signature_evidence_json LONGTEXT NULL CHECK(signature_evidence_json IS NULL OR JSON_VALID(signature_evidence_json)),
 reason VARCHAR(1000) NOT NULL,
 UNIQUE KEY uk_finished_inspection_reportreport_no(org_id,report_no),
 UNIQUE KEY uk_finished_report_generation(org_id,inspection_request_id,generation_no),
 CHECK((signature_id IS NULL)=(signature_evidence_json IS NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE qms_finished_report_review (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 report_id BIGINT NOT NULL,
 FOREIGN KEY(report_id) REFERENCES qms_finished_inspection_report(id),
 review_decision VARCHAR(80) NOT NULL CHECK(review_decision='APPROVE'),
 reviewed_by BIGINT NOT NULL,
 FOREIGN KEY(reviewed_by) REFERENCES sys_user(id),
 reviewed_at DATETIME(3) NOT NULL,
 signature_id BIGINT NULL,
 FOREIGN KEY(signature_id) REFERENCES gxp_signature(id),
 signature_evidence_json LONGTEXT NULL CHECK(signature_evidence_json IS NULL OR JSON_VALID(signature_evidence_json)),
 reason VARCHAR(1000) NOT NULL,
 CHECK((signature_id IS NULL)=(signature_evidence_json IS NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE wms_finished_shipment (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 shipment_no VARCHAR(80) NOT NULL,
 main_batch_id BIGINT NOT NULL,
 FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id),
 material_lot_id BIGINT NOT NULL,
 FOREIGN KEY(material_lot_id) REFERENCES md_material_lot(id),
 location_id BIGINT NOT NULL,
 FOREIGN KEY(location_id) REFERENCES wms_location(id),
 quantity DECIMAL(18,6) NOT NULL CHECK(quantity>0),
 unit_id BIGINT NOT NULL,
 FOREIGN KEY(unit_id) REFERENCES md_unit(id),
 receiving_party VARCHAR(200) NOT NULL,
 status VARCHAR(80) NOT NULL CHECK(status IN ('DRAFT','CONFIRMED','CANCELLED')),
 release_decision_id BIGINT NULL,
 FOREIGN KEY(release_decision_id) REFERENCES qms_release_decision(id),
 ledger_id BIGINT NULL,
 FOREIGN KEY(ledger_id) REFERENCES wms_inventory_ledger(id),
 confirmed_by BIGINT NULL,
 FOREIGN KEY(confirmed_by) REFERENCES sys_user(id),
 confirmed_at DATETIME(3) NULL,
 signature_id BIGINT NULL,
 FOREIGN KEY(signature_id) REFERENCES gxp_signature(id),
 signature_evidence_json LONGTEXT NULL CHECK(signature_evidence_json IS NULL OR JSON_VALID(signature_evidence_json)),
 reason VARCHAR(1000) NOT NULL,
 UNIQUE KEY uk_finished_shipmentshipment_no(org_id,shipment_no),
 UNIQUE KEY uk_finished_shipmentledger_id(org_id,ledger_id),
 CHECK((signature_id IS NULL)=(signature_evidence_json IS NULL)),
 CHECK(status<>'CONFIRMED' OR (confirmed_by IS NOT NULL AND confirmed_at IS NOT NULL AND signature_id IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE qms_release_decision ADD COLUMN finished_inspection_report_id BIGINT NULL, ADD CONSTRAINT fk_finished_decision_report FOREIGN KEY(finished_inspection_report_id) REFERENCES qms_finished_inspection_report(id);

CREATE TRIGGER no_qms_finished_sampling_record_update BEFORE UPDATE ON qms_finished_sampling_record FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable finished quality fact';

CREATE TRIGGER no_qms_finished_sampling_record_delete BEFORE DELETE ON qms_finished_sampling_record FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable finished quality fact';

CREATE TRIGGER no_qms_finished_report_review_update BEFORE UPDATE ON qms_finished_report_review FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable finished quality fact';

CREATE TRIGGER no_qms_finished_report_review_delete BEFORE DELETE ON qms_finished_report_review FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable finished quality fact';

DELIMITER $$

CREATE TRIGGER preserve_wms_finished_inbound_request BEFORE UPDATE ON wms_finished_inbound_request FOR EACH ROW BEGIN IF NOT(OLD.main_batch_id<=>NEW.main_batch_id) OR NOT(OLD.material_lot_id<=>NEW.material_lot_id) OR NOT(OLD.quantity<=>NEW.quantity) OR NOT(OLD.unit_id<=>NEW.unit_id) OR NOT(OLD.source_digest<=>NEW.source_digest) OR NOT(OLD.source_snapshot_json<=>NEW.source_snapshot_json) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Finished source fact is immutable'; END IF; END$$

CREATE TRIGGER preserve_qms_finished_inspection_request BEFORE UPDATE ON qms_finished_inspection_request FOR EACH ROW BEGIN IF NOT(OLD.main_batch_id<=>NEW.main_batch_id) OR NOT(OLD.material_lot_id<=>NEW.material_lot_id) OR NOT(OLD.inbound_request_id<=>NEW.inbound_request_id) OR NOT(OLD.qc_specification_version_id<=>NEW.qc_specification_version_id) OR NOT(OLD.specification_snapshot_json<=>NEW.specification_snapshot_json) OR NOT(OLD.specification_digest<=>NEW.specification_digest) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Finished source fact is immutable'; END IF; END$$

CREATE TRIGGER preserve_qms_finished_inspection_report BEFORE UPDATE ON qms_finished_inspection_report FOR EACH ROW BEGIN IF NOT(OLD.inspection_request_id<=>NEW.inspection_request_id) OR NOT(OLD.report_no<=>NEW.report_no) OR NOT(OLD.generation_no<=>NEW.generation_no) OR NOT(OLD.summary_json<=>NEW.summary_json) OR NOT(OLD.overall_conclusion<=>NEW.overall_conclusion) OR NOT(OLD.evidence_digest<=>NEW.evidence_digest) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Finished source fact is immutable'; END IF; END$$

CREATE TRIGGER no_wms_finished_inbound_request_delete BEFORE DELETE ON wms_finished_inbound_request FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record cannot be deleted'$$

CREATE TRIGGER no_qms_finished_inspection_request_delete BEFORE DELETE ON qms_finished_inspection_request FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record cannot be deleted'$$

CREATE TRIGGER no_qms_finished_inspection_report_delete BEFORE DELETE ON qms_finished_inspection_report FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record cannot be deleted'$$

CREATE TRIGGER no_wms_finished_shipment_delete BEFORE DELETE ON wms_finished_shipment FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record cannot be deleted'$$

DELIMITER ;

INSERT INTO sys_permission(permission_code,permission_type,display_name,enabled) SELECT p.code,'ACTION',p.code,TRUE FROM (SELECT 'wms:finished-inbound:view' code UNION ALL SELECT 'wms:finished-inbound:create' code UNION ALL SELECT 'wms:finished-inbound:submit' code UNION ALL SELECT 'wms:finished-inbound:cancel' code UNION ALL SELECT 'wms:finished-inbound:confirm' code UNION ALL SELECT 'wms:finished-shipment:view' code UNION ALL SELECT 'wms:finished-shipment:create' code UNION ALL SELECT 'wms:finished-shipment:confirm' code UNION ALL SELECT 'wms:finished-shipment:cancel' code UNION ALL SELECT 'qms:finished-request:view' code UNION ALL SELECT 'qms:finished-request:create' code UNION ALL SELECT 'qms:finished-request:submit' code UNION ALL SELECT 'qms:finished-request:accept' code UNION ALL SELECT 'qms:finished-sampling:view' code UNION ALL SELECT 'qms:finished-sampling:create' code UNION ALL SELECT 'qms:finished-report:view' code UNION ALL SELECT 'qms:finished-report:generate' code UNION ALL SELECT 'qms:finished-report:approve' code) p WHERE NOT EXISTS(SELECT 1 FROM sys_permission old WHERE old.permission_code=p.code);

INSERT INTO sys_role_permission(role_id,permission_id) SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON p.permission_code IN ('wms:finished-inbound:view','wms:finished-inbound:create','wms:finished-inbound:submit','wms:finished-inbound:cancel','wms:finished-inbound:confirm','wms:finished-shipment:view','wms:finished-shipment:create','wms:finished-shipment:confirm','wms:finished-shipment:cancel','qms:finished-request:view','qms:finished-request:create','qms:finished-request:submit','qms:finished-request:accept','qms:finished-sampling:view','qms:finished-sampling:create','qms:finished-report:view','qms:finished-report:generate','qms:finished-report:approve') WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_permission old WHERE old.role_id=r.id AND old.permission_id=p.id);

```

## V031__finished_fact_preservation.sql

```sql
-- Additive preservation guards; executed V030 remains byte-identical.
ALTER TABLE qms_finished_sampling_record ADD CONSTRAINT ck_finished_sampling_signed CHECK(signature_id IS NOT NULL AND signature_evidence_json IS NOT NULL);
ALTER TABLE qms_finished_report_review ADD CONSTRAINT ck_finished_report_review_signed CHECK(signature_id IS NOT NULL AND signature_evidence_json IS NOT NULL);
ALTER TABLE qms_finished_inspection_report ADD CONSTRAINT ck_finished_report_approved CHECK(status<>'APPROVED' OR (approved_by IS NOT NULL AND approved_at IS NOT NULL AND signature_id IS NOT NULL AND signature_evidence_json IS NOT NULL));
DELIMITER $$
CREATE TRIGGER freeze_finished_receipt_confirmation BEFORE UPDATE ON wms_finished_inbound_request FOR EACH ROW BEGIN IF OLD.status='CONFIRMED' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Confirmed receipt cannot be overwritten'; END IF; END$$
CREATE TRIGGER freeze_finished_report_approval BEFORE UPDATE ON qms_finished_inspection_report FOR EACH ROW BEGIN IF OLD.status='APPROVED' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Approved report cannot be overwritten'; END IF; END$$
CREATE TRIGGER preserve_finished_shipment BEFORE UPDATE ON wms_finished_shipment FOR EACH ROW BEGIN IF OLD.status<>'DRAFT' OR NOT(OLD.shipment_no<=>NEW.shipment_no) OR NOT(OLD.main_batch_id<=>NEW.main_batch_id) OR NOT(OLD.material_lot_id<=>NEW.material_lot_id) OR NOT(OLD.location_id<=>NEW.location_id) OR NOT(OLD.quantity<=>NEW.quantity) OR NOT(OLD.unit_id<=>NEW.unit_id) OR NOT(OLD.receiving_party<=>NEW.receiving_party) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Finished shipment source is immutable'; END IF; END$$
DELIMITER ;

```
