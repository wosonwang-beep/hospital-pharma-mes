"""Apply reviewed bounded corrections to the unreleased candidate only."""
from pathlib import Path
import csv,json
root=Path(__file__).resolve().parents[1]
b=root/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16'
p=b/'08_OPENAPI_FULL_V1.0.16_FROZEN.yaml'
a=json.loads(p.read_text(encoding='utf-8'));s=a['components']['schemas']
def ref(n):return {'$ref':'#/components/schemas/'+n}
def arr(x):return {'type':'array','items':x,'maxItems':1000}
def obj(props,required=None):return {'type':'object','additionalProperties':False,'properties':props,'required':list(props) if required is None else required}
ID={'type':'string','pattern':'^[1-9][0-9]*$'}
S={'type':'string','minLength':1,'maxLength':1000}
DEC={'type':'string','pattern':'^-?(?:0|[1-9][0-9]{0,15})(?:\\.[0-9]{1,8})?$'}
QTY={'type':'string','pattern':'^-?(?:0|[1-9][0-9]{0,11})(?:\\.[0-9]{1,6})?$'}
SHA={'type':'string','pattern':'^[0-9a-f]{64}$'}
UTC={'type':'string','format':'date-time','pattern':'^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,3})?Z$'}
def nullable(x):return {**x,'nullable':True}
new=set(json.loads((b/'00_MES_012_013_OPERATIONS.json').read_text(encoding='utf-8'))[0].keys())
# Only new schemas are normalized; inherited schemas are immutable compatibility contracts.
names=['ProductionBalanceDefinition','ProductionPlanCreateCommand','ProductionPlanUpdateCommand','ProductionPlanApproveCommand','ProductionSampleCreateCommand','ProductionSampleReceiveCommand','ProductionTestCreateCommand','ProductionTestResultCommand','ProductionTestReviewCommand','ProductionRetestCommand','ProductionDeviationCreateCommand','ProductionDeviationUpdateCommand','ProductionDeviationInvestigateCommand','ProductionDeviationDecideCommand','ProductionDeviationCloseCommand','CapaCreateCommand','CapaStartCommand','CapaCompleteCommand','CapaVerifyCommand','ProductionQuantityCommand','QuantityReversalCommand','BalanceRecalculateCommand','BalanceInvestigationCreateCommand','BalanceInvestigateCommand','BalanceApproveCommand','FinishedReleaseCommand','EbrPdfCommand','ProductionPlan','ProductionSample','ProductionTest','ProductionResult','ProductionResultReview','ProductionDeviation','ProductionCapa','CapaReview','QuantityFact','BalanceCalculation','BalanceInvestigation','FinishedDecision','EbrPdfManifest']
for name in names:
    for key,val in s[name].get('properties',{}).items():
        maybe=bool(val.get('nullable'))
        replacement=None
        if key.endswith('At'):replacement=UTC
        elif key.endswith(('Hash','Digest')):replacement=SHA
        elif key in ['quantity','resultNumeric','expectedValue','actualValue','differenceValue','differencePct','toleranceLow','toleranceHigh']:replacement=DEC
        elif key=='amount':replacement=QTY
        elif key in ['sampleNo','capaNo','balanceCode','deviationNo']:replacement={**S,'maxLength':80}
        elif key=='sourceRef':replacement={**S,'maxLength':100}
        if replacement is not None:s[name]['properties'][key]=nullable(replacement) if maybe else replacement.copy()
for name in ['ProductionPlan','ProductionSample','ProductionTest','ProductionDeviation','ProductionCapa','BalanceInvestigation']:
    for key in ['versionNo','allowedActions']:
        if key not in s[name]['required']:s[name]['required'].append(key)
