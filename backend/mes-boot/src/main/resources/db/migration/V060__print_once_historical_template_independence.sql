-- PRINT-ONCE-001, explicit human approval 2026-10-12.
-- Historical template numbers remain immutable provenance; no archive row is changed.
ALTER TABLE mes_print_artifact DROP FOREIGN KEY fk_print_artifact_version;
ALTER TABLE mes_print_batch DROP FOREIGN KEY fk_print_batch_template;

-- Preserve all other template/binding guards, and permit only the approved exact cleanup.
DROP TRIGGER bd_mes_print_template_version;
DROP TRIGGER bd_mes_print_binding;
DELIMITER $$
CREATE TRIGGER bd_mes_print_template_version BEFORE DELETE ON mes_print_template_version
FOR EACH ROW
BEGIN
 IF OLD.org_id <> 1 OR OLD.id NOT IN (10,30,31,32,33,40,41,42,43,44) OR OLD.status <> 'INACTIVE'
 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated print template deletion prohibited'; END IF;
END$$
CREATE TRIGGER bd_mes_print_binding BEFORE DELETE ON mes_print_binding
FOR EACH ROW
BEGIN
 IF OLD.org_id <> 1 OR OLD.id NOT IN (4,18,19,20,21,27,28,29,30,31)
 OR OLD.template_version_id NOT IN (10,30,31,32,33,40,41,42,43,44) OR OLD.enabled <> 0
 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Print binding deletion prohibited'; END IF;
END$$
DELIMITER ;
