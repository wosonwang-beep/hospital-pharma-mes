-- Append-only correction for the approved MATERIAL/PRODUCT subject split.
-- V056 remains byte-identical after isolated application. No regulated rows are rewritten.
-- Preserve the historical MATERIAL organization condition and error; add the equivalent PRODUCT guard.
DROP TRIGGER qc_specification_org_insert;
DELIMITER $$
CREATE TRIGGER qc_specification_org_insert BEFORE INSERT ON qc_specification FOR EACH ROW
BEGIN
 IF NEW.subject_type='MATERIAL' THEN
  IF NOT EXISTS(SELECT 1 FROM md_material WHERE id=NEW.material_id AND org_id=NEW.org_id) THEN
   SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC material organization mismatch';
  END IF;
 ELSEIF NEW.subject_type='PRODUCT' THEN
  IF NOT EXISTS(SELECT 1 FROM md_product WHERE id=NEW.product_id AND org_id=NEW.org_id) THEN
   SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='QC product organization mismatch';
  END IF;
 ELSE
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Unsupported QC specification subject';
 END IF;
END$$
DELIMITER ;
