"""Author approved v1.0.16 candidate. Does not switch authority or alter old releases."""
from pathlib import Path
import csv, hashlib, io, json, shutil

ROOT = Path(__file__).resolve().parents[1]
OLD = ROOT / 'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.15'
NEW = ROOT / 'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16'
if NEW.exists():
    raise SystemExit('Candidate exists: review/edit candidate explicitly, never overwrite it')
old_hashes = {str(p.relative_to(OLD)): hashlib.sha256(p.read_bytes()).hexdigest() for p in OLD.rglob('*') if p.is_file()}
shutil.copytree(OLD, NEW)
for p in list(NEW.rglob('*')):
    if p.is_file() and p.suffix in {'.md', '.csv', '.yaml', '.json', '.html', '.js', '.mjs', '.txt'}:
        s = p.read_text(encoding='utf-8-sig').replace('v1.0.15', 'v1.0.16').replace('V1.0.15', 'V1.0.16')
        p.write_text(s, encoding='utf-8')
        if '1.0.15' in p.name:
            p.rename(p.with_name(p.name.replace('1.0.15', '1.0.16')))
contract = NEW / '00_MES_012_013_COMPLETION_CONTRACT_V1.0.16.md'
contract.write_text((ROOT / 'docs/development/mes-012-013-contract-delta.md').read_text(encoding='utf-8'), encoding='utf-8')
approval = NEW / '00_DESIGN_CHANGE_DCP-MES-012-013-CONTRACT-001_APPROVED.md'
approval.write_text('# Approved DCP-MES-012-013-CONTRACT-001\n\nHuman approval 2026-10-04: “批准该范围，补齐正式契约后依次完成 MES-012、013”. Bounded scope is the recorded proposal and 00_MES_012_013_COMPLETION_CONTRACT_V1.0.16.md. This release remains CANDIDATE until consistency review and authority switch; design publication does not imply runtime PASS or human acceptance. Earlier releases are immutable.\n', encoding='utf-8')
note = '\n\n## Approved MES-012/013 completion — v1.0.16\n\nRead [00_MES_012_013_COMPLETION_CONTRACT_V1.0.16.md](00_MES_012_013_COMPLETION_CONTRACT_V1.0.16.md), DCP-MES-012-013-CONTRACT-001, approved 2026-10-04. It supersedes missing/generic production balance, investigation/CAPA, finished QA and PDF contracts only. BalanceResult remains immutable PASS/FAIL; approval belongs to the separate investigation. Original incoming and IPC contracts stay unchanged. No runtime readiness follows from publication.\n'
for prefix in ['01_', '04_', '05_', '07_', '08_API_', '09_', '10_UI_PAGE_', '11_', '12A_', '12_TEST_CASE_', '15_MODULE_']:
    for p in NEW.glob(prefix + '*.md'):
        p.write_text(p.read_text(encoding='utf-8') + note, encoding='utf-8')
for task in ['012', '013']:
    p = NEW / f'tasks/MES-{task}-R2.md'
    p.write_text(p.read_text(encoding='utf-8') + note.replace('](00_', '](../00_') + '\nRequired supplementary cases: TC-BAL-004 / TC-QMS-004 for MES-012, final complete batch lifecycle for MES-013. Narrow boot/production/execution/WMS/attachment producer-consumer handoffs in §8 are authorized Write Scope. No incoming reimplementation.\n', encoding='utf-8')

