-- Sentinel_4295715d8e908191bcd4792a81b09c04: final exact historical68/disabled49 cleanup.
-- Required because existing delete guards exclude these IDs. All other guards remain unchanged.
DROP TRIGGER bd_mes_print_template_version;
DROP TRIGGER bd_mes_print_binding;
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
   OR
   (OLD.id = 68 AND OLD.status = 'INACTIVE' AND OLD.template_revision = 1
    AND OLD.template_code = 'QA20261010ReceiptPdf'
    AND OLD.content_hash = '9d1424d658e45ab48c128bca8bf5c3233672ba98c67c3043214a8ce6cc5be3a2')
  )
 ) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated print template deletion prohibited'; END IF;
END$$
CREATE TRIGGER bd_mes_print_binding BEFORE DELETE ON mes_print_binding
FOR EACH ROW
BEGIN
 IF NOT (
  OLD.org_id = 1 AND OLD.enabled = 0 AND (
   (OLD.id IN (4,18,19,20,21,27,28,29,30,31)
    AND OLD.template_version_id IN (10,30,31,32,33,40,41,42,43,44))
   OR
   (OLD.id = 49 AND OLD.template_version_id = 68 AND OLD.business_type = 'WMS_RECEIPT')
  )
 ) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Print binding deletion prohibited'; END IF;
END$$
DELIMITER ;
