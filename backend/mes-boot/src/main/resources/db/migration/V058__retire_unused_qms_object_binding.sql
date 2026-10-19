-- 2026-10-11 user-authorized QC design simplification.
-- Retire the unused V055 polymorphic binding table; existing V016/V030 request,
-- sampling, test and report records and their physical references are unchanged.
-- Controlled preflight: qms_inspection_object_binding has 0 rows and 0 incoming FKs.
-- V055 remains in immutable Flyway history. No legacy signatures/snapshots edited.
DROP TABLE qms_inspection_object_binding;
