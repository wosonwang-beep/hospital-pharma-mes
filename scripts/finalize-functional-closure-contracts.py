"""Complete candidate contract artifacts; never switch the approved baseline pointer."""
from pathlib import Path
import json, hashlib, csv, io
root=Path(__file__).resolve().parents[1]
b=root/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.15'
p=b/'00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md'
s=p.read_text(encoding='utf-8');s=s[:s.index('## 9. 提案自审和待批准结论')]+'''## 9. 批准记录和发布状态

用户于 2026-10-04 明确回复“批准方案”，批准本文件 §§1–8 的完整边界。此审批仅授权开发，任务验收另行记录。

本发布为候选文档，尚未切换 authoritative pointer。V024 首次执行发生外键表名错误：实际批次事实源是 `md_material_lot`。第一次失败的迁移文件和原生失败历史保留；数据库恢复须按单独、透明的恢复提案取得例外授权。本契约不授权覆盖已执行迁移或修复历史。

嵌入式 IPC 明细同样要求 `qms:ipc:view`；无该权限时工作台返回空 IPC 明细，服务端 Gate 仍消费全部事实。所有 API 的 PK/FK/签名/人员 ID 为字符串，记录版本及修订号为整数。
''';p.write_text(s,encoding='utf-8')
ap=b/'08_OPENAPI_FULL_V1.0.15_FROZEN.yaml';api=json.loads(ap.read_text(encoding='utf-8'));schemas=api['components']['schemas']
schemas['IpcResultRevision']['properties']['revisionNo']={'type':'integer','minimum':1}
for name in ['IpcResultRevision','ClearanceRecord']:
 props=schemas[name]['properties'];props['createdBy']={'type':'string'};props['createdAt']={'type':'string','format':'date-time'};props['orgId']={'type':'string'};props['allowedActions']={'type':'array','items':{'type':'string'}}
schemas['ClearanceOperation']={'type':'object','additionalProperties':False,'properties':{k:{'type':'string','nullable':True} for k in ['id','orgId','createdBy','createdAt','updatedBy','updatedAt','executionUnitId','operationDefId','status','startedAt','completedAt','operatorId','gateEvidenceJson']}|{'operationSeq':{'type':'integer'},'versionNo':{'type':'integer'},'allowedActions':{'type':'array','items':{'type':'string'}},'clearanceRecords':{'type':'array','items':{'$ref':'#/components/schemas/ClearanceRecord'}}},'required':['id','versionNo','clearanceRecords']}
schemas['InventoryLotDecisionView']=json.loads(json.dumps(schemas.get('MaterialLot',{})))
if not schemas['InventoryLotDecisionView']:
 schemas['InventoryLotDecisionView']={'type':'object','properties':{k:{'type':'string','nullable':True} for k in ['id','materialId','supplierId','lotNo','supplierLotNo','qualityStatus','inventoryStatus','manufactureDate','expiryDate','retestDate','receiptItemId','createdBy','createdAt','updatedBy','updatedAt','orgId']}|{'versionNo':{'type':'integer'},'requiresIncomingInspectionSnapshot':{'type':'boolean'},'materialSnapshot':{'type':'object'},'allowedActions':{'type':'array','items':{'type':'string'}}},'required':['id','versionNo','qualityStatus','inventoryStatus']}
schemas['InventoryLotDecisionView'].setdefault('properties',{})['inventoryDecisions']={'type':'array','items':{'$ref':'#/components/schemas/InventoryDecision'}}
for path,ops in api['paths'].items():
 for method,op in ops.items():
  if not isinstance(op,dict) or op.get('tags')!=['functional-closure']:continue
  if path.startswith('/material-lots/'):response='InventoryLotDecisionView'
  elif path.startswith('/operations/'):response='ClearanceOperation'
  else:response='IpcPage' if path=='/ipc' and method=='get' else 'IpcInstance'
  wrapped=response+'Response';schemas[wrapped]={'type':'object','properties':{'code':{'type':'string'},'message':{'type':'string'},'traceId':{'type':'string'},'data':{'$ref':'#/components/schemas/'+response}},'required':['code','data','traceId']}
  op['responses']['200']['content']['application/json']['schema']={'$ref':'#/components/schemas/'+wrapped}
  for status in ['400','401','403','409','422']:op['responses'][status]={'description':'Controlled error','content':{'application/json':{'schema':{'$ref':'#/components/schemas/ApiError'}}}}
  if path=='/ipc' and method=='get':op['parameters']=[{'in':'query','name':key,'schema':({'type':'integer','minimum':0} if key in ['page','size'] else {'type':'string'})} for key in ['page','size','operationExecutionId','ipcCode','status']]
