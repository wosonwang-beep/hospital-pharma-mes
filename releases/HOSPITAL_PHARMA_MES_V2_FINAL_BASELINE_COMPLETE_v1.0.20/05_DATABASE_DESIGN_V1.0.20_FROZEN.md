# 数据库设计说明书与字段字典

## Current approved WMS successor delta — v1.0.20

[DCP-WMS-REQUEST-INVENTORY-RETURN-001](00_WMS_REQUEST_READ_CONTRACT_V1.0.20.md), section 2, supersedes inherited clauses only for formal material demand, request-linked issue, lot-level inventory read, return history and five WMS entry routes. Current approved scope and exact fields/states/rights/actions are in that normative section. Prior accepted tasks and QA/MES signature/stock rules remain unchanged. Do not infer runtime acceptance from publication.


公共列由 V1.0.20 冻结，不再留给 Implementation Agent 决定。

## Common Column Profile M — mutable regulated/configuration tables

- `id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY`; API JSON renders IDs as strings.
- `org_id BIGINT NOT NULL`; all business unique/index definitions lead with `org_id`; logical reference until the organization table is accepted.
- `created_by BIGINT NOT NULL`; logical user reference, no common metadata FK.
- `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)`; UTC, millisecond precision.
- `updated_by BIGINT NOT NULL`; equals creator on insert and is set explicitly on mutation.
- `updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)`; explicit application update; no `ON UPDATE` clause.
- `version_no BIGINT NOT NULL DEFAULT 0`; MyBatis-Plus optimistic lock, incremented once per successful mutation.

## Common Column Profile A — immutable append-only evidence tables

- `id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY`
- `org_id BIGINT NOT NULL`
- `created_by BIGINT NOT NULL`
- `created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)`
- `updated_by`, `updated_at` and `version_no` are omitted because mutation is illegal.

`created_by`/`updated_by` are logical identity references without physical FKs. Explicit `actor_id` and `signer_id` fields use physical FKs to existing `sys_user(id)`. All new timestamps are UTC `DATETIME(3)`. Profile A applies to `gxp_audit_event`; Profile M applies to `gxp_signature`, integration inbox/outbox and `platform_idempotency_record`. Existing physical V001/V002 tables are grandfathered and are not altered by MES-001.

## sys_user

- `username varchar(64) NN UK`
- `password_hash varchar(255) NN`
- `display_name varchar(100) NN`
- `status varchar(20) NN IDX`

## sys_role

- `role_code varchar(64) NN UK`
- `role_name varchar(100) NN`
- `status varchar(20) NN`

## sys_permission

- `permission_code varchar(128) NN UK`
- `permission_name varchar(128) NN`
- `permission_type varchar(20) NN IDX`

## sys_menu

- `parent_id bigint NULL IDX`
- `menu_code varchar(64) NN UK`
- `menu_name varchar(100) NN`
- `route_path varchar(255) NULL`
- `sort_no int NN`

## md_material

- `material_code varchar(50) NN UK`
- `material_name varchar(200) NN IDX`
- `generic_name varchar(200) NULL` — 历史保留列；当前业务停用，不展示、不写入、不参与查询。
- `english_name varchar(200) NULL` — 历史保留列；当前业务停用，不展示、不写入、不参与查询。
- `alias_name varchar(200) NULL` — 历史保留列；当前业务停用，不展示、不写入、不参与查询。
- `material_type varchar(30) NN IDX`
- `specification varchar(200) NULL`
- `grade_purity varchar(100) NULL`
- `appearance varchar(500) NULL`
- `base_unit_id bigint NN FK`
- `pack_spec varchar(200) NULL`
- `pack_unit_id bigint NULL FK`
- `manufacturer_name varchar(200) NULL`
- `quality_standard_code varchar(100) NULL`
- `storage_condition varchar(500) NULL`
- `shelf_life_days int NULL`
- `retest_period_days int NULL`
- `lot_controlled tinyint(1) NN`
- `sampling_required tinyint(1) NN`
- `inspection_required tinyint(1) NN`
- `release_required tinyint(1) NN`
- `weighing_required tinyint(1) NN`
- `critical_material tinyint(1) NN`
- `weighing_precision decimal(18,6) NULL`
- `weighing_tolerance_pct decimal(9,6) NULL`
- `special_control_type varchar(50) NULL`
- `status varchar(20) NN IDX`
- `effective_from datetime(3) NULL`
- `effective_to datetime(3) NULL`
- `remark varchar(1000) NULL`

## md_material_lot

- `material_id bigint NN FK`
- `lot_no varchar(100) NN UK`
- `supplier_lot_no varchar(100) NULL`
- `manufacture_date date NULL`
- `expiry_date date NULL IDX`
- `retest_date date NULL`
- `quality_status varchar(30) NN IDX`

## prd_main_batch

- `batch_no varchar(80) NN UK`
- `production_order_id bigint NN FK`
- `product_id bigint NN FK`
- `process_snapshot_id bigint NN FK`
- `planned_qty decimal(18,6) NN`
- `unit_id bigint NN FK`
- `status varchar(30) NN IDX`
- `released_at datetime(3) NULL`

## prd_sub_batch

- `main_batch_id bigint NN FK`
- `sub_batch_no varchar(80) NN`
- `sequence_no int NN`
- `planned_qty decimal(18,6) NULL`
- `status varchar(30) NN IDX`

## prd_execution_unit

- `main_batch_id bigint NN FK`
- `sub_batch_id bigint NULL FK`
- `unit_type varchar(20) NN IDX`
- `execution_no varchar(100) NN UK`
- `status varchar(30) NN IDX`

## mes_operation_execution

