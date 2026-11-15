-- DCP-QC-SPEC-BASIC-LIBRARY-001 follow-up.
-- A quality standard is a basic-data library entry: an authorised deletion removes the entry while the
-- maintenance trail survives as evidence. The revision rows therefore keep specification_id as a plain
-- historical identifier instead of a foreign key that would block the deletion, and each revision still
-- carries its own before/after definition snapshot plus the organisation scope it belongs to.

ALTER TABLE qc_specification_revision DROP FOREIGN KEY IF EXISTS fk_qc_revision_specification;
