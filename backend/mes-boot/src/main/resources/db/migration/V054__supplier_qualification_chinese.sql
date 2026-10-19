-- User-approved 2026-10-11 supplier qualification wording and semantics.
-- English identifiers remain technical only; qualification values are Chinese.
-- Standalone enable flag preserves the difference between suspension and failure.
-- Historical regulated records and material-supplier relationship approval are untouched.

ALTER TABLE md_supplier
  DROP CONSTRAINT ck_supplier_state,
  ADD COLUMN enabled TINYINT(1) NOT NULL DEFAULT 1;

UPDATE md_supplier
SET enabled = CASE WHEN qualification_status = 'INACTIVE' THEN 0 ELSE 1 END,
    qualification_status = CASE
      WHEN qualification_status = 'APPROVED' THEN '合格'
      WHEN qualification_status = 'UNAPPROVED' THEN '待评审'
      WHEN qualification_status = 'INACTIVE' THEN '待评审'
      ELSE qualification_status
    END
WHERE qualification_status IN ('APPROVED','UNAPPROVED','INACTIVE');

ALTER TABLE md_supplier
  ADD CONSTRAINT ck_supplier_qualification_chinese
    CHECK (qualification_status IN ('合格','不合格','待评审')),
  ADD CONSTRAINT ck_supplier_enabled CHECK (enabled IN (0,1));

INSERT INTO sys_dict_type
(org_id,dict_code,dict_name,dict_kind,structure_type,description,sort_no,status,created_by,updated_by)
SELECT 1,'SUPPLIER_QUALIFICATION_STATUS','供应商资格状态','SYSTEM','FLAT',
'供应商资格结论，固定为待评审、合格、不合格；停用独立管理',25,'ACTIVE',1,1
WHERE NOT EXISTS (
 SELECT 1 FROM sys_dict_type WHERE org_id=1 AND dict_code='SUPPLIER_QUALIFICATION_STATUS'
);

INSERT INTO sys_dict_item
(org_id,dict_type_id,item_code,item_label,description,sort_no,status,is_default,created_by,updated_by)
SELECT 1,t.id,x.item_code,x.item_code,x.description,x.sort_no,'ACTIVE',x.is_default,1,1
FROM sys_dict_type t
JOIN (
 SELECT '待评审' AS item_code,'资格资料待评审；新建默认值' AS description,10 AS sort_no,1 AS is_default
 UNION ALL SELECT '合格','资格评审通过，仍须检查证照及供货关系有效期',20,0
 UNION ALL SELECT '不合格','资格评审不通过，不允许用于收货',30,0
) x
WHERE t.org_id=1 AND t.dict_code='SUPPLIER_QUALIFICATION_STATUS'
  AND NOT EXISTS (
    SELECT 1 FROM sys_dict_item i
    WHERE i.org_id=1 AND i.dict_type_id=t.id AND i.item_code=x.item_code
  );