- `execution_unit_id bigint NN FK`
- `operation_def_id bigint NN FK`
- `operation_seq int NN`
- `status varchar(30) NN IDX`
- `started_at datetime(3) NULL`
- `completed_at datetime(3) NULL`
- `operator_id bigint NULL FK`

## wms_inventory_ledger

- `material_lot_id bigint NN FK`
- `location_id bigint NN FK`
- `container_id bigint NULL FK`
- `event_type varchar(30) NN IDX`
- `delta_qty decimal(18,6) NN`
- `unit_id bigint NN FK`
- `source_type varchar(50) NN`
- `source_ref varchar(100) NN IDX`
- `idempotency_key varchar(100) NN UK`
- `occurred_at datetime(3) NN IDX`

## mes_weighing_record

- `execution_unit_id bigint NN FK`
- `material_lot_id bigint NN FK`
- `bom_item_id bigint NN FK`
- `target_qty decimal(18,6) NN`
- `actual_qty decimal(18,6) NN`
- `unit_id bigint NN FK`
- `weighed_by bigint NN FK`
- `verified_by bigint NULL FK`
- `status varchar(20) NN`

## mes_material_charge

- `execution_unit_id bigint NN FK`
- `operation_execution_id bigint NN FK`
- `material_lot_id bigint NN FK`
- `weighing_record_id bigint NULL FK`
- `charged_qty decimal(18,6) NN`
- `unit_id bigint NN FK`
- `charged_by bigint NN FK`
- `verified_by bigint NULL FK`
- `charged_at datetime(3) NN IDX`
- `status varchar(20) NN`

## mes_quantity_event

- `main_batch_id bigint NN FK`
- `execution_unit_id bigint NULL FK`
- `operation_execution_id bigint NULL FK`
- `event_type varchar(30) NN IDX`
- `material_lot_id bigint NULL FK`
- `amount decimal(18,6) NN`
- `unit_id bigint NN FK`
- `source_type varchar(50) NN`
- `source_ref varchar(100) NN IDX`
- `occurred_at datetime(3) NN IDX`

## mes_balance_rule

- `process_snapshot_id bigint NN FK`
- `balance_code varchar(80) NN`
- `basis varchar(30) NN`
- `tolerance_low decimal(18,6) NULL`
- `tolerance_high decimal(18,6) NULL`
- `formula_expr text NN`
- `rule_version int NN`
- `check_point varchar(30) NN`

## mes_balance_result

- `main_batch_id bigint NN FK`
- `balance_rule_id bigint NN FK`
- `calculation_version int NN`
- `expected_value decimal(18,6) NULL`
- `actual_value decimal(18,6) NULL`
- `difference_value decimal(18,6) NULL`
- `difference_pct decimal(18,6) NULL`
- `status varchar(20) NN IDX`
- `calculated_at datetime(3) NN`

## mes_equipment_usage

- `execution_unit_id bigint NN FK`
- `operation_execution_id bigint NN FK`
- `equipment_id bigint NN FK`
- `usage_role varchar(30) NN`
- `clearance_status varchar(20) NULL`
- `qualification_status varchar(20) NULL`
- `bound_at datetime(3) NN`

## mes_equipment_run

- `equipment_usage_id bigint NN FK`
- `run_no varchar(100) NN UK`
- `status varchar(20) NN IDX`
- `started_at datetime(3) NN`
- `ended_at datetime(3) NULL`
- `source_message_id varchar(100) NULL IDX`

## qms_sample

- `main_batch_id bigint NULL FK`
- `material_lot_id bigint NULL FK`
- `sample_no varchar(80) NN UK`
- `sample_type varchar(30) NN`
- `status varchar(30) NN IDX`
- `sampled_at datetime(3) NULL`

## qms_deviation

- `deviation_no varchar(80) NN UK`
- `main_batch_id bigint NULL FK`
- `operation_execution_id bigint NULL FK`
- `severity varchar(20) NN IDX`
- `status varchar(30) NN IDX`
- `description text NN`
- `disposition text NULL`

## qms_release_decision

- `main_batch_id bigint NN UK FK`
- `finished_lot_id bigint NULL FK`
- `decision varchar(20) NN IDX`
- `decision_by bigint NN FK`
- `decision_at datetime(3) NN`
- `reason varchar(1000) NULL`
- `signature_id bigint NN FK`

## gxp_audit_event

Profile A plus:

- `actor_id BIGINT NOT NULL FK sys_user(id)`
- `actor_role VARCHAR(100) NULL`
- `action VARCHAR(80) NOT NULL`
- `object_type VARCHAR(80) NOT NULL`
- `object_id VARCHAR(100) NOT NULL`
- `old_value_digest TEXT NULL`
- `new_value_digest TEXT NULL`
- `reason VARCHAR(1000) NULL`
- `client_info VARCHAR(500) NULL`
- `occurred_at DATETIME(3) NOT NULL`
- `transaction_id VARCHAR(100) NOT NULL`
- `request_id VARCHAR(100) NULL`
- `source VARCHAR(30) NOT NULL CHECK source IN ('API','SCHEDULER','INTEGRATION','SYSTEM')`
- `idempotency_key VARCHAR(128) NULL`

Indexes: `(org_id,occurred_at,id)`, `(actor_id,occurred_at,id)`, `(object_type,object_id,occurred_at,id)`, `(action,occurred_at,id)`, `transaction_id`, `request_id`. Runtime access is SELECT/INSERT only. MariaDB `BEFORE UPDATE` and `BEFORE DELETE` guards signal SQLSTATE `45000`. Digests are lowercase 64-character SHA-256 values; credentials, tokens and raw regulated payloads are forbidden.

## gxp_signature

Profile M plus:

