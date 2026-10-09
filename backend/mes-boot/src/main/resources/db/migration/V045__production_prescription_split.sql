-- Split production prescription from production process.
-- Legacy proc_package/product/formula data is retained for historical snapshots and backward-compatible reads.

ALTER TABLE proc_package MODIFY product_id BIGINT NULL;

CREATE TABLE prd_prescription (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 prescription_code VARCHAR(64) NOT NULL,
 prescription_name VARCHAR(200) NOT NULL,
 product_id BIGINT NOT NULL,
 process_package_id BIGINT NOT NULL,
 batch_basis_qty DECIMAL(18,6) NOT NULL CHECK(batch_basis_qty>0),
 unit_id BIGINT NOT NULL,
 status VARCHAR(20) NOT NULL CHECK(status IN ('DRAFT','ACTIVE','INACTIVE')),
 effective_from DATETIME(3) NULL,
 effective_to DATETIME(3) NULL,
 active_product_id BIGINT GENERATED ALWAYS AS (CASE WHEN status='ACTIVE' THEN product_id ELSE NULL END) PERSISTENT,
 UNIQUE KEY uk_prd_prescription_code(org_id,prescription_code),
 UNIQUE KEY uk_prd_active_product(org_id,active_product_id),
 KEY idx_prd_prescription_product(org_id,product_id,status),
 KEY idx_prd_prescription_process(org_id,process_package_id,status),
 FOREIGN KEY(product_id) REFERENCES md_product(id),
 FOREIGN KEY(process_package_id) REFERENCES proc_package(id),
 FOREIGN KEY(unit_id) REFERENCES md_unit(id),
 CHECK(effective_to IS NULL OR effective_from IS NULL OR effective_to>=effective_from)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE prd_prescription_item (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 version_no BIGINT NOT NULL DEFAULT 0,
 prescription_id BIGINT NOT NULL,
 line_no INT NOT NULL CHECK(line_no>0),
 material_id BIGINT NOT NULL,
 required_qty DECIMAL(18,6) NOT NULL CHECK(required_qty>0),
 unit_id BIGINT NOT NULL,
 overage_pct DECIMAL(9,6) NULL CHECK(overage_pct>=0),
 critical BOOLEAN NOT NULL DEFAULT FALSE,
 UNIQUE KEY uk_prd_prescription_line(org_id,prescription_id,line_no),
 KEY idx_prd_prescription_material(org_id,material_id),
 FOREIGN KEY(prescription_id) REFERENCES prd_prescription(id),
 FOREIGN KEY(material_id) REFERENCES md_material(id),
 FOREIGN KEY(unit_id) REFERENCES md_unit(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Preserve existing operator-visible BOMs as DRAFT prescriptions.
-- Nothing is activated automatically because one product may currently have several active legacy packages.
INSERT INTO prd_prescription(
 org_id,created_by,created_at,updated_by,updated_at,version_no,
 prescription_code,prescription_name,product_id,process_package_id,batch_basis_qty,unit_id,status
)
SELECT p.org_id,p.created_by,p.created_at,p.updated_by,p.updated_at,0,
 CONCAT('RX-',LEFT(p.package_code,61)),
 CONCAT(prod.product_name,' · 历史迁移处方'),
 p.product_id,p.id,f.batch_basis_qty,f.unit_id,'DRAFT'
FROM proc_package p
JOIN md_product prod ON prod.id=p.product_id AND prod.org_id=p.org_id
JOIN proc_current_definition c ON c.package_id=p.id AND c.org_id=p.org_id
JOIN proc_formula_version f ON f.current_definition_id=c.id AND f.org_id=p.org_id
WHERE p.product_id IS NOT NULL
  AND NOT EXISTS(
   SELECT 1 FROM prd_prescription x
   WHERE x.org_id=p.org_id AND x.prescription_code=CONCAT('RX-',LEFT(p.package_code,61))
  );

INSERT INTO prd_prescription_item(
 org_id,created_by,created_at,updated_by,updated_at,version_no,
 prescription_id,line_no,material_id,required_qty,unit_id,overage_pct,critical
)
SELECT i.org_id,i.created_by,i.created_at,i.updated_by,i.updated_at,0,
 rx.id,i.line_no,i.material_id,i.required_qty,i.unit_id,i.overage_pct,i.critical
FROM proc_package p
JOIN proc_current_definition c ON c.package_id=p.id AND c.org_id=p.org_id
JOIN proc_formula_version f ON f.current_definition_id=c.id AND f.org_id=p.org_id
JOIN proc_formula_item i ON i.formula_version_id=f.id AND i.org_id=p.org_id
JOIN prd_prescription rx ON rx.org_id=p.org_id
 AND rx.prescription_code=CONCAT('RX-',LEFT(p.package_code,61))
WHERE NOT EXISTS(
 SELECT 1 FROM prd_prescription_item x
 WHERE x.org_id=i.org_id AND x.prescription_id=rx.id AND x.line_no=i.line_no
);

ALTER TABLE prd_process_snapshot
 MODIFY formula_version_id BIGINT NULL,
 ADD prescription_id BIGINT NULL,
 ADD KEY idx_prd_snapshot_prescription(org_id,prescription_id),
 ADD FOREIGN KEY(prescription_id) REFERENCES prd_prescription(id);

-- Execution/WMS evidence keeps legacy formula_item_id and adds explicit prescription_item_id.
-- New rows use prescription_item_id; legacy rows keep formula_item_id.
ALTER TABLE wms_reservation
 MODIFY formula_item_id BIGINT NULL,
 ADD prescription_item_id BIGINT NULL,
 ADD KEY idx_reservation_prescription_item(org_id,prescription_item_id),
 ADD FOREIGN KEY(prescription_item_id) REFERENCES prd_prescription_item(id),
 ADD CONSTRAINT ck_reservation_recipe_source CHECK(
  (formula_item_id IS NOT NULL AND prescription_item_id IS NULL)
  OR (formula_item_id IS NULL AND prescription_item_id IS NOT NULL)
 );

ALTER TABLE wms_material_request_item
 MODIFY formula_item_id BIGINT NULL,
 ADD prescription_item_id BIGINT NULL,
 ADD UNIQUE KEY uk_wms_request_prescription(org_id,request_id,prescription_item_id),
 ADD KEY idx_request_prescription_item(org_id,prescription_item_id),
 ADD FOREIGN KEY(prescription_item_id) REFERENCES prd_prescription_item(id),
 ADD CONSTRAINT ck_request_recipe_source CHECK(
  (formula_item_id IS NOT NULL AND prescription_item_id IS NULL)
  OR (formula_item_id IS NULL AND prescription_item_id IS NOT NULL)
 );

ALTER TABLE wms_material_issue_item
 ADD prescription_item_id BIGINT NULL,
 ADD KEY idx_issue_prescription_item(org_id,prescription_item_id),
 ADD FOREIGN KEY(prescription_item_id) REFERENCES prd_prescription_item(id),
 ADD CONSTRAINT ck_issue_recipe_source CHECK(NOT(formula_item_id IS NOT NULL AND prescription_item_id IS NOT NULL));

ALTER TABLE mes_weighing_record
 MODIFY bom_item_id BIGINT NULL,
 ADD prescription_item_id BIGINT NULL,
 ADD KEY idx_weighing_prescription_item(org_id,prescription_item_id),
 ADD FOREIGN KEY(prescription_item_id) REFERENCES prd_prescription_item(id),
 ADD CONSTRAINT ck_weighing_recipe_source CHECK(
  (bom_item_id IS NOT NULL AND prescription_item_id IS NULL)
  OR (bom_item_id IS NULL AND prescription_item_id IS NOT NULL)
 );

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled)
 SELECT 'production:prescription:view','ACTION','production:prescription:view',NULL,TRUE
 WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='production:prescription:view');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled)
 SELECT 'production:prescription:create','ACTION','production:prescription:create',NULL,TRUE
 WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='production:prescription:create');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled)
 SELECT 'production:prescription:update','ACTION','production:prescription:update',NULL,TRUE
 WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='production:prescription:update');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled)
 SELECT 'production:prescription:activate','ACTION','production:prescription:activate',NULL,TRUE
 WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='production:prescription:activate');

