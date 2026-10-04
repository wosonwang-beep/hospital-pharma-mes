-- LG-007A only. Runtime LG-007B remains owned by MES-009. No historical migration is altered.
CREATE TABLE ebr_template_version (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 package_version_id BIGINT NOT NULL,template_code VARCHAR(64) NOT NULL,version INT NOT NULL,
 status VARCHAR(20) NOT NULL CHECK(status IN ('DRAFT','SUBMITTED','APPROVED','EFFECTIVE','WITHDRAWN')),
 content_hash VARCHAR(128) NULL,effective_from DATETIME(3) NULL,approved_by BIGINT NULL,approved_at DATETIME(3) NULL,
 UNIQUE KEY uk_ebr_template_version(org_id,template_code,version),
 draft_template_code VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN status='DRAFT' THEN template_code ELSE NULL END) PERSISTENT,
 UNIQUE KEY uk_ebr_template_draft(org_id,draft_template_code),KEY idx_ebr_template_state(org_id,status),
 FOREIGN KEY(package_version_id) REFERENCES proc_package_version(id),FOREIGN KEY(approved_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ebr_section_def (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 template_version_id BIGINT NOT NULL,section_code VARCHAR(64) NOT NULL,title VARCHAR(200) NOT NULL,
 sequence_no INT NOT NULL CHECK(sequence_no>=0),repeat_mode VARCHAR(20) NOT NULL CHECK(repeat_mode IN ('NONE','LIST')),
 visibility_rule_id BIGINT NULL,page_break_flag BOOLEAN NOT NULL,
 UNIQUE KEY uk_ebr_section(org_id,template_version_id,section_code),FOREIGN KEY(template_version_id) REFERENCES ebr_template_version(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ebr_group_def (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 section_def_id BIGINT NOT NULL,group_code VARCHAR(64) NOT NULL,title VARCHAR(200) NULL,
 sequence_no INT NOT NULL CHECK(sequence_no>=0),layout_columns INT NOT NULL CHECK(layout_columns BETWEEN 1 AND 12),
 repeat_mode VARCHAR(20) NOT NULL CHECK(repeat_mode IN ('NONE','LIST')),min_occurs INT NULL,max_occurs INT NULL,
 CHECK(min_occurs IS NULL OR min_occurs>=0),CHECK(max_occurs IS NULL OR max_occurs>=0),CHECK(min_occurs IS NULL OR max_occurs IS NULL OR min_occurs<=max_occurs),
 UNIQUE KEY uk_ebr_group(org_id,section_def_id,group_code),FOREIGN KEY(section_def_id) REFERENCES ebr_section_def(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ebr_form_def (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 template_version_id BIGINT NOT NULL,operation_def_id BIGINT NULL,form_code VARCHAR(64) NOT NULL,form_name VARCHAR(200) NOT NULL,
 schema_version VARCHAR(20) NOT NULL,schema_json LONGTEXT NOT NULL CHECK(JSON_VALID(schema_json)),sequence_no INT NOT NULL CHECK(sequence_no>=0),
 status VARCHAR(20) NOT NULL CHECK(status IN ('DRAFT','SUBMITTED','APPROVED','EFFECTIVE','WITHDRAWN')),
 UNIQUE KEY uk_ebr_form(org_id,template_version_id,form_code),FOREIGN KEY(template_version_id) REFERENCES ebr_template_version(id),FOREIGN KEY(operation_def_id) REFERENCES proc_operation_def(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ebr_field_def (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 form_def_id BIGINT NOT NULL,group_def_id BIGINT NULL,field_code VARCHAR(64) NOT NULL,label VARCHAR(200) NOT NULL,
 field_type VARCHAR(30) NOT NULL,source_type VARCHAR(20) NOT NULL CHECK(source_type IN ('MANUAL','BARCODE','INSTRUMENT','SYSTEM','DERIVED')),
 data_type VARCHAR(30) NOT NULL,unit_id BIGINT NULL,precision_scale INT NULL CHECK(precision_scale IS NULL OR precision_scale BETWEEN 0 AND 12),
 required_flag BOOLEAN NOT NULL,readonly_flag BOOLEAN NOT NULL,default_expr TEXT NULL,placeholder VARCHAR(500) NULL,help_text VARCHAR(1000) NULL,
 sequence_no INT NOT NULL CHECK(sequence_no>=0),validation_json LONGTEXT NULL CHECK(validation_json IS NULL OR JSON_VALID(validation_json)),
 UNIQUE KEY uk_ebr_field(org_id,form_def_id,field_code),FOREIGN KEY(form_def_id) REFERENCES ebr_form_def(id),FOREIGN KEY(group_def_id) REFERENCES ebr_group_def(id),FOREIGN KEY(unit_id) REFERENCES md_unit(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ebr_option_def (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 field_def_id BIGINT NOT NULL,option_code VARCHAR(64) NOT NULL,option_label VARCHAR(200) NOT NULL,option_value VARCHAR(500) NOT NULL,
 sequence_no INT NOT NULL CHECK(sequence_no>=0),active_flag BOOLEAN NOT NULL,
 UNIQUE KEY uk_ebr_option(org_id,field_def_id,option_code),FOREIGN KEY(field_def_id) REFERENCES ebr_field_def(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ebr_rule_def (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 template_version_id BIGINT NOT NULL,form_def_id BIGINT NULL,field_def_id BIGINT NULL,rule_code VARCHAR(64) NOT NULL,
 rule_type VARCHAR(30) NOT NULL CHECK(rule_type IN ('VALIDATION','CALCULATION','VISIBILITY','BRANCH','COMPLETION','SIGNATURE','REVIEW','DEVIATION')),
 trigger_point VARCHAR(30) NOT NULL CHECK(trigger_point IN ('ON_CHANGE','ON_SAVE','ON_SUBMIT','ON_OPERATION_COMPLETE','ON_BATCH_CLOSE')),
 expression TEXT NOT NULL,severity VARCHAR(20) NULL CHECK(severity IS NULL OR severity IN ('BLOCK','WARN')),
 error_code VARCHAR(64) NULL,message_template VARCHAR(1000) NULL,deviation_trigger BOOLEAN NOT NULL,active_flag BOOLEAN NOT NULL,
 UNIQUE KEY uk_ebr_rule(org_id,template_version_id,rule_code),KEY idx_ebr_rule_type(org_id,rule_type),
 FOREIGN KEY(template_version_id) REFERENCES ebr_template_version(id),FOREIGN KEY(form_def_id) REFERENCES ebr_form_def(id),FOREIGN KEY(field_def_id) REFERENCES ebr_field_def(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ebr_signature_rule (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 template_version_id BIGINT NOT NULL,object_scope VARCHAR(30) NOT NULL,object_code VARCHAR(64) NOT NULL,
 meaning VARCHAR(100) NOT NULL,required_role VARCHAR(64) NOT NULL,reauth_required BOOLEAN NOT NULL,
 sequence_no INT NOT NULL CHECK(sequence_no>=0),invalidate_on_change BOOLEAN NOT NULL,
 UNIQUE KEY uk_ebr_signature_rule(org_id,template_version_id,object_scope,object_code,meaning,sequence_no),FOREIGN KEY(template_version_id) REFERENCES ebr_template_version(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ebr_review_rule (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 org_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_by BIGINT NOT NULL,updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),version_no BIGINT NOT NULL DEFAULT 0,
 template_version_id BIGINT NOT NULL,object_scope VARCHAR(30) NOT NULL,object_code VARCHAR(64) NOT NULL,
 review_type VARCHAR(20) NOT NULL CHECK(review_type IN ('VERIFY','APPROVE')),required_role VARCHAR(64) NOT NULL,
 independent_user_required BOOLEAN NOT NULL,sequence_no INT NOT NULL CHECK(sequence_no>=0),
 UNIQUE KEY uk_ebr_review_rule(org_id,template_version_id,object_scope,object_code,review_type,sequence_no),FOREIGN KEY(template_version_id) REFERENCES ebr_template_version(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE ebr_section_def ADD CONSTRAINT fk_ebr_section_visibility FOREIGN KEY(visibility_rule_id) REFERENCES ebr_rule_def(id);
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'ebr:template:view','ACTION','ebr:template:view',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='ebr:template:view');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'ebr:template:create','ACTION','ebr:template:create',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='ebr:template:create');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'ebr:template:update','ACTION','ebr:template:update',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='ebr:template:update');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'ebr:template:submit','ACTION','ebr:template:submit',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='ebr:template:submit');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'ebr:template:approve','ACTION','ebr:template:approve',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='ebr:template:approve');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'ebr:template:publish','ACTION','ebr:template:publish',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='ebr:template:publish');
INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT 'ebr:designer:edit','ACTION','ebr:designer:edit',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='ebr:designer:edit');
INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'ebr:template:view','eBR Templates','/ebr/templates',208,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='ebr:template:view');
INSERT INTO sys_role_permission(role_id,permission_id) SELECT r.id,p.id FROM sys_role r JOIN sys_permission p ON p.permission_code IN ('ebr:template:view','ebr:template:create','ebr:template:update','ebr:template:submit','ebr:template:approve','ebr:template:publish','ebr:designer:edit') WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_permission rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
INSERT INTO sys_role_menu(org_id,role_id,menu_id,created_by) SELECT m.org_id,r.id,m.id,1 FROM sys_role r JOIN sys_menu m ON m.menu_code='ebr:template:view' WHERE r.role_code='SYSTEM_ADMIN' AND NOT EXISTS(SELECT 1 FROM sys_role_menu rm WHERE rm.org_id=m.org_id AND rm.role_id=r.id AND rm.menu_id=m.id);
