-- V034: approved bounded printing expansion after initial V033 was applied.
-- Preserve V033, whose checksum 1576484630 matches the deployed 3-table schema.
-- New editor session, append-only triggers, and new document generation permission.
CREATE TABLE mes_print_editor_session (
 id CHAR(36) NOT NULL PRIMARY KEY,org_id BIGINT NOT NULL,actor_id BIGINT NOT NULL,login_session_id VARCHAR(120) NOT NULL,
 source_template_version_id BIGINT NOT NULL,saved_template_version_id BIGINT NULL,last_content_hash CHAR(64) NULL,expires_at DATETIME(3) NOT NULL,version_no BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT fk_print_editor_source FOREIGN KEY(org_id,source_template_version_id) REFERENCES mes_print_template_version(org_id,id),
 CONSTRAINT fk_print_editor_saved FOREIGN KEY(org_id,saved_template_version_id) REFERENCES mes_print_template_version(org_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled)
SELECT 'print:document:generate','ACTION','业务PDF生成与归档读取',NULL,TRUE
WHERE NOT EXISTS(SELECT 1 FROM sys_permission p WHERE p.permission_code='print:document:generate');

-- SYSTEM_ADMIN retains the previously authorized full-permission role model.
-- Do not grant new controlled printing privileges to ordinary business roles.
INSERT INTO sys_role_permission(role_id,permission_id)
SELECT r.id,p.id FROM sys_role r
JOIN sys_permission p ON p.enabled=TRUE
WHERE r.role_code='SYSTEM_ADMIN' AND r.enabled=TRUE
AND NOT EXISTS(SELECT 1 FROM sys_role_permission existing
 WHERE existing.role_id=r.id AND existing.permission_id=p.id);

DELIMITER $$
CREATE TRIGGER bd_mes_print_template_version BEFORE DELETE ON mes_print_template_version FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Regulated print template deletion prohibited'$$
CREATE TRIGGER bd_mes_print_binding BEFORE DELETE ON mes_print_binding FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Print binding deletion prohibited'$$
CREATE TRIGGER bd_mes_print_artifact BEFORE DELETE ON mes_print_artifact FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Print artifact deletion prohibited'$$
CREATE TRIGGER bu_mes_print_artifact BEFORE UPDATE ON mes_print_artifact FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable print artifact'$$
CREATE TRIGGER bu_mes_print_template_version BEFORE UPDATE ON mes_print_template_version FOR EACH ROW
BEGIN
 IF NOT(OLD.created_by <=> NEW.created_by) OR NOT(OLD.created_at <=> NEW.created_at) OR NOT(OLD.org_id <=> NEW.org_id) OR NOT(OLD.template_code <=> NEW.template_code) OR NOT(OLD.template_name <=> NEW.template_name) OR NOT(OLD.template_revision <=> NEW.template_revision) OR NOT(OLD.business_type <=> NEW.business_type) OR NOT(OLD.docx <=> NEW.docx) OR NOT(OLD.content_hash <=> NEW.content_hash) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable print template content'; END IF;
 IF OLD.status IN ('PUBLISHED','INACTIVE') AND (NOT(OLD.preview_pdf <=> NEW.preview_pdf) OR NOT(OLD.preview_hash <=> NEW.preview_hash) OR NOT(OLD.published_at <=> NEW.published_at) OR (OLD.status='INACTIVE' AND NEW.status<>'INACTIVE') OR (OLD.status='PUBLISHED' AND NEW.status NOT IN ('PUBLISHED','INACTIVE'))) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Published print template immutable'; END IF;
END$$
CREATE TRIGGER bu_mes_print_binding BEFORE UPDATE ON mes_print_binding FOR EACH ROW
BEGIN
 IF NOT(OLD.org_id <=> NEW.org_id) OR NOT(OLD.business_type <=> NEW.business_type) OR NOT(OLD.template_version_id <=> NEW.template_version_id) OR NOT(OLD.created_by <=> NEW.created_by) OR NOT(OLD.created_at <=> NEW.created_at) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Print binding identity immutable'; END IF;
END$$
DELIMITER ;