- `signer_id BIGINT NOT NULL FK sys_user(id)`
- `meaning VARCHAR(100) NOT NULL`
- `object_type VARCHAR(80) NOT NULL`
- `object_id VARCHAR(100) NOT NULL`
- `record_digest CHAR(64) NOT NULL`
- `signed_at DATETIME(3) NOT NULL`
- `status VARCHAR(20) NOT NULL CHECK status IN ('VALID','INVALIDATED')`
- `invalidated_at DATETIME(3) NULL`
- `invalidation_reason VARCHAR(1000) NULL`
- `auth_context_json LONGTEXT NULL`
- `revoked_signature_id BIGINT NULL FK gxp_signature(id)`

Indexes: `(org_id,object_type,object_id,status,id)` and `(signer_id,signed_at,id)`. Signed identity, meaning, object, digest and signed time are immutable. Only controlled invalidation fields and Profile M update metadata/version may change. `auth_context_json` contains schema version, method, reauthentication time, session ID hash and request ID; it never contains credentials or tokens.

# 扩展物理模型（V1.0 Frozen Candidate）

## md_organization

- `parent_id BIGINT FK`
- `org_code VARCHAR(64) UK`
- `org_name VARCHAR(200)`
- `org_type VARCHAR(30) IDX`
- `status VARCHAR(20)`

## md_unit

- `unit_code VARCHAR(32) UK`
- `unit_name VARCHAR(64)`
- `dimension VARCHAR(30) IDX`
- `scale INT`

## md_unit_conversion

- `from_unit_id BIGINT FK`
- `to_unit_id BIGINT FK`
- `factor DECIMAL(24,12)`
- `material_id BIGINT NULL FK`

## md_supplier

- `supplier_code VARCHAR(64) UK`
- `supplier_name VARCHAR(200)`
- `qualification_status VARCHAR(30) IDX`
- `valid_to DATE NULL`

## md_material_supplier

- `material_id BIGINT FK`
- `supplier_id BIGINT FK`
- `approved TINYINT(1) IDX`
- `valid_to DATE NULL`

## md_product

- `product_code VARCHAR(64) UK`
- `product_name VARCHAR(200)`
- `dosage_form VARCHAR(100) NULL`
- `specification VARCHAR(200) NULL`
- `base_unit_id BIGINT FK`
- `status VARCHAR(20) IDX`

## md_equipment

- `equipment_code VARCHAR(64) UK`
- `equipment_name VARCHAR(200)`
- `equipment_type VARCHAR(64) IDX`
- `status VARCHAR(30) IDX`
- `calibration_due_date DATE NULL`

## md_qualification

- `user_id BIGINT FK`
- `qualification_code VARCHAR(64) IDX`
- `valid_from DATE NULL`
- `valid_to DATE NULL`
- `status VARCHAR(20) IDX`

## proc_package

- `product_id BIGINT FK`
- `package_code VARCHAR(64) UK`
- `status VARCHAR(20) IDX`

## proc_package_version

- `package_id BIGINT FK`
- `version INT UK*`
- `status VARCHAR(20) IDX`
- `content_hash VARCHAR(128) NULL`
- `effective_from DATETIME(3) NULL`

## proc_formula_version

- `package_version_id BIGINT FK`
- `formula_code VARCHAR(64)`
- `version INT`
- `batch_basis_qty DECIMAL(18,6)`
- `unit_id BIGINT FK`

## proc_formula_item

- `formula_version_id BIGINT FK`
- `line_no INT UK*`
- `material_id BIGINT FK`
- `required_qty DECIMAL(18,6)`
- `unit_id BIGINT FK`
- `overage_pct DECIMAL(9,6) NULL`
- `critical TINYINT(1)`

## proc_route_version

- `package_version_id BIGINT FK`
- `route_code VARCHAR(64)`
- `version INT`

## proc_operation_def

- `route_version_id BIGINT FK`
- `operation_code VARCHAR(64) UK*`
- `operation_name VARCHAR(200)`
- `sequence_no INT IDX`
- `required_role VARCHAR(64) NULL`
- `completion_rule TEXT NULL`

## proc_parameter_def

- `operation_def_id BIGINT FK`
- `parameter_code VARCHAR(64) UK*`
- `parameter_name VARCHAR(200)`
- `acquisition_mode VARCHAR(20) IDX`
- `unit_id BIGINT NULL FK`
- `lower_limit DECIMAL(18,6) NULL`
- `upper_limit DECIMAL(18,6) NULL`

## ebr_template_version

- `package_version_id BIGINT FK`
- `template_code VARCHAR(64)`
- `version INT`
- `status VARCHAR(20) IDX`
- `content_hash VARCHAR(128) NULL`

## ebr_form_def

- `template_version_id BIGINT FK`
- `operation_def_id BIGINT NULL FK`
- `form_code VARCHAR(64) UK*`
- `form_name VARCHAR(200)`
- `schema_json LONGTEXT`

## ebr_field_def

- `form_def_id BIGINT FK`
- `field_code VARCHAR(64) UK*`
- `label VARCHAR(200)`
- `field_type VARCHAR(30)`
- `source_type VARCHAR(20)`
- `unit_id BIGINT NULL FK`
- `required_flag TINYINT(1)`
- `validation_json LONGTEXT NULL`

## ebr_rule_def

- `template_version_id BIGINT FK`
- `rule_code VARCHAR(64) UK*`
- `rule_type VARCHAR(30) IDX`
- `expression TEXT`
- `severity VARCHAR(20) NULL`

## prd_production_order

- `order_no VARCHAR(80) UK`
- `product_id BIGINT FK`
- `planned_qty DECIMAL(18,6)`
- `unit_id BIGINT FK`
- `status VARCHAR(30) IDX`

## prd_process_snapshot

