-- Approved bounded DOCX/PDF foundational printing change, 2026-10-08.
-- Append-only. No business data updates, role grants or historical migration edits.
CREATE TABLE mes_print_template_version (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
 template_code VARCHAR(60) NOT NULL, template_name VARCHAR(120) NOT NULL,
 template_revision BIGINT NOT NULL, business_type VARCHAR(60) NOT NULL,
 status VARCHAR(20) NOT NULL, content_hash CHAR(64) NOT NULL,
 docx MEDIUMBLOB NOT NULL, preview_pdf LONGBLOB NULL, preview_hash CHAR(64) NULL,
 published_at DATETIME(3) NULL,
 created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL,updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL,version_no BIGINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_print_version(org_id,template_code,template_revision),
 UNIQUE KEY uk_print_version_org(org_id,id),
 CONSTRAINT ck_print_template_status CHECK(status IN ('DRAFT','VALIDATED','PUBLISHED','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mes_print_binding (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,org_id BIGINT NOT NULL,business_type VARCHAR(60) NOT NULL,template_version_id BIGINT NOT NULL,enabled BOOLEAN NOT NULL,
 created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL,updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL,version_no BIGINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_print_binding(org_id,business_type,template_version_id),
 CONSTRAINT fk_print_binding_version FOREIGN KEY(org_id,template_version_id) REFERENCES mes_print_template_version(org_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE mes_print_artifact (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,org_id BIGINT NOT NULL,business_type VARCHAR(60) NOT NULL,business_id VARCHAR(80) NOT NULL,business_version VARCHAR(80) NOT NULL,report_no VARCHAR(120) NOT NULL,
 template_version_id BIGINT NOT NULL,template_revision BIGINT NOT NULL,formal BOOLEAN NOT NULL,
 snapshot_json LONGTEXT NOT NULL,snapshot_hash CHAR(64) NOT NULL,pdf_hash CHAR(64) NOT NULL,docx MEDIUMBLOB NOT NULL,pdf LONGBLOB NOT NULL,formal_key CHAR(64) NULL,
 created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL,updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL,version_no BIGINT NOT NULL DEFAULT 0,
 UNIQUE KEY uk_print_formal(org_id,formal_key),KEY ix_print_business(org_id,business_type,business_id,id),
 CONSTRAINT fk_print_artifact_version FOREIGN KEY(org_id,template_version_id) REFERENCES mes_print_template_version(org_id,id),
 CONSTRAINT ck_print_formal_key CHECK((formal=TRUE AND formal_key IS NOT NULL) OR (formal=FALSE AND formal_key IS NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled)
SELECT p.code,p.kind,p.name,p.route,TRUE FROM (
 SELECT 'print:template:view' code,'MENU' kind,'打印模板管理' name,'/admin/print-templates' route
 UNION ALL SELECT 'print:template:manage','ACTION','维护打印模板',NULL
 UNION ALL SELECT 'print:template:publish','ACTION','发布/停用/绑定打印模板',NULL
 UNION ALL SELECT 'print:generate','ACTION','业务PDF生成与归档读取',NULL
) p WHERE NOT EXISTS(SELECT 1 FROM sys_permission x WHERE x.permission_code=p.code);
