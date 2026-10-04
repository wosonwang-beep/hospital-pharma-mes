-- Approved incoming attachment facts; no existing migration is modified.
CREATE TABLE gxp_attachment (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 uploaded_by BIGINT NOT NULL,
 uploaded_at DATETIME(3) NOT NULL,
 file_name VARCHAR(255) NOT NULL,
 media_type VARCHAR(127) NOT NULL,
 byte_length BIGINT NOT NULL CHECK(byte_length BETWEEN 1 AND 10485760),
 sha256 CHAR(64) NOT NULL,
 content MEDIUMBLOB NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 1 CHECK(record_version=1),
 retention_status VARCHAR(20) NOT NULL CHECK(retention_status='RETAINED'),
 CONSTRAINT fk_attachment_uploader FOREIGN KEY(uploaded_by) REFERENCES sys_user(id),
 INDEX ix_attachment_org(org_id,id),
 CHECK(OCTET_LENGTH(content)=byte_length)
) ENGINE=InnoDB;
CREATE TABLE wms_receipt_attachment (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,
 receipt_id BIGINT NOT NULL,
 attachment_id BIGINT NOT NULL,
 purpose VARCHAR(20) NOT NULL CHECK(purpose IN ('COA','DELIVERY_DOCUMENT','OTHER')),
 reason VARCHAR(1000) NOT NULL,
 linked_by BIGINT NOT NULL,
 linked_at DATETIME(3) NOT NULL,
 record_version BIGINT NOT NULL DEFAULT 1 CHECK(record_version=1),
 CONSTRAINT fk_receipt_attachment_receipt FOREIGN KEY(receipt_id) REFERENCES wms_material_receipt(id),
 CONSTRAINT fk_receipt_attachment_file FOREIGN KEY(attachment_id) REFERENCES gxp_attachment(id),
 CONSTRAINT fk_receipt_attachment_actor FOREIGN KEY(linked_by) REFERENCES sys_user(id),
 UNIQUE KEY uk_receipt_attachment(org_id,receipt_id,attachment_id)
) ENGINE=InnoDB;
DELIMITER $$
CREATE TRIGGER gxp_attachment_no_update BEFORE UPDATE ON gxp_attachment FOR EACH ROW
BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Original attachment evidence is immutable'; END$$
CREATE TRIGGER gxp_attachment_no_delete BEFORE DELETE ON gxp_attachment FOR EACH ROW
BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Original attachment evidence cannot be deleted'; END$$
CREATE TRIGGER receipt_attachment_no_update BEFORE UPDATE ON wms_receipt_attachment FOR EACH ROW
BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Receipt attachment evidence is immutable'; END$$
CREATE TRIGGER receipt_attachment_no_delete BEFORE DELETE ON wms_receipt_attachment FOR EACH ROW
BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Receipt attachment evidence cannot be deleted'; END$$
DELIMITER ;
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled)
SELECT 'attachment:upload','ACTION','上传随货资料',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='attachment:upload');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled)
SELECT 'attachment:view','ACTION','查看随货资料',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='attachment:view');
