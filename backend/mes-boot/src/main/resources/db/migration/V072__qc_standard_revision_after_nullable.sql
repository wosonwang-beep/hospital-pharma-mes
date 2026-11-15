-- DCP-QC-SPEC-BASIC-LIBRARY-001 follow-up.
-- A deletion revision has no resulting definition, mirroring a creation revision that has no
-- previous definition. Both halves of a revision snapshot must therefore accept NULL, while any
-- snapshot that is present must still be well-formed JSON.

ALTER TABLE qc_specification_revision DROP CONSTRAINT IF EXISTS after_json;
ALTER TABLE qc_specification_revision MODIFY after_json LONGTEXT NULL COMMENT 'Definition after the change; NULL for a deletion';
ALTER TABLE qc_specification_revision DROP CONSTRAINT IF EXISTS ck_qc_revision_after_json;
ALTER TABLE qc_specification_revision ADD CONSTRAINT ck_qc_revision_after_json CHECK (after_json IS NULL OR JSON_VALID(after_json));