api_path = NEW / '08_OPENAPI_FULL_V1.0.16_FROZEN.yaml'
api = json.loads(api_path.read_text(encoding='utf-8'))
schemas = api['components']['schemas']
def ref(n): return {'$ref': '#/components/schemas/' + n}
def obj(props, required=None): return {'type': 'object', 'additionalProperties': False, 'properties': props, 'required': list(props) if required is None else required}
S = {'type': 'string', 'minLength': 1, 'maxLength': 1000}
ID = {'type': 'string', 'pattern': '^[1-9][0-9]*$'}
DEC = {'type': 'string', 'pattern': '^-?(?:0|[1-9][0-9]{0,23})(?:\\.[0-9]{1,8})?$'}
INT = {'type': 'integer', 'minimum': 0}
def enum(*v): return {'type': 'string', 'enum': list(v)}
def nullable(v): return {**v, 'nullable': True}
def arr(v): return {'type': 'array', 'maxItems': 1000, 'items': v}
BASE = {'versionNo': INT, 'reason': S}
SIGNED = {**BASE, 'signature': ref('ControlledActionSignature')}
events = enum('CHARGE', 'ISSUE', 'RETURN', 'OUTPUT', 'SAMPLE', 'LOSS', 'SCRAP', 'WIP')
schemas['BalanceExpression'] = {'oneOf': [obj({'sum': arr(events)}), obj({'constant': DEC}), obj({'op': enum('ADD', 'SUBTRACT', 'MULTIPLY', 'DIVIDE'), 'left': ref('BalanceExpression'), 'right': ref('BalanceExpression')})]}
schemas['BalanceFormula'] = obj({'dslVersion': {'type': 'integer', 'enum': [1]}, 'expected': ref('BalanceExpression'), 'actual': ref('BalanceExpression'), 'metric': enum('DIFFERENCE_PCT', 'YIELD_PCT')})
schemas['ProductionBalanceDefinition'] = obj({'balanceCode': S, 'basis': enum('BATCH', 'OPERATION', 'PACKAGING'), 'operationCode': nullable(S), 'materialId': nullable(ID), 'unitId': ID, 'formulaExpr': ref('BalanceFormula'), 'toleranceLow': DEC, 'toleranceHigh': DEC, 'checkPoint': enum('OPERATION_COMPLETE', 'BATCH_COMPLETE', 'QA_RELEASE')}, ['balanceCode','basis','unitId','formulaExpr','toleranceLow','toleranceHigh','checkPoint'])
plan = {'mainBatchId': ID, 'finishedMaterialId': ID, 'qcSpecificationVersionId': ID, 'balanceRules': {**arr(ref('ProductionBalanceDefinition')), 'minItems': 1}, 'reason': S}
commands = {
 'ProductionPlanCreateCommand': obj(plan),
 'ProductionPlanUpdateCommand': obj({**plan, 'versionNo': INT}),
 'ProductionPlanApproveCommand': obj(SIGNED),
 'ProductionSampleCreateCommand': obj({'mainBatchId': ID, 'sampleNo': S, 'sampleType': enum('TEST_SAMPLE','RETENTION_SAMPLE','RETEST_SAMPLE','OTHER_APPROVED'), 'materialLotId': nullable(ID), 'quantity': DEC, 'unitId': ID, 'sourceRef': S, 'reason': S, 'investigationScope': enum('PRODUCTION')}, ['mainBatchId','sampleNo','sampleType','quantity','unitId','sourceRef','reason','investigationScope']),
 'ProductionSampleReceiveCommand': obj(BASE),
 'ProductionTestCreateCommand': obj({'sampleId': ID, 'specificationItemId': ID, 'instrumentId': nullable(ID), 'reason': S}, ['sampleId','specificationItemId','reason']),
 'ProductionTestResultCommand': obj({**SIGNED,'previousRevisionId':nullable(ID),'resultNumeric':nullable(DEC),'resultText':nullable(S)}, list(SIGNED)),
 'ProductionTestReviewCommand': obj({**SIGNED,'resultRevisionId':ID,'disposition':enum('CONFIRMED','INVALIDATED')}),
 'ProductionRetestCommand': obj({**BASE,'investigationId':ID}),
 'ProductionDeviationCreateCommand': obj({'investigationScope':enum('PRODUCTION'),'mainBatchId':ID,'operationExecutionId':nullable(ID),'investigationKind':enum('DEVIATION','OOS','OOT'),'severity':enum('MINOR','MAJOR','CRITICAL'),'description':S,'productionTestInstanceId':nullable(ID),'originalResultRevisionId':nullable(ID),'reason':S}, ['investigationScope','mainBatchId','investigationKind','severity','description','reason']),
 'ProductionDeviationUpdateCommand': obj({**BASE,'investigationScope':enum('PRODUCTION'),'description':S}),
 'ProductionDeviationInvestigateCommand': obj({**BASE,'investigationScope':enum('PRODUCTION'),'investigationSummary':S}),
 'ProductionDeviationDecideCommand': obj({**SIGNED,'investigationScope':enum('PRODUCTION'),'decisionCode':enum('REJECT','AUTHORIZE_RETEST','ACCEPT_WITH_JUSTIFICATION'),'disposition':S,'authorizedRetestCount':nullable({'type':'integer','minimum':1,'maximum':10}),'originalResultDisposition':nullable(enum('VALID','INVALID'))}, list(SIGNED)+['investigationScope','decisionCode','disposition']),
 'ProductionDeviationCloseCommand': obj({**SIGNED,'investigationScope':enum('PRODUCTION'),'selectedResultRevisionId':nullable(ID)},list(SIGNED)+['investigationScope']),
 'CapaCreateCommand': obj({'capaNo':S,'actionText':S,'reason':S}),
 'CapaStartCommand': obj(BASE), 'CapaCompleteCommand': obj({**BASE,'completionText':S}),
 'CapaVerifyCommand': obj({**SIGNED,'decision':enum('VERIFIED','REOPEN')}),
 'ProductionQuantityCommand': obj({'versionNo':INT,'eventType':enum('OUTPUT','SAMPLE','LOSS','SCRAP','WIP'),'operationExecutionId':nullable(ID),'materialLotId':nullable(ID),'amount':DEC,'unitId':ID,'sourceRef':S,'reason':S,'signature':ref('ControlledActionSignature'),'lotNo':nullable(S),'locationId':nullable(ID),'productionDate':nullable({'type':'string','format':'date'}),'expiryDate':nullable({'type':'string','format':'date'})},['versionNo','eventType','amount','unitId','sourceRef','reason','signature']),
 'QuantityReversalCommand': obj(SIGNED),
 'BalanceRecalculateCommand': obj(BASE),
 'BalanceInvestigationCreateCommand': obj({**BASE,'investigationText':S}),
 'BalanceInvestigateCommand': obj({**BASE,'investigationText':S}),
 'BalanceApproveCommand': obj({**SIGNED,'decision':enum('ACCEPT_EXCEPTION','REQUIRE_RECALCULATION')}),
 'FinishedReleaseCommand': obj({**SIGNED,'mainBatchId':ID,'finishedLotId':ID,'decision':enum('RELEASED','REJECTED'),'releaseBasis':enum('FULL_INSPECTION'),'reviewDigest':{'type':'string','pattern':'^[0-9a-f]{64}$'},'supersedesDecisionId':nullable(ID)},list(SIGNED)+['mainBatchId','finishedLotId','decision','releaseBasis','reviewDigest']),
 'EbrPdfCommand': obj({'versionNo':INT,'archiveKind':enum('REVIEW_COPY','FINAL'),'reason':S})
}
schemas.update(commands)
# Structured record projections: exact persisted facts, closed top-level, explicit nested frozen evidence.
record_props = {
 'ProductionPlan': {'mainBatchId':ID,'finishedMaterialId':ID,'qcSpecificationVersionId':ID,'balanceRules':arr(ref('ProductionBalanceDefinition')),'status':enum('DRAFT','APPROVED'),'contentHash':nullable(S),'approvedBy':nullable(ID),'approvedAt':nullable({'type':'string','format':'date-time'}),'signatureId':nullable(ID)},
 'ProductionSample': {'mainBatchId':ID,'sampleNo':S,'sampleScope':enum('PRODUCTION'),'sampleType':S,'status':S,'quantity':DEC,'unitId':ID,'productionPlanId':ID,'qcSpecificationVersionId':ID,'sourceRef':S,'materialLotId':nullable(ID),'sampledAt':nullable(S),'receivedBy':nullable(ID),'receivedAt':nullable(S)},
 'ProductionTest': {'sampleId':ID,'productionPlanId':ID,'qcSpecificationItemId':ID,'testCode':S,'attemptNo':INT,'previousInstanceId':nullable(ID),'approvedInvestigationId':nullable(ID),'status':enum('READY','TESTING','COMPLETED'),'currentResultRevisionId':nullable(ID),'instrumentId':nullable(ID),'performedBy':nullable(ID),'results':arr(ref('ProductionResult'))},
 'ProductionResult': {'sampleId':ID,'productionTestInstanceId':ID,'testCode':S,'revisionNo':INT,'previousRevisionId':nullable(ID),'resultNumeric':nullable(DEC),'resultText':nullable(S),'resultConclusion':enum('PASS','FAIL'),'recordedBy':ID,'recordedAt':S,'signatureId':ID,'review':nullable(ref('ProductionResultReview'))},
 'ProductionResultReview': {'resultRevisionId':ID,'disposition':enum('CONFIRMED','INVALIDATED'),'reason':S,'reviewedBy':ID,'reviewedAt':S,'signatureId':ID},
 'ProductionDeviation': {'deviationNo':S,'investigationScope':enum('PRODUCTION'),'mainBatchId':ID,'operationExecutionId':nullable(ID),'investigationKind':enum('DEVIATION','OOS','OOT'),'severity':enum('MINOR','MAJOR','CRITICAL'),'status':enum('OPEN','INVESTIGATING','DECIDED','CLOSED'),'description':S,'investigationSummary':nullable(S),'disposition':nullable(S),'decisionCode':nullable(S),'productionTestInstanceId':nullable(ID),'originalResultRevisionId':nullable(ID),'decisionSignatureId':nullable(ID),'closeSignatureId':nullable(ID),'capas':arr(ref('ProductionCapa'))},
 'ProductionCapa': {'capaNo':S,'sourceType':enum('PRODUCTION_DEVIATION'),'sourceId':ID,'status':enum('OPEN','IN_PROGRESS','COMPLETED','VERIFIED'),'actionText':S,'completionText':nullable(S),'completedBy':nullable(ID),'completedAt':nullable(S),'reviews':arr(ref('CapaReview'))},
 'CapaReview': {'capaId':ID,'reviewNo':INT,'decision':enum('VERIFIED','REOPEN'),'reason':S,'reviewedBy':ID,'reviewedAt':S,'signatureId':ID},
 'QuantityFact': {'mainBatchId':ID,'executionUnitId':nullable(ID),'operationExecutionId':nullable(ID),'eventType':S,'materialLotId':nullable(ID),'amount':DEC,'unitId':ID,'sourceType':S,'sourceRef':S,'occurredAt':S,'reversalOfId':nullable(ID),'reason':nullable(S),'signatureId':nullable(ID)},
 'BalanceCalculation': {'mainBatchId':ID,'balanceRuleId':ID,'calculationVersion':INT,'expectedValue':DEC,'actualValue':DEC,'differenceValue':DEC,'differencePct':DEC,'status':enum('PASS','FAIL'),'calculatedAt':S,'calculatedBy':ID,'inputDigest':S,'investigation':nullable(ref('BalanceInvestigation'))},
 'BalanceInvestigation': {'balanceResultId':ID,'status':enum('OPEN','INVESTIGATING','APPROVED_EXCEPTION','RECALCULATE_REQUIRED'),'investigationText':S,'decision':nullable(S),'inputDigest':S,'investigatedBy':nullable(ID),'approvedBy':nullable(ID),'approvedAt':nullable(S),'signatureId':nullable(ID)},
 'FinishedDecision': {'mainBatchId':ID,'finishedLotId':ID,'releaseScope':enum('FINISHED_PRODUCT'),'decision':enum('RELEASED','REJECTED'),'releaseBasis':enum('FULL_INSPECTION'),'decisionBy':ID,'decisionAt':S,'reason':S,'signatureId':ID,'supersedesDecisionId':nullable(ID)},
 'EbrPdfManifest': {'mainBatchId':ID,'generationVersion':INT,'definitionHash':S,'recordDigest':S,'fileId':ID,'fileHash':S,'generatedBy':ID,'generatedAt':S,'archiveKind':enum('REVIEW_COPY','FINAL'),'releaseDecisionId':nullable(ID)}
}
meta={'id':ID,'orgId':ID,'createdBy':ID,'createdAt':S,'updatedBy':nullable(ID),'updatedAt':nullable(S),'versionNo':INT,'allowedActions':arr(S)}
for name, props in record_props.items():
    schemas[name]=obj({**meta,**props},['id','orgId']+list(props))
    schemas[name+'Page']=obj({'items':arr(ref(name)),'total':INT,'page':INT,'size':INT})
