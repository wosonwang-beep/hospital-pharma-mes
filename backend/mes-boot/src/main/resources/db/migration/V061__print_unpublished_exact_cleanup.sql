-- Sentinel_88767a63e37c81918a08f7c75b92cb38: exact six unpublished DEV templates.
-- No changes to update/content/signature/audit/evidence guards or any retained record.
DROP TRIGGER bd_mes_print_template_version;
DELIMITER $$
CREATE TRIGGER bd_mes_print_template_version BEFORE DELETE ON mes_print_template_version
FOR EACH ROW
BEGIN
 IF NOT (
  OLD.org_id = 1 AND (
   (OLD.id IN (10,30,31,32,33,40,41,42,43,44) AND OLD.status = 'INACTIVE')
   OR
   (OLD.id IN (7,8,9,13,14,29) AND OLD.status IN ('DRAFT','VALIDATED')
    AND OLD.published_at IS NULL AND OLD.template_revision = 1)
  )
 ) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated print template deletion prohibited'; END IF;
END$$
DELIMITER ;
