-- Incoming quality approved v1.0.11. New tables only; prior migrations remain immutable.

CREATE TABLE qms_inspection_request (
 `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `org_id` BIGINT NOT NULL,
 `created_by` BIGINT NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `updated_by` BIGINT NOT NULL,
 `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `version_no` BIGINT NOT NULL DEFAULT 0,
 `request_no` VARCHAR(80) NOT NULL,
 `material_lot_id` BIGINT NOT NULL,
 `qc_specification_version_id` BIGINT NOT NULL,
 `approved_investigation_id` BIGINT NULL,
 `specification_content_hash` CHAR(64) NOT NULL,
 `requested_quantity` DECIMAL(24,8) NOT NULL,
 CHECK (`requested_quantity` > 0),
 `requested_unit_id` BIGINT NOT NULL,
 `requested_package_count` INT NOT NULL,
 CHECK (`requested_package_count` > 0),
 `requested_date` DATE NOT NULL,
 `priority` VARCHAR(30) NOT NULL,
 `requested_by` BIGINT NOT NULL,
 `request_type` VARCHAR(30) NOT NULL,
 `status` VARCHAR(30) NOT NULL,
 `record_status` VARCHAR(30) NOT NULL,
 `reason` VARCHAR(1000) NOT NULL,
 `submitted_by` BIGINT NULL,
 `submitted_at` DATETIME(3) NULL,
 `accepted_by` BIGINT NULL,
 `accepted_at` DATETIME(3) NULL,
 UNIQUE KEY uk_inspection_request_0 (org_id, request_no),
 KEY ix_inspection_request_material_lot_id (org_id, `material_lot_id`, id),
 KEY ix_inspection_request_qc_specification_version (org_id, `qc_specification_version_id`, id),
 KEY ix_inspection_request_approved_investigation_i (org_id, `approved_investigation_id`, id),
 KEY ix_inspection_request_requested_unit_id (org_id, `requested_unit_id`, id),
 KEY ix_inspection_request_requested_by (org_id, `requested_by`, id),
 KEY ix_inspection_request_submitted_by (org_id, `submitted_by`, id),
 KEY ix_inspection_request_accepted_by (org_id, `accepted_by`, id),
 KEY ix_inspection_request_status (org_id,status,id)
) ENGINE=InnoDB;

CREATE TABLE qms_inspection_request_item (
 `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `org_id` BIGINT NOT NULL,
 `created_by` BIGINT NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `inspection_request_id` BIGINT NOT NULL,
 `material_lot_id` BIGINT NOT NULL,
 `item_no` INT NOT NULL,
 CHECK (`item_no` > 0),
 UNIQUE KEY uk_inspection_request_item_0 (org_id, inspection_request_id, item_no),
 UNIQUE KEY uk_inspection_request_item_1 (org_id, inspection_request_id, material_lot_id),
 KEY ix_inspection_request_item_inspection_request_id (org_id, `inspection_request_id`, id),
 KEY ix_inspection_request_item_material_lot_id (org_id, `material_lot_id`, id)
) ENGINE=InnoDB;

CREATE TABLE qms_sampling_task (
 `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `org_id` BIGINT NOT NULL,
 `created_by` BIGINT NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `updated_by` BIGINT NOT NULL,
 `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `version_no` BIGINT NOT NULL DEFAULT 0,
 `sampling_task_no` VARCHAR(80) NOT NULL,
 `inspection_request_id` BIGINT NOT NULL,
 `inspection_request_item_id` BIGINT NOT NULL,
 `sampling_plan` VARCHAR(1000) NOT NULL,
 `required_package_count` INT NOT NULL,
 CHECK (`required_package_count` > 0),
 `status` VARCHAR(30) NOT NULL,
 `record_status` VARCHAR(30) NOT NULL,
 `assigned_to` BIGINT NULL,
 `started_at` DATETIME(3) NULL,
 `completed_at` DATETIME(3) NULL,
 `plan_signature_id` BIGINT NULL,
 `plan_signature_evidence_json` LONGTEXT NULL,
 CHECK (`plan_signature_evidence_json` IS NULL OR JSON_VALID(`plan_signature_evidence_json`)),
 `completion_signature_id` BIGINT NULL,
 `completion_signature_evidence_json` LONGTEXT NULL,
 CHECK (`completion_signature_evidence_json` IS NULL OR JSON_VALID(`completion_signature_evidence_json`)),
 `other_sample_name` VARCHAR(100) NULL,
 `other_sample_reason` VARCHAR(1000) NULL,
 UNIQUE KEY uk_sampling_task_0 (org_id, sampling_task_no),
 KEY ix_sampling_task_inspection_request_id (org_id, `inspection_request_id`, id),
 KEY ix_sampling_task_inspection_request_item_ (org_id, `inspection_request_item_id`, id),
 KEY ix_sampling_task_assigned_to (org_id, `assigned_to`, id),
 KEY ix_sampling_task_plan_signature_id (org_id, `plan_signature_id`, id),
 CHECK ((`plan_signature_id` IS NULL) = (`plan_signature_evidence_json` IS NULL)),
 KEY ix_sampling_task_completion_signature_id (org_id, `completion_signature_id`, id),
 CHECK ((`completion_signature_id` IS NULL) = (`completion_signature_evidence_json` IS NULL)),
 KEY ix_sampling_task_status (org_id,status,id)
) ENGINE=InnoDB;

CREATE TABLE qms_sampling_detail (
 `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `org_id` BIGINT NOT NULL,
 `created_by` BIGINT NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `sampling_task_id` BIGINT NOT NULL,
 `detail_no` INT NOT NULL,
 CHECK (`detail_no` > 0),
 `container_no` VARCHAR(80) NOT NULL,
 `sampling_point` VARCHAR(200) NULL,
 `sample_quantity` DECIMAL(24,8) NOT NULL,
 CHECK (`sample_quantity` > 0),
 `unit_id` BIGINT NOT NULL,
 `sampled_at` DATETIME(3) NOT NULL,
 `sampled_by` BIGINT NOT NULL,
 `package_resealed` TINYINT(1) NOT NULL,
 UNIQUE KEY uk_sampling_detail_0 (org_id, sampling_task_id, detail_no),
 KEY ix_sampling_detail_sampling_task_id (org_id, `sampling_task_id`, id),
 KEY ix_sampling_detail_unit_id (org_id, `unit_id`, id),
 KEY ix_sampling_detail_sampled_by (org_id, `sampled_by`, id)
) ENGINE=InnoDB;

