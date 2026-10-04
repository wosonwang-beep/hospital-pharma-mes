-- Complete the frozen IDX declarations without modifying executed V007/V008.
CREATE INDEX idx_material_name ON md_material (org_id, material_name);
CREATE INDEX idx_material_type ON md_material (org_id, material_type);
CREATE INDEX idx_material_status ON md_material (org_id, status);
CREATE INDEX idx_material_version_status ON md_material_version (org_id, status);
CREATE INDEX idx_supplier_qualification ON md_supplier (org_id, qualification_status);
CREATE INDEX idx_material_supplier_approved ON md_material_supplier (org_id, material_id, approved);
