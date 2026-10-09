-- Department status is a controlled dictionary. Existing department rows are unchanged.
INSERT INTO sys_dict_type(org_id,dict_code,dict_name,dict_kind,structure_type,description,sort_no,status,created_by,updated_by)
SELECT 1,'DEPARTMENT_STATUS','部门状态','SYSTEM','FLAT','部门启用和停用状态的受控选项',50,'ACTIVE',1,1
WHERE NOT EXISTS(SELECT 1 FROM sys_dict_type WHERE org_id=1 AND dict_code='DEPARTMENT_STATUS');
INSERT INTO sys_dict_item(org_id,dict_type_id,item_code,item_label,sort_no,status,created_by,updated_by)
SELECT 1,t.id,x.code,x.label,x.sort_no,'ACTIVE',1,1
FROM sys_dict_type t
JOIN (SELECT 'ACTIVE' code,'启用' label,10 sort_no UNION ALL SELECT 'INACTIVE','停用',20) x
WHERE t.org_id=1 AND t.dict_code='DEPARTMENT_STATUS'
AND NOT EXISTS(SELECT 1 FROM sys_dict_item i WHERE i.org_id=1 AND i.dict_type_id=t.id AND i.item_code=x.code);