CREATE TABLE qms_sample (
 `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `org_id` BIGINT NOT NULL,
 `created_by` BIGINT NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `updated_by` BIGINT NOT NULL,
 `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `version_no` BIGINT NOT NULL DEFAULT 0,
 `sample_no` VARCHAR(80) NOT NULL,
 `sample_scope` VARCHAR(30) NOT NULL,
 `main_batch_id` BIGINT NULL,
 `material_lot_id` BIGINT NULL,
 `sampling_task_id` BIGINT NULL,
 `sampling_detail_id` BIGINT NULL,
 `inspection_request_item_id` BIGINT NULL,
 `sample_type` VARCHAR(30) NOT NULL,
 `status` VARCHAR(30) NOT NULL,
 `sampled_at` DATETIME(3) NULL,
 `quantity` DECIMAL(24,8) NULL,
 CHECK (`quantity` > 0),
 `unit_id` BIGINT NULL,
 `received_by` BIGINT NULL,
 `received_at` DATETIME(3) NULL,
 `storage_location` VARCHAR(200) NULL,
 `disposal_signature_id` BIGINT NULL,
 `disposal_signature_evidence_json` LONGTEXT NULL,
 CHECK (`disposal_signature_evidence_json` IS NULL OR JSON_VALID(`disposal_signature_evidence_json`)),
 UNIQUE KEY uk_sample_0 (org_id, sample_no),
 KEY ix_sample_main_batch_id (org_id, `main_batch_id`, id),
 KEY ix_sample_material_lot_id (org_id, `material_lot_id`, id),
 KEY ix_sample_sampling_task_id (org_id, `sampling_task_id`, id),
 KEY ix_sample_sampling_detail_id (org_id, `sampling_detail_id`, id),
 KEY ix_sample_inspection_request_item_ (org_id, `inspection_request_item_id`, id),
 KEY ix_sample_unit_id (org_id, `unit_id`, id),
 KEY ix_sample_received_by (org_id, `received_by`, id),
 KEY ix_sample_disposal_signature_id (org_id, `disposal_signature_id`, id),
 CHECK ((`disposal_signature_id` IS NULL) = (`disposal_signature_evidence_json` IS NULL)),
 KEY ix_sample_status (org_id,status,id)
) ENGINE=InnoDB;

CREATE TABLE qms_inspection_task (
 `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `org_id` BIGINT NOT NULL,
 `created_by` BIGINT NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `updated_by` BIGINT NOT NULL,
 `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `version_no` BIGINT NOT NULL DEFAULT 0,
 `inspection_task_no` VARCHAR(80) NOT NULL,
 `inspection_request_id` BIGINT NOT NULL,
 `sample_id` BIGINT NOT NULL,
 `status` VARCHAR(30) NOT NULL,
 `record_status` VARCHAR(30) NOT NULL,
 `assigned_to` BIGINT NULL,
 `started_at` DATETIME(3) NULL,
 `submitted_at` DATETIME(3) NULL,
 `reviewed_by` BIGINT NULL,
 `reviewed_at` DATETIME(3) NULL,
 `review_signature_id` BIGINT NULL,
 `review_signature_evidence_json` LONGTEXT NULL,
 CHECK (`review_signature_evidence_json` IS NULL OR JSON_VALID(`review_signature_evidence_json`)),
 `reviewed_result_ids_json` LONGTEXT NULL,
 CHECK (`reviewed_result_ids_json` IS NULL OR JSON_VALID(`reviewed_result_ids_json`)),
 UNIQUE KEY uk_inspection_task_0 (org_id, inspection_task_no),
 KEY ix_inspection_task_inspection_request_id (org_id, `inspection_request_id`, id),
 KEY ix_inspection_task_sample_id (org_id, `sample_id`, id),
 KEY ix_inspection_task_assigned_to (org_id, `assigned_to`, id),
 KEY ix_inspection_task_reviewed_by (org_id, `reviewed_by`, id),
 KEY ix_inspection_task_review_signature_id (org_id, `review_signature_id`, id),
 CHECK ((`review_signature_id` IS NULL) = (`review_signature_evidence_json` IS NULL)),
 KEY ix_inspection_task_status (org_id,status,id)
) ENGINE=InnoDB;

CREATE TABLE qms_inspection_item (
 `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `org_id` BIGINT NOT NULL,
 `created_by` BIGINT NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `inspection_task_id` BIGINT NOT NULL,
 `qc_specification_item_id` BIGINT NOT NULL,
 `item_code` VARCHAR(100) NOT NULL,
 `item_name` VARCHAR(200) NOT NULL,
 `required` TINYINT(1) NOT NULL,
 `result_type` VARCHAR(30) NOT NULL,
 `lower_limit` DECIMAL(18,6) NULL,
 `upper_limit` DECIMAL(18,6) NULL,
 `unit_id` BIGINT NULL,
 `text_acceptance_criteria` VARCHAR(1000) NULL,
 `method_code` VARCHAR(100) NOT NULL,
 `method_version` VARCHAR(50) NOT NULL,
 UNIQUE KEY uk_inspection_item_0 (org_id, inspection_task_id, qc_specification_item_id),
 KEY ix_inspection_item_inspection_task_id (org_id, `inspection_task_id`, id),
 KEY ix_inspection_item_qc_specification_item_id (org_id, `qc_specification_item_id`, id),
 KEY ix_inspection_item_unit_id (org_id, `unit_id`, id)
) ENGINE=InnoDB;

CREATE TABLE qms_test_execution (
 `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `org_id` BIGINT NOT NULL,
 `created_by` BIGINT NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `inspection_item_id` BIGINT NOT NULL,
 `attempt_no` INT NOT NULL,
 CHECK (`attempt_no` > 0),
 `original_execution_id` BIGINT NULL,
 `approved_investigation_id` BIGINT NULL,
 `instrument_id` BIGINT NULL,
 `raw_data_json` LONGTEXT NOT NULL,
 CHECK (`raw_data_json` IS NULL OR JSON_VALID(`raw_data_json`)),
 `observation` VARCHAR(1000) NULL,
 `calculation_input_json` LONGTEXT NOT NULL,
 CHECK (`calculation_input_json` IS NULL OR JSON_VALID(`calculation_input_json`)),
 `started_at` DATETIME(3) NOT NULL,
 `completed_at` DATETIME(3) NOT NULL,
 `performed_by` BIGINT NOT NULL,
 UNIQUE KEY uk_test_execution_0 (org_id, inspection_item_id, attempt_no),
 KEY ix_test_execution_inspection_item_id (org_id, `inspection_item_id`, id),
 KEY ix_test_execution_original_execution_id (org_id, `original_execution_id`, id),
 KEY ix_test_execution_approved_investigation_i (org_id, `approved_investigation_id`, id),
 KEY ix_test_execution_instrument_id (org_id, `instrument_id`, id),
 KEY ix_test_execution_performed_by (org_id, `performed_by`, id)
) ENGINE=InnoDB;