schemas['BalanceView']=obj({'mainBatchId':ID,'results':arr(ref('BalanceCalculation')),'blockingCodes':arr(S),'allowedActions':arr(S)})
schemas['FinishedGate']=obj({'code':S,'passed':{'type':'boolean'},'blockingCodes':arr(S),'evidenceIds':arr(ID)})
schemas['FinishedReviewModel']=obj({'mainBatchId':ID,'versionNo':INT,'batchNo':S,'status':S,'finishedLots':arr(ID),'gates':arr(ref('FinishedGate')),'blockingCodes':arr(S),'reviewDigest':S,'allowedActions':arr(S)})
# eBR aggregation references existing frozen models; does not permit another editable data set.
schemas['BatchEbrReadModel']=obj({'mainBatchId':ID,'definitionHash':S,'recordDigest':S,'batch':ref('MainBatch'),'forms':arr(ref('FormRuntimeSummary')),'qcTests':arr(ref('ProductionTest')),'balances':arr(ref('BalanceCalculation')),'deviations':arr(ref('ProductionDeviation')),'decisions':arr(ref('FinishedDecision')),'pdfManifests':arr(ref('EbrPdfManifest'))})

operations=[]
def add(path,method,permission,request,response,task='MES-012-R2',create=False):
    params=[{'name':n,'in':'path','required':True,'schema':ID} for n in ['id','manifestId'] if '{'+n+'}' in path]
    if method=='get' and response.endswith('Page'):
        params += [{'name':n,'in':'query','schema':({'type':'integer','minimum':0,'default':0} if n=='page' else {'type':'integer','minimum':1,'maximum':100,'default':20})} for n in ['page','size']]
        params += [{'name':n,'in':'query','schema':S} for n in ['mainBatchId','status','keyword']]
    if request:
        params.append({'$ref':'#/components/parameters/IdempotencyKey'})
        if 'versionNo' in schemas[request]['properties']:
            params.append({'name':'If-Match','in':'header','required':True,'schema':{'type':'string','pattern':'^"[0-9]+"$'}})
    op={'operationId':(path.strip('/')+'_'+method).replace('/','_').replace('{','').replace('}','').replace('-','_'),'tags':['production-quality' if task=='MES-012-R2' else 'finished-release'],'x-permission':permission,'x-mes-task':task,'x-audit-required':bool(request),'parameters':params,'responses':{str(201 if create else 200):{'description':'Success','content':{'application/json':{'schema':ref(response)}}},**{str(code):{'description':meaning,'content':{'application/json':{'schema':ref('ApiError')}}} for code,meaning in [(400,'Input'),(401,'Authentication'),(403,'Permission'),(404,'Scoped missing'),(409,'Conflict'),(422,'Business gate')]}}}
    if request:op['requestBody']={'required':True,'content':{'application/json':{'schema':ref(request)}}}
    api['paths'].setdefault(path,{})[method]=op
    operations.append({'path':path,'method':method,'permission':permission,'request':request,'response':response,'task':task,'create':create})