ap.write_text(json.dumps(api,ensure_ascii=False,indent=2),encoding='utf-8')
for pattern,rows in {
 '*ENDPOINT_CATALOG*.csv':['Inventory freeze/unfreeze: POST /material-lots/{id}/freeze|unfreeze; qa:material-inventory:freeze|unfreeze; MES-008-R2','IPC: GET/POST /ipc, GET /ipc/{id}, POST submit-result/review-result; qms:ipc:view|record|review; MES-012 staged','Clearance: POST /operations/{id}/record-clearance|review-clearance; mes:clearance:record|review; MES-010'],
 '*INTEGRATION_CONTRACT_MATRIX*.csv':['InventoryDecision/MaterialEligibility,MES-008,MES-008A/011,Current MaterialLot lock and current QA/date/investigation evidence','IPC production stage,MES-012 staged,MES-010,ExecutionQualityPort sync initialization and required/valid FAIL gate','Clearance,MES-010,START/RESUME,Exact operation/current binding/current signed record+independent review'],
 '*TASK_DEPENDENCY_MATRIX*.csv':['MES-012 IPC producer stage,physically available MES-008A/009/010 Operation/011/eBR contracts,Only approved staged IPC scope; whole task dependencies unchanged'],
 '*MIGRATION_DEPENDENCY_MATRIX*.csv':['Functional closure physical V024,successful V001-V023,First attempt failed and original evidence retained; recovery pending explicit exception'],
 '*REQUIREMENT_TRACEABILITY_MATRIX*.csv':['WMS-ELG-001,MES-008,InventoryDecision,TC-CLOSURE-FREEZE,UI-WMS-LOT-V','QMS-IPC-001,MES-012 staged,IpcInstance/ResultRevision/Review,TC-OP-002/TC-QMS-004,UI-EXEC-W','MES-OP-001,MES-010,ClearanceRecord/Review,TC-CLOSURE-CLEARANCE,UI-EXEC-W']
}.items():
 for target in b.glob(pattern):
  with target.open('a',encoding='utf-8',newline='') as f:
   writer=csv.writer(f);writer.writerow(['APPROVED ADDITIVE MAP — authoritative exact contract: 00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md'])
   for row in rows:writer.writerow(row.split(','))
db=b/'05_DATABASE_DESIGN_V1.0.15_FROZEN.md'
db.write_text(db.read_text(encoding='utf-8')+'\n\n### Exact physical FK authority\n\nMaterialLot identity is `md_material_lot(id)`, not a new `wms_material_lot` table. Inventory decisions reference that existing table. IPC and clearance refer `mes_operation_execution(id)`; result/review/signature FKs and six tables are enumerated in the approved closure contract. Failed V024 is implementation evidence, not a changed database design.\n',encoding='utf-8')
# Machine-resolve every OpenAPI schema reference before recording candidate review.
def refs(x):
 if isinstance(x,dict):
  if '$ref' in x:yield x['$ref']
  for v in x.values():yield from refs(v)
 elif isinstance(x,list):
  for v in x:yield from refs(v)
missing=[]
for ref in refs(api):
 if ref.startswith('#/'):
  node=api
  try:
   for part in ref[2:].split('/'):node=node[part]
  except KeyError:missing.append(ref)
if missing:raise SystemExit('Unresolved references: '+str(sorted(set(missing))))
review={'state':'CANDIDATE / NOT SWITCHED','approval':'2026-10-04 user 批准方案','openapiReferences':'PASS','domainScopes':'six owner records; existing MaterialLot identity preserved','migration':'V024 FAILED; no repair or rewrite','nativeAcceptance':'BLOCKED pending explicit recovery exception','ui':'existing Lot360/Execution/Process; horizontal fields; no new routes','permission':'explicit new grants; no implicit role changes; embedded IPC requires view','taskScope':'bounded MES-012 IPC producer only'}
(b/'00_FUNCTIONAL_CLOSURE_CONSISTENCY_REVIEW.json').write_text(json.dumps(review,ensure_ascii=False,indent=2),encoding='utf-8')
manifest={str(p.relative_to(b)).replace('\\','/'):hashlib.sha256(p.read_bytes()).hexdigest() for p in b.rglob('*') if p.is_file() and p.name!='00_FUNCTIONAL_CLOSURE_MANIFEST.json'}
(b/'00_FUNCTIONAL_CLOSURE_MANIFEST.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
print('Candidate contracts synchronized and all OpenAPI references resolve; v1.0.14 remains authoritative.')