CREATE TABLE qms_test_result_revision (
 `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `org_id` BIGINT NOT NULL,
 `created_by` BIGINT NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `sample_id` BIGINT NOT NULL,
 `inspection_item_id` BIGINT NOT NULL,
 `test_execution_id` BIGINT NOT NULL,
 `test_code` VARCHAR(100) NOT NULL,
 `revision_no` INT NOT NULL,
 CHECK (`revision_no` > 0),
 `previous_revision_id` BIGINT NULL,
 `result_numeric` DECIMAL(24,8) NULL,
 `result_text` VARCHAR(1000) NULL,
 `result_unit_id` BIGINT NULL,
 `result_conclusion` VARCHAR(30) NOT NULL,
 `reason_for_change` VARCHAR(1000) NULL,
 `recorded_by` BIGINT NOT NULL,
 `recorded_at` DATETIME(3) NOT NULL,
 `signature_id` BIGINT NOT NULL,
 `signature_evidence_json` LONGTEXT NOT NULL,
 CHECK (`signature_evidence_json` IS NULL OR JSON_VALID(`signature_evidence_json`)),
 UNIQUE KEY uk_test_result_revision_0 (org_id, test_execution_id, revision_no),
 KEY ix_test_result_revision_sample_id (org_id, `sample_id`, id),
 KEY ix_test_result_revision_inspection_item_id (org_id, `inspection_item_id`, id),
 KEY ix_test_result_revision_test_execution_id (org_id, `test_execution_id`, id),
 KEY ix_test_result_revision_previous_revision_id (org_id, `previous_revision_id`, id),
 KEY ix_test_result_revision_result_unit_id (org_id, `result_unit_id`, id),
 KEY ix_test_result_revision_recorded_by (org_id, `recorded_by`, id),
 KEY ix_test_result_revision_signature_id (org_id, `signature_id`, id),
 CHECK ((`signature_id` IS NULL) = (`signature_evidence_json` IS NULL))
) ENGINE=InnoDB;

CREATE TABLE qms_inspection_report (
 `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `org_id` BIGINT NOT NULL,
 `created_by` BIGINT NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `updated_by` BIGINT NOT NULL,
 `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `version_no` BIGINT NOT NULL DEFAULT 0,
 `report_no` VARCHAR(80) NOT NULL,
 `inspection_request_id` BIGINT NOT NULL,
 `status` VARCHAR(30) NOT NULL,
 `record_status` VARCHAR(30) NOT NULL,
 `overall_result` VARCHAR(30) NOT NULL,
 `evidence_digest` CHAR(64) NOT NULL,
 `supersedes_report_id` BIGINT NULL,
 `reviewed_by` BIGINT NULL,
 `reviewed_at` DATETIME(3) NULL,
 `approved_by` BIGINT NULL,
 `approved_at` DATETIME(3) NULL,
 `approval_signature_id` BIGINT NULL,
 `approval_signature_evidence_json` LONGTEXT NULL,
 CHECK (`approval_signature_evidence_json` IS NULL OR JSON_VALID(`approval_signature_evidence_json`)),
 UNIQUE KEY uk_inspection_report_0 (org_id, report_no),
 UNIQUE KEY uk_inspection_report_1 (org_id, supersedes_report_id),
 KEY ix_inspection_report_inspection_request_id (org_id, `inspection_request_id`, id),
 KEY ix_inspection_report_supersedes_report_id (org_id, `supersedes_report_id`, id),
 KEY ix_inspection_report_reviewed_by (org_id, `reviewed_by`, id),
 KEY ix_inspection_report_approved_by (org_id, `approved_by`, id),
 KEY ix_inspection_report_approval_signature_id (org_id, `approval_signature_id`, id),
 CHECK ((`approval_signature_id` IS NULL) = (`approval_signature_evidence_json` IS NULL)),
 KEY ix_inspection_report_status (org_id,status,id)
) ENGINE=InnoDB;

CREATE TABLE qms_inspection_report_item (
 `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `org_id` BIGINT NOT NULL,
 `created_by` BIGINT NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `inspection_report_id` BIGINT NOT NULL,
 `qc_specification_item_id` BIGINT NOT NULL,
 `inspection_item_id` BIGINT NOT NULL,
 `result_revision_id` BIGINT NOT NULL,
 `original_result_revision_id` BIGINT NOT NULL,
 `investigation_id` BIGINT NULL,
 UNIQUE KEY uk_inspection_report_item_0 (org_id, inspection_report_id, qc_specification_item_id),
 KEY ix_inspection_report_item_inspection_report_id (org_id, `inspection_report_id`, id),
 KEY ix_inspection_report_item_qc_specification_item_id (org_id, `qc_specification_item_id`, id),
 KEY ix_inspection_report_item_inspection_item_id (org_id, `inspection_item_id`, id),
 KEY ix_inspection_report_item_result_revision_id (org_id, `result_revision_id`, id),
 KEY ix_inspection_report_item_original_result_revision (org_id, `original_result_revision_id`, id),
 KEY ix_inspection_report_item_investigation_id (org_id, `investigation_id`, id)
) ENGINE=InnoDB;