- `package_version_id BIGINT FK`
- `formula_version_id BIGINT FK`
- `route_version_id BIGINT FK`
- `ebr_template_version_id BIGINT FK`
- `snapshot_json LONGTEXT`
- `snapshot_hash VARCHAR(128) UK`

## wms_warehouse

- `warehouse_code VARCHAR(64) UK`
- `warehouse_name VARCHAR(200)`
- `warehouse_type VARCHAR(30) IDX`
- `status VARCHAR(20)`

## wms_location

- `warehouse_id BIGINT FK`
- `location_code VARCHAR(64) UK*`
- `location_name VARCHAR(200) NULL`
- `status VARCHAR(20)`

## wms_container

- `container_code VARCHAR(100) UK`
- `container_type VARCHAR(30) NULL`
- `status VARCHAR(20) IDX`

## wms_reservation

- `main_batch_id BIGINT FK`
- `formula_item_id BIGINT FK`
- `material_lot_id BIGINT NULL FK`
- `reserved_qty DECIMAL(18,6)`
- `unit_id BIGINT FK`
- `status VARCHAR(20) IDX`

## wms_material_issue

- `main_batch_id BIGINT FK`
- `issue_no VARCHAR(80) UK`
- `status VARCHAR(20) IDX`
- `issued_at DATETIME(3) NULL`

## wms_material_issue_item

- `issue_id BIGINT FK`
- `material_lot_id BIGINT FK`
- `formula_item_id BIGINT NULL FK`
- `issued_qty DECIMAL(18,6)`
- `unit_id BIGINT FK`

## ebr_form_instance

- `operation_execution_id BIGINT FK`
- `form_def_id BIGINT FK`
- `status VARCHAR(20) IDX`
- `submitted_at DATETIME(3) NULL`

## ebr_field_value_revision

- `form_instance_id BIGINT FK`
- `field_def_id BIGINT FK`
- `revision_no INT UK*`
- `previous_revision_id BIGINT NULL FK`
- `raw_value LONGTEXT NULL`
- `derived_value LONGTEXT NULL`
- `change_reason VARCHAR(1000) NULL`
- `recorded_at DATETIME(3) IDX`

## mes_parameter_value

- `operation_execution_id BIGINT FK`
- `parameter_def_id BIGINT FK`
- `source_mode VARCHAR(20) IDX`
- `raw_value DECIMAL(24,8) NULL`
- `text_value VARCHAR(1000) NULL`
- `unit_id BIGINT NULL FK`
- `source_message_id VARCHAR(100) NULL IDX`
- `captured_at DATETIME(3) IDX`

## mes_genealogy

- `main_batch_id BIGINT FK`
- `input_material_lot_id BIGINT FK`
- `charge_id BIGINT FK`
- `output_lot_id BIGINT NULL FK`
- `relation_type VARCHAR(30)`

## mes_balance_investigation

- `balance_result_id BIGINT FK`
- `status VARCHAR(20) IDX`
- `investigation_text TEXT NULL`
- `decision VARCHAR(30) NULL`
- `approved_by BIGINT NULL FK`
- `approved_at DATETIME(3) NULL`

## qms_ipc_instance

- `operation_execution_id BIGINT FK`
- `ipc_code VARCHAR(64) IDX`
- `status VARCHAR(20) IDX`
- `result VARCHAR(20) NULL`
- `completed_at DATETIME(3) NULL`

## qms_test_result_revision

- `sample_id BIGINT FK`
- `test_code VARCHAR(64) IDX`
- `revision_no INT UK*`
- `previous_revision_id BIGINT NULL FK`
- `result_value VARCHAR(1000) NULL`
- `result_status VARCHAR(20) NULL`
- `recorded_at DATETIME(3)`

## qms_capa

- `capa_no VARCHAR(80) UK`
- `source_type VARCHAR(30)`
- `source_id BIGINT IDX`
- `status VARCHAR(30) IDX`
- `action_text TEXT`

## integration_outbox

Profile M plus `message_id VARCHAR(128) NOT NULL`, `target_system VARCHAR(80) NOT NULL`, `event_type VARCHAR(80) NOT NULL`, `aggregate_type VARCHAR(80) NOT NULL`, `aggregate_id VARCHAR(100) NOT NULL`, `payload_json LONGTEXT NOT NULL`, `status VARCHAR(20) NOT NULL`, `retry_count INT NOT NULL DEFAULT 0`, `next_retry_at DATETIME(3) NULL`, `published_at DATETIME(3) NULL`, `last_error_code VARCHAR(100) NULL`, `last_error_message VARCHAR(1000) NULL`.

Allowed status: `PENDING`, `DISPATCHING`, `RETRY_WAIT`, `PUBLISHED`, `DEAD_LETTER`. Unique `(org_id,message_id)`. Indexes `(org_id,status,next_retry_at,id)`, `(org_id,aggregate_type,aggregate_id,id)`, `(org_id,target_system,created_at,id)`. `retry_count >= 0`. Identity, aggregate reference and payload are immutable.

## integration_inbox

Profile M plus `source_system VARCHAR(80) NOT NULL`, `message_id VARCHAR(128) NOT NULL`, `payload_json LONGTEXT NOT NULL`, `status VARCHAR(20) NOT NULL`, `received_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)`, `processed_at DATETIME(3) NULL`, `retry_count INT NOT NULL DEFAULT 0`, `next_retry_at DATETIME(3) NULL`, `last_error_code VARCHAR(100) NULL`, `last_error_message VARCHAR(1000) NULL`.

Allowed status: `RECEIVED`, `PROCESSING`, `RETRY_WAIT`, `PROCESSED`, `DEAD_LETTER`. Unique `(org_id,source_system,message_id)`. Indexes `(org_id,status,next_retry_at,id)` and `(org_id,received_at,id)`. `retry_count >= 0`. Identity and payload are immutable.