for name,base_path,kind in [('ProductionPlan','/quality/production-plans','plan'),('ProductionSample','/samples','sample'),('ProductionTest','/quality/production-tests','test')]:
    req={'ProductionPlan':'ProductionPlanCreateCommand','ProductionSample':'ProductionSampleCreateCommand','ProductionTest':'ProductionTestCreateCommand'}[name]
    add(base_path,'get','qms:'+kind+':view',None,name+'Page')
    add(base_path,'post','qms:'+kind+(':record' if kind=='test' else ':create'),req,name,create=True)
    add(base_path+'/{id}','get','qms:'+kind+':view',None,name)
add('/quality/production-plans/{id}','put','qms:plan:update','ProductionPlanUpdateCommand','ProductionPlan')
add('/quality/production-plans/{id}/approve','post','qms:plan:approve','ProductionPlanApproveCommand','ProductionPlan')
add('/samples/{id}/receive','post','qms:sample:receive','ProductionSampleReceiveCommand','ProductionSample')
for suffix,perm,req in [('results','record','ProductionTestResultCommand'),('revisions','record','ProductionTestResultCommand'),('review','review','ProductionTestReviewCommand'),('retest','retest','ProductionRetestCommand')]:
    add('/quality/production-tests/{id}/'+suffix,'post','qms:test:'+perm,req,'ProductionTest',create=suffix in ['results','revisions','retest'])
