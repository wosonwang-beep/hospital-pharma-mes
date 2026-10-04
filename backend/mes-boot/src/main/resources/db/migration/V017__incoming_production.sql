-- v1.0.11 real production; append-only migration. Native history allocation is controlled by root.
CREATE TABLE prd_production_order (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 order_no VARCHAR(80) NOT NULL, product_id BIGINT NOT NULL, planned_qty DECIMAL(18,6) NOT NULL CHECK(planned_qty>0), unit_id BIGINT NOT NULL, planned_date DATE NULL,
 status VARCHAR(30) NOT NULL CHECK(status IN ('DRAFT','IN_PROGRESS','COMPLETED')),
 UNIQUE KEY uk_prd_order(org_id,order_no), FOREIGN KEY(product_id) REFERENCES md_product(id), FOREIGN KEY(unit_id) REFERENCES md_unit(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE prd_process_snapshot (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 package_version_id BIGINT NOT NULL, formula_version_id BIGINT NOT NULL, route_version_id BIGINT NOT NULL, ebr_template_version_id BIGINT NOT NULL,
 snapshot_json LONGTEXT NOT NULL CHECK(JSON_VALID(snapshot_json)), snapshot_hash VARCHAR(128) NOT NULL,
 UNIQUE KEY uk_prd_snapshot_hash(snapshot_hash),
 FOREIGN KEY(package_version_id) REFERENCES proc_package_version(id), FOREIGN KEY(formula_version_id) REFERENCES proc_formula_version(id),
 FOREIGN KEY(route_version_id) REFERENCES proc_route_version(id), FOREIGN KEY(ebr_template_version_id) REFERENCES ebr_template_version(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE prd_main_batch (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 batch_no VARCHAR(80) NOT NULL, production_order_id BIGINT NOT NULL, product_id BIGINT NOT NULL, process_snapshot_id BIGINT NULL,
 planned_qty DECIMAL(18,6) NOT NULL CHECK(planned_qty>0), unit_id BIGINT NOT NULL, planned_date DATE NULL,
 status VARCHAR(30) NOT NULL CHECK(status IN ('DRAFT','RELEASED','IN_PROGRESS','PRODUCTION_COMPLETED','PENDING_QA','QA_RELEASED','REJECTED')),
 released_at DATETIME(3) NULL, started_at DATETIME(3) NULL, completed_at DATETIME(3) NULL,
 CHECK((status='DRAFT' AND process_snapshot_id IS NULL) OR (status<>'DRAFT' AND process_snapshot_id IS NOT NULL)),
 UNIQUE KEY uk_prd_batch(org_id,batch_no), KEY idx_prd_batch_order(org_id,production_order_id), KEY idx_prd_batch_plan(org_id,planned_date),
 FOREIGN KEY(production_order_id) REFERENCES prd_production_order(id), FOREIGN KEY(product_id) REFERENCES md_product(id),
 FOREIGN KEY(process_snapshot_id) REFERENCES prd_process_snapshot(id), FOREIGN KEY(unit_id) REFERENCES md_unit(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE prd_sub_batch (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 main_batch_id BIGINT NOT NULL, sub_batch_no VARCHAR(80) NOT NULL, sequence_no INT NOT NULL CHECK(sequence_no>0), planned_qty DECIMAL(18,6) NULL CHECK(planned_qty IS NULL OR planned_qty>0),
 status VARCHAR(30) NOT NULL CHECK(status IN ('PENDING','READY','IN_PROGRESS','COMPLETED','CANCELLED')),
 UNIQUE KEY uk_prd_sub_no(org_id,sub_batch_no), UNIQUE KEY uk_prd_sub_seq(org_id,main_batch_id,sequence_no), FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE prd_execution_unit (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 main_batch_id BIGINT NOT NULL, sub_batch_id BIGINT NULL, unit_type VARCHAR(20) NOT NULL CHECK(unit_type IN ('DIRECT','SUB_BATCH')), execution_no VARCHAR(100) NOT NULL,
 status VARCHAR(30) NOT NULL CHECK(status IN ('PENDING','READY','IN_PROGRESS','PAUSED','COMPLETED','BLOCKED')),
 CHECK((unit_type='DIRECT' AND sub_batch_id IS NULL) OR (unit_type='SUB_BATCH' AND sub_batch_id IS NOT NULL)),
 direct_batch_id BIGINT GENERATED ALWAYS AS (CASE WHEN unit_type='DIRECT' THEN main_batch_id ELSE NULL END) PERSISTENT,
 UNIQUE KEY uk_prd_execution_no(org_id,execution_no), UNIQUE KEY uk_prd_execution_direct(org_id,direct_batch_id), UNIQUE KEY uk_prd_execution_sub(org_id,sub_batch_id),
 FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id), FOREIGN KEY(sub_batch_id) REFERENCES prd_sub_batch(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE wms_reservation ADD CONSTRAINT fk_wms_reservation_main_batch FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id);
ALTER TABLE wms_material_issue ADD CONSTRAINT fk_wms_material_issue_main_batch FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id);
ALTER TABLE qms_sample ADD CONSTRAINT fk_qms_sample_main_batch FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id);
ALTER TABLE qms_release_decision ADD CONSTRAINT fk_qms_release_decision_main_batch FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id);
ALTER TABLE qms_deviation ADD CONSTRAINT fk_qms_deviation_main_batch FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id);
