-- DCP-BASIC-NO-AUDIT-VERSION-001. Append-only upgrade; legacy evidence is not rewritten.
CREATE TABLE proc_current_definition (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL, package_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL,
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL,
 status VARCHAR(20) NOT NULL CHECK(status IN ('DRAFT','EFFECTIVE')),
 UNIQUE KEY uk_current_process(org_id,package_id),
 FOREIGN KEY(package_id) REFERENCES proc_package(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE proc_formula_version
 MODIFY package_version_id BIGINT NULL, MODIFY version INT NULL,
 ADD current_definition_id BIGINT NULL,
 ADD UNIQUE KEY uk_current_formula(org_id,current_definition_id),
 ADD FOREIGN KEY(current_definition_id) REFERENCES proc_current_definition(id),
 ADD CONSTRAINT ck_formula_binding CHECK((package_version_id IS NULL) <> (current_definition_id IS NULL));
ALTER TABLE proc_route_version
 MODIFY package_version_id BIGINT NULL, MODIFY version INT NULL,
 ADD current_definition_id BIGINT NULL,
 ADD UNIQUE KEY uk_current_route(org_id,current_definition_id),
 ADD FOREIGN KEY(current_definition_id) REFERENCES proc_current_definition(id),
 ADD CONSTRAINT ck_route_binding CHECK((package_version_id IS NULL) <> (current_definition_id IS NULL));

-- Copy the current operator-visible configuration, never reuse a historical mutable root.
INSERT INTO proc_current_definition(id,org_id,package_id,created_by,created_at,updated_by,updated_at,status)
 SELECT p.id,p.org_id,p.id,p.created_by,p.created_at,p.updated_by,p.updated_at,
 CASE WHEN v.status IN ('APPROVED','EFFECTIVE') THEN 'EFFECTIVE' ELSE 'DRAFT' END
 FROM proc_package p LEFT JOIN proc_package_version v ON v.package_id=p.id AND v.org_id=p.org_id
 AND v.version=(SELECT MAX(v2.version) FROM proc_package_version v2 WHERE v2.org_id=p.org_id AND v2.package_id=p.id);

INSERT INTO proc_formula_version(org_id,created_by,created_at,updated_by,updated_at,version_no,current_definition_id,formula_code,batch_basis_qty,unit_id)
 SELECT f.org_id,f.created_by,f.created_at,f.updated_by,f.updated_at,0,c.id,f.formula_code,f.batch_basis_qty,f.unit_id
 FROM proc_current_definition c JOIN proc_package_version v ON v.package_id=c.package_id AND v.org_id=c.org_id
 AND v.version=(SELECT MAX(v2.version) FROM proc_package_version v2 WHERE v2.org_id=c.org_id AND v2.package_id=c.package_id)
 JOIN proc_formula_version f ON f.package_version_id=v.id AND f.org_id=c.org_id;
INSERT INTO proc_formula_item(org_id,created_by,created_at,updated_by,updated_at,version_no,formula_version_id,line_no,material_id,required_qty,unit_id,overage_pct,critical)
 SELECT i.org_id,i.created_by,i.created_at,i.updated_by,i.updated_at,0,n.id,i.line_no,i.material_id,i.required_qty,i.unit_id,i.overage_pct,i.critical
 FROM proc_formula_version n JOIN proc_current_definition c ON c.id=n.current_definition_id
 JOIN proc_package_version v ON v.package_id=c.package_id AND v.org_id=c.org_id
 AND v.version=(SELECT MAX(v2.version) FROM proc_package_version v2 WHERE v2.org_id=c.org_id AND v2.package_id=c.package_id)
 JOIN proc_formula_version f ON f.package_version_id=v.id AND f.org_id=c.org_id
 JOIN proc_formula_item i ON i.formula_version_id=f.id AND i.org_id=c.org_id;
INSERT INTO proc_route_version(org_id,created_by,created_at,updated_by,updated_at,version_no,current_definition_id,route_code)
 SELECT r.org_id,r.created_by,r.created_at,r.updated_by,r.updated_at,0,c.id,r.route_code
 FROM proc_current_definition c JOIN proc_package_version v ON v.package_id=c.package_id AND v.org_id=c.org_id
 AND v.version=(SELECT MAX(v2.version) FROM proc_package_version v2 WHERE v2.org_id=c.org_id AND v2.package_id=c.package_id)
 JOIN proc_route_version r ON r.package_version_id=v.id AND r.org_id=c.org_id;
INSERT INTO proc_operation_def(org_id,created_by,created_at,updated_by,updated_at,version_no,route_version_id,operation_code,operation_name,sequence_no,required_role,completion_rule,predecessor_codes_json,required_equipment_type,clearance_required,ipc_definitions_json)
 SELECT o.org_id,o.created_by,o.created_at,o.updated_by,o.updated_at,0,n.id,o.operation_code,o.operation_name,o.sequence_no,o.required_role,o.completion_rule,o.predecessor_codes_json,o.required_equipment_type,o.clearance_required,o.ipc_definitions_json
 FROM proc_route_version n JOIN proc_current_definition c ON c.id=n.current_definition_id
 JOIN proc_package_version v ON v.package_id=c.package_id AND v.org_id=c.org_id
 AND v.version=(SELECT MAX(v2.version) FROM proc_package_version v2 WHERE v2.org_id=c.org_id AND v2.package_id=c.package_id)
 JOIN proc_route_version r ON r.package_version_id=v.id AND r.org_id=c.org_id
 JOIN proc_operation_def o ON o.route_version_id=r.id AND o.org_id=c.org_id;
INSERT INTO proc_parameter_def(org_id,created_by,created_at,updated_by,updated_at,version_no,operation_def_id,parameter_code,parameter_name,acquisition_mode,unit_id,lower_limit,upper_limit)
 SELECT q.org_id,q.created_by,q.created_at,q.updated_by,q.updated_at,0,nop.id,q.parameter_code,q.parameter_name,q.acquisition_mode,q.unit_id,q.lower_limit,q.upper_limit
 FROM proc_route_version nr JOIN proc_current_definition c ON c.id=nr.current_definition_id
 JOIN proc_package_version v ON v.package_id=c.package_id AND v.org_id=c.org_id
 AND v.version=(SELECT MAX(v2.version) FROM proc_package_version v2 WHERE v2.org_id=c.org_id AND v2.package_id=c.package_id)
 JOIN proc_route_version r ON r.package_version_id=v.id AND r.org_id=c.org_id
 JOIN proc_operation_def op ON op.route_version_id=r.id AND op.org_id=c.org_id
 JOIN proc_operation_def nop ON nop.route_version_id=nr.id AND nop.org_id=c.org_id AND nop.operation_code=op.operation_code
 JOIN proc_parameter_def q ON q.operation_def_id=op.id AND q.org_id=c.org_id;

ALTER TABLE ebr_template_version
 MODIFY package_version_id BIGINT NULL,
 ADD process_package_id BIGINT NULL,
 ADD FOREIGN KEY(process_package_id) REFERENCES proc_package(id),
 ADD CONSTRAINT ck_ebr_process_binding CHECK((package_version_id IS NULL) <> (process_package_id IS NULL));
ALTER TABLE prd_process_snapshot
 MODIFY package_version_id BIGINT NULL,
 ADD process_package_id BIGINT NULL,
 ADD FOREIGN KEY(process_package_id) REFERENCES proc_package(id),
 ADD CONSTRAINT ck_snapshot_process_binding CHECK((package_version_id IS NULL) <> (process_package_id IS NULL));
