-- V068 follow-up: permit exactly one controlled outbound-ledger link while preserving immutable sampling facts.
DROP TRIGGER bu_qms_sampling_detail;
DELIMITER $$
CREATE TRIGGER bu_qms_sampling_detail BEFORE UPDATE ON qms_sampling_detail FOR EACH ROW BEGIN
 IF NOT (OLD.sampling_task_id <=> NEW.sampling_task_id)
 OR NOT (OLD.detail_no <=> NEW.detail_no)
 OR NOT (OLD.container_no <=> NEW.container_no)
 OR NOT (OLD.sampling_point <=> NEW.sampling_point)
 OR NOT (OLD.sample_quantity <=> NEW.sample_quantity)
 OR NOT (OLD.unit_id <=> NEW.unit_id)
 OR NOT (OLD.sampled_at <=> NEW.sampled_at)
 OR NOT (OLD.sampled_by <=> NEW.sampled_by)
 OR NOT (OLD.package_resealed <=> NEW.package_resealed)
 OR NOT (OLD.source_location_id <=> NEW.source_location_id)
 OR NOT (OLD.source_container_id <=> NEW.source_container_id)
 OR OLD.sample_outbound_ledger_id IS NOT NULL
 OR NEW.sample_outbound_ledger_id IS NULL
 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable incoming evidence'; END IF;
END$$
DELIMITER ;
