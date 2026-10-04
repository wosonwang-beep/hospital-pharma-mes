-- Enforce the existing v1.0.14 immutable ProcessSnapshot contract.
-- V017 has executed; no historical migration or snapshot content is rewritten.
DELIMITER $$
CREATE TRIGGER prd_snapshot_no_update BEFORE UPDATE ON prd_process_snapshot FOR EACH ROW
BEGIN
 SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Production process snapshot immutable';
END$$
CREATE TRIGGER prd_snapshot_no_delete BEFORE DELETE ON prd_process_snapshot FOR EACH ROW
BEGIN
 SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Production process snapshot retained';
END$$
DELIMITER ;