CREATE TABLE qms_deviation (
 `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `org_id` BIGINT NOT NULL,
 `created_by` BIGINT NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `updated_by` BIGINT NOT NULL,
 `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `version_no` BIGINT NOT NULL DEFAULT 0,
 `deviation_no` VARCHAR(80) NOT NULL,
 `main_batch_id` BIGINT NULL,
 `operation_execution_id` BIGINT NULL,
 `severity` VARCHAR(20) NOT NULL,
 `status` VARCHAR(30) NOT NULL,
 `description` LONGTEXT NOT NULL,
 `disposition` LONGTEXT NULL,
 `investigation_scope` VARCHAR(30) NOT NULL,
 `investigation_kind` VARCHAR(30) NOT NULL,
 `material_lot_id` BIGINT NULL,
 `test_execution_id` BIGINT NULL,
 `original_result_revision_id` BIGINT NULL,
 `investigation_summary` LONGTEXT NULL,
 `decision_code` VARCHAR(30) NULL,
 `decision_signature_id` BIGINT NULL,
 `decision_signature_evidence_json` LONGTEXT NULL,
 CHECK (`decision_signature_evidence_json` IS NULL OR JSON_VALID(`decision_signature_evidence_json`)),
 `decided_by` BIGINT NULL,
 `decided_at` DATETIME(3) NULL,
 `authorized_retest_count` INT NULL,
 `selected_result_revision_id` BIGINT NULL,
 `original_result_disposition` VARCHAR(30) NULL,
 `close_reason` VARCHAR(1000) NULL,
 `close_signature_id` BIGINT NULL,
 `close_signature_evidence_json` LONGTEXT NULL,
 CHECK (`close_signature_evidence_json` IS NULL OR JSON_VALID(`close_signature_evidence_json`)),
 `closed_by` BIGINT NULL,
 `closed_at` DATETIME(3) NULL,
 UNIQUE KEY uk_deviation_0 (org_id, deviation_no),
 KEY ix_deviation_main_batch_id (org_id, `main_batch_id`, id),
 KEY ix_deviation_operation_execution_id (org_id, `operation_execution_id`, id),
 KEY ix_deviation_material_lot_id (org_id, `material_lot_id`, id),
 KEY ix_deviation_test_execution_id (org_id, `test_execution_id`, id),
 KEY ix_deviation_original_result_revision (org_id, `original_result_revision_id`, id),
 KEY ix_deviation_decision_signature_id (org_id, `decision_signature_id`, id),
 CHECK ((`decision_signature_id` IS NULL) = (`decision_signature_evidence_json` IS NULL)),
 KEY ix_deviation_decided_by (org_id, `decided_by`, id),
 KEY ix_deviation_selected_result_revision (org_id, `selected_result_revision_id`, id),
 KEY ix_deviation_close_signature_id (org_id, `close_signature_id`, id),
 CHECK ((`close_signature_id` IS NULL) = (`close_signature_evidence_json` IS NULL)),
 KEY ix_deviation_closed_by (org_id, `closed_by`, id),
 KEY ix_deviation_status (org_id,status,id)
) ENGINE=InnoDB;

