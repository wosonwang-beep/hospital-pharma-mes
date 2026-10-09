-- User-authorized database menu tree. Existing role permission grants are not changed.

ALTER TABLE sys_menu ADD COLUMN permission_code VARCHAR(160) NULL, ADD COLUMN required_permissions VARCHAR(512) NULL;

CREATE TABLE sys_menu_permission (menu_id BIGINT NOT NULL,permission_id BIGINT NOT NULL,created_by BIGINT NOT NULL,created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),PRIMARY KEY(menu_id,permission_id),CONSTRAINT fk_menu_function_menu FOREIGN KEY(menu_id) REFERENCES sys_menu(id),CONSTRAINT fk_menu_function_permission FOREIGN KEY(permission_id) REFERENCES sys_permission(id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

UPDATE sys_menu m JOIN sys_permission p ON p.permission_code=m.menu_code SET m.permission_code=p.permission_code;

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'nav_master','基础管理',NULL,100,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='nav_master');

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_master'),'nav_master-materials','物料主数据','/master/materials',10,'ACTIVE',1,1,'master:material:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/master/materials');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_master' SET m.parent_id=parent.id,m.menu_name='物料主数据',m.sort_no=10,m.permission_code='master:material:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/master/materials';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_master'),'nav_master-suppliers','供应商','/master/suppliers',20,'ACTIVE',1,1,'master:supplier:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/master/suppliers');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_master' SET m.parent_id=parent.id,m.menu_name='供应商',m.sort_no=20,m.permission_code='master:supplier:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/master/suppliers';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_master'),'nav_master-organizations','组织','/master/organizations',30,'ACTIVE',1,1,'master:org:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/master/organizations');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_master' SET m.parent_id=parent.id,m.menu_name='组织',m.sort_no=30,m.permission_code='master:org:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/master/organizations';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_master'),'nav_master-units','单位','/master/units',40,'ACTIVE',1,1,'master:uom:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/master/units');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_master' SET m.parent_id=parent.id,m.menu_name='单位',m.sort_no=40,m.permission_code='master:uom:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/master/units';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_master'),'nav_master-unit-conversions','单位换算','/master/unit-conversions',50,'ACTIVE',1,1,'master:uom:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/master/unit-conversions');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_master' SET m.parent_id=parent.id,m.menu_name='单位换算',m.sort_no=50,m.permission_code='master:uom:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/master/unit-conversions';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_master'),'nav_master-equipment','设备','/master/equipment',60,'ACTIVE',1,1,'master:equipment:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/master/equipment');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_master' SET m.parent_id=parent.id,m.menu_name='设备',m.sort_no=60,m.permission_code='master:equipment:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/master/equipment';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_master'),'nav_master-qualifications','人员资格','/master/qualifications',70,'ACTIVE',1,1,'master:qualification:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/master/qualifications');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_master' SET m.parent_id=parent.id,m.menu_name='人员资格',m.sort_no=70,m.permission_code='master:qualification:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/master/qualifications';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_master'),'nav_process-products','产品管理','/process/products',80,'ACTIVE',1,1,'master:product:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/process/products');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_master' SET m.parent_id=parent.id,m.menu_name='产品管理',m.sort_no=80,m.permission_code='master:product:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/process/products';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_master'),'nav_process-packages','工艺包管理','/process/packages',90,'ACTIVE',1,1,'process:package:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/process/packages');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_master' SET m.parent_id=parent.id,m.menu_name='工艺包管理',m.sort_no=90,m.permission_code='process:package:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/process/packages';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'nav_wms','WMS管理',NULL,200,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='nav_wms');

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_wms'),'nav_wms-receipts','原辅料收货记录','/wms/receipts',10,'ACTIVE',1,1,'wms:receipt:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/wms/receipts');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_wms' SET m.parent_id=parent.id,m.menu_name='原辅料收货记录',m.sort_no=10,m.permission_code='wms:receipt:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/wms/receipts';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_wms'),'nav_wms-inventory','库存管理','/wms/inventory',20,'ACTIVE',1,1,'wms:inventory:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/wms/inventory');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_wms' SET m.parent_id=parent.id,m.menu_name='库存管理',m.sort_no=20,m.permission_code='wms:inventory:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/wms/inventory';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_wms'),'nav_wms-requests','领料申请','/wms/requests',30,'ACTIVE',1,1,'wms:request:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/wms/requests');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_wms' SET m.parent_id=parent.id,m.menu_name='领料申请',m.sort_no=30,m.permission_code='wms:request:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/wms/requests';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_wms'),'nav_wms-issues','出库管理','/wms/issues',40,'ACTIVE',1,1,'wms:issue:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/wms/issues');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_wms' SET m.parent_id=parent.id,m.menu_name='出库管理',m.sort_no=40,m.permission_code='wms:issue:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/wms/issues';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_wms'),'nav_wms-returns','退料管理','/wms/returns',50,'ACTIVE',1,1,'wms:issue:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/wms/returns');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_wms' SET m.parent_id=parent.id,m.menu_name='退料管理',m.sort_no=50,m.permission_code='wms:issue:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/wms/returns';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'nav_quality','质量管理',NULL,300,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='nav_quality');

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_quality'),'nav_incoming-inspection-requests-list','请验单','/quality/inspection-requests',10,'ACTIVE',1,1,'qms:inspection-request:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/quality/inspection-requests');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_quality' SET m.parent_id=parent.id,m.menu_name='请验单',m.sort_no=10,m.permission_code='qms:inspection-request:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/quality/inspection-requests';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_quality'),'nav_incoming-sampling-tasks-list','取样记录','/quality/sampling-tasks',20,'ACTIVE',1,1,'qms:sampling:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/quality/sampling-tasks');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_quality' SET m.parent_id=parent.id,m.menu_name='取样记录',m.sort_no=20,m.permission_code='qms:sampling:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/quality/sampling-tasks';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_quality'),'nav_incoming-samples-list','样品','/quality/samples',30,'ACTIVE',1,1,'qms:test:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/quality/samples');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_quality' SET m.parent_id=parent.id,m.menu_name='样品',m.sort_no=30,m.permission_code='qms:test:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/quality/samples';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_quality'),'nav_incoming-inspection-tasks-list','检验记录','/quality/inspection-tasks',40,'ACTIVE',1,1,'qms:test:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/quality/inspection-tasks');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_quality' SET m.parent_id=parent.id,m.menu_name='检验记录',m.sort_no=40,m.permission_code='qms:test:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/quality/inspection-tasks';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_quality'),'nav_incoming-inspection-reports-list','检验报告','/quality/inspection-reports',50,'ACTIVE',1,1,'qms:report:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/quality/inspection-reports');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_quality' SET m.parent_id=parent.id,m.menu_name='检验报告',m.sort_no=50,m.permission_code='qms:report:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/quality/inspection-reports';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_quality'),'nav_qc-specifications','QC质量标准','/quality/specifications',60,'ACTIVE',1,1,'qms:specification:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/quality/specifications');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_quality' SET m.parent_id=parent.id,m.menu_name='QC质量标准',m.sort_no=60,m.permission_code='qms:specification:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/quality/specifications';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_quality'),'nav_production-plans-list','生产质量计划','/quality/production-plans',70,'ACTIVE',1,1,'qms:plan:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/quality/production-plans');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_quality' SET m.parent_id=parent.id,m.menu_name='生产质量计划',m.sort_no=70,m.permission_code='qms:plan:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/quality/production-plans';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_quality'),'nav_production-tests-list','生产检验','/quality/production-tests',80,'ACTIVE',1,1,'qms:test:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/quality/production-tests');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_quality' SET m.parent_id=parent.id,m.menu_name='生产检验',m.sort_no=80,m.permission_code='qms:test:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/quality/production-tests';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_quality'),'nav_incoming-deviations-list','质量调查','/deviations',90,'ACTIVE',1,1,'qms:deviation:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/deviations');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_quality' SET m.parent_id=parent.id,m.menu_name='质量调查',m.sort_no=90,m.permission_code='qms:deviation:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/deviations';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'nav_production','生产管理',NULL,400,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='nav_production');

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_production'),'nav_production-orders-list','生产订单','/production/orders',10,'ACTIVE',1,1,'production:order:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/production/orders');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_production' SET m.parent_id=parent.id,m.menu_name='生产订单',m.sort_no=10,m.permission_code='production:order:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/production/orders';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_production'),'nav_production-batches-list','生产批','/production/batches',20,'ACTIVE',1,1,'production:batch:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/production/batches');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_production' SET m.parent_id=parent.id,m.menu_name='生产批',m.sort_no=20,m.permission_code='production:batch:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/production/batches';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_production'),'nav_production-execution','生产执行','/production/execution',30,'ACTIVE',1,1,'mes:execution:view','mes:operation:view' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/production/execution');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_production' SET m.parent_id=parent.id,m.menu_name='生产执行',m.sort_no=30,m.permission_code='mes:execution:view',m.required_permissions='mes:operation:view',m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/production/execution';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_production'),'nav_ebr-book-templates','批记录册模板','/ebr/book-templates',40,'ACTIVE',1,1,'ebr:template:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/ebr/book-templates');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_production' SET m.parent_id=parent.id,m.menu_name='批记录册模板',m.sort_no=40,m.permission_code='ebr:template:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/ebr/book-templates';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_production'),'nav_ebr-templates','eBR模板','/ebr/templates',50,'ACTIVE',1,1,'ebr:template:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/ebr/templates');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_production' SET m.parent_id=parent.id,m.menu_name='eBR模板',m.sort_no=50,m.permission_code='ebr:template:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/ebr/templates';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_production'),'nav_production-balances','物料平衡','/production/balances',60,'ACTIVE',1,1,'balance:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/production/balances');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_production' SET m.parent_id=parent.id,m.menu_name='物料平衡',m.sort_no=60,m.permission_code='balance:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/production/balances';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'nav_finished','成品管理',NULL,500,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='nav_finished');

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_finished'),'nav_finished-inbound','成品入库申请','/finished/inbound',10,'ACTIVE',1,1,'wms:finished-inbound:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/finished/inbound');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_finished' SET m.parent_id=parent.id,m.menu_name='成品入库申请',m.sort_no=10,m.permission_code='wms:finished-inbound:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/finished/inbound';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_finished'),'nav_finished-receiving','成品生产入库（待检）','/finished/receiving',20,'ACTIVE',1,1,'wms:finished-inbound:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/finished/receiving');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_finished' SET m.parent_id=parent.id,m.menu_name='成品生产入库（待检）',m.sort_no=20,m.permission_code='wms:finished-inbound:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/finished/receiving';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_finished'),'nav_finished-requests','成品请验','/finished/requests',30,'ACTIVE',1,1,'qms:finished-request:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/finished/requests');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_finished' SET m.parent_id=parent.id,m.menu_name='成品请验',m.sort_no=30,m.permission_code='qms:finished-request:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/finished/requests';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_finished'),'nav_finished-sampling','成品取样','/finished/sampling',40,'ACTIVE',1,1,'qms:finished-sampling:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/finished/sampling');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_finished' SET m.parent_id=parent.id,m.menu_name='成品取样',m.sort_no=40,m.permission_code='qms:finished-sampling:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/finished/sampling';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_finished'),'nav_finished-tests','成品检验','/finished/tests',50,'ACTIVE',1,1,'qms:test:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/finished/tests');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_finished' SET m.parent_id=parent.id,m.menu_name='成品检验',m.sort_no=50,m.permission_code='qms:test:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/finished/tests';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_finished'),'nav_finished-qa-reviews','QA批审核','/finished/qa-reviews',60,'ACTIVE',1,1,'qa:batch-review',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/finished/qa-reviews');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_finished' SET m.parent_id=parent.id,m.menu_name='QA批审核',m.sort_no=60,m.permission_code='qa:batch-review',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/finished/qa-reviews';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_finished'),'nav_finished-releases','成品放行','/finished/releases',70,'ACTIVE',1,1,'qa:release','qa:batch-review' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/finished/releases');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_finished' SET m.parent_id=parent.id,m.menu_name='成品放行',m.sort_no=70,m.permission_code='qa:release',m.required_permissions='qa:batch-review',m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/finished/releases';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_finished'),'nav_finished-inventory','成品库存','/finished/inventory',80,'ACTIVE',1,1,'wms:inventory:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/finished/inventory');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_finished' SET m.parent_id=parent.id,m.menu_name='成品库存',m.sort_no=80,m.permission_code='wms:inventory:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/finished/inventory';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_finished'),'nav_finished-shipments','成品发货出库','/finished/shipments',90,'ACTIVE',1,1,'wms:finished-shipment:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/finished/shipments');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_finished' SET m.parent_id=parent.id,m.menu_name='成品发货出库',m.sort_no=90,m.permission_code='wms:finished-shipment:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/finished/shipments';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by) SELECT 1,NULL,'nav_system','系统管理',NULL,600,'ACTIVE',1,1 WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND menu_code='nav_system');

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_system'),'nav_print-templates','打印模板','/admin/print-templates',10,'ACTIVE',1,1,'print:template:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/admin/print-templates');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_system' SET m.parent_id=parent.id,m.menu_name='打印模板',m.sort_no=10,m.permission_code='print:template:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/admin/print-templates';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_system'),'nav_users','用户管理','/admin/users',20,'ACTIVE',1,1,'iam:user:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/admin/users');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_system' SET m.parent_id=parent.id,m.menu_name='用户管理',m.sort_no=20,m.permission_code='iam:user:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/admin/users';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_system'),'nav_roles','角色与权限','/admin/roles',30,'ACTIVE',1,1,'iam:role:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/admin/roles');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_system' SET m.parent_id=parent.id,m.menu_name='角色与权限',m.sort_no=30,m.permission_code='iam:role:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/admin/roles';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_system'),'nav_menus','菜单管理','/admin/menus',40,'ACTIVE',1,1,'iam:menu:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/admin/menus');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_system' SET m.parent_id=parent.id,m.menu_name='菜单管理',m.sort_no=40,m.permission_code='iam:menu:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/admin/menus';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_system'),'nav_trace','完整追溯','/trace',50,'ACTIVE',1,1,'trace:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/trace');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_system' SET m.parent_id=parent.id,m.menu_name='完整追溯',m.sort_no=50,m.permission_code='trace:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/trace';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_system'),'nav_audit','GMP Audit Trail','/audit',60,'ACTIVE',1,1,'audit:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/audit');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_system' SET m.parent_id=parent.id,m.menu_name='GMP Audit Trail',m.sort_no=60,m.permission_code='audit:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/audit';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code,required_permissions) SELECT 1,(SELECT id FROM sys_menu WHERE org_id=1 AND menu_code='nav_system'),'nav_integration-operations','Integration Operations','/integration/operations',70,'ACTIVE',1,1,'integration:view',NULL WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/integration/operations');