# V1.0 Freeze Reconciliation

## Frozen decisions
- Finished goods lot: use `md_material_lot` with `md_material.material_type=FINISHED`; no separate wms_finished_lot.
- Material master: `md_material` holds identity/common controls; change-sensitive quality/storage/production controls are versioned in child tables below.
- eBR runtime identifies fields by frozen `field_code + occurrence_path`; `field_def_id` is retained as snapshot provenance only.
- All eBR runtime/config tables carry org_id and standard audit/version columns except immutable append-only evidence tables where update columns are not used.

## md_material_version
- `material_id BIGINT NOT NULL FK`
- `version_no_business INT NOT NULL UK*`
- `status VARCHAR(20) NOT NULL IDX`
- `effective_from DATETIME(3) NULL`
- `effective_to DATETIME(3) NULL`
- `content_hash VARCHAR(128) NOT NULL`
- `approved_by BIGINT NULL FK`
- `approved_at DATETIME(3) NULL`

## md_material_quality_spec
- `material_version_id BIGINT NOT NULL FK`
- `standard_code VARCHAR(100) NOT NULL`
- `sampling_required TINYINT(1) NOT NULL`
- `inspection_required TINYINT(1) NOT NULL`
- `release_required TINYINT(1) NOT NULL`
- `spec_json LONGTEXT NOT NULL`

## md_material_storage_rule
- `material_version_id BIGINT NOT NULL FK`
- `storage_condition VARCHAR(500) NULL`
- `temperature_min DECIMAL(9,3) NULL`
- `temperature_max DECIMAL(9,3) NULL`
- `humidity_min DECIMAL(9,3) NULL`
- `humidity_max DECIMAL(9,3) NULL`
- `shelf_life_days INT NULL`
- `retest_period_days INT NULL`
- `fefo_required TINYINT(1) NOT NULL`

## md_material_production_rule
- `material_version_id BIGINT NOT NULL FK`
- `weighing_required TINYINT(1) NOT NULL`
- `critical_material TINYINT(1) NOT NULL`
- `weighing_precision DECIMAL(18,6) NULL`
- `weighing_tolerance_pct DECIMAL(9,6) NULL`
- `special_control_type VARCHAR(50) NULL`

## ebr_section_def
- `template_version_id BIGINT NOT NULL FK`
- `section_code VARCHAR(64) NOT NULL UK*`
- `title VARCHAR(200) NOT NULL`
- `sequence_no INT NOT NULL`
- `repeat_mode VARCHAR(20) NOT NULL`
- `visibility_rule_id BIGINT NULL FK`
- `page_break_flag TINYINT(1) NOT NULL`

## ebr_group_def
- `section_def_id BIGINT NOT NULL FK`
- `group_code VARCHAR(64) NOT NULL UK*`
- `title VARCHAR(200) NULL`
- `sequence_no INT NOT NULL`
- `layout_columns INT NOT NULL`
- `repeat_mode VARCHAR(20) NOT NULL`
- `min_occurs INT NULL`
- `max_occurs INT NULL`

## ebr_option_def
- `field_def_id BIGINT NOT NULL FK`
- `option_code VARCHAR(64) NOT NULL UK*`
- `option_label VARCHAR(200) NOT NULL`
- `option_value VARCHAR(500) NOT NULL`
- `sequence_no INT NOT NULL`
- `active_flag TINYINT(1) NOT NULL`

## ebr_signature_rule
- `template_version_id BIGINT NOT NULL FK`
- `object_scope VARCHAR(30) NOT NULL`
- `object_code VARCHAR(64) NOT NULL`
- `meaning VARCHAR(100) NOT NULL`
- `required_role VARCHAR(64) NOT NULL`
- `reauth_required TINYINT(1) NOT NULL`
- `sequence_no INT NOT NULL`
- `invalidate_on_change TINYINT(1) NOT NULL`

## ebr_review_rule
- `template_version_id BIGINT NOT NULL FK`
- `object_scope VARCHAR(30) NOT NULL`
- `object_code VARCHAR(64) NOT NULL`
- `review_type VARCHAR(20) NOT NULL`
- `required_role VARCHAR(64) NOT NULL`
- `independent_user_required TINYINT(1) NOT NULL`
- `sequence_no INT NOT NULL`

## ebr_batch_snapshot
- `main_batch_id BIGINT NOT NULL UK/FK`
- `template_version_id BIGINT NOT NULL FK`
- `definition_hash VARCHAR(128) NOT NULL`
- `snapshot_json LONGTEXT NOT NULL`
- `frozen_at DATETIME(3) NOT NULL`

## ebr_rule_execution
- `form_instance_id BIGINT NOT NULL FK`
- `rule_code VARCHAR(64) NOT NULL IDX`
- `rule_version VARCHAR(32) NOT NULL`
- `trigger_point VARCHAR(30) NOT NULL`
- `passed TINYINT(1) NOT NULL`
- `severity VARCHAR(20) NULL`
- `input_snapshot_json LONGTEXT NOT NULL`
- `output_json LONGTEXT NULL`
- `engine_version VARCHAR(32) NOT NULL`
- `executed_at DATETIME(3) NOT NULL`

## ebr_review_record
- `object_type VARCHAR(50) NOT NULL IDX`
- `object_id BIGINT NOT NULL IDX`
- `review_type VARCHAR(20) NOT NULL`
- `reviewer_id BIGINT NOT NULL FK`
- `role_snapshot VARCHAR(100) NOT NULL`
- `decision VARCHAR(20) NOT NULL`
- `comment VARCHAR(1000) NULL`
- `reviewed_at DATETIME(3) NOT NULL`
- `signature_id BIGINT NOT NULL FK`
- `invalidated_at DATETIME(3) NULL`
- `invalidation_reason VARCHAR(1000) NULL`

