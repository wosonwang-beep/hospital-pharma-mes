"""Generate new approved storage files. Existing physical migrations stay untouched."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
tables={
'wms_material_lot_inventory_decision':('mes-wms','wms','InventoryDecisionEntity',False,{'materialLotId':'Long','previousDecisionId':'Long','action':'String','previousInventoryStatus':'String','resultingInventoryStatus':'String','reason':'String','decidedBy':'Long','decidedAt':'LocalDateTime','signatureId':'Long','signatureEvidenceJson':'String'}),
'mes_clearance_record':('mes-execution','execution','ClearanceRecordEntity',False,{'operationExecutionId':'Long','equipmentUsageId':'Long','previousRecordId':'Long','outcome':'String','reason':'String','performedBy':'Long','performedAt':'LocalDateTime','signatureId':'Long','signatureEvidenceJson':'String'}),
'mes_clearance_review':('mes-execution','execution','ClearanceReviewEntity',False,{'clearanceRecordId':'Long','decision':'String','reason':'String','reviewedBy':'Long','reviewedAt':'LocalDateTime','signatureId':'Long','signatureEvidenceJson':'String'}),
'qms_ipc_instance':('mes-qms','qms','IpcInstanceEntity',True,{'operationExecutionId':'Long','ipcCode':'String','status':'String','result':'String','completedAt':'LocalDateTime','definitionSnapshotJson':'String','currentResultRevisionId':'Long'}),
'qms_ipc_result_revision':('mes-qms','qms','IpcResultEntity',False,{'ipcInstanceId':'Long','revisionNo':'Integer','previousRevisionId':'Long','resultNumeric':'BigDecimal','resultText':'String','resultConclusion':'String','definitionSnapshotJson':'String','reasonForChange':'String','recordedBy':'Long','recordedAt':'LocalDateTime','signatureId':'Long','signatureEvidenceJson':'String'}),
'qms_ipc_review':('mes-qms','qms','IpcReviewEntity',False,{'ipcResultRevisionId':'Long','disposition':'String','reason':'String','reviewedBy':'Long','reviewedAt':'LocalDateTime','signatureId':'Long','signatureEvidenceJson':'String'})}
import re
def col(k):return re.sub(r'([a-z])([A-Z])',r'\1_\2',k).lower()
fks={'materialLotId':'md_material_lot','previousDecisionId':'wms_material_lot_inventory_decision','operationExecutionId':'mes_operation_execution','equipmentUsageId':'mes_equipment_usage','previousRecordId':'mes_clearance_record','clearanceRecordId':'mes_clearance_record','currentResultRevisionId':'qms_ipc_result_revision','ipcInstanceId':'qms_ipc_instance','previousRevisionId':'qms_ipc_result_revision','ipcResultRevisionId':'qms_ipc_result_revision','decidedBy':'sys_user','performedBy':'sys_user','reviewedBy':'sys_user','recordedBy':'sys_user','signatureId':'gxp_signature'}
nullable={'previousDecisionId','equipmentUsageId','previousRecordId','result','completedAt','currentResultRevisionId','previousRevisionId','resultNumeric','resultText','reasonForChange'}
sql=['-- DCP-MES-008-011-FUNCTIONAL-CLOSURE-001 explicitly approved 2026-10-04. Append-only V024.','ALTER TABLE proc_operation_def ADD COLUMN IF NOT EXISTS ipc_definitions_json LONGTEXT NOT NULL DEFAULT (\'[]\') CHECK(JSON_VALID(ipc_definitions_json));']
for table,(module,pkg,cls,mutable,fields) in tables.items():
    path=root/f'backend/{module}/src/main/java/com/hospital/mes/{pkg}/infrastructure'
    exclude='' if mutable else ', excludeProperty={"updatedBy","updatedAt","versionNo"}'
    source=f'package com.hospital.mes.{pkg}.infrastructure;\nimport com.baomidou.mybatisplus.annotation.TableName;\nimport com.hospital.mes.masterdata.infrastructure.ScopedEntity;\nimport java.time.LocalDateTime;\nimport java.math.BigDecimal;\n@TableName(value="{table}"{exclude}) public class {cls} extends ScopedEntity {{\n'
    for name,typ in fields.items():
        cap=name[0].upper()+name[1:]
        source+=f' private {typ} {name};\n public {typ} get{cap}(){{return {name};}} public void set{cap}({typ} value){{{name}=value;}}\n'
    source+=' @Override public java.util.List<String> allowedActions(){return java.util.List.of();}\n}\n'
    (path/f'{cls}.java').write_text(source,encoding='utf-8')
    mapper=cls.removesuffix('Entity')+'Mapper'
    (path/f'{mapper}.java').write_text(f'package com.hospital.mes.{pkg}.infrastructure;\n@org.apache.ibatis.annotations.Mapper public interface {mapper} extends com.baomidou.mybatisplus.core.mapper.BaseMapper<{cls}> {{}}\n',encoding='utf-8')
    columns=['id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY','org_id BIGINT NOT NULL','created_by BIGINT NOT NULL','created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)']
    if mutable: columns+=['updated_by BIGINT NOT NULL','updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)','version_no BIGINT NOT NULL DEFAULT 0']
    for name,typ in fields.items():
        dbtyp={'Long':'BIGINT','Integer':'INT','LocalDateTime':'DATETIME(3)','BigDecimal':'DECIMAL(24,8)'}.get(typ,'VARCHAR(1000)')
        if name.endswith('Json'):dbtyp='LONGTEXT'
        if name in {'ipcCode'}:dbtyp='VARCHAR(64)'
        if name in {'action','status','result','outcome','decision','disposition','previousInventoryStatus','resultingInventoryStatus','resultConclusion'}:dbtyp='VARCHAR(30)'
        columns.append(f'{col(name)} {dbtyp} '+('NULL' if name in nullable else 'NOT NULL'))
        if name.endswith('Json'):columns.append(f'CHECK(JSON_VALID({col(name)}))')
        if name in fks and name!='currentResultRevisionId':columns.append(f'FOREIGN KEY({col(name)}) REFERENCES {fks[name]}(id)')
    key=next(iter(fields))
    columns.append(f'KEY ix_{table}_root(org_id,{col(key)},id)')
    if table=='wms_material_lot_inventory_decision': columns+=['UNIQUE KEY uk_inventory_predecessor(org_id,previous_decision_id)',"CHECK(action IN ('FREEZE','UNFREEZE'))","CHECK((action='FREEZE' AND previous_inventory_status='AVAILABLE' AND resulting_inventory_status='FROZEN') OR (action='UNFREEZE' AND previous_inventory_status='FROZEN' AND resulting_inventory_status='AVAILABLE'))"]
    if table=='qms_ipc_instance': columns+=['UNIQUE KEY uk_ipc_operation_code(org_id,operation_execution_id,ipc_code)',"CHECK(status IN ('PENDING','TESTING','COMPLETED'))","CHECK(result IS NULL OR result IN ('PASS','FAIL','INVALID'))"]
    if table=='qms_ipc_result_revision': columns+=['UNIQUE KEY uk_ipc_revision(org_id,ipc_instance_id,revision_no)','UNIQUE KEY uk_ipc_predecessor(org_id,previous_revision_id)',"CHECK(result_conclusion IN ('PASS','FAIL','INVALID'))",'CHECK(revision_no>0)','CHECK((revision_no=1 AND previous_revision_id IS NULL) OR (revision_no>1 AND previous_revision_id IS NOT NULL AND reason_for_change IS NOT NULL))','CHECK((result_numeric IS NULL) <> (result_text IS NULL))']
    if table=='qms_ipc_review':columns+=['UNIQUE KEY uk_ipc_review(org_id,ipc_result_revision_id)',"CHECK(disposition IN ('CONFIRMED','INVALIDATED'))"]
    if table=='mes_clearance_record':columns+=['UNIQUE KEY uk_clearance_predecessor(org_id,previous_record_id)',"CHECK(outcome IN ('PASS','FAIL'))"]
    if table=='mes_clearance_review':columns+=['UNIQUE KEY uk_clearance_review(org_id,clearance_record_id)',"CHECK(decision IN ('APPROVED','REJECTED'))"]
    sql.append(f'CREATE TABLE {table} (\n '+',\n '.join(columns)+'\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;')
sql.append('ALTER TABLE qms_ipc_instance ADD FOREIGN KEY(current_result_revision_id) REFERENCES qms_ipc_result_revision(id);')
sql.append('DELIMITER $$')
for table,(_,_,_,mutable,_) in tables.items():
    if not mutable:sql.append(f"CREATE TRIGGER bu_{table} BEFORE UPDATE ON {table} FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Immutable controlled fact'$$")
    sql.append(f"CREATE TRIGGER bd_{table} BEFORE DELETE ON {table} FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Controlled fact deletion prohibited'$$")
sql.append('DELIMITER ;')
for permission in ['qa:material-inventory:freeze','qa:material-inventory:unfreeze','qms:ipc:view','qms:ipc:record','qms:ipc:review','mes:clearance:record','mes:clearance:review']:
    sql.append(f"INSERT INTO sys_permission(permission_code,permission_type,display_name,menu_route,enabled) SELECT '{permission}','ACTION','{permission}',NULL,TRUE WHERE NOT EXISTS(SELECT 1 FROM sys_permission WHERE permission_code='{permission}');")
migration=root/'backend/mes-boot/src/main/resources/db/migration/V024__controlled_inventory_ipc_clearance.sql'
if migration.exists():raise SystemExit('Do not overwrite a migration')
migration.write_text('\n\n'.join(sql)+'\n',encoding='utf-8')
print('Generated six owner-side mappings and append-only V024; not executed.')
