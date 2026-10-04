-- v1.0.12: retained return entitlement evidence, never fictitious inventory movement.
CREATE TABLE wms_issue_return_item (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
 issue_id BIGINT NOT NULL, issue_item_id BIGINT NOT NULL, returned_qty DECIMAL(18,6) NOT NULL CHECK(returned_qty>0), unit_id BIGINT NOT NULL,
 reason VARCHAR(1000) NOT NULL CHECK(CHAR_LENGTH(TRIM(reason))>0), returned_by BIGINT NOT NULL, returned_at DATETIME(3) NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 1 CHECK(record_version=1), KEY idx_issue_return(org_id,issue_id,issue_item_id),
 FOREIGN KEY(issue_id) REFERENCES wms_material_issue(id),FOREIGN KEY(issue_item_id) REFERENCES wms_material_issue_item(id),
 FOREIGN KEY(unit_id) REFERENCES md_unit(id),FOREIGN KEY(returned_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TRIGGER trg_issue_return_update BEFORE UPDATE ON wms_issue_return_item FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable issue return evidence';
CREATE TRIGGER trg_issue_return_delete BEFORE DELETE ON wms_issue_return_item FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable issue return evidence';