INSERT INTO sys_role_permission(role_id,permission_id)
 SELECT r.id,p.id FROM sys_role r JOIN sys_permission p
 ON p.permission_code IN (
  'production:prescription:view','production:prescription:create',
  'production:prescription:update','production:prescription:activate'
 )
 WHERE r.role_code='SYSTEM_ADMIN'
 AND NOT EXISTS(
  SELECT 1 FROM sys_role_permission rp
  WHERE rp.role_id=r.id AND rp.permission_id=p.id
 );

INSERT INTO sys_menu(
 org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,
 created_by,updated_by,permission_code,required_permissions
)
SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_production'),
 'nav_production-prescriptions','生产处方','/production/prescriptions',5,'ACTIVE',
 1,1,'production:prescription:view','master:product:view;process:package:view'
WHERE NOT EXISTS(
 SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/production/prescriptions'
);

INSERT INTO sys_role_menu(org_id,role_id,menu_id,created_by)
 SELECT m.org_id,r.id,m.id,1
 FROM sys_role r JOIN sys_menu m ON m.route_path='/production/prescriptions'
 WHERE r.role_code='SYSTEM_ADMIN'
 AND NOT EXISTS(
  SELECT 1 FROM sys_role_menu rm
  WHERE rm.org_id=m.org_id AND rm.role_id=r.id AND rm.menu_id=m.id
 );
