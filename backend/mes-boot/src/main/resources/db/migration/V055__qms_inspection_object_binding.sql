-- New polymorphic request object binding. Historical V016/V030 source records stay intact.
-- The polymorphic target has no physical FK; resolver enforces concrete typed FK + organization.
CREATE TABLE qms_inspection_object_binding (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 request_scope VARCHAR(20) NOT NULL,
 request_id BIGINT NOT NULL,
 inspection_object_type VARCHAR(40) NOT NULL,
 inspection_object_id BIGINT NOT NULL,
 snapshot_json LONGTEXT NULL,
 snapshot_sha256 CHAR(64) NULL,
 frozen_at DATETIME(3) NULL,
 frozen_by BIGINT NULL,
 created_by BIGINT NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 UNIQUE KEY uk_qms_object_request (org_id,request_scope,request_id),
 KEY ix_qms_object_identity (org_id,inspection_object_type,inspection_object_id),
 CONSTRAINT ck_qms_object_scope CHECK (request_scope IN ('INCOMING','FINISHED')),
 CONSTRAINT ck_qms_object_type CHECK (inspection_object_type IN
  ('MATERIAL','FINISHED_PRODUCT','INTERMEDIATE','ENVIRONMENT','PROCESS')),
 CONSTRAINT ck_qms_object_id CHECK (inspection_object_id > 0 AND request_id > 0),
 CONSTRAINT ck_qms_object_snapshot_valid CHECK (snapshot_json IS NULL OR JSON_VALID(snapshot_json)),
 CONSTRAINT ck_qms_object_snapshot_atomic CHECK (
  (snapshot_json IS NULL AND snapshot_sha256 IS NULL AND frozen_at IS NULL AND frozen_by IS NULL)
  OR
  (snapshot_json IS NOT NULL AND snapshot_sha256 IS NOT NULL AND frozen_at IS NOT NULL AND frozen_by IS NOT NULL)
 )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELIMITER $$
CREATE TRIGGER preserve_qms_inspection_object_binding
BEFORE UPDATE ON qms_inspection_object_binding
FOR EACH ROW
BEGIN
 IF OLD.frozen_at IS NOT NULL THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Frozen quality inspection object cannot be changed';
 END IF;
 IF NOT (OLD.org_id <=> NEW.org_id)
   OR NOT (OLD.request_scope <=> NEW.request_scope)
   OR NOT (OLD.request_id <=> NEW.request_id)
   OR NOT (OLD.inspection_object_type <=> NEW.inspection_object_type)
   OR NOT (OLD.inspection_object_id <=> NEW.inspection_object_id)
   OR NOT (OLD.created_by <=> NEW.created_by)
   OR NOT (OLD.created_at <=> NEW.created_at) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Inspection object identity cannot be changed';
 END IF;
END$$

CREATE TRIGGER prevent_qms_inspection_object_binding_delete
BEFORE DELETE ON qms_inspection_object_binding
FOR EACH ROW
 SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Quality inspection object evidence cannot be deleted'$$
DELIMITER ;
