from pathlib import Path
import csv,json,hashlib
root=Path(__file__).resolve().parents[1];b=root/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.15'
api=json.loads((b/'08_OPENAPI_FULL_V1.0.15_FROZEN.yaml').read_text(encoding='utf-8'))
def load(pattern):
 p=next(b.glob(pattern))
 source=root/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.14'/p.name.replace('1.0.15','1.0.14')
 with source.open(encoding='utf-8-sig',newline='') as f:rows=list(csv.reader(f))
 width=len(rows[0]);rows=[row+['']*(width-len(row)) if len(row)<=width else row[:width-1]+[';'.join(row[width-1:])] for row in rows]
 index=next((i for i,r in enumerate(rows) if r and r[0].startswith('APPROVED ADDITIVE MAP')),len(rows));return p,rows[:index]
def save(p,rows):
 with p.open('w',encoding='utf-8',newline='') as f:csv.writer(f).writerows(rows)
 width=len(rows[0]);assert all(len(row)==width for row in rows),str(p)
p,rows=load('*ENDPOINT_CATALOG*.csv')
new=[]
for path,ops in api['paths'].items():
 for method,op in ops.items():
  if isinstance(op,dict) and op.get('tags')==['functional-closure']:new.append([method.upper(),path,op['operationId'],'functional-closure',op['x-permission'],op['x-mes-task'],str(op['x-audit-required']).lower()])
keys={(r[0],r[1]) for r in new};rows=[r for i,r in enumerate(rows) if i==0 or (r[0],r[1]) not in keys]+new;save(p,rows)
p,rows=load('*INTEGRATION_CONTRACT_MATRIX*.csv');rows += [
 ['WMS','InventoryDecision/current MaterialLot restriction','MES-008A;MES-011','wms_material_lot_inventory_decision;md_material_lot','Lot lock + signed append + audit + idempotency atomic','422 restriction/QA/date gate;409 stale version','Signed freeze/unfreeze and final-use races'],
 ['MES-012 bounded IPC stage','ExecutionQualityPort initialization/Gate','MES-010','qms_ipc_instance;qms_ipc_result_revision;qms_ipc_review','Synchronous same release transaction;root/operation current lock','Missing/valid FAIL/unreviewed required result blocks;producer failure rolls back release','TC-OP-002;TC-QMS-004;exact result and completion contention'],
 ['MES-010','Controlled clearance current evidence','START;RESUME','mes_clearance_record;mes_clearance_review','Root/Operation lock + immutable signed facts','Missing/FAIL/rejected/wrong binding blocks start','Actual independent review and start/rerecord contention']];save(p,rows)
p,rows=load('*TASK_DEPENDENCY_MATRIX*.csv');rows.append(['MES-012-R2 (approved IPC producer stage)','Only IPC instance/result/review producer','Physically available MES-008A/009 Operation/011/eBR contracts;full task dependencies unchanged','MES-007','MES-010','ExecutionQualityPort;no expansion to CAPA/balance/finished QA']);save(p,rows)
p,rows=load('*MIGRATION_DEPENDENCY_MATRIX*.csv');rows.append(['LG-FUNCTIONAL-CLOSURE','Approved DCP additive group','MES-008;010;012 bounded IPC','controlled_inventory_ipc_clearance','Six controlled tables;proc_operation_def.ipc_definitions_json','V001-V023 current successful physical chain','V024','FAILED first attempt;original preserved;recovery awaiting explicit exception']);save(p,rows)
p,rows=load('*REQUIREMENT_TRACEABILITY_MATRIX*.csv')
replacements={
'WMS-ELG-001':['WMS-ELG-001','Inventory freeze and final eligibility','wms_material_lot_inventory_decision;md_material_lot','POST material-lots/{id}/freeze|unfreeze','UI-WMS-LOT-V','MES-008;008A;011','TC-CLOSURE-FREEZE;final-use concurrency','IMPLEMENTED;NATIVE ACCEPTANCE BLOCKED'],
'QMS-IPC-001':['QMS-IPC-001','Frozen production IPC and independent review','qms_ipc_instance;qms_ipc_result_revision;qms_ipc_review','/ipc;/ipc/{id}/submit-result;/ipc/{id}/review-result','UI-EXEC-W;existing process editor','MES-012 approved IPC stage;MES-010 consumer','TC-OP-002;TC-QMS-004','IMPLEMENTED;NATIVE ACCEPTANCE BLOCKED']}
seen=set()
for i,row in enumerate(rows[1:],1):
 if row[0] in replacements:rows[i]=replacements[row[0]];seen.add(row[0])
rows.extend(v for k,v in replacements.items() if k not in seen)
rows.append(['MES-OP-001-CLEARANCE','Approved bounded clearance Gate','mes_clearance_record;mes_clearance_review','POST operations/{id}/record-clearance|review-clearance','UI-EXEC-W','MES-010','TC-CLOSURE-CLEARANCE','IMPLEMENTED;NATIVE ACCEPTANCE BLOCKED']);save(p,rows)
prototype=b/'ui-prototype/incoming-contract.html';s=prototype.read_text(encoding='utf-8')
addition='''<section id="functional-closure"><h2>批准的功能补齐 · v1.0.15</h2><p>现有物料批次360°：显示库存决定历史；受权冻结/解除冻结按钮；原因与签名密码沿用同行表单。此操作不替代 QA 放行。</p><p>现有工艺操作编辑：IPC 定义包含编码、名称、必检标记、结果类型、适用标准上下限/单位或预期文本、方法版本引用；批准版本及运行快照只读。</p><p>现有执行工作台：清场按精确工序/设备绑定记录和独立复核；IPC 显示冻结标准、原始修订、结果有效性决定及签名。原始 FAIL 保留，不能手工指定 PASS 或修改 status。</p><p>正式样式沿用 DESIGN_SYSTEM、UI-EXEC-W 与批准列表/编辑样式。已实现组件见 FunctionalQualityActions.vue；桌面/手机同行截图保存在本轮验收证据。</p></section>'''
if 'id="functional-closure"' not in s:s=s.replace('</body>',addition+'</body>');prototype.write_text(s,encoding='utf-8')
manifest={str(p.relative_to(b)).replace('\\','/'):hashlib.sha256(p.read_bytes()).hexdigest() for p in b.rglob('*') if p.is_file() and p.name!='00_FUNCTIONAL_CLOSURE_MANIFEST.json'}
(b/'00_FUNCTIONAL_CLOSURE_MANIFEST.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
print('All changed CSV matrices retain exact original column widths; candidate pointer remains unchanged.')
