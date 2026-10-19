-- DCP-PRODUCT-FINISHED-ROOT-001 / approved direct MainBatch model B.
-- One new ledger; no standalone finished batch, seed, historical rewrite, trigger or permission changes.
-- DEV application is NOT authorized in this task. Highest successful DEV version verified 054.
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
ALTER TABLE prd_main_batch
 ADD production_schema_version SMALLINT NOT NULL DEFAULT 1,
 ADD internal_order_guard BIGINT GENERATED ALWAYS AS (CASE WHEN production_schema_version=2 THEN production_order_id ELSE NULL END) STORED,
 ADD UNIQUE KEY uk_new_instruction_order (org_id,internal_order_guard),
 ADD finished_manufacture_date DATE NULL, ADD finished_expiry_date DATE NULL,
 ADD finished_base_unit_id BIGINT NULL,
 ADD finished_product_snapshot_json LONGTEXT NULL, ADD finished_product_snapshot_digest CHAR(64) NULL,
 ADD finished_quality_status VARCHAR(30) NULL, ADD finished_inventory_status VARCHAR(30) NULL,
 ADD UNIQUE KEY uk_batch_frozen_product_unit (org_id,id,product_id,finished_base_unit_id),
 ADD CONSTRAINT fk_batch_finished_base_unit FOREIGN KEY (finished_base_unit_id) REFERENCES md_unit(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_production_schema CHECK (production_schema_version IN (1,2)),
 ADD CONSTRAINT ck_direct_batch_no_material_lot CHECK (production_schema_version=1 OR finished_lot_id IS NULL),
 ADD CONSTRAINT ck_direct_finished_attributes CHECK (
  (finished_base_unit_id IS NULL AND finished_manufacture_date IS NULL AND finished_expiry_date IS NULL AND finished_product_snapshot_json IS NULL AND finished_product_snapshot_digest IS NULL AND finished_quality_status IS NULL AND finished_inventory_status IS NULL) OR
  (production_schema_version=2 AND finished_base_unit_id IS NOT NULL AND finished_manufacture_date IS NOT NULL AND finished_expiry_date IS NOT NULL AND finished_expiry_date>finished_manufacture_date AND finished_product_snapshot_json IS NOT NULL AND JSON_VALID(finished_product_snapshot_json) AND finished_product_snapshot_digest IS NOT NULL AND finished_product_snapshot_digest REGEXP '^[a-f0-9]{64}$' AND finished_quality_status IS NOT NULL AND finished_quality_status IN ('QUARANTINE','RELEASED','REJECTED') AND finished_inventory_status IS NOT NULL AND finished_inventory_status IN ('BLOCKED','AVAILABLE','FROZEN')));
ALTER TABLE mes_quantity_event
 ADD finished_schema_version SMALLINT NOT NULL DEFAULT 1,
 ADD product_id BIGINT NULL,
 ADD CONSTRAINT ck_mes_quantity_event_finished_mode CHECK (finished_schema_version IN (1,2)),
 ADD CONSTRAINT fk_mes_quantity_event_batch_product FOREIGN KEY (org_id,main_batch_id,product_id) REFERENCES prd_main_batch(org_id,id,product_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_mes_quantity_event_finished_subject CHECK ((finished_schema_version=1 AND product_id IS NULL) OR (finished_schema_version=2 AND material_lot_id IS NULL AND main_batch_id IS NOT NULL AND product_id IS NOT NULL)),
 ADD CONSTRAINT ck_direct_quantity_kind CHECK (finished_schema_version=1 OR event_type IN ('OUTPUT','SAMPLE','LOSS','SCRAP','WIP','REVERSAL'));
ALTER TABLE qms_sample
 ADD finished_schema_version SMALLINT NOT NULL DEFAULT 1,
 ADD product_id BIGINT NULL,
 ADD CONSTRAINT ck_qms_sample_finished_mode CHECK (finished_schema_version IN (1,2)),
 ADD CONSTRAINT fk_qms_sample_batch_product FOREIGN KEY (org_id,main_batch_id,product_id) REFERENCES prd_main_batch(org_id,id,product_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_qms_sample_finished_subject CHECK ((finished_schema_version=1 AND product_id IS NULL) OR (finished_schema_version=2 AND material_lot_id IS NULL AND main_batch_id IS NOT NULL AND product_id IS NOT NULL)),
 ADD CONSTRAINT ck_direct_sample_scope CHECK (finished_schema_version=1 OR sample_scope='PRODUCTION');
ALTER TABLE qms_release_decision
 ADD finished_schema_version SMALLINT NOT NULL DEFAULT 1,
 ADD product_id BIGINT NULL,
 ADD CONSTRAINT ck_qms_release_decision_finished_mode CHECK (finished_schema_version IN (1,2)),
 ADD CONSTRAINT fk_qms_release_decision_batch_product FOREIGN KEY (org_id,main_batch_id,product_id) REFERENCES prd_main_batch(org_id,id,product_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_qms_release_decision_finished_subject CHECK ((finished_schema_version=1 AND product_id IS NULL) OR (finished_schema_version=2 AND finished_lot_id IS NULL AND main_batch_id IS NOT NULL AND product_id IS NOT NULL)),
 ADD CONSTRAINT ck_direct_decision_scope CHECK (finished_schema_version=1 OR release_scope='FINISHED_PRODUCT'),
 DROP CONSTRAINT ck_finished_release_target,
 ADD CONSTRAINT ck_finished_release_target CHECK (release_scope<>'FINISHED_PRODUCT' OR ((finished_schema_version=1 AND finished_lot_id IS NOT NULL) OR (finished_schema_version=2 AND finished_lot_id IS NULL AND product_id IS NOT NULL)) AND inspection_report_id IS NULL AND decision_source='USER_QA' AND decision IN ('RELEASED','REJECTED') AND release_basis='FULL_INSPECTION');
ALTER TABLE wms_finished_inbound_request
 MODIFY material_lot_id BIGINT NULL,
 ADD finished_schema_version SMALLINT NOT NULL DEFAULT 1,
 ADD CONSTRAINT ck_wms_finished_inbound_request_finished_mode CHECK (finished_schema_version IN (1,2)),
 ADD CONSTRAINT fk_wms_finished_inbound_request_batch_product FOREIGN KEY (org_id,main_batch_id,product_id) REFERENCES prd_main_batch(org_id,id,product_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_wms_finished_inbound_request_finished_subject CHECK ((finished_schema_version=1 AND material_lot_id IS NOT NULL) OR (finished_schema_version=2 AND material_lot_id IS NULL AND main_batch_id IS NOT NULL AND product_id IS NOT NULL)),
 ADD UNIQUE KEY uk_wms_finished_inbound_request_source_subject (org_id,id,main_batch_id,product_id);
ALTER TABLE wms_finished_shipment
 MODIFY material_lot_id BIGINT NULL,
 ADD finished_schema_version SMALLINT NOT NULL DEFAULT 1,
 ADD product_id BIGINT NULL,
 ADD CONSTRAINT ck_wms_finished_shipment_finished_mode CHECK (finished_schema_version IN (1,2)),
 ADD CONSTRAINT fk_wms_finished_shipment_batch_product FOREIGN KEY (org_id,main_batch_id,product_id) REFERENCES prd_main_batch(org_id,id,product_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_wms_finished_shipment_finished_subject CHECK ((finished_schema_version=1 AND material_lot_id IS NOT NULL AND product_id IS NULL) OR (finished_schema_version=2 AND material_lot_id IS NULL AND main_batch_id IS NOT NULL AND product_id IS NOT NULL)),
 ADD UNIQUE KEY uk_wms_finished_shipment_source_subject (org_id,id,main_batch_id,product_id);
ALTER TABLE qms_finished_inspection_request
 MODIFY material_lot_id BIGINT NULL,
 ADD finished_schema_version SMALLINT NOT NULL DEFAULT 1,
 ADD product_id BIGINT NULL,
 ADD CONSTRAINT ck_qms_finished_inspection_request_finished_mode CHECK (finished_schema_version IN (1,2)),
 ADD CONSTRAINT fk_qms_finished_inspection_request_batch_product FOREIGN KEY (org_id,main_batch_id,product_id) REFERENCES prd_main_batch(org_id,id,product_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_qms_finished_inspection_request_finished_subject CHECK ((finished_schema_version=1 AND material_lot_id IS NOT NULL AND product_id IS NULL) OR (finished_schema_version=2 AND material_lot_id IS NULL AND main_batch_id IS NOT NULL AND product_id IS NOT NULL));
ALTER TABLE qms_finished_sampling_record
 MODIFY material_lot_id BIGINT NULL,
 ADD finished_schema_version SMALLINT NOT NULL DEFAULT 1,
 ADD product_id BIGINT NULL,
 ADD main_batch_id BIGINT NULL,
 ADD CONSTRAINT ck_qms_finished_sampling_record_finished_mode CHECK (finished_schema_version IN (1,2)),
 ADD CONSTRAINT fk_qms_finished_sampling_record_batch_product FOREIGN KEY (org_id,main_batch_id,product_id) REFERENCES prd_main_batch(org_id,id,product_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_qms_finished_sampling_record_finished_subject CHECK ((finished_schema_version=1 AND material_lot_id IS NOT NULL AND product_id IS NULL) OR (finished_schema_version=2 AND material_lot_id IS NULL AND main_batch_id IS NOT NULL AND product_id IS NOT NULL));
CREATE TABLE wms_finished_inventory_ledger (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL,
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL, version_no BIGINT NOT NULL DEFAULT 0,
 main_batch_id BIGINT NOT NULL, product_id BIGINT NOT NULL, location_id BIGINT NOT NULL, unit_id BIGINT NOT NULL,
 event_type VARCHAR(30) NOT NULL, delta_qty DECIMAL(18,6) NOT NULL,
 inbound_request_id BIGINT NULL, shipment_id BIGINT NULL,
 idempotency_key VARCHAR(100) NOT NULL, occurred_at DATETIME(3) NOT NULL,
 UNIQUE KEY uk_finished_ledger_key (org_id,idempotency_key),
 UNIQUE KEY uk_finished_receipt_source (org_id,inbound_request_id),
 UNIQUE KEY uk_finished_shipment_source (org_id,shipment_id),
 CONSTRAINT fk_finished_ledger_batch_product_unit FOREIGN KEY (org_id,main_batch_id,product_id,unit_id) REFERENCES prd_main_batch(org_id,id,product_id,finished_base_unit_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_finished_ledger_product FOREIGN KEY (org_id,product_id) REFERENCES md_product(org_id,id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_finished_ledger_location FOREIGN KEY (location_id) REFERENCES wms_location(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_finished_ledger_inbound_subject FOREIGN KEY (org_id,inbound_request_id,main_batch_id,product_id) REFERENCES wms_finished_inbound_request(org_id,id,main_batch_id,product_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_finished_ledger_shipment_subject FOREIGN KEY (org_id,shipment_id,main_batch_id,product_id) REFERENCES wms_finished_shipment(org_id,id,main_batch_id,product_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT ck_finished_ledger_source_sign CHECK (
  (event_type='FINISHED_RECEIPT' AND delta_qty>0 AND inbound_request_id IS NOT NULL AND shipment_id IS NULL) OR
  (event_type='FINISHED_SHIPMENT' AND delta_qty<0 AND shipment_id IS NOT NULL AND inbound_request_id IS NULL))
) ENGINE=InnoDB;
ALTER TABLE wms_finished_inbound_request ADD finished_ledger_id BIGINT NULL,
 ADD CONSTRAINT fk_wms_finished_inbound_request_finished_ledger FOREIGN KEY (finished_ledger_id) REFERENCES wms_finished_inventory_ledger(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_wms_finished_inbound_request_ledger_mode CHECK (
  (finished_schema_version=1 AND finished_ledger_id IS NULL) OR
  (finished_schema_version=2 AND ledger_id IS NULL AND (status<>'CONFIRMED' OR finished_ledger_id IS NOT NULL)));
ALTER TABLE wms_finished_shipment ADD finished_ledger_id BIGINT NULL,
 ADD CONSTRAINT fk_wms_finished_shipment_finished_ledger FOREIGN KEY (finished_ledger_id) REFERENCES wms_finished_inventory_ledger(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 ADD CONSTRAINT ck_wms_finished_shipment_ledger_mode CHECK (
  (finished_schema_version=1 AND finished_ledger_id IS NULL) OR
  (finished_schema_version=2 AND ledger_id IS NULL AND (status<>'CONFIRMED' OR finished_ledger_id IS NOT NULL)));