## ebr_attachment
- `object_type VARCHAR(50) NOT NULL IDX`
- `object_id BIGINT NOT NULL IDX`
- `field_code VARCHAR(64) NULL`
- `file_id BIGINT NOT NULL`
- `file_hash VARCHAR(128) NOT NULL`
- `mime_type VARCHAR(100) NULL`
- `captured_at DATETIME(3) NULL`
- `uploaded_by BIGINT NOT NULL FK`

## ebr_pdf_manifest
- `main_batch_id BIGINT NOT NULL FK`
- `generation_version INT NOT NULL UK*`
- `definition_hash VARCHAR(128) NOT NULL`
- `record_digest VARCHAR(128) NOT NULL`
- `file_id BIGINT NOT NULL`
- `file_hash VARCHAR(128) NOT NULL`
- `generated_by BIGINT NOT NULL FK`
- `generated_at DATETIME(3) NOT NULL`

## ebr_field_def_v1_frozen additions
- `group_def_id BIGINT NULL FK`
- `data_type VARCHAR(30) NOT NULL`
- `precision_scale INT NULL`
- `readonly_flag TINYINT(1) NOT NULL`
- `default_expr TEXT NULL`
- `placeholder VARCHAR(500) NULL`
- `help_text VARCHAR(1000) NULL`
- `sequence_no INT NOT NULL`

## ebr_form_def_v1_frozen additions
- `schema_version VARCHAR(20) NOT NULL`
- `sequence_no INT NOT NULL`

## ebr_form_instance_v1_frozen additions
- `occurrence_no INT NOT NULL`
- `revision INT NOT NULL`
- `submitted_by BIGINT NULL FK`

## ebr_field_value_revision_v1_frozen additions
- `field_code VARCHAR(64) NOT NULL`
- `occurrence_path VARCHAR(255) NOT NULL`
- `raw_value_json LONGTEXT NULL`
- `normalized_value_json LONGTEXT NULL`
- `unit_id BIGINT NULL FK`
- `source_type VARCHAR(20) NOT NULL`
- `source_ref VARCHAR(128) NULL`
- `recorded_by BIGINT NOT NULL FK`

## ebr_rule_def_v1_frozen additions
- `trigger_point VARCHAR(30) NOT NULL`
- `error_code VARCHAR(64) NULL`
- `message_template VARCHAR(1000) NULL`
- `deviation_trigger TINYINT(1) NOT NULL`

## ebr_template_version_v1_frozen additions
- `effective_from DATETIME(3) NULL`
- `approved_by BIGINT NULL FK`
- `approved_at DATETIME(3) NULL`

## gxp_signature_v1.0.20 reconciliation
The V1.0 frozen additions are consolidated into the normative `gxp_signature` section above; no duplicate or alternative field set remains.


# V1.0.20 Platform Additions — DCP-MES-001-R2-001

## platform_idempotency_record

Profile M plus `actor_id BIGINT NOT NULL FK sys_user(id)`, `operation_code VARCHAR(100) NOT NULL`, `idempotency_key VARCHAR(128) NOT NULL`, `request_digest CHAR(64) NOT NULL`, `state VARCHAR(20) NOT NULL CHECK state IN ('IN_PROGRESS','COMPLETED')`, `http_status SMALLINT NULL`, `response_json LONGTEXT NULL`, `resource_type VARCHAR(80) NULL`, `resource_id VARCHAR(100) NULL`, `expires_at DATETIME(3) NOT NULL`. Unique `(org_id,actor_id,operation_code,idempotency_key)`. This is the default platform infrastructure; downstream generic module idempotency tables are prohibited absent an approved special business requirement.

## Physical migration compatibility

Physical V001 and V002 remain immutable forever. MES-001 alters none of their tables. The next planned physical migrations are V003 for the five platform tables/constraints/indexes/guards and V004 for the four platform permission seeds. These are design allocations only: v1.0.20 contains no SQL migration file and executes no migration. Section 15 identifiers V001/V002/V007A and similar are logical migration-group labels, not physical Flyway versions.

## DCP-MES-002-R2-001 — Incoming Material Quality Schema

Canonical material-lot table: **`md_material_lot`**. The former WMS-prefixed lot-table spelling is an obsolete documentation error and must not be created as an alias or second table.

### Existing tables extended by future append-only migrations

- `md_material_version`: `requires_incoming_inspection TINYINT(1) NOT NULL DEFAULT 1`.
- `md_material_lot`: `material_snapshot_json LONGTEXT NOT NULL`, `receipt_item_id BIGINT NULL`, `requires_incoming_inspection_snapshot TINYINT(1) NOT NULL`, `quality_status VARCHAR(30) NOT NULL`, `inventory_status VARCHAR(30) NOT NULL`, `expiry_date DATE NULL`, `retest_date DATE NULL`.
- `qms_sample`: `sample_scope`, `sampling_task_id`, `inspection_request_item_id`, `sample_type`, `sample_status`, collection/receipt/storage fields.
- `qms_test_result_revision`: `inspection_item_id`, `test_execution_id`, numeric/text result fields, unit, conclusion, change reason, recorder/reviewer timestamps, and signature reference; unique `(inspection_item_id, revision_no)`.
- `qms_release_decision`: `release_scope`, nullable `main_batch_id`, nullable `material_lot_id`, nullable `inspection_report_id`, `decision`, `release_basis`, `decision_source`, reason, actor/time/signature, and nullable `supersedes_decision_id`. Scope/target check constraints enforce exactly one applicable target. Decisions are immutable except via supersession.

### New WMS tables