UPDATE sys_menu m JOIN sys_menu parent ON parent.org_id=m.org_id AND parent.menu_code='nav_system' SET m.parent_id=parent.id,m.menu_name='Integration Operations',m.sort_no=70,m.permission_code='integration:view',m.required_permissions=NULL,m.version_no=m.version_no+1 WHERE m.org_id=1 AND m.route_path='/integration/operations';

INSERT INTO sys_menu(org_id,parent_id,menu_code,menu_name,route_path,sort_no,status,created_by,updated_by,permission_code) SELECT 1,NULL,'nav_home','首页','/',0,'ACTIVE',1,1,'menu:home' WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE org_id=1 AND route_path='/');

INSERT INTO sys_menu_permission(menu_id,permission_id,created_by) SELECT m.id,p.id,1 FROM sys_menu m JOIN sys_permission p ON p.permission_code=m.permission_code OR (m.permission_code LIKE '%:%:%' AND p.permission_code LIKE CONCAT(SUBSTRING_INDEX(m.permission_code,':',2),':%')) WHERE m.org_id=1 AND m.route_path IS NOT NULL;

INSERT INTO sys_role_menu(org_id,role_id,menu_id,created_by) SELECT m.org_id,rp.role_id,m.id,1 FROM sys_menu m JOIN sys_permission p ON p.permission_code=m.permission_code JOIN sys_role_permission rp ON rp.permission_id=p.id WHERE m.org_id=1 AND (m.required_permissions IS NULL OR EXISTS(SELECT 1 FROM sys_role_permission extra JOIN sys_permission ep ON ep.id=extra.permission_id WHERE extra.role_id=rp.role_id AND ep.permission_code=m.required_permissions)) AND NOT EXISTS(SELECT 1 FROM sys_role_menu rm WHERE rm.org_id=m.org_id AND rm.role_id=rp.role_id AND rm.menu_id=m.id);
