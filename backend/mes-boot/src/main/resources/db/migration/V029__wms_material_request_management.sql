-- Approved DCP-WMS-REQUEST-INVENTORY-RETURN-001; native successful history 028 verified before allocation.
CREATE TABLE wms_material_request (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 request_no VARCHAR(80) NOT NULL, main_batch_id BIGINT NOT NULL, process_snapshot_id BIGINT NOT NULL,
 status VARCHAR(30) NOT NULL CHECK(status IN ('DRAFT','SUBMITTED','PARTIALLY_ISSUED','FULFILLED','CANCELLED')),
 submitted_by BIGINT NULL, submitted_at DATETIME(3) NULL, cancelled_by BIGINT NULL, cancelled_at DATETIME(3) NULL, cancellation_reason VARCHAR(1000) NULL,
 UNIQUE KEY uk_wms_request_number(org_id,request_no),
 KEY ix_wms_request_batch(org_id,main_batch_id,status,id), KEY ix_wms_request_status(org_id,status,created_at,id),
 FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id), FOREIGN KEY(process_snapshot_id) REFERENCES prd_process_snapshot(id),
 CHECK((submitted_by IS NULL)=(submitted_at IS NULL)), CHECK((cancelled_by IS NULL)=(cancelled_at IS NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE wms_material_request_item (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), version_no BIGINT NOT NULL DEFAULT 0,
 request_id BIGINT NOT NULL, formula_item_id BIGINT NOT NULL, material_id BIGINT NOT NULL,
 requested_qty DECIMAL(18,6) NOT NULL CHECK(requested_qty>0), unit_id BIGINT NOT NULL,
 UNIQUE KEY uk_wms_request_formula(org_id,request_id,formula_item_id),
 FOREIGN KEY(request_id) REFERENCES wms_material_request(id), FOREIGN KEY(formula_item_id) REFERENCES proc_formula_item(id),
 FOREIGN KEY(material_id) REFERENCES md_material(id), FOREIGN KEY(unit_id) REFERENCES md_unit(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
ALTER TABLE wms_material_issue ADD COLUMN material_request_id BIGINT NULL,
 ADD CONSTRAINT fk_wms_issue_request FOREIGN KEY(material_request_id) REFERENCES wms_material_request(id),
 ADD KEY ix_wms_issue_request(org_id,material_request_id);
ALTER TABLE wms_material_issue_item ADD COLUMN material_request_item_id BIGINT NULL,
 ADD CONSTRAINT fk_wms_issue_request_item FOREIGN KEY(material_request_item_id) REFERENCES wms_material_request_item(id),
 ADD KEY ix_wms_issue_request_item(org_id,material_request_item_id);
INSERT INTO sys_permission(permission_code,permission_type,display_name,enabled)
SELECT p.code,'ACTION',p.label,TRUE FROM (
 SELECT 'wms:request:view' code,'领料申请查看' label UNION ALL SELECT 'wms:request:create','领料申请新建'
 UNION ALL SELECT 'wms:request:update','领料申请编辑' UNION ALL SELECT 'wms:request:submit','领料申请提交'
 UNION ALL SELECT 'wms:request:cancel','领料申请取消'
) p WHERE NOT EXISTS(SELECT 1 FROM sys_permission old WHERE old.permission_code=p.code);
INSERT INTO sys_role_permission(role_id,permission_id)
SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON p.permission_code IN
 ('wms:request:view','wms:request:create','wms:request:update','wms:request:submit','wms:request:cancel')
WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_permission old WHERE old.role_id=r.id AND old.permission_id=p.id);