- `wms_material_receipt`: receipt number, supplier, purchase/delivery references, warehouse, received/confirmed actors and times, and `record_status`.
- `wms_material_receipt_item`: receipt/material/version, supplier/manufacturer lots and dates, quantity/unit, package spec/count, package/seal/label/damage/contamination checks, and inspection-policy snapshot.

### New incoming-QMS tables

- `qms_inspection_request`, `qms_inspection_request_item`
- `qms_sampling_task`, `qms_sampling_detail`
- `qms_inspection_task`, `qms_inspection_item`, `qms_test_execution`
- `qms_inspection_report`, `qms_inspection_report_item`

All controlled headers carry org, stable business number, `record_status`, version/optimistic lock, created/updated metadata, and no physical-delete path. Detail rows preserve parent lineage. Six-record audit/signature links use platform GxP tables rather than duplicate columns containing credentials.

### Status separation

- `quality_status`: `QUARANTINE`, `PENDING_SAMPLING`, `SAMPLING`, `SAMPLED`, `TESTING`, `PENDING_QC_REVIEW`, `QC_PASSED`, `QC_FAILED`, `PENDING_QA_RELEASE`, `PENDING_DISPOSITION`, `RELEASED`, `REJECTED`.
- `inventory_status`: `BLOCKED`, `AVAILABLE`, `FROZEN`.
- `record_status`: `DRAFT`, `SUBMITTED`, `REVIEWED`, `APPROVED`, `EFFECTIVE`, `SUPERSEDED`, `CANCELLED` as applicable to the record type.

This release defines logical schema only. Implementation must allocate new physical Flyway versions after the actual highest successful version; no existing migration may be changed.





## DCP-MES-003-R2-001 approved delta

md_equipment adds `location VARCHAR(200) NULL`. All five MES-003 tables use Profile M. Code/pair indexes lead with org_id. Organization parent and UOM pair references have physical FKs. md_qualification.user_id references sys_user(id). md_unit_conversion.material_id FK is deferred to LG-004 as explicitly defined by DCP-MES-003-R2-001.


## DCP-MES-004-005-R2-001 approved contract completion

Material creation 201, complete typed DTOs, explicit version target/root optimistic token, Material root DRAFT/APPROVED/INACTIVE and version DRAFT/SUBMITTED/APPROVED, Supplier UNAPPROVED/APPROVED/INACTIVE and named commands, historical relationship revocation, and LG-004 delayed conversion-material FK follow 00_DESIGN_CHANGE_PROPOSAL_DCP-MES-004-005-R2-001_APPROVED.md. No existing table/column/path/permission/task dependency is added or removed. All previous unrelated contracts remain applicable.


## DCP-MATERIAL-BASIC-001 authoritative replacement

Read 00_DESIGN_CHANGE_DCP-MATERIAL-BASIC-001_APPROVED.md. This delta supersedes earlier material business version/approval wording, including inherited v1.0.4 delta sections; unrelated versioned aggregates and supplier qualification remain unchanged. Material is directly editable basic master, root ACTIVE/INACTIVE; historical DRAFT/APPROVED rows remain evidence-compatible enabled records until audited maintenance. Basic unit/conversion and multiple suppliers with exactly one preferred are current scope. versionNo is only an optimistic-lock token. Legacy version/rule tables are retired, not dropped. Consumer snapshots freeze material values at use time.

Current V010 additions: md_material.requires_incoming_inspection TINYINT(1) NOT NULL DEFAULT 1; md_material_supplier.preferred TINYINT(1) NOT NULL DEFAULT 0; generated preferred_material_id plus UNIQUE(org_id,preferred_material_id) and CHECK(preferred=0 OR approved=1). Legacy md_material_version/quality/storage/production tables are archived history only. Legacy non-basic required control columns become nullable without value erasure.

## DCP-MES-006-R2-001 current contract

The approved 00_DESIGN_CHANGE_DCP-MES-006-R2-001_APPROVED.md is normative for MES-006 and supersedes prior contradictory process/product/eBR scope prose. Product is owned here; material has no business version; eBR is independently owned by MES-007. Process signature binds immutable business version and definition content. New physical V011 only. Test requirements include TC-PROC-004.


## DCP-MATERIAL-NAMES-UI-001 current correction
See `00_DESIGN_CHANGE_DCP-MATERIAL-NAMES-UI-001_APPROVED.md`. The three alternate material-name fields are retired from current API/UI/search/consumer snapshots; legacy physical values and audits are preserved. Labels and controls remain side by side on all viewport sizes. This supersedes inherited inconsistent descriptions within that boundary.


## DCP-MES-007-008-SEQUENCING-001 authoritative delta

Read `00_DESIGN_CHANGE_DCP-MES-007-008-SEQUENCING-001_APPROVED.md` and the stage contract appendices. Explicit human approval preserves the full MES-007/008 functionality and required tests, while separating current implementation from later real integration. LG-007A definition/Designer/DSL/published contracts is the current MES-007 gate; LG-007B remains MES-009 after LG-009A, with its operation_execution_id FK installed and validated by LG-010. LG-008 reservation/issue main_batch_id FKs are installed and validated by LG-009A. Before these physical producers exist, dependent writes fail closed; no placeholder batch/operation rows, bypassed qualification, or mutable regulated history. MES-008A owns actual MaterialEligibilityService/release evidence; no quantity-only eligibility.

The existing independent receipt edit UI is retained. `PUT /wms/receipts/{id}` uses `wms:receipt:update`, edits only DRAFT authored receipt fields, requires optimistic version, reason, idempotency and same-transaction audit, and returns 200; Confirmed receipt facts (recordStatus APPROVED) cannot be edited. Formal contracts are the concrete OpenAPI and stage appendices. All current labels/control pairs stay horizontal on desktop/mobile.

