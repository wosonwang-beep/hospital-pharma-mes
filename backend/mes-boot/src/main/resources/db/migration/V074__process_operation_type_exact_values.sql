-- Approved DCP-PROCESS-OPERATION-TYPE-001: exact enum values under case-insensitive database collations.
ALTER TABLE proc_operation_def
  DROP CONSTRAINT chk_proc_operation_type,
  ADD CONSTRAINT chk_proc_operation_type
    CHECK (operation_type IS NULL OR BINARY operation_type IN ('PRODUCTION', 'IPC'));
