-- Approved DCP-PROCESS-OPERATION-TYPE-001: no inferred classification or data backfill.
ALTER TABLE proc_operation_def
  ADD COLUMN operation_type VARCHAR(16) NULL,
  ADD CONSTRAINT chk_proc_operation_type
    CHECK (operation_type IS NULL OR operation_type IN ('PRODUCTION', 'IPC'));
