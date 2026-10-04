"""Build the approved additive v1.0.15 release; never change v1.0.14."""
from pathlib import Path
import json, shutil, hashlib

root = Path(__file__).resolve().parents[1]
old = root / 'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.14'
new = root / 'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.15'
if new.exists():
    raise SystemExit('Release already exists; review rather than overwrite')
shutil.copytree(old, new)
for p in list(new.rglob('*')):
    if p.is_file() and p.suffix in {'.md', '.csv', '.yaml', '.json', '.html', '.js', '.mjs', '.txt'}:
        data = p.read_text(encoding='utf-8-sig').replace('v1.0.14', 'v1.0.15').replace('V1.0.14', 'V1.0.15')
        p.write_text(data, encoding='utf-8')
        if '1.0.14' in p.name:
            p.rename(p.with_name(p.name.replace('1.0.14', '1.0.15')))
spec = root / 'docs/development/DCP-MES-008-011-FUNCTIONAL-CLOSURE-001-PROPOSED.md'
contract = new / '00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md'
contract.write_text('# Approved functional closure contract\n\nApproval: user explicitly replied “批准方案” on 2026-10-04. DCP-MES-008-011-FUNCTIONAL-CLOSURE-001. The following approved additive contract supersedes missing/generic freeze, IPC and clearance contracts only. Previous releases are immutable.\n\n' + spec.read_text(encoding='utf-8').replace('**PROPOSED / 待明确批准**', '**APPROVED**').replace('本文件不是已批准基线，不授权修改产品代码。', '本累计发布内的契约已经用户明确批准。'), encoding='utf-8')
note = '\n\n## Approved functional closure delta — v1.0.15\n\nDCP-MES-008-011-FUNCTIONAL-CLOSURE-001 approved by the user on 2026-10-04. Mandatory authoritative sections: [00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md) §§3–8. Inventory freezing is independent of QA disposition; IPC producer stage is bounded within MES-012; clearance/IPC Gate share current production-root/Operation locks. No arbitrary status API. This additive contract supersedes older statements that these producers are unavailable. Incoming six-record facts and completionRule grammar remain unchanged.\n'
for p in new.glob('*.md'):
    if p != contract and not p.name.startswith('00_DESIGN_CHANGE'):
        p.write_text(p.read_text(encoding='utf-8') + note, encoding='utf-8')
for task in ['008', '008A', '010', '011', '012']:
    p = new / f'tasks/MES-{task}-R2.md'
    p.write_text(p.read_text(encoding='utf-8') + '\n\n## Approved bounded functional closure\n\nRead ../00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md §§3–8. This approved delta adds inventory decision commands (MES-008), eligibility integration (MES-008A/011), clearance and IPC consumers (MES-010), and only the staged IPC producer (MES-012). Actual physically available upstream contracts satisfy this staged producer dependency; other MES-012 scope remains unchanged.\n', encoding='utf-8')

api_path = new / '08_OPENAPI_FULL_V1.0.15_FROZEN.yaml'
api = json.loads(api_path.read_text(encoding='utf-8'))
schemas = api['components']['schemas']
def string(nullable=False): return {'type':'string', **({'nullable':True} if nullable else {})}
def obj(props, required): return {'type':'object','additionalProperties':False,'properties':props,'required':required}
sig = obj({'reauthToken':string()}, ['reauthToken'])
schemas['ControlledActionSignature'] = sig
common = {'versionNo':{'type':'integer','minimum':0}, 'reason':{'type':'string','minLength':1,'maxLength':1000}, 'signature':{'$ref':'#/components/schemas/ControlledActionSignature'}}
schemas['InventoryDecisionCommand'] = obj(common, list(common))
schemas['IpcCreateCommand'] = obj({'operationExecutionId':string(),'ipcCode':string(),'reason':string()}, ['operationExecutionId','ipcCode','reason'])
schemas['IpcResultCommand'] = obj({**common,'previousRevisionId':string(True),'resultNumeric':string(True),'resultText':string(True)}, list(common))
schemas['IpcReviewCommand'] = obj({**common,'resultRevisionId':string(),'disposition':{'type':'string','enum':['CONFIRMED','INVALIDATED']}}, list(common)+['resultRevisionId','disposition'])
schemas['ClearanceRecordCommand'] = obj({**common,'equipmentUsageId':string(True),'previousRecordId':string(True),'outcome':{'type':'string','enum':['PASS','FAIL']}}, list(common)+['outcome'])
schemas['ClearanceReviewCommand'] = obj({**common,'clearanceRecordId':string(),'decision':{'type':'string','enum':['APPROVED','REJECTED']}},list(common)+['clearanceRecordId','decision'])
schemas['IpcDefinition'] = obj({'ipcCode':string(),'name':string(),'required':{'type':'boolean'},'resultType':{'type':'string','enum':['NUMERIC','TEXT']},'lowerLimit':string(True),'upperLimit':string(True),'expectedText':string(True),'unitId':string(True),'methodReference':string()}, ['ipcCode','name','required','resultType','methodReference'])
for schema in schemas.values():
    if isinstance(schema,dict) and 'clearanceRequired' in schema.get('properties',{}):
        schema['properties']['ipcDefinitions']={'type':'array','items':{'$ref':'#/components/schemas/IpcDefinition'}}