# Incoming deviation routes are retained, with a closed disjoint PRODUCTION branch.
for path,method,req in [('/deviations','post','ProductionDeviationCreateCommand'),('/deviations/{id}','put','ProductionDeviationUpdateCommand'),('/deviations/{id}/investigate','post','ProductionDeviationInvestigateCommand'),('/deviations/{id}/decide','post','ProductionDeviationDecideCommand'),('/deviations/{id}/close','post','ProductionDeviationCloseCommand')]:
    op=api['paths'][path][method]; old=op['requestBody']['content']['application/json']['schema']
    incoming=next((b for b in old.get('anyOf',[]) if '$ref' in b and b['$ref'].endswith(('IncomingDeviationCommand','UpdateDeviationCommand','InvestigateCommand','DecideDeviationCommand','CloseDeviationCommand'))),None)
    if not incoming:raise RuntimeError('Incoming schema missing: '+path)
    op['requestBody']['content']['application/json']['schema']={'oneOf':[incoming,ref(req)]}
    op['x-scope-dispatch']['PRODUCTION']='00_MES_012_013_COMPLETION_CONTRACT_V1.0.16.md'
    for status,r in op['responses'].items():
        schema=r.get('content',{}).get('application/json',{}).get('schema',{})
        if status.startswith('2') and 'anyOf' in schema:
            schema['anyOf']=[b for b in schema['anyOf'] if not b.get('$ref','').endswith('GenericResponse')]+[ref('ProductionDeviation')]
