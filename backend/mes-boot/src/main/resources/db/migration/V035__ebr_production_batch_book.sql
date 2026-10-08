-- DCP-EBR-BOOK-001 explicitly approved 2026-10-08; actual DEV highest success V034.
-- No edits to executed history. No source business data, role memberships or signatures changed.
CREATE TABLE ebr_book_template (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
 template_code VARCHAR(64) NOT NULL, revision_no INT NOT NULL,
 status VARCHAR(20) NOT NULL CHECK(status IN ('DRAFT','PUBLISHED','INACTIVE')),
 definition_json LONGTEXT NOT NULL CHECK(JSON_VALID(definition_json)), definition_hash CHAR(64) NOT NULL,
 created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 published_by BIGINT NULL, published_at DATETIME(3) NULL, version_no BIGINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_book_template_revision(org_id,template_code,revision_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE ebr_book_pdf (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,org_id BIGINT NOT NULL,main_batch_id BIGINT NOT NULL,
 template_version_id BIGINT NOT NULL, template_hash CHAR(64) NOT NULL,source_hash CHAR(64) NOT NULL,
 manifest_json LONGTEXT NOT NULL CHECK(JSON_VALID(manifest_json)),pdf LONGBLOB NOT NULL,pdf_hash CHAR(64) NOT NULL,
 archive_kind VARCHAR(20) NOT NULL CHECK(archive_kind IN ('REVIEW_COPY','FINAL')),
 legacy_manifest_id BIGINT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 CONSTRAINT fk_book_pdf_batch FOREIGN KEY(main_batch_id) REFERENCES prd_main_batch(id),
 CONSTRAINT fk_book_pdf_template FOREIGN KEY(template_version_id) REFERENCES ebr_book_template(id),
 CONSTRAINT fk_book_pdf_evidence FOREIGN KEY(legacy_manifest_id) REFERENCES ebr_pdf_manifest(id),
 UNIQUE KEY uk_book_pdf_source(org_id,main_batch_id,template_version_id,archive_kind,source_hash),
 CHECK(archive_kind='REVIEW_COPY' OR legacy_manifest_id IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
ALTER TABLE ebr_form_instance ADD COLUMN shared_main_batch_id BIGINT NULL,
 ADD CONSTRAINT fk_shared_ebr_batch FOREIGN KEY(shared_main_batch_id) REFERENCES prd_main_batch(id),
 ADD UNIQUE KEY uk_batch_shared_form(org_id,shared_main_batch_id,form_def_id);
CREATE TRIGGER bu_ebr_book_template BEFORE UPDATE ON ebr_book_template FOR EACH ROW
BEGIN
 IF NOT(OLD.org_id<=>NEW.org_id) OR NOT(OLD.template_code<=>NEW.template_code) OR NOT(OLD.revision_no<=>NEW.revision_no)
 OR NOT(OLD.definition_json<=>NEW.definition_json) OR NOT(OLD.definition_hash<=>NEW.definition_hash) OR NOT(OLD.created_by<=>NEW.created_by)
 OR NOT(OLD.created_at<=>NEW.created_at) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable book template content'; END IF;
 IF NOT((OLD.status='DRAFT' AND NEW.status='PUBLISHED') OR (OLD.status='PUBLISHED' AND NEW.status='INACTIVE'))
 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Illegal book template transition'; END IF;
END;
CREATE TRIGGER bd_ebr_book_template BEFORE DELETE ON ebr_book_template FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Retained book template';
CREATE TRIGGER bu_ebr_book_pdf BEFORE UPDATE ON ebr_book_pdf FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable book PDF';
CREATE TRIGGER bd_ebr_book_pdf BEFORE DELETE ON ebr_book_pdf FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Retained book PDF';
