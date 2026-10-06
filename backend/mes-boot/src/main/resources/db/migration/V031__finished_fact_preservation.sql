-- Additive preservation guards; executed V030 remains byte-identical.
ALTER TABLE qms_finished_sampling_record ADD CONSTRAINT ck_finished_sampling_signed CHECK(signature_id IS NOT NULL AND signature_evidence_json IS NOT NULL);
ALTER TABLE qms_finished_report_review ADD CONSTRAINT ck_finished_report_review_signed CHECK(signature_id IS NOT NULL AND signature_evidence_json IS NOT NULL);
ALTER TABLE qms_finished_inspection_report ADD CONSTRAINT ck_finished_report_approved CHECK(status<>'APPROVED' OR (approved_by IS NOT NULL AND approved_at IS NOT NULL AND signature_id IS NOT NULL AND signature_evidence_json IS NOT NULL));
DELIMITER $$
CREATE TRIGGER freeze_finished_receipt_confirmation BEFORE UPDATE ON wms_finished_inbound_request FOR EACH ROW BEGIN IF OLD.status='CONFIRMED' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Confirmed receipt cannot be overwritten'; END IF; END$$
CREATE TRIGGER freeze_finished_report_approval BEFORE UPDATE ON qms_finished_inspection_report FOR EACH ROW BEGIN IF OLD.status='APPROVED' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Approved report cannot be overwritten'; END IF; END$$
CREATE TRIGGER preserve_finished_shipment BEFORE UPDATE ON wms_finished_shipment FOR EACH ROW BEGIN IF OLD.status<>'DRAFT' OR NOT(OLD.shipment_no<=>NEW.shipment_no) OR NOT(OLD.main_batch_id<=>NEW.main_batch_id) OR NOT(OLD.material_lot_id<=>NEW.material_lot_id) OR NOT(OLD.location_id<=>NEW.location_id) OR NOT(OLD.quantity<=>NEW.quantity) OR NOT(OLD.unit_id<=>NEW.unit_id) OR NOT(OLD.receiving_party<=>NEW.receiving_party) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Finished shipment source is immutable'; END IF; END$$
DELIMITER ;
