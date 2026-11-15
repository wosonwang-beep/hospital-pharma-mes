-- INCOMING-QMS-CROSSROLE-002 G4-BATCH-A only.
-- V016..V066 are immutable. Warehouse signature and SAMPLE_OUT are deferred to V068.

ALTER TABLE qms_inspection_request
  MODIFY qc_specification_version_id BIGINT NULL,
  MODIFY specification_content_hash CHAR(64) NULL,
  ADD COLUMN initial_material_lot_id BIGINT
    GENERATED ALWAYS AS (CASE WHEN request_type='INITIAL' THEN material_lot_id ELSE NULL END) STORED,
  ADD UNIQUE KEY uk_qms_initial_request_lot (org_id,initial_material_lot_id),
  ADD CONSTRAINT chk_qms_request_spec_pair
    CHECK ((qc_specification_version_id IS NULL)=(specification_content_hash IS NULL)),
  ADD CONSTRAINT chk_qms_submitted_pending_spec
    CHECK (qc_specification_version_id IS NOT NULL OR status='SUBMITTED');

ALTER TABLE qms_sampling_task
  ADD COLUMN planned_sampling_quantity DECIMAL(24,8) NULL,
  ADD COLUMN planned_unit_id BIGINT NULL,
  ADD COLUMN qc_auto_generated_request_id BIGINT NULL,
  ADD CONSTRAINT fk_qms_sampling_planned_unit FOREIGN KEY(planned_unit_id) REFERENCES md_unit(id),
  ADD CONSTRAINT fk_qms_sampling_auto_request FOREIGN KEY(qc_auto_generated_request_id) REFERENCES qms_inspection_request(id),
  ADD UNIQUE KEY uk_qms_qc_autosampling (org_id,qc_auto_generated_request_id),
  ADD CONSTRAINT chk_qms_sampling_plan_quantity CHECK (planned_sampling_quantity IS NULL OR planned_sampling_quantity>0),
  ADD CONSTRAINT chk_qms_sampling_plan_pair CHECK ((planned_sampling_quantity IS NULL)=(planned_unit_id IS NULL)),
  ADD CONSTRAINT chk_qms_sampling_auto_plan CHECK (qc_auto_generated_request_id IS NULL OR planned_sampling_quantity IS NOT NULL);

ALTER TABLE qms_inspection_task
  MODIFY sample_id BIGINT NULL,
  ADD COLUMN qc_auto_generated_request_id BIGINT NULL,
  ADD CONSTRAINT fk_qms_inspection_auto_request FOREIGN KEY(qc_auto_generated_request_id) REFERENCES qms_inspection_request(id),
  ADD UNIQUE KEY uk_qms_qc_autoinspection (org_id,qc_auto_generated_request_id),
  ADD CONSTRAINT chk_qms_task_sample_ready CHECK (sample_id IS NOT NULL OR status='CREATED'),
  ADD CONSTRAINT chk_qms_waiting_sample_origin CHECK (sample_id IS NOT NULL OR qc_auto_generated_request_id IS NOT NULL);
