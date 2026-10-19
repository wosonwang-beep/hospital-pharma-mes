-- SUPERSEDED BY 2026-10-11 DIRECT PRODUCT + MAINBATCH DESIGN. DO NOT APPLY.
-- DCP-PRODUCT-FINISHED-ROOT-001: additive legacy compatibility; no trigger/account changes.
-- NOT APPLIED TO DEV. New ledger immutability is application-owned, not arbitrary-SQL protection.
ALTER TABLE md_product ADD UNIQUE KEY uk_product_org_identity (org_id,id);
ALTER TABLE prd_main_batch ADD UNIQUE KEY uk_batch_org_product_identity (org_id,id,product_id);
ALTER TABLE qc_specification
 MODIFY material_id BIGINT NULL,
 ADD subject_type VARCHAR(20) NOT NULL DEFAULT 'MATERIAL',
 ADD product_id BIGINT NULL,
 ADD CONSTRAINT fk_qc_product_subject FOREIGN KEY (product_id) REFERENCES md_product(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_qc_subject_identity CHECK (
   (subject_type='MATERIAL' AND material_id IS NOT NULL AND product_id IS NULL) OR
   (subject_type='PRODUCT' AND material_id IS NULL AND product_id IS NOT NULL));
ALTER TABLE qc_specification_version ADD definition_schema_version SMALLINT NOT NULL DEFAULT 1,
 ADD CONSTRAINT ck_qc_definition_schema CHECK (definition_schema_version IN (1,2));
ALTER TABLE qms_production_plan MODIFY finished_material_id BIGINT NULL,
 ADD plan_schema_version SMALLINT NOT NULL DEFAULT 1,
 ADD CONSTRAINT ck_plan_schema_subject CHECK (
  (plan_schema_version=1 AND finished_material_id IS NOT NULL) OR
  (plan_schema_version=2 AND finished_material_id IS NULL));
CREATE TABLE wms_finished_lot (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 main_batch_id BIGINT NOT NULL, product_id BIGINT NOT NULL,
 lot_no VARCHAR(100) NOT NULL, manufacture_date DATE NOT NULL, expiry_date DATE NOT NULL,
 base_unit_id BIGINT NOT NULL,
 product_snapshot_json LONGTEXT NOT NULL, product_snapshot_digest CHAR(64) NOT NULL,
 quality_status VARCHAR(30) NOT NULL, inventory_status VARCHAR(30) NOT NULL,
 UNIQUE KEY uk_finished_batch (org_id,main_batch_id),
 UNIQUE KEY uk_finished_lot_no (org_id,lot_no),
 UNIQUE KEY uk_finished_lot_unit (org_id,id,base_unit_id),
 CONSTRAINT fk_finished_lot_batch_product FOREIGN KEY (org_id,main_batch_id,product_id)
   REFERENCES prd_main_batch(org_id,id,product_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_finished_lot_product FOREIGN KEY (org_id,product_id)
   REFERENCES md_product(org_id,id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_finished_lot_unit FOREIGN KEY (base_unit_id) REFERENCES md_unit(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT ck_finished_lot_dates CHECK (expiry_date>manufacture_date),
 CONSTRAINT ck_finished_product_snapshot CHECK (JSON_VALID(product_snapshot_json)),
 CONSTRAINT ck_finished_product_digest CHECK (product_snapshot_digest REGEXP '^[a-f0-9]{64}$'),
 CONSTRAINT ck_finished_quality CHECK (quality_status IN ('QUARANTINE','RELEASED','REJECTED')),
 CONSTRAINT ck_finished_availability CHECK (inventory_status IN ('BLOCKED','AVAILABLE','FROZEN'))
) ENGINE=InnoDB;
CREATE TABLE wms_finished_inventory_ledger (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 finished_product_lot_id BIGINT NOT NULL, location_id BIGINT NOT NULL, unit_id BIGINT NOT NULL,
 event_type VARCHAR(30) NOT NULL, delta_qty DECIMAL(18,6) NOT NULL,
 inbound_request_id BIGINT NULL, shipment_id BIGINT NULL,
 idempotency_key VARCHAR(100) NOT NULL, occurred_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_finished_ledger_key (org_id,idempotency_key),
 UNIQUE KEY uk_finished_receipt_source (org_id,inbound_request_id),
 UNIQUE KEY uk_finished_shipment_source (org_id,shipment_id),
 CONSTRAINT fk_finished_ledger_lot_unit FOREIGN KEY (org_id,finished_product_lot_id,unit_id)
  REFERENCES wms_finished_lot(org_id,id,base_unit_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_finished_ledger_location FOREIGN KEY (location_id) REFERENCES wms_location(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_finished_ledger_unit FOREIGN KEY (unit_id) REFERENCES md_unit(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_finished_ledger_inbound FOREIGN KEY (inbound_request_id) REFERENCES wms_finished_inbound_request(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_finished_ledger_shipment FOREIGN KEY (shipment_id) REFERENCES wms_finished_shipment(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT ck_finished_ledger_source_sign CHECK (
  (event_type='FINISHED_RECEIPT' AND delta_qty>0 AND inbound_request_id IS NOT NULL AND shipment_id IS NULL) OR
  (event_type='FINISHED_SHIPMENT' AND delta_qty<0 AND shipment_id IS NOT NULL AND inbound_request_id IS NULL))
) ENGINE=InnoDB;
ALTER TABLE prd_main_batch
 ADD finished_product_lot_id BIGINT NULL,
 ADD CONSTRAINT fk_batch_product_lot FOREIGN KEY (finished_product_lot_id) REFERENCES wms_finished_lot(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_batch_lot_identity CHECK ((finished_lot_id IS NOT NULL) + (finished_product_lot_id IS NOT NULL) <= 1);
ALTER TABLE mes_quantity_event
 ADD finished_product_lot_id BIGINT NULL,
 ADD CONSTRAINT fk_quantity_product_lot FOREIGN KEY (finished_product_lot_id) REFERENCES wms_finished_lot(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_quantity_lot_identity CHECK ((material_lot_id IS NOT NULL) + (finished_product_lot_id IS NOT NULL) <= 1);
ALTER TABLE qms_sample
 ADD finished_product_lot_id BIGINT NULL,
 ADD CONSTRAINT fk_sample_product_lot FOREIGN KEY (finished_product_lot_id) REFERENCES wms_finished_lot(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_sample_lot_identity CHECK ((material_lot_id IS NOT NULL) + (finished_product_lot_id IS NOT NULL) <= 1);
ALTER TABLE qms_release_decision
 ADD finished_product_lot_id BIGINT NULL,
 ADD CONSTRAINT fk_qa_decision_product_lot FOREIGN KEY (finished_product_lot_id) REFERENCES wms_finished_lot(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_qa_decision_lot_identity CHECK ((finished_lot_id IS NOT NULL) + (finished_product_lot_id IS NOT NULL) <= 1);
ALTER TABLE wms_finished_inbound_request
 MODIFY material_lot_id BIGINT NULL,
 ADD finished_product_lot_id BIGINT NULL,
 ADD CONSTRAINT fk_inbound_product_lot FOREIGN KEY (finished_product_lot_id) REFERENCES wms_finished_lot(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_inbound_lot_identity CHECK ((material_lot_id IS NOT NULL) + (finished_product_lot_id IS NOT NULL) = 1);
ALTER TABLE wms_finished_shipment
 MODIFY material_lot_id BIGINT NULL,
 ADD finished_product_lot_id BIGINT NULL,
 ADD CONSTRAINT fk_shipment_product_lot FOREIGN KEY (finished_product_lot_id) REFERENCES wms_finished_lot(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_shipment_lot_identity CHECK ((material_lot_id IS NOT NULL) + (finished_product_lot_id IS NOT NULL) = 1);
ALTER TABLE qms_finished_inspection_request
 MODIFY material_lot_id BIGINT NULL,
 ADD finished_product_lot_id BIGINT NULL,
 ADD CONSTRAINT fk_inspection_product_lot FOREIGN KEY (finished_product_lot_id) REFERENCES wms_finished_lot(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_inspection_lot_identity CHECK ((material_lot_id IS NOT NULL) + (finished_product_lot_id IS NOT NULL) = 1);
ALTER TABLE qms_finished_sampling_record
 MODIFY material_lot_id BIGINT NULL,
 ADD finished_product_lot_id BIGINT NULL,
 ADD CONSTRAINT fk_sampling_product_lot FOREIGN KEY (finished_product_lot_id) REFERENCES wms_finished_lot(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_sampling_lot_identity CHECK ((material_lot_id IS NOT NULL) + (finished_product_lot_id IS NOT NULL) = 1);
ALTER TABLE mes_quantity_event ADD CONSTRAINT ck_product_quantity_kind CHECK (
 finished_product_lot_id IS NULL OR event_type IN ('OUTPUT','SAMPLE','LOSS','SCRAP','WIP','REVERSAL'));
ALTER TABLE qms_sample ADD CONSTRAINT ck_product_sample_scope CHECK (
 finished_product_lot_id IS NULL OR sample_scope='PRODUCTION');
ALTER TABLE qms_release_decision DROP CONSTRAINT ck_finished_release_target,
 ADD CONSTRAINT ck_finished_release_target CHECK (release_scope<>'FINISHED_PRODUCT' OR (
  (finished_lot_id IS NOT NULL)+(finished_product_lot_id IS NOT NULL)=1 AND
  inspection_report_id IS NULL AND decision_source='USER_QA' AND
  decision IN ('RELEASED','REJECTED') AND release_basis='FULL_INSPECTION')),
 ADD CONSTRAINT ck_incoming_no_product_lot CHECK (release_scope<>'INCOMING_MATERIAL' OR finished_product_lot_id IS NULL);
ALTER TABLE wms_finished_inbound_request ADD finished_ledger_id BIGINT NULL,
 ADD CONSTRAINT fk_inbound_finished_ledger FOREIGN KEY (finished_ledger_id) REFERENCES wms_finished_inventory_ledger(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_inbound_ledger_identity CHECK (
  (material_lot_id IS NOT NULL AND finished_ledger_id IS NULL) OR
  (finished_product_lot_id IS NOT NULL AND ledger_id IS NULL AND (status<>'CONFIRMED' OR finished_ledger_id IS NOT NULL)));
ALTER TABLE wms_finished_shipment ADD finished_ledger_id BIGINT NULL,
 ADD CONSTRAINT fk_shipment_finished_ledger FOREIGN KEY (finished_ledger_id) REFERENCES wms_finished_inventory_ledger(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_shipment_ledger_identity CHECK (
  (material_lot_id IS NOT NULL AND finished_ledger_id IS NULL) OR
  (finished_product_lot_id IS NOT NULL AND ledger_id IS NULL AND (status<>'CONFIRMED' OR finished_ledger_id IS NOT NULL)));
