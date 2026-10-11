-- Controlled quality inspection object types.
-- Fixed system codes; display labels may be maintained under dictionary permissions.
-- No existing inspection documents or business rows are rewritten.
INSERT INTO sys_dict_type
(org_id,dict_code,dict_name,dict_kind,structure_type,description,sort_no,status,created_by,updated_by)
SELECT 1,'QC_OBJECT_TYPE','检验对象类型','SYSTEM','FLAT',
'统一质量管理固定基础字典；对象编码及含义不得随意修改',60,'ACTIVE',1,1
WHERE NOT EXISTS (
 SELECT 1 FROM sys_dict_type WHERE org_id=1 AND dict_code='QC_OBJECT_TYPE'
);

INSERT INTO sys_dict_item
(org_id,dict_type_id,item_code,item_label,description,sort_no,status,created_by,updated_by)
SELECT 1,t.id,x.item_code,x.item_label,x.description,x.sort_no,'ACTIVE',1,1
FROM sys_dict_type t
JOIN (
 SELECT 'MATERIAL' AS item_code,'物料' AS item_label,'关联物料批次 md_material_lot' AS description,10 AS sort_no
 UNION ALL SELECT 'FINISHED_PRODUCT','成品','关联生产批次 prd_main_batch',20
 UNION ALL SELECT 'INTERMEDIATE','中间品','关联生产批次与工序执行对象',30
 UNION ALL SELECT 'ENVIRONMENT','环境','关联环境监测点或采样位置',40
 UNION ALL SELECT 'PROCESS','过程检验','关联生产批次与工序执行对象',50
) x
WHERE t.org_id=1 AND t.dict_code='QC_OBJECT_TYPE'
AND NOT EXISTS (
 SELECT 1 FROM sys_dict_item i
 WHERE i.org_id=1 AND i.dict_type_id=t.id AND i.item_code=x.item_code
);
