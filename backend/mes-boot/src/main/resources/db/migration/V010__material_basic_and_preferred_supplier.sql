-- DCP-MATERIAL-BASIC-001: append-only basic maintenance; legacy tables/history stay intact.
ALTER TABLE md_material ADD COLUMN requires_incoming_inspection TINYINT(1) NOT NULL DEFAULT 1;
UPDATE md_material m SET requires_incoming_inspection=(
 SELECT v.requires_incoming_inspection FROM md_material_version v
 WHERE v.org_id=m.org_id AND v.material_id=m.id
 ORDER BY (v.status='APPROVED') DESC,v.version_no_business DESC LIMIT 1
) WHERE EXISTS(SELECT 1 FROM md_material_version v WHERE v.org_id=m.org_id AND v.material_id=m.id);
ALTER TABLE md_material DROP CONSTRAINT ck_material_state,
 ADD CONSTRAINT ck_material_basic_state CHECK(status IN ('ACTIVE','INACTIVE','DRAFT','APPROVED')),
 MODIFY sampling_required TINYINT(1) NULL,
 MODIFY inspection_required TINYINT(1) NULL,
 MODIFY release_required TINYINT(1) NULL,
 MODIFY weighing_required TINYINT(1) NULL,
 MODIFY critical_material TINYINT(1) NULL;
ALTER TABLE md_material_supplier
 ADD COLUMN preferred TINYINT(1) NOT NULL DEFAULT 0,
 ADD COLUMN preferred_material_id BIGINT GENERATED ALWAYS AS (CASE WHEN preferred=1 AND approved=1 THEN material_id ELSE NULL END) PERSISTENT,
 ADD CONSTRAINT ck_material_supplier_preferred CHECK(preferred=0 OR approved=1),
 ADD UNIQUE KEY uk_material_supplier_preferred(org_id,preferred_material_id);
-- Existing relationships have no invented preferred selection; owner explicitly selects one on maintenance.
UPDATE sys_permission SET enabled=FALSE WHERE permission_code IN ('master:material:submit','master:material:approve');