for suffix,perm,request in [('start','update','CapaStartCommand'),('complete','update','CapaCompleteCommand'),('verify','verify','CapaVerifyCommand')]:add('/capas/{id}/'+suffix,'post','qms:capa:'+perm,request,'ProductionCapa')
add('/deviations/{id}/capas','get','qms:capa:view',None,'ProductionCapaPage')
add('/deviations/{id}/capas','post','qms:capa:create','CapaCreateCommand','ProductionCapa',create=True)
add('/main-batches/{id}/quantity-events','get','balance:view',None,'QuantityFactPage')
add('/main-batches/{id}/quantity-events','post','mes:quantity:record','ProductionQuantityCommand','QuantityFact',create=True)
add('/quantity-events/{id}/reverse','post','mes:quantity:reverse','QuantityReversalCommand','QuantityFact',create=True)
add('/main-batches/{id}/material-balance','get','balance:view',None,'BalanceView')
add('/main-batches/{id}/material-balance/recalculate','post','balance:view','BalanceRecalculateCommand','BalanceView')
add('/balances/{id}/investigations','post','balance:investigate','BalanceInvestigationCreateCommand','BalanceInvestigation',create=True)
add('/balance-investigations/{id}/investigate','post','balance:investigate','BalanceInvestigateCommand','BalanceInvestigation')
add('/balance-investigations/{id}/approve','post','balance:approve','BalanceApproveCommand','BalanceInvestigation')
for path,method,perm,req,res in [('/qa/batches/{id}/review-model','get','qa:batch-review',None,'FinishedReviewModel'),('/release-decisions','post','qa:release','FinishedReleaseCommand','FinishedDecision'),('/release-decisions/{id}','get','qa:batch-review',None,'FinishedDecision'),('/main-batches/{id}/ebr','get','ebr:form:view',None,'BatchEbrReadModel'),('/main-batches/{id}/ebr/pdf','post','ebr:pdf:generate','EbrPdfCommand','EbrPdfManifest')]:add(path,method,perm,req,res,'MES-013-R2',create=path=='/release-decisions')
add('/main-batches/{id}/ebr/pdf/{manifestId}','get','ebr:form:view',None,'EbrPdfManifest','MES-013-R2')
api['paths']['/main-batches/{id}/ebr/pdf/{manifestId}']['get']['responses']['200']['content']={'application/pdf':{'schema':{'type':'string','format':'binary'}}}
api_path.write_text(json.dumps(api,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
(NEW/'00_MES_012_013_OPERATIONS.json').write_text(json.dumps(operations,ensure_ascii=False,indent=2),encoding='utf-8')

def append_csv(name, rows):
    p=NEW/name
    with p.open('a',encoding='utf-8',newline='') as f:csv.writer(f,lineterminator='\n').writerows(rows)
append_csv('10_UI_PAGE_ROUTE_MATRIX_V1.0.16_FROZEN.csv',[[f'UI-P{kind}-{suffix}',title,mode,path,req,perm] for kind,title,base_path,req in [('PLAN','生产质量计划','/quality/production-plans','QMS-IPC-001'),('TEST','生产检验','/quality/production-tests','QMS-001')] for suffix,mode,path,perm in [('Q','QUERY',base_path,'qms:'+('plan' if kind=='PLAN' else 'test')+':view'),('C','CREATE',base_path+'/create','qms:'+('plan:create' if kind=='PLAN' else 'test:record')),('V','VIEW',base_path+'/:id','qms:'+('plan' if kind=='PLAN' else 'test')+':view')]]+[['UI-PPLAN-E','生产质量计划','EDIT','/quality/production-plans/:id/edit','QMS-IPC-001','qms:plan:update']])
append_csv('15_INTEGRATION_CONTRACT_MATRIX_V1.0.16_FROZEN.csv',[
 ['QMS plan','Frozen production quality plan','Batch dispatch / balance / finished QC','qms_production_plan + prd_process_snapshot','Same dispatch transaction','Missing plan blocks later completion; no invented evidence','TC-BAL-001..004;TC-QMS-001..004'],
 ['QMS balance','Current input digest / exception','Operation / batch close / QA','mes_balance_result / mes_balance_investigation','Current shared batch root locks','Missing/stale/FAIL without signed exception blocks','TC-BAL-002..003;TC-REL-003'],
 ['Finished QA','FINISHED_PRODUCT effective decision','MainBatch / stock / archive','qms_release_decision','Decision + signature + inventory + audit + outbox','Rollback all effects / competing successor rejected','TC-REL-001..004;TC-EBR-010']])
append_csv('13_REQUIREMENT_TRACEABILITY_MATRIX_V1.0.16_FROZEN.csv',[
 ['BAL-001..003','Approved material-balance completion','mes_quantity_event;mes_balance_rule;mes_balance_result;mes_balance_investigation','/main-batches/{id}/material-balance;/balances/{id}/investigations','UI-BAL-V','MES-012-R2','TC-BAL-001..004','FROZEN'],
 ['QMS-IPC-001;QMS-001;QMS-DEV-001','Approved production QC/investigation/CAPA','qms_production_plan;qms_sample;qms_production_test_instance;qms_test_result_revision;qms_deviation;qms_capa','/samples;/quality/production-tests;/deviations;/capas','Production quality + existing deviation/IPC UI','MES-012-R2','TC-QMS-001..004','FROZEN'],
 ['REL-001;EBR-008','Approved finished QA / archive','qms_release_decision;ebr_pdf_manifest','/release-decisions;/main-batches/{id}/ebr/pdf','UI-QA-RELEASE','MES-013-R2','TC-REL-001..004;TC-EBR-010','FROZEN']])

def references(node):
    if isinstance(node,dict):
        if '$ref' in node:yield node['$ref']
        for v in node.values():yield from references(v)
    elif isinstance(node,list):
        for v in node:yield from references(v)
missing=[]
for r in references(api):
    if r.startswith('#/'):
        n=api
        try:
            for part in r[2:].split('/'):n=n[part.replace('~1','/').replace('~0','~')]
        except KeyError:missing.append(r)
old_changed=[name for name,h in old_hashes.items() if hashlib.sha256((OLD/name).read_bytes()).hexdigest()!=h]
review={'status':'CANDIDATE / NOT SWITCHED','oldReleaseFiles':len(old_hashes),'oldReleaseChanged':old_changed,'unresolvedOpenapiRefs':sorted(set(missing)),'operationsAddedOrTyped':len(operations),'approval':'2026-10-04 explicit bounded DCP approval','runtimeAcceptance':'NOT CLAIMED'}
(NEW/'00_MES_012_013_CONSISTENCY_REVIEW.json').write_text(json.dumps(review,indent=2),encoding='utf-8')
manifest={str(p.relative_to(NEW)).replace('\\','/'):hashlib.sha256(p.read_bytes()).hexdigest() for p in NEW.rglob('*') if p.is_file() and not p.name.endswith('_MANIFEST.json')}
(NEW/'00_MES_012_013_MANIFEST.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
print(json.dumps(review,ensure_ascii=False))
if missing or old_changed:raise SystemExit(1)
