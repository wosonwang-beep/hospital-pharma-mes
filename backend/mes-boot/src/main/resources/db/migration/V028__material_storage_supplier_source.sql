-- Approved DCP-MATERIAL-STORAGE-SOURCE-001; existing storage_condition is reused.
-- NULL on historical sources means unknown; never infer or backfill from current masters.
ALTER TABLE md_material_supplier ADD COLUMN manufacturer_name VARCHAR(200) NULL;
ALTER TABLE wms_material_receipt_item
 ADD COLUMN source_snapshot_json LONGTEXT NULL,
 ADD CONSTRAINT chk_receipt_source_json CHECK (source_snapshot_json IS NULL OR JSON_VALID(source_snapshot_json));