recordprops={k:string(True) for k in ['id','orgId','createdBy','createdAt','updatedBy','updatedAt','operationExecutionId','ipcCode','status','result','completedAt','currentResultRevisionId']}
recordprops.update({'versionNo':{'type':'integer'},'definitionSnapshot':{'$ref':'#/components/schemas/IpcDefinition'},'results':{'type':'array','items':{'$ref':'#/components/schemas/IpcResultRevision'}},'allowedActions':{'type':'array','items':string()}})
schemas['IpcInstance']=obj(recordprops,['id','operationExecutionId','ipcCode','status','versionNo','definitionSnapshot','results','allowedActions'])
schemas['IpcResultRevision']=obj({k:string(True) for k in ['id','ipcInstanceId','revisionNo','previousRevisionId','resultNumeric','resultText','resultConclusion','reasonForChange','recordedBy','recordedAt','signatureId']} | {'definitionSnapshot':{'$ref':'#/components/schemas/IpcDefinition'},'review':{'type':'object','nullable':True,'properties':{k:string(True) for k in ['id','disposition','reason','reviewedBy','reviewedAt','signatureId']}}}, ['id','ipcInstanceId','revisionNo','resultConclusion','definitionSnapshot','signatureId'])
schemas['InventoryDecision']=obj({k:string(True) for k in ['id','orgId','materialLotId','previousDecisionId','action','previousInventoryStatus','resultingInventoryStatus','reason','decidedBy','decidedAt','signatureId','createdBy','createdAt']}, ['id','materialLotId','action','reason','signatureId'])
schemas['ClearanceRecord']=obj({k:string(True) for k in ['id','orgId','operationExecutionId','equipmentUsageId','previousRecordId','outcome','reason','performedBy','performedAt','signatureId','createdBy','createdAt']} | {'review':{'type':'object','nullable':True,'properties':{k:string(True) for k in ['id','decision','reason','reviewedBy','reviewedAt','signatureId']}}}, ['id','operationExecutionId','outcome','reason','signatureId'])
schemas['IpcPage']=obj({'items':{'type':'array','items':{'$ref':'#/components/schemas/IpcInstance'}},'total':{'type':'integer'},'page':{'type':'integer'},'size':{'type':'integer'}},['items','total','page','size'])
for s in schemas.values():
    props=s.get('properties',{}) if isinstance(s,dict) else {}
    if 'inventoryStatus' in props and 'lotNo' in props: props['inventoryDecisions']={'type':'array','items':{'$ref':'#/components/schemas/InventoryDecision'}}
    if 'operationDefId' in props and 'executionUnitId' in props: props['clearanceRecords']={'type':'array','items':{'$ref':'#/components/schemas/ClearanceRecord'}}
def add(path,method,permission,request,response,task):
    params=[]
    if '{id}' in path: params.append({'name':'id','in':'path','required':True,'schema':string()})
    if method=='post': params.append({'$ref':'#/components/parameters/IdempotencyKey'})
    if method=='post' and request!='IpcCreateCommand': params.append({'name':'If-Match','in':'header','required':True,'schema':string()})
    operation={'operationId':path.strip('/').replace('/','_').replace('{id}','id')+'_'+method,'tags':['functional-closure'],'x-permission':permission,'x-mes-task':task,'x-audit-required':method=='post','parameters':params,'responses':{'200':{'description':'OK','content':{'application/json':{'schema':{'$ref':'#/components/schemas/'+response}}}}}}
    if request: operation['requestBody']={'required':True,'content':{'application/json':{'schema':{'$ref':'#/components/schemas/'+request}}}}
    api['paths'].setdefault(path,{})[method]=operation
for action in ['freeze','unfreeze']: add('/material-lots/{id}/'+action,'post','qa:material-inventory:'+action,'InventoryDecisionCommand','MaterialLot' if 'MaterialLot' in schemas else 'InventoryDecision','MES-008-R2')
add('/ipc','get','qms:ipc:view',None,'IpcPage','MES-012-R2')
add('/ipc','post','qms:ipc:record','IpcCreateCommand','IpcInstance','MES-012-R2')
add('/ipc/{id}','get','qms:ipc:view',None,'IpcInstance','MES-012-R2')
add('/ipc/{id}/submit-result','post','qms:ipc:record','IpcResultCommand','IpcInstance','MES-012-R2')
add('/ipc/{id}/review-result','post','qms:ipc:review','IpcReviewCommand','IpcInstance','MES-012-R2')
add('/operations/{id}/record-clearance','post','mes:clearance:record','ClearanceRecordCommand','ClearanceRecord','MES-010-R2')
add('/operations/{id}/review-clearance','post','mes:clearance:review','ClearanceReviewCommand','ClearanceRecord','MES-010-R2')
api_path.write_text(json.dumps(api,ensure_ascii=False,indent=2),encoding='utf-8')
for csv in new.glob('*MATRIX*.csv'):
    # Preserve existing rows; bounded delta rows are in the explicit additive map.
    pass
(new/'00_FUNCTIONAL_CLOSURE_RTM_V1.0.15.csv').write_text('Requirement,Owner,Producer,Consumer,Test,UI\nWMS-ELG-001,MES-008,wms_material_lot_inventory_decision,MES-008A/011,TC-CLOSURE-FREEZE,UI-WMS-LOT-V\nQMS-IPC-001,MES-012 staged IPC,qms_ipc_instance/results/review,MES-010,TC-OP-002/TC-QMS-004,UI-EXEC-W\nMES-OP-001,MES-010,mes_clearance_record/review,START/RESUME,TC-CLOSURE-CLEARANCE,UI-EXEC-W\n',encoding='utf-8')
hashes={str(p.relative_to(new)).replace('\\','/'):hashlib.sha256(p.read_bytes()).hexdigest() for p in new.rglob('*') if p.is_file()}
(new/'00_FUNCTIONAL_CLOSURE_MANIFEST.json').write_text(json.dumps(hashes,indent=2),encoding='utf-8')
print('Created v1.0.15 candidate, approval recorded, previous release unchanged; pointer not switched.')