s['ProductionTest']['properties']['specificationSnapshot']=ref('SpecificationItemSnapshot')
s['ProductionTest']['required'].append('specificationSnapshot') if 'specificationSnapshot' not in s['ProductionTest']['required'] else None
s['BalanceInputConversion']=obj({'eventId':ID,'sourceUnitId':ID,'targetUnitId':ID,'materialId':nullable(ID),'sourceAmount':QTY,'convertedAmount':DEC,'factor':DEC,'conversionId':nullable(ID)})
s['BalanceInputSnapshot']=obj({'events':arr(ref('QuantityFact')),'conversions':arr(ref('BalanceInputConversion'))})
s['BalanceCalculation']['properties'].update(inputSnapshot=ref('BalanceInputSnapshot'),ruleSnapshot=ref('ProductionBalanceDefinition'))
for key in ['inputSnapshot','ruleSnapshot']:
    if key not in s['BalanceCalculation']['required']:s['BalanceCalculation']['required'].append(key)
s['EbrFormArchive']=obj({'form':ref('FormRuntimeSummary'),'values':arr(ref('RuntimeValueRevision')),'reviews':arr(ref('RuntimeReviewRecord')),'ruleExecutions':arr(ref('RuntimeRuleExecution')),'signatures':arr(ref('SignatureEvidenceEnvelope'))})
s['BatchGenealogyEvidence']=obj({'id':ID,'mainBatchId':ID,'inputMaterialLotId':ID,'chargeId':ID,'outputLotId':nullable(ID),'relationType':S})
s['BatchEbrReadModel']['properties'].update(forms=arr(ref('EbrFormArchive')),processSnapshot=ref('ProcessSnapshot'),operations=arr(ref('Operation')),charges=arr(ref('MaterialCharge')),quantityEvents=arr(ref('QuantityFact')),genealogy=arr(ref('BatchGenealogyEvidence')),signatures=arr(ref('SignatureEvidenceEnvelope')))
s['BatchEbrReadModel']['required']=list(s['BatchEbrReadModel']['properties'])
s['MainBatch']['properties']['finishedLotId']=nullable(ID)
operations=json.loads((b/'00_MES_012_013_OPERATIONS.json').read_text(encoding='utf-8'))
responses={v['response'] for v in operations}|{'ProductionDeviation','ProductionDeviationPage'}
for name in responses:s['Api'+name]=obj({'code':{'type':'string','enum':['OK']},'message':S,'data':ref(name),'traceId':S})
for item in operations:
    op=a['paths'][item['path']][item['method']]
    for code,r in op['responses'].items():
        if code.startswith('2') and 'application/json' in r.get('content',{}):r['content']['application/json']['schema']=ref('Api'+item['response'])
for path,incoming,production in [('/deviations','DeviationPageResponse','ProductionDeviationPage'),('/deviations/{id}','DeviationResponse','ProductionDeviation')]:
    op=a['paths'][path]['get']
    op['responses']['200']['content']['application/json']['schema']={('anyOf' if path=='/deviations' else 'oneOf'):[ref(incoming),ref('Api'+production)]}
    op['x-scope-dispatch']={'INCOMING_MATERIAL':'00_INCOMING_CHAIN_CONTRACT_V1.0.16.md','PRODUCTION':'00_MES_012_013_COMPLETION_CONTRACT_V1.0.16.md'}
    for item in op.get('parameters',[]):
        if item.get('name')=='investigationScope':item.update(schema={'type':'string','enum':['INCOMING_MATERIAL','PRODUCTION']},description='Omitted scope selects PRODUCTION; persisted target scope governs detail/actions.')
for path,method in [('/deviations','post'),('/deviations/{id}','put'),('/deviations/{id}/investigate','post'),('/deviations/{id}/decide','post'),('/deviations/{id}/close','post')]:
    op=a['paths'][path][method]
    for code,r in op['responses'].items():
        if code.startswith('2'):
            r['content']['application/json']['schema']={'oneOf':[ref('DeviationResponse'),ref('ApiProductionDeviation')]}
