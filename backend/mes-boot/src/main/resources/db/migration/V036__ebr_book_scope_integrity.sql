-- DCP-EBR-BOOK-001 additive integrity check after successful V035; never rewrites V035.
CREATE TRIGGER bi_shared_ebr_scope BEFORE INSERT ON ebr_form_instance FOR EACH ROW
BEGIN
 IF NEW.shared_main_batch_id IS NOT NULL AND NOT EXISTS(
 SELECT 1 FROM mes_operation_execution o JOIN prd_execution_unit e ON e.id=o.execution_unit_id AND e.org_id=o.org_id
 WHERE o.id=NEW.operation_execution_id AND o.org_id=NEW.org_id AND e.main_batch_id=NEW.shared_main_batch_id)
 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Shared form must belong to actual batch operation'; END IF;
END;
CREATE TRIGGER bu_shared_ebr_scope BEFORE UPDATE ON ebr_form_instance FOR EACH ROW
BEGIN
 IF NOT(OLD.shared_main_batch_id<=>NEW.shared_main_batch_id)
 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Frozen shared form scope immutable'; END IF;
END;
CREATE TRIGGER bi_ebr_book_pdf_scope BEFORE INSERT ON ebr_book_pdf FOR EACH ROW
BEGIN
 IF NOT EXISTS(SELECT 1 FROM prd_main_batch b WHERE b.id=NEW.main_batch_id AND b.org_id=NEW.org_id)
 OR NOT EXISTS(SELECT 1 FROM ebr_book_template t WHERE t.id=NEW.template_version_id AND t.org_id=NEW.org_id)
 OR (NEW.legacy_manifest_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM ebr_pdf_manifest m WHERE m.id=NEW.legacy_manifest_id AND m.org_id=NEW.org_id AND m.main_batch_id=NEW.main_batch_id))
 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Book archive source organization or batch mismatch'; END IF;
END;
