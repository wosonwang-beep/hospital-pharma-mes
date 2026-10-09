-- DCP-EBR-TEMPLATE-NAME-001. Preserve legacy names, hashes, signatures and frozen evidence.
ALTER TABLE ebr_template_version ADD COLUMN template_name VARCHAR(100) NULL AFTER template_code;