p.write_text(json.dumps(a,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

supplement='''\n\n## Reviewed exact archive/read-model and constraint supplement\n\nAll JSON API success responses use the existing ApiResponse envelope code/message/data/traceId; exact record schemas are the data branch, not the envelope. Binary PDF download returns application/pdf. Mutable roots always return versionNo and allowedActions. New persisted DECIMAL(24,8) values have at most16 integral/8fraction digits; QuantityEvent DECIMAL(18,6) has12 integral/6fraction digits. DSL constants are separately bounded by evaluator limits. IDs and exact decimals remain strings, hashes lowercase64hex, event/source/sample identifiers respect SQL lengths, and all API timestamps are UTC RFC3339.\n\nBatchEbrReadModel contains process snapshot; every runtime form with immutable field-value revisions, corrections/invalidation/reviews/rule execution/signatures; actual operations, charges, quantity events, genealogy; frozen production QC item snapshots and every original/revised/retest result; balance rule/input/conversion snapshots and signed investigations; deviation/CAPA histories; QA decision chain and manifests. Projections reference those actual source identities. recordDigest is SHA256 of canonical source evidence ONLY: exclude pdfManifests, Attachment/file rows, generatedAt/generatedBy/archive-generation audit/outbox side effects, computed allowedActions and recordDigest itself. A generation must not change its own source digest. Original source audit/signature lineage remains included through actual related identity/evidence; no archive metadata is a new GMP fact. Concurrent source changes are blocked by root locks while model/file/manifest commit; stale caller versions reject.\n'''
c=b/'00_MES_012_013_COMPLETION_CONTRACT_V1.0.16.md'
text=c.read_text(encoding='utf-8')
if 'Reviewed exact archive/read-model and constraint supplement' not in text:c.write_text(text+supplement,encoding='utf-8')
doc=root/'docs/development/mes-012-013-contract-delta.md'
text=doc.read_text(encoding='utf-8')
if 'Reviewed exact archive/read-model and constraint supplement' not in text:doc.write_text(text+supplement,encoding='utf-8')
# Explicit physical/logical ownership and permission registry complement the normative appendix.
def append_once(name,marker,rows):
    p=b/name
    if marker in p.read_text(encoding='utf-8'):return
    with p.open('a',encoding='utf-8',newline='') as f:csv.writer(f,lineterminator='\n').writerows(rows)
append_once('15_MIGRATION_DEPENDENCY_MATRIX_V1.0.16_FROZEN.csv','LG-012-013-COMPLETION',[
 ['LG-012-013-COMPLETION','Approved DCP-MES-012-013-CONTRACT-001','MES-012/MES-013','production_quality_balance_finished_archive','qms_production_plan;production_test_instance/review;qms_capa/review;balance_rule/result/investigation;ebr_pdf_manifest;additive existing columns','Existing physical V001..V024; no cyclic consumer FK','V025 planned after verified highest24; implementation validation pending','APPROVED DESIGN / NOT APPLIED']])
append_once('15_TASK_DEPENDENCY_MATRIX_V1.0.16_FROZEN.csv','MES-012/013 approved completion',[
 ['MES-012/013 approved completion','Approved production quality/balance then finished QA/archive','MES-008A/009/010/011 accepted; MES-013 consumes actual MES-012','MES-007','MES-013','Production QC/CAPA/BalanceResult; signed finished decision/PDF']])
permission_rows=sorted({item['permission'] for item in operations}|{'qms:capa:view','qms:capa:create','qms:capa:update','qms:capa:verify','qms:deviation:view','qms:deviation:create','qms:deviation:update','qms:deviation:investigate','qms:deviation:decide','qms:deviation:close'})
(b/'00_MES_012_013_PERMISSION_MATRIX.csv').write_text('Permission,Owner,SeedAssignment,IndependentActorRule\n'+''.join(f'{code},{"MES-013" if code.startswith(("qa:","ebr:")) else "MES-012"},Existing SYSTEM_ADMIN and existing explicitly matching business roles; absent role requires IAM assignment,Required on approve/review/decide/verify/release\n' for code in permission_rows),encoding='utf-8')
print('Reviewed corrections applied to candidate only; run verifier and independent rereview before authority switch.')
