-- DCP005 approved by main design 2026-10-10. Additive V052; preserve all historical migration checksums and archive triggers.
CREATE TABLE mes_print_data_source (
id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL,updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL,version_no BIGINT NOT NULL DEFAULT 0,
UNIQUE KEY uk_data_source_org_id(org_id,id),source_code VARCHAR(60) NOT NULL,source_name VARCHAR(120) NOT NULL,status VARCHAR(16) NOT NULL,
draft_revision_id BIGINT NULL,published_revision_id BIGINT NULL,deleted_at DATETIME(3) NULL,deleted_by BIGINT NULL,
UNIQUE KEY uk_source_code(org_id,source_code),CHECK(status IN ('ENABLED','DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mes_print_data_source_revision (
id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL,updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL,version_no BIGINT NOT NULL DEFAULT 0,
UNIQUE KEY uk_data_source_revision_org_id(org_id,id),source_id BIGINT NOT NULL,revision_no BIGINT NOT NULL,status VARCHAR(16) NOT NULL,schema_version INT NOT NULL,
main_catalog_id VARCHAR(80) NOT NULL,parameter_declarations_json LONGTEXT NOT NULL,config_json LONGTEXT NOT NULL,config_hash CHAR(64) NOT NULL,
published_business_types_json LONGTEXT NULL,type_metadata_snapshots_json LONGTEXT NULL,published_binding_set_hash CHAR(64) NULL,publication_evidence_hash CHAR(64) NULL,
published_catalog_version VARCHAR(80) NULL,published_by BIGINT NULL,published_at DATETIME(3) NULL,
UNIQUE KEY uk_source_revision_no(org_id,source_id,revision_no),UNIQUE KEY uk_source_revision_identity(org_id,source_id,id),
FOREIGN KEY(org_id,source_id) REFERENCES mes_print_data_source(org_id,id),
CHECK(JSON_VALID(parameter_declarations_json)),CHECK(JSON_VALID(config_json)),CHECK(published_business_types_json IS NULL OR JSON_VALID(published_business_types_json)),CHECK(type_metadata_snapshots_json IS NULL OR JSON_VALID(type_metadata_snapshots_json)),
CHECK(status IN ('DRAFT','PUBLISHED')),CHECK(schema_version=1),
CHECK((status='DRAFT' AND published_at IS NULL) OR (status='PUBLISHED' AND published_business_types_json IS NOT NULL AND type_metadata_snapshots_json IS NOT NULL AND published_binding_set_hash IS NOT NULL AND publication_evidence_hash IS NOT NULL AND published_catalog_version IS NOT NULL AND published_by IS NOT NULL AND published_at IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mes_print_document_type_metadata (
id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL,updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL,version_no BIGINT NOT NULL DEFAULT 0,
UNIQUE KEY uk_document_type_metadata_org_id(org_id,id),dict_item_id BIGINT NOT NULL,business_type VARCHAR(80) NOT NULL,authorization_policy_ref VARCHAR(80) NOT NULL,
compatible_main_catalog_ids_json LONGTEXT NOT NULL,fixed_type_filters_json LONGTEXT NOT NULL,metadata_version VARCHAR(80) NOT NULL,metadata_hash CHAR(64) NOT NULL,
UNIQUE KEY uk_print_type_metadata_code(org_id,business_type),UNIQUE KEY uk_print_type_metadata_dictionary(org_id,dict_item_id),
FOREIGN KEY(org_id,dict_item_id) REFERENCES sys_dict_item(org_id,id),CHECK(JSON_VALID(compatible_main_catalog_ids_json)),CHECK(JSON_VALID(fixed_type_filters_json))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mes_print_data_source_type_binding (
id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL,updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL,version_no BIGINT NOT NULL DEFAULT 0,
UNIQUE KEY uk_data_source_type_binding_org_id(org_id,id),business_type VARCHAR(80) NOT NULL,source_id BIGINT NOT NULL,
UNIQUE KEY uk_source_type_binding(org_id,business_type),FOREIGN KEY(org_id,source_id) REFERENCES mes_print_data_source(org_id,id),
FOREIGN KEY(org_id,business_type) REFERENCES mes_print_document_type_metadata(org_id,business_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mes_print_source_validation (
id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL,updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL,version_no BIGINT NOT NULL DEFAULT 0,
UNIQUE KEY uk_source_validation_org_id(org_id,id),source_revision_id BIGINT NOT NULL,source_version_no BIGINT NOT NULL,config_hash CHAR(64) NOT NULL,binding_set_hash CHAR(64) NOT NULL,publication_evidence_hash CHAR(64) NOT NULL,
catalog_version VARCHAR(80) NOT NULL,type_metadata_snapshots_json LONGTEXT NOT NULL,type_results_json LONGTEXT NOT NULL,result VARCHAR(8) NOT NULL,
validated_by BIGINT NOT NULL,validated_at DATETIME(3) NOT NULL,expires_at DATETIME(3) NOT NULL,invalidated_at DATETIME(3) NULL,
KEY ix_source_validation(org_id,source_revision_id,config_hash,expires_at),FOREIGN KEY(org_id,source_revision_id) REFERENCES mes_print_data_source_revision(org_id,id),
CHECK(JSON_VALID(type_metadata_snapshots_json)),CHECK(JSON_VALID(type_results_json)),CHECK(result IN ('PASS','FAIL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE mes_print_data_source
 ADD CONSTRAINT fk_source_draft_revision FOREIGN KEY(org_id,id,draft_revision_id) REFERENCES mes_print_data_source_revision(org_id,source_id,id),
 ADD CONSTRAINT fk_source_published_revision FOREIGN KEY(org_id,id,published_revision_id) REFERENCES mes_print_data_source_revision(org_id,source_id,id);
ALTER TABLE mes_print_template_version
 ADD source_execution_mode VARCHAR(24) NOT NULL DEFAULT 'LEGACY_PROVIDER', ADD source_id BIGINT NULL,ADD source_revision_id BIGINT NULL,ADD source_config_hash CHAR(64) NULL,
 ADD CONSTRAINT fk_template_source_revision FOREIGN KEY(org_id,source_id,source_revision_id) REFERENCES mes_print_data_source_revision(org_id,source_id,id),
 ADD CONSTRAINT ck_template_source_mode CHECK((source_execution_mode='LEGACY_PROVIDER' AND source_id IS NULL AND source_revision_id IS NULL AND source_config_hash IS NULL) OR (source_execution_mode='CONFIGURED' AND source_id IS NOT NULL AND source_revision_id IS NOT NULL AND source_config_hash IS NOT NULL));
ALTER TABLE mes_print_artifact ADD UNIQUE KEY uk_artifact_org_id(org_id,id);
ALTER TABLE mes_print_batch ADD UNIQUE KEY uk_batch_org_id(org_id,id);
CREATE TABLE mes_print_artifact_source_evidence (
id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,org_id BIGINT NOT NULL,artifact_id BIGINT NOT NULL,source_id BIGINT NOT NULL,source_revision_id BIGINT NOT NULL,
config_hash CHAR(64) NOT NULL,type_metadata_hash CHAR(64) NOT NULL,catalog_version VARCHAR(80) NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL,
UNIQUE KEY uk_artifact_evidence_org_id(org_id,id),UNIQUE KEY uk_artifact_evidence_owner(org_id,artifact_id),
FOREIGN KEY(org_id,artifact_id) REFERENCES mes_print_artifact(org_id,id),FOREIGN KEY(org_id,source_id,source_revision_id) REFERENCES mes_print_data_source_revision(org_id,source_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE mes_print_batch_source_evidence (
id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,org_id BIGINT NOT NULL,batch_id BIGINT NOT NULL,source_id BIGINT NOT NULL,source_revision_id BIGINT NOT NULL,
config_hash CHAR(64) NOT NULL,type_metadata_hash CHAR(64) NOT NULL,catalog_version VARCHAR(80) NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL,
UNIQUE KEY uk_batch_evidence_org_id(org_id,id),UNIQUE KEY uk_batch_evidence_owner(org_id,batch_id),
FOREIGN KEY(org_id,batch_id) REFERENCES mes_print_batch(org_id,id),FOREIGN KEY(org_id,source_id,source_revision_id) REFERENCES mes_print_data_source_revision(org_id,source_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DELIMITER $$
CREATE TRIGGER bd_mes_print_data_source BEFORE DELETE ON mes_print_data_source FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Print configuration/evidence deletion prohibited'$$
CREATE TRIGGER bd_mes_print_data_source_revision BEFORE DELETE ON mes_print_data_source_revision FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Print configuration/evidence deletion prohibited'$$
CREATE TRIGGER bd_mes_print_source_validation BEFORE DELETE ON mes_print_source_validation FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Print configuration/evidence deletion prohibited'$$
CREATE TRIGGER bd_mes_print_artifact_source_evidence BEFORE DELETE ON mes_print_artifact_source_evidence FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Print configuration/evidence deletion prohibited'$$
CREATE TRIGGER bd_mes_print_batch_source_evidence BEFORE DELETE ON mes_print_batch_source_evidence FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Print configuration/evidence deletion prohibited'$$
CREATE TRIGGER bu_mes_print_artifact_source_evidence BEFORE UPDATE ON mes_print_artifact_source_evidence FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable source evidence'$$
CREATE TRIGGER bu_mes_print_batch_source_evidence BEFORE UPDATE ON mes_print_batch_source_evidence FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable source evidence'$$
CREATE TRIGGER bu_mes_print_data_source_revision BEFORE UPDATE ON mes_print_data_source_revision FOR EACH ROW
BEGIN IF OLD.status='PUBLISHED' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Published source revision immutable'; END IF;
 IF NOT(OLD.org_id <=> NEW.org_id) OR NOT(OLD.source_id <=> NEW.source_id) OR NOT(OLD.revision_no <=> NEW.revision_no) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Source revision identity immutable'; END IF; END$$
CREATE TRIGGER bu_mes_print_data_source BEFORE UPDATE ON mes_print_data_source FOR EACH ROW
BEGIN IF NOT(OLD.org_id <=> NEW.org_id) OR NOT(OLD.source_code <=> NEW.source_code) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Source identity immutable'; END IF; END$$
CREATE TRIGGER bu_print_template_source_reference BEFORE UPDATE ON mes_print_template_version FOR EACH ROW
BEGIN IF NOT(OLD.source_execution_mode <=> NEW.source_execution_mode) OR NOT(OLD.source_id <=> NEW.source_id) OR NOT(OLD.source_revision_id <=> NEW.source_revision_id) OR NOT(OLD.source_config_hash <=> NEW.source_config_hash) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Template source reference immutable'; END IF; END$$
DELIMITER ;
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled)
SELECT p.code,p.kind,p.label,p.route,TRUE FROM (
SELECT 'print:source:view' code,'MENU' kind,'打印数据源配置' label,'/admin/print-data-sources' route
UNION ALL SELECT 'print:source:manage','ACTION','维护打印数据源',NULL
UNION ALL SELECT 'print:source:publish','ACTION','发布/停用/启用打印数据源',NULL
) p WHERE NOT EXISTS(SELECT 1 FROM sys_permission existing WHERE existing.permission_code=p.code);
-- Existing SYSTEM_ADMIN full-permission governance only; no ordinary role grants.
INSERT INTO sys_role_permission(role_id,permission_id)
SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON p.permission_code LIKE 'print:source:%'
WHERE r.role_code='SYSTEM_ADMIN' AND r.enabled=TRUE AND NOT EXISTS(SELECT 1 FROM sys_role_permission old WHERE old.role_id=r.id AND old.permission_id=p.id);
INSERT INTO sys_dict_type(org_id,dict_code,dict_name,dict_kind,structure_type,description,created_by,updated_by)
SELECT 1,'PRINT_BUSINESS_DOCUMENT','打印业务单据','BUSINESS','TREE','打印单据类型身份；类型元数据独立管理',1,1
WHERE NOT EXISTS(SELECT 1 FROM sys_dict_type WHERE org_id=1 AND dict_code='PRINT_BUSINESS_DOCUMENT');
INSERT INTO sys_dict_item(org_id,dict_type_id,item_code,item_label,created_by,updated_by) SELECT 1,t.id,'WMS','WMS管理',1,1 FROM sys_dict_type t WHERE t.org_id=1 AND t.dict_code='PRINT_BUSINESS_DOCUMENT' AND NOT EXISTS(SELECT 1 FROM sys_dict_item i WHERE i.org_id=1 AND i.dict_type_id=t.id AND i.item_code='WMS');
INSERT INTO sys_dict_item(org_id,dict_type_id,item_code,item_label,created_by,updated_by) SELECT 1,t.id,'QUALITY','质量管理',1,1 FROM sys_dict_type t WHERE t.org_id=1 AND t.dict_code='PRINT_BUSINESS_DOCUMENT' AND NOT EXISTS(SELECT 1 FROM sys_dict_item i WHERE i.org_id=1 AND i.dict_type_id=t.id AND i.item_code='QUALITY');
INSERT INTO sys_dict_item(org_id,dict_type_id,item_code,item_label,created_by,updated_by) SELECT 1,t.id,'PRODUCTION','生产管理',1,1 FROM sys_dict_type t WHERE t.org_id=1 AND t.dict_code='PRINT_BUSINESS_DOCUMENT' AND NOT EXISTS(SELECT 1 FROM sys_dict_item i WHERE i.org_id=1 AND i.dict_type_id=t.id AND i.item_code='PRODUCTION');
INSERT INTO sys_dict_item(org_id,dict_type_id,parent_id,item_code,item_label,created_by,updated_by) SELECT 1,t.id,p.id,'WMS_RECEIPT','收货单',1,1 FROM sys_dict_type t JOIN sys_dict_item p ON p.org_id=t.org_id AND p.dict_type_id=t.id AND p.item_code='WMS' WHERE t.org_id=1 AND t.dict_code='PRINT_BUSINESS_DOCUMENT' AND NOT EXISTS(SELECT 1 FROM sys_dict_item i WHERE i.org_id=1 AND i.dict_type_id=t.id AND i.item_code='WMS_RECEIPT');
INSERT INTO sys_dict_item(org_id,dict_type_id,parent_id,item_code,item_label,created_by,updated_by) SELECT 1,t.id,p.id,'INSPECTION_REPORT','检验报告',1,1 FROM sys_dict_type t JOIN sys_dict_item p ON p.org_id=t.org_id AND p.dict_type_id=t.id AND p.item_code='QUALITY' WHERE t.org_id=1 AND t.dict_code='PRINT_BUSINESS_DOCUMENT' AND NOT EXISTS(SELECT 1 FROM sys_dict_item i WHERE i.org_id=1 AND i.dict_type_id=t.id AND i.item_code='INSPECTION_REPORT');
INSERT INTO sys_dict_item(org_id,dict_type_id,parent_id,item_code,item_label,created_by,updated_by) SELECT 1,t.id,p.id,'EBR_PROCESS_FORM','eBR过程表单',1,1 FROM sys_dict_type t JOIN sys_dict_item p ON p.org_id=t.org_id AND p.dict_type_id=t.id AND p.item_code='PRODUCTION' WHERE t.org_id=1 AND t.dict_code='PRINT_BUSINESS_DOCUMENT' AND NOT EXISTS(SELECT 1 FROM sys_dict_item i WHERE i.org_id=1 AND i.dict_type_id=t.id AND i.item_code='EBR_PROCESS_FORM');

-- Same-level system navigation for the approved source page; preserve all existing menu grants.
INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions)
SELECT 1,m.id,'nav_print-data-sources','打印数据源配置','/admin/print-data-sources',69,'ACTIVE',1,1,'print:source:view',NULL FROM sys_menu m
WHERE m.org_id=1 AND m.menu_code='nav_system' AND NOT EXISTS(SELECT 1 FROM sys_menu old WHERE old.org_id=1 AND old.menu_code='nav_print-data-sources');
INSERT INTO sys_role_menu(org_id,role_id,menu_id,created_by)
SELECT m.org_id,r.id,m.id,1 FROM sys_role r JOIN sys_menu m ON m.org_id=1 AND m.menu_code='nav_print-data-sources'
WHERE r.role_code='SYSTEM_ADMIN' AND r.enabled=TRUE AND NOT EXISTS(SELECT 1 FROM sys_role_menu old WHERE old.org_id=m.org_id AND old.role_id=r.id AND old.menu_id=m.id);
INSERT INTO sys_menu_permission(menu_id,permission_id,created_by)
SELECT m.id,p.id,1 FROM sys_menu m JOIN sys_permission p ON p.permission_code LIKE 'print:source:%'
WHERE m.org_id=1 AND m.menu_code='nav_print-data-sources' AND NOT EXISTS(SELECT 1 FROM sys_menu_permission old WHERE old.menu_id=m.id AND old.permission_id=p.id);