Current targeted tests prove only current-stage capabilities; deferred runtime/eligibility/batch tests remain required. MES-007 and MES-008 remain IN PROGRESS until every original applicable gate passes against real producer contracts. The approved stage does not authorize implementation of MES-008A/009/010 or marking tasks ACCEPTED.


## DCP-MES-008A-CONTRACT-001 approved bounded delta

Read `00_DESIGN_CHANGE_DCP-MES-008A-CONTRACT-001_APPROVED.md`. Its six independent create/execute routes, existing workbench mapping, nullable incoming MainBatch references, MES-009 delayed FK ownership and canonical finished_lot_id target supersede conflicting inherited wording only within this boundary. Existing fields, API/permissions, state machines and GxP requirements remain unchanged. Unresolved incoming implementation-map gaps are not resolved by this release.


## Approved incoming full-chain completion in v1.0.20

Read `00_DESIGN_CHANGE_DCP-INCOMING-QUALITY-GAPS-001_APPROVED.md` and the four `00_INCOMING_*_CONTRACT_V1.0.20.md` appendices. Their explicit columns, state guards, API schemas, permissions, signing envelopes, UI fields, tests and dependency ownership supersede contradictory inherited text within the approved scope only. Former DG-01..08 and BC-01/02 now have implementation contracts; delivery is still subject to actual code and required validation. No full workflow PASS follows from publication.


## Authorized v1.0.20 incoming completion supplement

Human approval covers the bounded contract completion and full incoming acceptance tasks. For eBR runtime, issue returns, weighing verification and actual trace identities, the following precise supplements supersede generic or conflicting clauses in this chapter; unaffected contracts remain unchanged.

- [00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.20.md](00_INCOMING_EBR_RUNTIME_SUPPLEMENT_V1.0.20.md)
- [00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.20.md](00_INCOMING_WEIGH_POLICY_SUPPLEMENT_V1.0.20.md)
- [00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.20.md](00_INCOMING_ISSUE_RETURN_SUPPLEMENT_V1.0.20.md)


## Authorized v1.0.20 material weighing policy producer

The approved full incoming acceptance scope includes this missing producer. [00_INCOMING_MATERIAL_WEIGH_POLICY_V1.0.20.md](00_INCOMING_MATERIAL_WEIGH_POLICY_V1.0.20.md) governs deployment configuration, immutable production snapshot, consumers and required tests. It does not restore retired material master fields or change material APIs. It supersedes earlier references to nullable legacy material weighing fields. No new table, permission, route or status.


## v1.0.20 approved trace-read completion

See [00_INCOMING_TRACE_STANDARD_V1.0.20.md](00_INCOMING_TRACE_STANDARD_V1.0.20.md) for exact frozen QC standard node and immutable signature-policy identity evidence. Unaffected contracts remain unchanged.


## Approved functional closure delta — v1.0.20

DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.20.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.20.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.


### Exact physical FK authority

MaterialLot identity is `md_material_lot(id)`, not a new `wms_material_lot` table. Inventory decisions reference that existing table. IPC and clearance refer `mes_operation_execution(id)`; result/review/signature FKs and six tables are enumerated in the approved closure contract. Failed V024 is implementation evidence, not a changed database design.


## Verified functional closure recovery — 2026-10-04

The approved bounded delta and one-time V024 exception are recorded in [functional closure §§9–10](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.20.md). V024 is successful; V001–V023 remain immutable. Trace inventory decisions use INVENTORY_DECISION / INVENTORY_CONTROL with original SIGNED_EVIDENCE, decimal signing values are exact strings, and mandatory missing headers return 400. Scope-level native verification PASS does not waive other formal task RTM or authorize full MES-012/013.


## Approved MES-012/013 completion — v1.0.20

Read [00_MES_012_013_COMPLETION_CONTRACT_V1.0.20.md](00_MES_012_013_COMPLETION_CONTRACT_V1.0.20.md), DCP-MES-012-013-CONTRACT-001, approved 2026-10-04. It supersedes missing/generic production balance, investigation/CAPA, finished QA and PDF contracts only. BalanceResult remains immutable PASS/FAIL; approval belongs to the separate investigation. Original incoming and IPC contracts stay unchanged. No runtime readiness follows from publication.


## Approved audit HIGH delta — v1.0.20

[00_AUDIT_HIGH_CONTRACT_V1.0.20.md](00_AUDIT_HIGH_CONTRACT_V1.0.20.md) governs only productionOrderId query publication, incoming read-only allowedActions and exclusive active equipment occupancy. All other inherited contracts remain unchanged. No schema/migration, new permission/route/state or signature/release-rule change.


## Approved storage/source maintenance — v1.0.20

[00_MATERIAL_STORAGE_SOURCE_CONTRACT_V1.0.20.md](00_MATERIAL_STORAGE_SOURCE_CONTRACT_V1.0.20.md) governs the bounded storageCondition, relationship manufacturerName, receipt-source snapshot, UI field cleanup and V028 delta. Other inherited contracts unchanged.


## Approved finished-goods delta — v1.0.20

[00_FINISHED_GOODS_CONTRACT_V1.0.20.md](00_FINISHED_GOODS_CONTRACT_V1.0.20.md) controls the approved finished chain. It supersedes inherited wording only for new OUTPUT stock effects (identity/quantity only), independent warehouse receipt, post-completion finished request/sampling/report, two additional QA gates, signed shipment and finished trace. Existing incoming quality, historical OUTPUT ledgers, immutable original results/OOS/retest, independent signed QA and post-decision FINAL PDF remain authoritative. V030/V031 append-only. UI V2/T1–T6 unchanged. Inherited embedded manifests/reviews attest prior deltas; current finished-goods manifest governs cumulative bytes. Acceptance/readiness remains in MES_TASKS.md.
