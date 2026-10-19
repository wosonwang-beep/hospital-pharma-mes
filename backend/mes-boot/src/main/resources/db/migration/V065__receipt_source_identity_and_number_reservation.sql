-- Receiving SOURCE_A integration. Existing records/values and Flyway history remain intact.
ALTER TABLE wms_material_receipt
 ADD COLUMN input_contract VARCHAR(32) NULL COMMENT 'Receiving contract version; null preserves legacy semantics',
 ADD COLUMN arrival_date DATE NULL COMMENT 'Actual arrival date; not backfilled from creation time',
 ADD COLUMN acceptance_text VARCHAR(1000) NULL COMMENT 'Operator arrival acceptance notes',
 MODIFY transport_check_passed TINYINT(1) NULL COMMENT 'NULL unchecked, 0 failed, 1 passed';
ALTER TABLE wms_material_receipt_item
 MODIFY package_check_passed TINYINT(1) NULL,
 MODIFY seal_check_passed TINYINT(1) NULL,
 MODIFY label_check_passed TINYINT(1) NULL,
 MODIFY damage_check_passed TINYINT(1) NULL,
 MODIFY contamination_check_passed TINYINT(1) NULL;
CREATE TABLE wms_receipt_number_rule (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL,
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL, version_no BIGINT NOT NULL DEFAULT 0,
 prefix VARCHAR(20) NOT NULL, date_pattern VARCHAR(20) NOT NULL,
 sequence_width INT NOT NULL, next_value BIGINT NOT NULL DEFAULT 1,
 timezone_id VARCHAR(64) NOT NULL, approval_reference VARCHAR(200) NULL,
 enabled TINYINT(1) NOT NULL DEFAULT 0,
 UNIQUE KEY uk_receipt_number_rule_org(org_id),
 CHECK(sequence_width BETWEEN 1 AND 18), CHECK(next_value > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Controlled receiving number configuration; no automatically approved rule';
CREATE TABLE wms_receipt_number_reservation (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL,
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL, version_no BIGINT NOT NULL DEFAULT 0,
 rule_id BIGINT NOT NULL, material_id BIGINT NOT NULL, client_line_id VARCHAR(100) NOT NULL,
 lot_no VARCHAR(100) NOT NULL, approval_reference VARCHAR(200) NOT NULL,
 bound_receipt_item_id BIGINT NULL,
 UNIQUE KEY uk_receipt_reserved_number(org_id,lot_no),
 UNIQUE KEY uk_receipt_reserved_intent(org_id,client_line_id),
 FOREIGN KEY(rule_id) REFERENCES wms_receipt_number_rule(id),
 FOREIGN KEY(material_id) REFERENCES md_material(id),
 FOREIGN KEY(bound_receipt_item_id) REFERENCES wms_material_receipt_item(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Persistent reservation is not stock; never recycled after cancellation';