CREATE TABLE qms_release_decision (
 `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 `org_id` BIGINT NOT NULL,
 `created_by` BIGINT NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `release_scope` VARCHAR(30) NOT NULL,
 `main_batch_id` BIGINT NULL,
 `finished_lot_id` BIGINT NULL,
 `material_lot_id` BIGINT NULL,
 `inspection_report_id` BIGINT NULL,
 `decision` VARCHAR(20) NOT NULL,
 `release_basis` VARCHAR(30) NOT NULL,
 `decision_source` VARCHAR(30) NOT NULL,
 `reason` VARCHAR(1000) NOT NULL,
 `decision_by` BIGINT NULL,
 `decision_at` DATETIME(3) NOT NULL,
 `signature_id` BIGINT NULL,
 `signature_evidence_json` LONGTEXT NULL,
 CHECK (`signature_evidence_json` IS NULL OR JSON_VALID(`signature_evidence_json`)),
 `supersedes_decision_id` BIGINT NULL,
 `rule_evidence_json` LONGTEXT NULL,
 CHECK (`rule_evidence_json` IS NULL OR JSON_VALID(`rule_evidence_json`)),
 `evidence_digest` CHAR(64) NOT NULL,
 UNIQUE KEY uk_release_decision_0 (org_id, supersedes_decision_id),
 KEY ix_release_decision_main_batch_id (org_id, `main_batch_id`, id),
 KEY ix_release_decision_finished_lot_id (org_id, `finished_lot_id`, id),
 KEY ix_release_decision_material_lot_id (org_id, `material_lot_id`, id),
 KEY ix_release_decision_inspection_report_id (org_id, `inspection_report_id`, id),
 KEY ix_release_decision_decision_by (org_id, `decision_by`, id),
 KEY ix_release_decision_signature_id (org_id, `signature_id`, id),
 CHECK ((`signature_id` IS NULL) = (`signature_evidence_json` IS NULL)),
 KEY ix_release_decision_supersedes_decision_id (org_id, `supersedes_decision_id`, id)
) ENGINE=InnoDB;

ALTER TABLE qms_inspection_request ADD CONSTRAINT fk_inspection_request_material_lot_id FOREIGN KEY (`material_lot_id`) REFERENCES md_material_lot(id);

ALTER TABLE qms_inspection_request ADD CONSTRAINT fk_inspection_request_qc_specification_version_id FOREIGN KEY (`qc_specification_version_id`) REFERENCES qc_specification_version(id);

ALTER TABLE qms_inspection_request ADD CONSTRAINT fk_inspection_request_approved_investigation_id FOREIGN KEY (`approved_investigation_id`) REFERENCES qms_deviation(id);

ALTER TABLE qms_inspection_request ADD CONSTRAINT fk_inspection_request_requested_unit_id FOREIGN KEY (`requested_unit_id`) REFERENCES md_unit(id);

ALTER TABLE qms_inspection_request ADD CONSTRAINT fk_inspection_request_requested_by FOREIGN KEY (`requested_by`) REFERENCES sys_user(id);

ALTER TABLE qms_inspection_request ADD CONSTRAINT fk_inspection_request_submitted_by FOREIGN KEY (`submitted_by`) REFERENCES sys_user(id);

ALTER TABLE qms_inspection_request ADD CONSTRAINT fk_inspection_request_accepted_by FOREIGN KEY (`accepted_by`) REFERENCES sys_user(id);

ALTER TABLE qms_inspection_request_item ADD CONSTRAINT fk_inspection_request_i_inspection_request_id FOREIGN KEY (`inspection_request_id`) REFERENCES qms_inspection_request(id);

ALTER TABLE qms_inspection_request_item ADD CONSTRAINT fk_inspection_request_i_material_lot_id FOREIGN KEY (`material_lot_id`) REFERENCES md_material_lot(id);

ALTER TABLE qms_sampling_task ADD CONSTRAINT fk_sampling_task_inspection_request_id FOREIGN KEY (`inspection_request_id`) REFERENCES qms_inspection_request(id);

ALTER TABLE qms_sampling_task ADD CONSTRAINT fk_sampling_task_inspection_request_item_id FOREIGN KEY (`inspection_request_item_id`) REFERENCES qms_inspection_request_item(id);

ALTER TABLE qms_sampling_task ADD CONSTRAINT fk_sampling_task_assigned_to FOREIGN KEY (`assigned_to`) REFERENCES sys_user(id);

ALTER TABLE qms_sampling_task ADD CONSTRAINT fk_sampling_task_plan_signature_id FOREIGN KEY (`plan_signature_id`) REFERENCES gxp_signature(id);

ALTER TABLE qms_sampling_task ADD CONSTRAINT fk_sampling_task_completion_signature_id FOREIGN KEY (`completion_signature_id`) REFERENCES gxp_signature(id);

ALTER TABLE qms_sampling_detail ADD CONSTRAINT fk_sampling_detail_sampling_task_id FOREIGN KEY (`sampling_task_id`) REFERENCES qms_sampling_task(id);

ALTER TABLE qms_sampling_detail ADD CONSTRAINT fk_sampling_detail_unit_id FOREIGN KEY (`unit_id`) REFERENCES md_unit(id);

ALTER TABLE qms_sampling_detail ADD CONSTRAINT fk_sampling_detail_sampled_by FOREIGN KEY (`sampled_by`) REFERENCES sys_user(id);

ALTER TABLE qms_sample ADD CONSTRAINT fk_sample_material_lot_id FOREIGN KEY (`material_lot_id`) REFERENCES md_material_lot(id);

ALTER TABLE qms_sample ADD CONSTRAINT fk_sample_sampling_task_id FOREIGN KEY (`sampling_task_id`) REFERENCES qms_sampling_task(id);

ALTER TABLE qms_sample ADD CONSTRAINT fk_sample_sampling_detail_id FOREIGN KEY (`sampling_detail_id`) REFERENCES qms_sampling_detail(id);

ALTER TABLE qms_sample ADD CONSTRAINT fk_sample_inspection_request_item_id FOREIGN KEY (`inspection_request_item_id`) REFERENCES qms_inspection_request_item(id);

ALTER TABLE qms_sample ADD CONSTRAINT fk_sample_unit_id FOREIGN KEY (`unit_id`) REFERENCES md_unit(id);

ALTER TABLE qms_sample ADD CONSTRAINT fk_sample_received_by FOREIGN KEY (`received_by`) REFERENCES sys_user(id);

ALTER TABLE qms_sample ADD CONSTRAINT fk_sample_disposal_signature_id FOREIGN KEY (`disposal_signature_id`) REFERENCES gxp_signature(id);

ALTER TABLE qms_inspection_task ADD CONSTRAINT fk_inspection_task_inspection_request_id FOREIGN KEY (`inspection_request_id`) REFERENCES qms_inspection_request(id);

ALTER TABLE qms_inspection_task ADD CONSTRAINT fk_inspection_task_sample_id FOREIGN KEY (`sample_id`) REFERENCES qms_sample(id);

ALTER TABLE qms_inspection_task ADD CONSTRAINT fk_inspection_task_assigned_to FOREIGN KEY (`assigned_to`) REFERENCES sys_user(id);

ALTER TABLE qms_inspection_task ADD CONSTRAINT fk_inspection_task_reviewed_by FOREIGN KEY (`reviewed_by`) REFERENCES sys_user(id);

ALTER TABLE qms_inspection_task ADD CONSTRAINT fk_inspection_task_review_signature_id FOREIGN KEY (`review_signature_id`) REFERENCES gxp_signature(id);

ALTER TABLE qms_inspection_item ADD CONSTRAINT fk_inspection_item_inspection_task_id FOREIGN KEY (`inspection_task_id`) REFERENCES qms_inspection_task(id);

ALTER TABLE qms_inspection_item ADD CONSTRAINT fk_inspection_item_qc_specification_item_id FOREIGN KEY (`qc_specification_item_id`) REFERENCES qc_specification_item(id);

ALTER TABLE qms_inspection_item ADD CONSTRAINT fk_inspection_item_unit_id FOREIGN KEY (`unit_id`) REFERENCES md_unit(id);

ALTER TABLE qms_test_execution ADD CONSTRAINT fk_test_execution_inspection_item_id FOREIGN KEY (`inspection_item_id`) REFERENCES qms_inspection_item(id);

ALTER TABLE qms_test_execution ADD CONSTRAINT fk_test_execution_original_execution_id FOREIGN KEY (`original_execution_id`) REFERENCES qms_test_execution(id);

ALTER TABLE qms_test_execution ADD CONSTRAINT fk_test_execution_approved_investigation_id FOREIGN KEY (`approved_investigation_id`) REFERENCES qms_deviation(id);

ALTER TABLE qms_test_execution ADD CONSTRAINT fk_test_execution_instrument_id FOREIGN KEY (`instrument_id`) REFERENCES md_equipment(id);

ALTER TABLE qms_test_execution ADD CONSTRAINT fk_test_execution_performed_by FOREIGN KEY (`performed_by`) REFERENCES sys_user(id);

ALTER TABLE qms_test_result_revision ADD CONSTRAINT fk_test_result_revision_sample_id FOREIGN KEY (`sample_id`) REFERENCES qms_sample(id);

ALTER TABLE qms_test_result_revision ADD CONSTRAINT fk_test_result_revision_inspection_item_id FOREIGN KEY (`inspection_item_id`) REFERENCES qms_inspection_item(id);

ALTER TABLE qms_test_result_revision ADD CONSTRAINT fk_test_result_revision_test_execution_id FOREIGN KEY (`test_execution_id`) REFERENCES qms_test_execution(id);

ALTER TABLE qms_test_result_revision ADD CONSTRAINT fk_test_result_revision_previous_revision_id FOREIGN KEY (`previous_revision_id`) REFERENCES qms_test_result_revision(id);

ALTER TABLE qms_test_result_revision ADD CONSTRAINT fk_test_result_revision_result_unit_id FOREIGN KEY (`result_unit_id`) REFERENCES md_unit(id);

ALTER TABLE qms_test_result_revision ADD CONSTRAINT fk_test_result_revision_recorded_by FOREIGN KEY (`recorded_by`) REFERENCES sys_user(id);

ALTER TABLE qms_test_result_revision ADD CONSTRAINT fk_test_result_revision_signature_id FOREIGN KEY (`signature_id`) REFERENCES gxp_signature(id);

ALTER TABLE qms_inspection_report ADD CONSTRAINT fk_inspection_report_inspection_request_id FOREIGN KEY (`inspection_request_id`) REFERENCES qms_inspection_request(id);

ALTER TABLE qms_inspection_report ADD CONSTRAINT fk_inspection_report_supersedes_report_id FOREIGN KEY (`supersedes_report_id`) REFERENCES qms_inspection_report(id);

ALTER TABLE qms_inspection_report ADD CONSTRAINT fk_inspection_report_reviewed_by FOREIGN KEY (`reviewed_by`) REFERENCES sys_user(id);

ALTER TABLE qms_inspection_report ADD CONSTRAINT fk_inspection_report_approved_by FOREIGN KEY (`approved_by`) REFERENCES sys_user(id);

ALTER TABLE qms_inspection_report ADD CONSTRAINT fk_inspection_report_approval_signature_id FOREIGN KEY (`approval_signature_id`) REFERENCES gxp_signature(id);

ALTER TABLE qms_inspection_report_item ADD CONSTRAINT fk_inspection_report_it_inspection_report_id FOREIGN KEY (`inspection_report_id`) REFERENCES qms_inspection_report(id);

ALTER TABLE qms_inspection_report_item ADD CONSTRAINT fk_inspection_report_it_qc_specification_item_id FOREIGN KEY (`qc_specification_item_id`) REFERENCES qc_specification_item(id);

ALTER TABLE qms_inspection_report_item ADD CONSTRAINT fk_inspection_report_it_inspection_item_id FOREIGN KEY (`inspection_item_id`) REFERENCES qms_inspection_item(id);

ALTER TABLE qms_inspection_report_item ADD CONSTRAINT fk_inspection_report_it_result_revision_id FOREIGN KEY (`result_revision_id`) REFERENCES qms_test_result_revision(id);

ALTER TABLE qms_inspection_report_item ADD CONSTRAINT fk_inspection_report_it_original_result_revision_id FOREIGN KEY (`original_result_revision_id`) REFERENCES qms_test_result_revision(id);

ALTER TABLE qms_inspection_report_item ADD CONSTRAINT fk_inspection_report_it_investigation_id FOREIGN KEY (`investigation_id`) REFERENCES qms_deviation(id);

ALTER TABLE qms_deviation ADD CONSTRAINT fk_deviation_material_lot_id FOREIGN KEY (`material_lot_id`) REFERENCES md_material_lot(id);

ALTER TABLE qms_deviation ADD CONSTRAINT fk_deviation_test_execution_id FOREIGN KEY (`test_execution_id`) REFERENCES qms_test_execution(id);

ALTER TABLE qms_deviation ADD CONSTRAINT fk_deviation_original_result_revision_id FOREIGN KEY (`original_result_revision_id`) REFERENCES qms_test_result_revision(id);

ALTER TABLE qms_deviation ADD CONSTRAINT fk_deviation_decision_signature_id FOREIGN KEY (`decision_signature_id`) REFERENCES gxp_signature(id);

ALTER TABLE qms_deviation ADD CONSTRAINT fk_deviation_decided_by FOREIGN KEY (`decided_by`) REFERENCES sys_user(id);

ALTER TABLE qms_deviation ADD CONSTRAINT fk_deviation_selected_result_revision_id FOREIGN KEY (`selected_result_revision_id`) REFERENCES qms_test_result_revision(id);

ALTER TABLE qms_deviation ADD CONSTRAINT fk_deviation_close_signature_id FOREIGN KEY (`close_signature_id`) REFERENCES gxp_signature(id);

ALTER TABLE qms_deviation ADD CONSTRAINT fk_deviation_closed_by FOREIGN KEY (`closed_by`) REFERENCES sys_user(id);

ALTER TABLE qms_release_decision ADD CONSTRAINT fk_release_decision_finished_lot_id FOREIGN KEY (`finished_lot_id`) REFERENCES md_material_lot(id);

ALTER TABLE qms_release_decision ADD CONSTRAINT fk_release_decision_material_lot_id FOREIGN KEY (`material_lot_id`) REFERENCES md_material_lot(id);

ALTER TABLE qms_release_decision ADD CONSTRAINT fk_release_decision_inspection_report_id FOREIGN KEY (`inspection_report_id`) REFERENCES qms_inspection_report(id);

ALTER TABLE qms_release_decision ADD CONSTRAINT fk_release_decision_decision_by FOREIGN KEY (`decision_by`) REFERENCES sys_user(id);

ALTER TABLE qms_release_decision ADD CONSTRAINT fk_release_decision_signature_id FOREIGN KEY (`signature_id`) REFERENCES gxp_signature(id);

ALTER TABLE qms_release_decision ADD CONSTRAINT fk_release_decision_supersedes_decision_id FOREIGN KEY (`supersedes_decision_id`) REFERENCES qms_release_decision(id);

DELIMITER $$

CREATE TRIGGER bd_qms_inspection_request BEFORE DELETE ON qms_inspection_request FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited'$$

CREATE TRIGGER bd_qms_inspection_request_item BEFORE DELETE ON qms_inspection_request_item FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited'$$

CREATE TRIGGER bu_qms_inspection_request_item BEFORE UPDATE ON qms_inspection_request_item FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable incoming evidence'$$

CREATE TRIGGER bd_qms_sampling_task BEFORE DELETE ON qms_sampling_task FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited'$$

CREATE TRIGGER bu_qms_sampling_task BEFORE UPDATE ON qms_sampling_task FOR EACH ROW BEGIN IF (OLD.`plan_signature_id` IS NOT NULL AND NOT (OLD.`plan_signature_id` <=> NEW.`plan_signature_id`)) OR (OLD.`plan_signature_evidence_json` IS NOT NULL AND NOT (OLD.`plan_signature_evidence_json` <=> NEW.`plan_signature_evidence_json`)) OR (OLD.`completion_signature_id` IS NOT NULL AND NOT (OLD.`completion_signature_id` <=> NEW.`completion_signature_id`)) OR (OLD.`completion_signature_evidence_json` IS NOT NULL AND NOT (OLD.`completion_signature_evidence_json` <=> NEW.`completion_signature_evidence_json`)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable signature evidence'; END IF; END$$

CREATE TRIGGER bd_qms_sampling_detail BEFORE DELETE ON qms_sampling_detail FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited'$$

CREATE TRIGGER bu_qms_sampling_detail BEFORE UPDATE ON qms_sampling_detail FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable incoming evidence'$$

CREATE TRIGGER bd_qms_sample BEFORE DELETE ON qms_sample FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited'$$

CREATE TRIGGER bu_qms_sample BEFORE UPDATE ON qms_sample FOR EACH ROW BEGIN IF (OLD.`disposal_signature_id` IS NOT NULL AND NOT (OLD.`disposal_signature_id` <=> NEW.`disposal_signature_id`)) OR (OLD.`disposal_signature_evidence_json` IS NOT NULL AND NOT (OLD.`disposal_signature_evidence_json` <=> NEW.`disposal_signature_evidence_json`)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable signature evidence'; END IF; END$$

CREATE TRIGGER bd_qms_inspection_task BEFORE DELETE ON qms_inspection_task FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited'$$

CREATE TRIGGER bu_qms_inspection_task BEFORE UPDATE ON qms_inspection_task FOR EACH ROW BEGIN IF (OLD.`review_signature_id` IS NOT NULL AND NOT (OLD.`review_signature_id` <=> NEW.`review_signature_id`)) OR (OLD.`review_signature_evidence_json` IS NOT NULL AND NOT (OLD.`review_signature_evidence_json` <=> NEW.`review_signature_evidence_json`)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable signature evidence'; END IF; END$$

CREATE TRIGGER bd_qms_inspection_item BEFORE DELETE ON qms_inspection_item FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited'$$

CREATE TRIGGER bu_qms_inspection_item BEFORE UPDATE ON qms_inspection_item FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable incoming evidence'$$

CREATE TRIGGER bd_qms_test_execution BEFORE DELETE ON qms_test_execution FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited'$$

CREATE TRIGGER bu_qms_test_execution BEFORE UPDATE ON qms_test_execution FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable incoming evidence'$$

CREATE TRIGGER bd_qms_test_result_revision BEFORE DELETE ON qms_test_result_revision FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited'$$

CREATE TRIGGER bu_qms_test_result_revision BEFORE UPDATE ON qms_test_result_revision FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable incoming evidence'$$

CREATE TRIGGER bd_qms_inspection_report BEFORE DELETE ON qms_inspection_report FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited'$$

CREATE TRIGGER bu_qms_inspection_report BEFORE UPDATE ON qms_inspection_report FOR EACH ROW BEGIN IF (OLD.`approval_signature_id` IS NOT NULL AND NOT (OLD.`approval_signature_id` <=> NEW.`approval_signature_id`)) OR (OLD.`approval_signature_evidence_json` IS NOT NULL AND NOT (OLD.`approval_signature_evidence_json` <=> NEW.`approval_signature_evidence_json`)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable signature evidence'; END IF; END$$

CREATE TRIGGER bd_qms_inspection_report_item BEFORE DELETE ON qms_inspection_report_item FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited'$$

CREATE TRIGGER bu_qms_inspection_report_item BEFORE UPDATE ON qms_inspection_report_item FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable incoming evidence'$$

CREATE TRIGGER bd_qms_deviation BEFORE DELETE ON qms_deviation FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited'$$

CREATE TRIGGER bu_qms_deviation BEFORE UPDATE ON qms_deviation FOR EACH ROW BEGIN IF (OLD.`decision_signature_id` IS NOT NULL AND NOT (OLD.`decision_signature_id` <=> NEW.`decision_signature_id`)) OR (OLD.`decision_signature_evidence_json` IS NOT NULL AND NOT (OLD.`decision_signature_evidence_json` <=> NEW.`decision_signature_evidence_json`)) OR (OLD.`close_signature_id` IS NOT NULL AND NOT (OLD.`close_signature_id` <=> NEW.`close_signature_id`)) OR (OLD.`close_signature_evidence_json` IS NOT NULL AND NOT (OLD.`close_signature_evidence_json` <=> NEW.`close_signature_evidence_json`)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable signature evidence'; END IF; END$$

CREATE TRIGGER bd_qms_release_decision BEFORE DELETE ON qms_release_decision FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated record deletion prohibited'$$

CREATE TRIGGER bu_qms_release_decision BEFORE UPDATE ON qms_release_decision FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable incoming evidence'$$

DELIMITER ;

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'audit:view','ACTION','audit:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='audit:view');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qa:material-release:decide','ACTION','qa:material-release:decide',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qa:material-release:decide');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qa:material-release:view','ACTION','qa:material-release:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qa:material-release:view');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:deviation:close','ACTION','qms:deviation:close',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:deviation:close');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:deviation:create','ACTION','qms:deviation:create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:deviation:create');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:deviation:decide','ACTION','qms:deviation:decide',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:deviation:decide');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:deviation:investigate','ACTION','qms:deviation:investigate',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:deviation:investigate');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:deviation:update','ACTION','qms:deviation:update',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:deviation:update');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:deviation:view','ACTION','qms:deviation:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:deviation:view');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:inspection-request:accept','ACTION','qms:inspection-request:accept',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:inspection-request:accept');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:inspection-request:create','ACTION','qms:inspection-request:create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:inspection-request:create');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:inspection-request:submit','ACTION','qms:inspection-request:submit',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:inspection-request:submit');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:inspection-request:view','ACTION','qms:inspection-request:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:inspection-request:view');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:report:approve','ACTION','qms:report:approve',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:report:approve');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:report:create','ACTION','qms:report:create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:report:create');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:report:review','ACTION','qms:report:review',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:report:review');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:report:view','ACTION','qms:report:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:report:view');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:sampling:approve-plan','ACTION','qms:sampling:approve-plan',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:sampling:approve-plan');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:sampling:assign','ACTION','qms:sampling:assign',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:sampling:assign');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:sampling:complete','ACTION','qms:sampling:complete',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:sampling:complete');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:sampling:create','ACTION','qms:sampling:create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:sampling:create');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:sampling:execute','ACTION','qms:sampling:execute',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:sampling:execute');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:sampling:view','ACTION','qms:sampling:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:sampling:view');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:test:correct','ACTION','qms:test:correct',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:test:correct');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:test:execute','ACTION','qms:test:execute',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:test:execute');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:test:review','ACTION','qms:test:review',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:test:review');

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'qms:test:view','ACTION','qms:test:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='qms:test:view');

INSERT INTO sys_role_permission(role_id,permission_id) SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON p.permission_code IN ('audit:view','qa:material-release:decide','qa:material-release:view','qms:deviation:close','qms:deviation:create','qms:deviation:decide','qms:deviation:investigate','qms:deviation:update','qms:deviation:view','qms:inspection-request:accept','qms:inspection-request:create','qms:inspection-request:submit','qms:inspection-request:view','qms:report:approve','qms:report:create','qms:report:review','qms:report:view','qms:sampling:approve-plan','qms:sampling:assign','qms:sampling:complete','qms:sampling:create','qms:sampling:execute','qms:sampling:view','qms:test:correct','qms:test:execute','qms:test:review','qms:test:view') WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_permission rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);

-- Frozen conditional shapes and state dictionaries (not workflow bypass APIs).
ALTER TABLE qms_inspection_request ADD CHECK(status IN ('DRAFT','SUBMITTED','ACCEPTED','IN_PROGRESS','COMPLETED')), ADD CHECK(request_type IN ('INITIAL','RETEST','SUPPLEMENTARY','INVESTIGATION')), ADD CHECK(priority IN ('NORMAL','URGENT')), ADD CHECK(request_type <> 'RETEST' OR approved_investigation_id IS NOT NULL);
ALTER TABLE qms_sampling_task ADD CHECK(status IN ('PLANNED','ASSIGNED','IN_PROGRESS','COMPLETED'));
ALTER TABLE qms_sample ADD CHECK(status IN ('CREATED','COLLECTED','RECEIVED','IN_TEST','TEST_COMPLETED','RETAINED','DISPOSED')), ADD CHECK(sample_type IN ('TEST_SAMPLE','RETENTION_SAMPLE','RETEST_SAMPLE','OTHER_APPROVED')), ADD CHECK(sample_scope <> 'INCOMING_MATERIAL' OR (main_batch_id IS NULL AND material_lot_id IS NOT NULL AND sampling_task_id IS NOT NULL AND sampling_detail_id IS NOT NULL AND inspection_request_item_id IS NOT NULL AND quantity IS NOT NULL AND unit_id IS NOT NULL AND sampled_at IS NOT NULL));
ALTER TABLE qms_inspection_task ADD CHECK(status IN ('CREATED','ASSIGNED','IN_PROGRESS','PENDING_REVIEW','QC_PASSED','QC_FAILED'));
ALTER TABLE qms_inspection_item ADD CHECK((result_type='NUMERIC' AND unit_id IS NOT NULL AND (lower_limit IS NOT NULL OR upper_limit IS NOT NULL) AND text_acceptance_criteria IS NULL AND (lower_limit IS NULL OR upper_limit IS NULL OR lower_limit <= upper_limit)) OR (result_type='TEXT' AND unit_id IS NULL AND lower_limit IS NULL AND upper_limit IS NULL AND text_acceptance_criteria IS NOT NULL));
ALTER TABLE qms_test_execution ADD CHECK((original_execution_id IS NULL)=(approved_investigation_id IS NULL)), ADD CHECK(completed_at >= started_at);
ALTER TABLE qms_test_result_revision ADD CHECK(result_conclusion IN ('PASS','FAIL','INCONCLUSIVE','INVALID')), ADD CHECK((revision_no=1 AND previous_revision_id IS NULL) OR (revision_no>1 AND previous_revision_id IS NOT NULL AND reason_for_change IS NOT NULL)), ADD CHECK(NOT(result_numeric IS NOT NULL AND result_text IS NOT NULL));
ALTER TABLE qms_inspection_report ADD CHECK(status IN ('DRAFT','REVIEWED','APPROVED')), ADD CHECK(overall_result IN ('PASS','FAIL','INCONCLUSIVE','INVALID'));
ALTER TABLE qms_deviation ADD CHECK(status IN ('OPEN','INVESTIGATING','DECIDED','CLOSED')), ADD CHECK(investigation_scope <> 'INCOMING_MATERIAL' OR (material_lot_id IS NOT NULL AND main_batch_id IS NULL AND operation_execution_id IS NULL)), ADD CHECK(investigation_kind <> 'OOS' OR (test_execution_id IS NOT NULL AND original_result_revision_id IS NOT NULL)), ADD CHECK(decision_code IS NULL OR decision_code IN ('REJECT','AUTHORIZE_RETEST','ACCEPT_WITH_JUSTIFICATION')), ADD CHECK(decision_code <> 'AUTHORIZE_RETEST' OR (authorized_retest_count IS NOT NULL AND authorized_retest_count > 0)), ADD CHECK(original_result_disposition IS NULL OR original_result_disposition IN ('VALID','INVALID'));
ALTER TABLE qms_release_decision ADD CHECK(release_scope IN ('INCOMING_MATERIAL','FINISHED_PRODUCT')), ADD CHECK((release_scope='INCOMING_MATERIAL' AND material_lot_id IS NOT NULL AND main_batch_id IS NULL AND finished_lot_id IS NULL) OR (release_scope='FINISHED_PRODUCT' AND main_batch_id IS NOT NULL AND material_lot_id IS NULL)), ADD CHECK(decision IN ('RELEASED','REJECTED','OTHER_DISPOSITION')), ADD CHECK(release_basis IN ('FULL_INSPECTION','INSPECTION_EXEMPT','RETEST','OTHER_APPROVED_BASIS')), ADD CHECK((decision_source='USER_QA' AND decision_by IS NOT NULL AND signature_id IS NOT NULL AND signature_evidence_json IS NOT NULL) OR (decision_source='SYSTEM_RULE' AND decision_by IS NULL AND signature_id IS NULL AND signature_evidence_json IS NULL AND rule_evidence_json IS NOT NULL AND decision='RELEASED' AND release_basis='INSPECTION_EXEMPT'));
