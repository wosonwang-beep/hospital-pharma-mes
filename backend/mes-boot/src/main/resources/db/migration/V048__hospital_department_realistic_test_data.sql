-- Persistent, clearly labeled hospital MES department sample data. No fabricated staff assignments.
-- Idempotent by department_code. Existing departments and user mappings are untouched.
INSERT INTO sys_department(org_id,organization_id,parent_id,department_code,department_name,sort_no,status,description,created_by,updated_by)
SELECT o.org_id,o.id,NULL,'MES_DEMO_PHARMACY','药学部',10,'ACTIVE','测试样例｜药事管理与制剂业务统筹',1,1
FROM md_organization o WHERE o.org_id=1 AND o.status='ACTIVE' AND o.id=(SELECT MIN(id) FROM md_organization WHERE org_id=1 AND status='ACTIVE')
AND NOT EXISTS(SELECT 1 FROM sys_department d WHERE d.org_id=o.org_id AND d.department_code='MES_DEMO_PHARMACY');
INSERT INTO sys_department(org_id,organization_id,parent_id,department_code,department_name,sort_no,status,description,created_by,updated_by)
SELECT o.org_id,o.id,(SELECT p.id FROM sys_department p WHERE p.org_id=o.org_id AND p.department_code='MES_DEMO_PHARMACY' LIMIT 1),'MES_DEMO_PREPARATION_CENTER','制剂中心',20,'ACTIVE','测试样例｜医院制剂生产与执行管理',1,1
FROM md_organization o WHERE o.org_id=1 AND o.status='ACTIVE' AND o.id=(SELECT MIN(id) FROM md_organization WHERE org_id=1 AND status='ACTIVE')
AND NOT EXISTS(SELECT 1 FROM sys_department d WHERE d.org_id=o.org_id AND d.department_code='MES_DEMO_PREPARATION_CENTER');
INSERT INTO sys_department(org_id,organization_id,parent_id,department_code,department_name,sort_no,status,description,created_by,updated_by)
SELECT o.org_id,o.id,(SELECT p.id FROM sys_department p WHERE p.org_id=o.org_id AND p.department_code='MES_DEMO_PREPARATION_CENTER' LIMIT 1),'MES_DEMO_PRODUCTION_MANAGEMENT','生产管理组',30,'ACTIVE','测试样例｜生产计划、批次管理和生产指令',1,1
FROM md_organization o WHERE o.org_id=1 AND o.status='ACTIVE' AND o.id=(SELECT MIN(id) FROM md_organization WHERE org_id=1 AND status='ACTIVE')
AND NOT EXISTS(SELECT 1 FROM sys_department d WHERE d.org_id=o.org_id AND d.department_code='MES_DEMO_PRODUCTION_MANAGEMENT');
INSERT INTO sys_department(org_id,organization_id,parent_id,department_code,department_name,sort_no,status,description,created_by,updated_by)
SELECT o.org_id,o.id,(SELECT p.id FROM sys_department p WHERE p.org_id=o.org_id AND p.department_code='MES_DEMO_PREPARATION_CENTER' LIMIT 1),'MES_DEMO_PREPARATION_WORKSHOP','制剂生产车间',40,'ACTIVE','测试样例｜配制、灌装、包装及现场生产',1,1
FROM md_organization o WHERE o.org_id=1 AND o.status='ACTIVE' AND o.id=(SELECT MIN(id) FROM md_organization WHERE org_id=1 AND status='ACTIVE')
AND NOT EXISTS(SELECT 1 FROM sys_department d WHERE d.org_id=o.org_id AND d.department_code='MES_DEMO_PREPARATION_WORKSHOP');
INSERT INTO sys_department(org_id,organization_id,parent_id,department_code,department_name,sort_no,status,description,created_by,updated_by)
SELECT o.org_id,o.id,(SELECT p.id FROM sys_department p WHERE p.org_id=o.org_id AND p.department_code='MES_DEMO_PREPARATION_CENTER' LIMIT 1),'MES_DEMO_PACKAGING_GROUP','包装组',50,'ACTIVE','测试样例｜内外包装、标签与包装记录',1,1
FROM md_organization o WHERE o.org_id=1 AND o.status='ACTIVE' AND o.id=(SELECT MIN(id) FROM md_organization WHERE org_id=1 AND status='ACTIVE')
AND NOT EXISTS(SELECT 1 FROM sys_department d WHERE d.org_id=o.org_id AND d.department_code='MES_DEMO_PACKAGING_GROUP');
INSERT INTO sys_department(org_id,organization_id,parent_id,department_code,department_name,sort_no,status,description,created_by,updated_by)
SELECT o.org_id,o.id,(SELECT p.id FROM sys_department p WHERE p.org_id=o.org_id AND p.department_code='MES_DEMO_PHARMACY' LIMIT 1),'MES_DEMO_QA','质量保证组（QA）',60,'ACTIVE','测试样例｜偏差、变更、批审核及放行',1,1
FROM md_organization o WHERE o.org_id=1 AND o.status='ACTIVE' AND o.id=(SELECT MIN(id) FROM md_organization WHERE org_id=1 AND status='ACTIVE')
AND NOT EXISTS(SELECT 1 FROM sys_department d WHERE d.org_id=o.org_id AND d.department_code='MES_DEMO_QA');
INSERT INTO sys_department(org_id,organization_id,parent_id,department_code,department_name,sort_no,status,description,created_by,updated_by)
SELECT o.org_id,o.id,(SELECT p.id FROM sys_department p WHERE p.org_id=o.org_id AND p.department_code='MES_DEMO_PHARMACY' LIMIT 1),'MES_DEMO_QC','质量控制组（QC）',70,'ACTIVE','测试样例｜取样、检验、报告与质量标准',1,1
FROM md_organization o WHERE o.org_id=1 AND o.status='ACTIVE' AND o.id=(SELECT MIN(id) FROM md_organization WHERE org_id=1 AND status='ACTIVE')
AND NOT EXISTS(SELECT 1 FROM sys_department d WHERE d.org_id=o.org_id AND d.department_code='MES_DEMO_QC');
INSERT INTO sys_department(org_id,organization_id,parent_id,department_code,department_name,sort_no,status,description,created_by,updated_by)
SELECT o.org_id,o.id,(SELECT p.id FROM sys_department p WHERE p.org_id=o.org_id AND p.department_code='MES_DEMO_PHARMACY' LIMIT 1),'MES_DEMO_MATERIAL_WAREHOUSE','原辅料仓库',80,'ACTIVE','测试样例｜收货、储存、领料与退料',1,1
FROM md_organization o WHERE o.org_id=1 AND o.status='ACTIVE' AND o.id=(SELECT MIN(id) FROM md_organization WHERE org_id=1 AND status='ACTIVE')
AND NOT EXISTS(SELECT 1 FROM sys_department d WHERE d.org_id=o.org_id AND d.department_code='MES_DEMO_MATERIAL_WAREHOUSE');
INSERT INTO sys_department(org_id,organization_id,parent_id,department_code,department_name,sort_no,status,description,created_by,updated_by)
SELECT o.org_id,o.id,(SELECT p.id FROM sys_department p WHERE p.org_id=o.org_id AND p.department_code='MES_DEMO_PHARMACY' LIMIT 1),'MES_DEMO_FINISHED_WAREHOUSE','成品仓库',90,'ACTIVE','测试样例｜成品入库、库存及发货出库',1,1
FROM md_organization o WHERE o.org_id=1 AND o.status='ACTIVE' AND o.id=(SELECT MIN(id) FROM md_organization WHERE org_id=1 AND status='ACTIVE')
AND NOT EXISTS(SELECT 1 FROM sys_department d WHERE d.org_id=o.org_id AND d.department_code='MES_DEMO_FINISHED_WAREHOUSE');
INSERT INTO sys_department(org_id,organization_id,parent_id,department_code,department_name,sort_no,status,description,created_by,updated_by)
SELECT o.org_id,o.id,(SELECT p.id FROM sys_department p WHERE p.org_id=o.org_id AND p.department_code='MES_DEMO_PREPARATION_CENTER' LIMIT 1),'MES_DEMO_EQUIPMENT','设备管理组',100,'ACTIVE','测试样例｜设备维护、校准和状态管理',1,1
FROM md_organization o WHERE o.org_id=1 AND o.status='ACTIVE' AND o.id=(SELECT MIN(id) FROM md_organization WHERE org_id=1 AND status='ACTIVE')
AND NOT EXISTS(SELECT 1 FROM sys_department d WHERE d.org_id=o.org_id AND d.department_code='MES_DEMO_EQUIPMENT');
INSERT INTO sys_department(org_id,organization_id,parent_id,department_code,department_name,sort_no,status,description,created_by,updated_by)
SELECT o.org_id,o.id,(SELECT p.id FROM sys_department p WHERE p.org_id=o.org_id AND p.department_code='MES_DEMO_PHARMACY' LIMIT 1),'MES_DEMO_DOCUMENT_CONTROL','文件与记录管理组',110,'ACTIVE','测试样例｜受控文件、记录归档与追溯',1,1
FROM md_organization o WHERE o.org_id=1 AND o.status='ACTIVE' AND o.id=(SELECT MIN(id) FROM md_organization WHERE org_id=1 AND status='ACTIVE')
AND NOT EXISTS(SELECT 1 FROM sys_department d WHERE d.org_id=o.org_id AND d.department_code='MES_DEMO_DOCUMENT_CONTROL');
INSERT INTO sys_department(org_id,organization_id,parent_id,department_code,department_name,sort_no,status,description,created_by,updated_by)
SELECT o.org_id,o.id,NULL,'MES_DEMO_INFORMATION','信息管理组',120,'ACTIVE','测试样例｜信息系统运行与账号支持',1,1
FROM md_organization o WHERE o.org_id=1 AND o.status='ACTIVE' AND o.id=(SELECT MIN(id) FROM md_organization WHERE org_id=1 AND status='ACTIVE')
AND NOT EXISTS(SELECT 1 FROM sys_department d WHERE d.org_id=o.org_id AND d.department_code='MES_DEMO_INFORMATION');
