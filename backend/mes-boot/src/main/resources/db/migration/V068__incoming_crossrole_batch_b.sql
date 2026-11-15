-- INCOMING-QMS-CROSSROLE-002 batch B. V067 and all prior migrations are immutable.
ALTER TABLE qms_sampling_task
  ADD COLUMN submitted_by BIGINT NULL,
  ADD COLUMN submitted_at DATETIME(3) NULL,
  ADD COLUMN warehouse_confirmed_by BIGINT NULL,
  ADD COLUMN warehouse_confirmed_at DATETIME(3) NULL,
  ADD COLUMN warehouse_signature_id BIGINT NULL,
  ADD COLUMN warehouse_signature_evidence_json LONGTEXT NULL,
  ADD CONSTRAINT fk_sampling_task_submitted_by FOREIGN KEY(submitted_by) REFERENCES sys_user(id),
  ADD CONSTRAINT fk_sampling_task_warehouse_confirmed_by FOREIGN KEY(warehouse_confirmed_by) REFERENCES sys_user(id),
  ADD CONSTRAINT fk_sampling_task_warehouse_signature FOREIGN KEY(warehouse_signature_id) REFERENCES gxp_signature(id),
  ADD CONSTRAINT chk_sampling_task_warehouse_signature_json CHECK(warehouse_signature_evidence_json IS NULL OR JSON_VALID(warehouse_signature_evidence_json)),
  ADD CONSTRAINT chk_sampling_task_warehouse_signature_pair CHECK((warehouse_signature_id IS NULL)=(warehouse_signature_evidence_json IS NULL)),
  ADD CONSTRAINT chk_sampling_task_submission_pair CHECK((submitted_by IS NULL)=(submitted_at IS NULL)),
  ADD CONSTRAINT chk_sampling_task_warehouse_pair CHECK((warehouse_confirmed_by IS NULL)=(warehouse_confirmed_at IS NULL));

ALTER TABLE qms_sampling_detail
  ADD COLUMN source_location_id BIGINT NULL,
  ADD COLUMN source_container_id BIGINT NULL,
  ADD COLUMN sample_outbound_ledger_id BIGINT NULL,
  ADD CONSTRAINT fk_sampling_detail_source_location FOREIGN KEY(source_location_id) REFERENCES wms_location(id),
  ADD CONSTRAINT fk_sampling_detail_source_container FOREIGN KEY(source_container_id) REFERENCES wms_container(id),
  ADD CONSTRAINT fk_sampling_detail_outbound_ledger FOREIGN KEY(sample_outbound_ledger_id) REFERENCES wms_inventory_ledger(id),
  ADD UNIQUE KEY uk_sampling_detail_outbound_ledger(org_id,sample_outbound_ledger_id);

ALTER TABLE qms_inspection_report
  ADD COLUMN auto_generated_request_id BIGINT NULL,
  ADD CONSTRAINT fk_inspection_report_auto_request FOREIGN KEY(auto_generated_request_id) REFERENCES qms_inspection_request(id),
  ADD UNIQUE KEY uk_inspection_report_auto_request(org_id,auto_generated_request_id);

SET @sampling_status_check=(SELECT tc.CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS tc JOIN information_schema.CHECK_CONSTRAINTS cc ON cc.CONSTRAINT_SCHEMA=tc.CONSTRAINT_SCHEMA AND cc.CONSTRAINT_NAME=tc.CONSTRAINT_NAME WHERE tc.CONSTRAINT_SCHEMA=DATABASE() AND tc.TABLE_NAME='qms_sampling_task' AND tc.CONSTRAINT_TYPE='CHECK' AND cc.CHECK_CLAUSE LIKE '%status%IN (%PLANNED%' LIMIT 1);
SET @drop_sampling_status=IF(@sampling_status_check IS NULL,'SELECT 1',CONCAT('ALTER TABLE qms_sampling_task DROP CONSTRAINT `',@sampling_status_check,'`'));
PREPARE sampling_stmt FROM @drop_sampling_status; EXECUTE sampling_stmt; DEALLOCATE PREPARE sampling_stmt;
ALTER TABLE qms_sampling_task ADD CONSTRAINT chk_sampling_task_status_v068 CHECK(status IN ('PLANNED','ASSIGNED','IN_PROGRESS','PENDING_WAREHOUSE','COMPLETED'));

DROP TRIGGER bu_qms_sampling_task;
DELIMITER $$
CREATE TRIGGER bu_qms_sampling_task BEFORE UPDATE ON qms_sampling_task FOR EACH ROW BEGIN
 IF (OLD.plan_signature_id IS NOT NULL AND NOT (OLD.plan_signature_id <=> NEW.plan_signature_id))
 OR (OLD.plan_signature_evidence_json IS NOT NULL AND NOT (OLD.plan_signature_evidence_json <=> NEW.plan_signature_evidence_json))
 OR (OLD.completion_signature_id IS NOT NULL AND NOT (OLD.completion_signature_id <=> NEW.completion_signature_id))
 OR (OLD.completion_signature_evidence_json IS NOT NULL AND NOT (OLD.completion_signature_evidence_json <=> NEW.completion_signature_evidence_json))
 OR (OLD.warehouse_signature_id IS NOT NULL AND NOT (OLD.warehouse_signature_id <=> NEW.warehouse_signature_id))
 OR (OLD.warehouse_signature_evidence_json IS NOT NULL AND NOT (OLD.warehouse_signature_evidence_json <=> NEW.warehouse_signature_evidence_json))
 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable signature evidence'; END IF;
END$$
DELIMITER ;

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:sampling:warehouse-confirm','ACTION','qms:sampling:warehouse-confirm',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:sampling:warehouse-confirm');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'wms:inventory:sample-outbound','ACTION','wms:inventory:sample-outbound',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='wms:inventory:sample-outbound');
INSERT INTO sys_role_permission(role_id,permission_id) SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON p.permission_code IN ('qms:sampling:warehouse-confirm','wms:inventory:sample-outbound') WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_permission rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
