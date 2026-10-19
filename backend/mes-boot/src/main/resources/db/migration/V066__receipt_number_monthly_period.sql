-- User-approved monthly receiving number. Existing configuration and history are retained.
ALTER TABLE wms_receipt_number_rule ADD COLUMN sequence_period CHAR(6) NULL COMMENT 'YYYYMM of the monthly sequence; legacy rules remain null';
