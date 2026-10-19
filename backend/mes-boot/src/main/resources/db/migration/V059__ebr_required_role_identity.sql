-- ROLE-ID-SIGNING-001: additive schema only; no legacy backfill or regulated history rewrite.
ALTER TABLE ebr_template_version
    ADD COLUMN definition_schema_version SMALLINT NOT NULL DEFAULT 1,
    ADD CONSTRAINT ck_ebr_template_definition_schema CHECK (definition_schema_version IN (1, 2));

ALTER TABLE ebr_signature_rule
    ADD COLUMN required_role_id BIGINT NULL,
    ADD COLUMN required_role_name_snapshot VARCHAR(128) NULL,
    ADD INDEX ix_ebr_signature_required_role (required_role_id),
    ADD CONSTRAINT fk_ebr_signature_required_role FOREIGN KEY (required_role_id) REFERENCES sys_role(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    ADD CONSTRAINT ck_ebr_signature_role_snapshot CHECK (
        (required_role_id IS NULL AND required_role_name_snapshot IS NULL) OR
        (required_role_id IS NOT NULL AND required_role_name_snapshot IS NOT NULL AND CHAR_LENGTH(TRIM(required_role_name_snapshot)) > 0));

ALTER TABLE ebr_review_rule
    ADD COLUMN required_role_id BIGINT NULL,
    ADD COLUMN required_role_name_snapshot VARCHAR(128) NULL,
    ADD INDEX ix_ebr_review_required_role (required_role_id),
    ADD CONSTRAINT fk_ebr_review_required_role FOREIGN KEY (required_role_id) REFERENCES sys_role(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    ADD CONSTRAINT ck_ebr_review_role_snapshot CHECK (
        (required_role_id IS NULL AND required_role_name_snapshot IS NULL) OR
        (required_role_id IS NOT NULL AND required_role_name_snapshot IS NOT NULL AND CHAR_LENGTH(TRIM(required_role_name_snapshot)) > 0));
