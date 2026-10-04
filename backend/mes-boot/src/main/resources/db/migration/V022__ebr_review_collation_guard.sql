-- V018 is already executed and immutable. Compare regulated identifiers exactly across
-- the existing unicode_ci definition/signature tables and native runtime default collation.
DROP TRIGGER IF EXISTS ebr_review_insert;
DELIMITER $$
CREATE TRIGGER ebr_review_insert BEFORE INSERT ON ebr_review_record FOR EACH ROW
BEGIN
 IF NOT EXISTS(
  SELECT 1 FROM ebr_form_instance f
  JOIN ebr_form_def d ON d.id=f.form_def_id AND d.org_id=f.org_id
  JOIN ebr_review_rule r ON r.template_version_id=d.template_version_id AND r.org_id=f.org_id
  WHERE f.id=NEW.object_id AND f.org_id=NEW.org_id AND f.revision=NEW.form_revision
   AND r.id=NEW.review_rule_id
   AND BINARY r.review_type=BINARY NEW.review_type
   AND BINARY r.required_role=BINARY NEW.role_snapshot
 ) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR review frozen rule mismatch'; END IF;
 IF NOT EXISTS(
  SELECT 1 FROM gxp_signature
  WHERE id=NEW.signature_id AND org_id=NEW.org_id AND signer_id=NEW.reviewer_id
   AND BINARY object_type=BINARY 'EBR_REVIEW_RECORD'
   AND BINARY object_id=BINARY CONCAT(NEW.object_id,':',NEW.form_revision,':',NEW.review_rule_id)
   AND BINARY meaning=BINARY NEW.review_type
   AND BINARY status=BINARY 'VALID'
 ) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='eBR review signature mismatch'; END IF;
END$$
DELIMITER ;
