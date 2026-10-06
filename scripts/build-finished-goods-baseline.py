"""Approved finished-goods candidate builder; parent/pointers are never edited."""
from pathlib import Path
import copy, csv, hashlib, json, shutil
ROOT=Path(__file__).resolve().parents[1]
OLD=ROOT/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.19'
NEW=ROOT/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.20'
if NEW.exists(): raise SystemExit('Candidate exists: refuse to overwrite frozen content')
hashes={p.relative_to(OLD).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in OLD.rglob('*') if p.is_file()}
shutil.copytree(OLD,NEW)
for p in list(NEW.rglob('*')):
 if p.is_file() and p.suffix in {'.md','.csv','.yaml','.json','.html','.js','.mjs','.txt'}:
  p.write_text(p.read_text(encoding='utf-8-sig').replace('v1.0.19','v1.0.20').replace('V1.0.19','V1.0.20'),encoding='utf-8')
  if '1.0.19' in p.name:p.rename(p.with_name(p.name.replace('1.0.19','1.0.20')))
def save(name,value): (NEW/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
contract=(ROOT/'docs/development/DCP-FINISHED-GOODS-CHAIN-001-APPROVED.md').read_text(encoding='utf-8')
for name in ['00_FINISHED_GOODS_CONTRACT_V1.0.20.md','00_DESIGN_CHANGE_DCP-FINISHED-GOODS-CHAIN-001_APPROVED.md']:(NEW/name).write_text(contract,encoding='utf-8')
save('00_FINISHED_GOODS_PARENT_HASHES.json',hashes)
note='\n\n## Approved finished-goods delta — v1.0.20\n\n[00_FINISHED_GOODS_CONTRACT_V1.0.20.md](00_FINISHED_GOODS_CONTRACT_V1.0.20.md) controls the approved finished chain. It supersedes inherited wording only for new OUTPUT stock effects (identity/quantity only), independent warehouse receipt, post-completion finished request/sampling/report, two additional QA gates, signed shipment and finished trace. Existing incoming quality, historical OUTPUT ledgers, immutable original results/OOS/retest, independent signed QA and post-decision FINAL PDF remain authoritative. V030/V031 append-only. UI V2/T1–T6 unchanged. Inherited embedded manifests/reviews attest prior deltas; current finished-goods manifest governs cumulative bytes. Acceptance/readiness remains in MES_TASKS.md.\n'
for p in NEW.glob('*.md'):
 if p.name[:2] in ['01','02','03','04','05','06','07','08','09','10','11','12','13','14','15'] or 'MANIFEST_FINAL' in p.name or p.name=='README.md':p.write_text(p.read_text(encoding='utf-8')+note,encoding='utf-8')
for task in ['008','008A','009','010','011','012','013']:
 for p in (NEW/'tasks').glob('MES-'+task+'*.md'):p.write_text(p.read_text(encoding='utf-8')+note.replace('](00_','](../00_'),encoding='utf-8')
api=json.loads((NEW/'08_OPENAPI_FULL_V1.0.20_FROZEN.yaml').read_text(encoding='utf-8'));api['info']['version']='1.0.20';sc=api['components']['schemas']
def ref(n):return {'$ref':'#/components/schemas/'+n}
def obj(p,required=None):return {'type':'object','additionalProperties':False,'properties':p,'required':list(p) if required is None else required}
def arr(x):return {'type':'array','items':x}
def txt(n=1000):return {'type':'string','maxLength':n}
ID={'type':'string','pattern':'^[1-9][0-9]*$'};NID={**ID,'nullable':True};INT={'type':'integer','minimum':0};TIME={'type':'string','format':'date-time','nullable':True};QTY={'type':'string','pattern':r'^(?:0|[1-9][0-9]{0,11})(?:\.[0-9]{1,6})?$'}
sc['FinishedSourceFacts']={'type':'object','additionalProperties':True,'description':'Immutable source JSON: actual approved plan/output/Sample/TestResult/review/investigation. Read-only frozen content, never writable command input.'}
base={'id':ID,'orgId':ID,'createdBy':ID,'createdAt':TIME,'updatedBy':ID,'updatedAt':TIME,'versionNo':INT,'allowedActions':arr(txt(80))}
def states(*values):return {'type':'string','enum':list(values)}
fields={
 'FinishedInboundRequest':{'requestNo':txt(80),'mainBatchId':ID,'materialLotId':ID,'productId':ID,'quantity':QTY,'unitId':ID,'status':states('DRAFT','SUBMITTED','CONFIRMED','CANCELLED'),'sourceSnapshot':ref('FinishedSourceFacts'),'sourceDigest':txt(64),'submittedBy':NID,'submittedAt':TIME,'confirmedBy':NID,'confirmedAt':TIME,'locationId':NID,'ledgerId':NID,'signatureId':NID,'reason':txt(),'batch':ref('FinishedSourceFacts'),'lot':ref('FinishedSourceFacts')},
 'FinishedSamplingRecord':{'inspectionRequestId':ID,'sampleId':ID,'materialLotId':ID,'samplingNo':txt(80),'samplingLocation':txt(200),'quantity':QTY,'unitId':ID,'samplingMethod':txt(),'sampledBy':ID,'sampledAt':TIME,'signatureId':ID,'reason':txt(),'sample':ref('ProductionSample')},
 'FinishedReportReview':{'reportId':ID,'reviewDecision':states('APPROVE'),'reviewedBy':ID,'reviewedAt':TIME,'signatureId':ID,'reason':txt()},
 'FinishedInspectionReport':{'reportNo':txt(80),'inspectionRequestId':ID,'generationNo':INT,'overallConclusion':states('PASS','FAIL'),'status':states('GENERATED','APPROVED'),'evidenceDigest':txt(64),'summary':ref('FinishedSourceFacts'),'generatedBy':ID,'generatedAt':TIME,'approvedBy':NID,'approvedAt':TIME,'signatureId':NID,'reason':txt(),'reviews':arr(ref('FinishedReportReview'))},
 'FinishedInspectionRequest':{'inspectionRequestNo':txt(80),'mainBatchId':ID,'materialLotId':ID,'inboundRequestId':ID,'qcSpecificationVersionId':ID,'specificationSnapshot':ref('FinishedSourceFacts'),'specificationDigest':txt(64),'status':states('DRAFT','SUBMITTED','ACCEPTED','COMPLETED'),'submittedBy':NID,'submittedAt':TIME,'acceptedBy':NID,'acceptedAt':TIME,'reason':txt(),'batch':ref('FinishedSourceFacts'),'samplingRecords':arr(ref('FinishedSamplingRecord')),'reports':arr(ref('FinishedInspectionReport'))},
 'FinishedShipment':{'shipmentNo':txt(80),'mainBatchId':ID,'materialLotId':ID,'locationId':ID,'quantity':QTY,'unitId':ID,'receivingParty':txt(200),'status':states('DRAFT','CONFIRMED','CANCELLED'),'releaseDecisionId':NID,'ledgerId':NID,'confirmedBy':NID,'confirmedAt':TIME,'signatureId':NID,'reason':txt(),'batch':ref('FinishedSourceFacts'),'lot':ref('FinishedSourceFacts')}}
for name,props in fields.items():
 sc[name]=obj({**base,**props},['id','orgId','versionNo']+[k for k in props if k not in ['sample','batch','lot']])
 if name=='FinishedReportReview':sc[name]['properties'].pop('allowedActions')
 sc[name+'Page']=obj({'items':arr(ref(name)),'total':INT,'page':INT,'size':INT})
 for suffix in ['','Page']:sc['Api'+name+suffix]=obj({'code':txt(),'message':txt(),'data':ref(name+suffix),'traceId':txt()})
for name in ['FinishedInboundRequest','FinishedSamplingRecord','FinishedInspectionRequest','FinishedInspectionReport','FinishedShipment']:
 source=json.loads(json.dumps(sc[name]))
 for k in ['allowedActions','batch','lot','sample']:source['properties'].pop(k,None)
 if name=='FinishedInspectionRequest':source['properties']['samplingRecords']=arr(ref('FinishedSamplingRecordEvidence'));source['properties']['reports']=arr(ref('FinishedInspectionReportEvidence'))
 sc[name+'Evidence']=source
sc['FinishedQualityEvidence']=obj({'requests':arr(ref('FinishedInspectionRequestEvidence'))})
sc['FinishedDecision']['properties']['finishedInspectionReportId']=NID
sc['BatchEbrReadModel']['properties'].update(finishedQuality=ref('FinishedQualityEvidence'),finishedReceiving=arr(ref('FinishedInboundRequestEvidence')))
sc['BatchEbrReadModel']['required']+=['finishedQuality','finishedReceiving']
sc['FinishedMaterialLot']=copy.deepcopy(sc['MaterialLot'])
for k in ['supplierLotNo','receiptItemId','retestDate']:sc['FinishedMaterialLot']['properties'][k]['nullable']=True
sc['FinishedMaterialLot']['properties']['sourceSnapshot']={'$ref':'#/components/schemas/ReceiptSourceSnapshot','readOnly':True,'nullable':True}
for name in ['FinishedInboundRequest','FinishedShipment']:
 if 'lot' in sc[name]['properties']:sc[name]['properties']['lot']=ref('FinishedMaterialLot')
for key in ['samplingRecords','reports']:sc['FinishedInspectionRequest']['required'].remove(key)
sc['FinishedLotChain']=obj({'mainBatchId':ID,'lot':ref('FinishedMaterialLot'),'access':{'type':'object','additionalProperties':states('AVAILABLE','NOT_AUTHORIZED')},'production':ref('FinishedSourceFacts'),'receiving':arr(ref('FinishedInboundRequestEvidence')),'finishedQuality':ref('FinishedQualityEvidence'),'rawQuality':ref('FinishedSourceFacts'),'decisions':arr(ref('FinishedSourceFacts')),'shipments':arr(ref('FinishedShipmentEvidence')),'ledger':arr(ref('FinishedSourceFacts'))},['mainBatchId','lot','access'])
sc['ApiFinishedLotChain']=obj({'code':txt(),'message':txt(),'data':ref('FinishedLotChain'),'traceId':txt()})
sc['FinishedSignatureInput']=obj({'reauthToken':txt(2000)})
reason={'reason':txt()};version={'versionNo':INT,**reason};signature=ref('FinishedSignatureInput')
commands={'FinishedInboundCreate':{'requestNo':txt(80),'mainBatchId':ID,**reason},'FinishedInspectionCreate':{'inspectionRequestNo':txt(80),'inboundRequestId':ID,**reason},'FinishedShipmentCreate':{'shipmentNo':txt(80),'mainBatchId':ID,'locationId':ID,'quantity':QTY,'unitId':ID,'receivingParty':txt(200),**reason},'FinishedReasonCommand':version,'FinishedReceiptConfirm':{**version,'locationId':ID,'signature':signature},'FinishedSamplingCommand':{**version,'samplingNo':txt(80),'sampleNo':txt(80),'sampleType':states('TEST_SAMPLE','RETENTION_SAMPLE','RETEST_SAMPLE','OTHER_APPROVED'),'samplingLocation':txt(200),'quantity':QTY,'unitId':ID,'samplingMethod':txt(),'signature':signature},'FinishedReportGenerate':{**version,'reportNo':txt(80)},'FinishedSignedCommand':{**version,'signature':signature}}
for name,props in commands.items():sc[name]=obj(props)
operations=[]
def operation(path,method,name,permission,response,body=None,created=False,filters=None):
 params=[]
 if '{id}' in path:params.append({'name':'id','in':'path','required':True,'schema':ID})
 if body:
  params.append({'name':'Idempotency-Key','in':'header','required':True,'schema':txt(200)})
  if not body.endswith('Create'):params.append({'name':'If-Match','in':'header','required':True,'schema':{'type':'string','pattern':r'^"(?:0|[1-9][0-9]*)"$'}})
 for f,schema in (filters or {}).items():params.append({'name':f,'in':'query','required':False,'schema':schema})
 op={'operationId':name,'tags':['finished-goods'],'x-permission':permission,'security':[{'bearerAuth':[]}],'parameters':params,'responses':{'201' if created else '200':{'description':'Success','content':{'application/json':{'schema':ref('Api'+response)}}},'400':{'description':'Closed input invalid'},'401':{'description':'Unauthenticated'},'403':{'description':'Forbidden'},'404':{'description':'Scoped record absent'},'409':{'description':'Version/state/idempotency/concurrency conflict'},'422':{'description':'Business gate blocked'}}}
 if body:op['requestBody']={'required':True,'content':{'application/json':{'schema':ref(body)}}}
 api['paths'].setdefault(path,{})[method]=op;operations.append({'path':path,'method':method.upper(),'permission':permission,'request':body,'response':response,'created':created})
resources=[('/finished-inbound-requests','FinishedInboundRequest','wms:finished-inbound','FinishedInboundCreate',['mainBatchId','status']),('/quality/finished-inspection-requests','FinishedInspectionRequest','qms:finished-request','FinishedInspectionCreate',['mainBatchId','status']),('/quality/finished-sampling-records','FinishedSamplingRecord','qms:finished-sampling',None,['inspectionRequestId']),('/quality/finished-inspection-reports','FinishedInspectionReport','qms:finished-report',None,['inspectionRequestId','status']),('/finished-shipments','FinishedShipment','wms:finished-shipment','FinishedShipmentCreate',['mainBatchId','status'])]
for path,name,permission,body,extra in resources:
 filters={'page':{**INT,'default':0},'size':{'type':'integer','minimum':1,'maximum':100,'default':20},'keyword':txt(200),'sort':txt(80)}
 for f in extra:filters[f]=ID if f.endswith('Id') else fields[name]['status']
 operation(path,'get','query'+name,permission+':view',name+'Page',filters=filters);operation(path+'/{id}','get','get'+name,permission+':view',name)
 if body:operation(path,'post','create'+name,permission+':create',name,body,True)
actions=[('/finished-inbound-requests','submit','wms:finished-inbound:submit','FinishedInboundRequest','FinishedReasonCommand',False),('/finished-inbound-requests','cancel','wms:finished-inbound:cancel','FinishedInboundRequest','FinishedReasonCommand',False),('/finished-inbound-requests','confirm','wms:finished-inbound:confirm','FinishedInboundRequest','FinishedReceiptConfirm',False),('/quality/finished-inspection-requests','submit','qms:finished-request:submit','FinishedInspectionRequest','FinishedReasonCommand',False),('/quality/finished-inspection-requests','accept','qms:finished-request:accept','FinishedInspectionRequest','FinishedReasonCommand',False),('/quality/finished-inspection-requests','sample','qms:finished-sampling:create','FinishedSamplingRecord','FinishedSamplingCommand',True),('/quality/finished-inspection-requests','generate-report','qms:finished-report:generate','FinishedInspectionReport','FinishedReportGenerate',True),('/quality/finished-inspection-reports','approve','qms:finished-report:approve','FinishedInspectionReport','FinishedSignedCommand',False),('/finished-shipments','confirm','wms:finished-shipment:confirm','FinishedShipment','FinishedSignedCommand',False),('/finished-shipments','cancel','wms:finished-shipment:cancel','FinishedShipment','FinishedReasonCommand',False)]
for path,action,permission,response,body,created in actions:operation(path+'/{id}/'+action,'post',action.replace('-','')+response,permission,response,body,created)
operation('/finished-material-lots/{id}/chain','get','finishedLotChain','wms:inventory:view','FinishedLotChain')
save('08_OPENAPI_FULL_V1.0.20_FROZEN.yaml',api);save('00_FINISHED_GOODS_OPERATIONS.json',operations)
database='# Exact approved physical schema — v1.0.20\n\nV030 after native29, V031 after verified30; no executed migration edits. Common scoped ProfileM metadata / optimistic mutable roots, append-only sampling/review. Existing Sample/TestResult remains sole raw facts. Historical ReleaseDecision report refs nullable with no backfill. Scope validation additionally enforced in application.\n'
for version in ['V030__finished_goods_controlled_chain.sql','V031__finished_fact_preservation.sql']:
 sql=(ROOT/'backend/mes-boot/src/main/resources/db/migration'/version).read_text(encoding='utf-8');database+='\n## '+version+'\n\n```sql\n'+sql+'\n```\n'
(NEW/'00_FINISHED_GOODS_DATABASE_V1.0.20.md').write_text(database,encoding='utf-8')
def matrix(name,header,rows):
 with (NEW/name).open('w',newline='',encoding='utf-8') as f:w=csv.writer(f);w.writerow(header);w.writerows(rows)
matrix('00_FINISHED_GOODS_PERMISSION_MATRIX_V1.0.20.csv',['permission','default_role','operations'],[[p,'SYSTEM_ADMIN only' if 'finished-' in p else 'existing',','.join(o['method']+' '+o['path'] for o in operations if o['permission']==p)] for p in sorted({o['permission'] for o in operations})])
routes=[]
for resource,permission,create in [('inbound','wms:finished-inbound',True),('requests','qms:finished-request',True),('sampling','qms:finished-sampling',False),('reports','qms:finished-report',False),('shipments','wms:finished-shipment',True)]:
 routes += [['/finished/'+resource,'T1',permission+':view'],['/finished/'+resource+'/:id','T3',permission+':view']]
 if create:routes += [['/finished/'+resource+'/create','T2',permission+':create']]
routes += [['/wms/material-lots/:id','T4','wms:inventory:view'],['/production/batches/:id','T4','production:batch:view'],['/qa/batches/:id/review|release','T6','qa:batch-review|qa:release']]
matrix('00_FINISHED_GOODS_UI_ROUTE_MATRIX_V1.0.20.csv',['route','template','permission'],routes)
scenarios=['completed production required','signed output alone zero stock','independent signed receipt once/replay','legacy actual ledger no duplicate','frozen finished request and real sample','original FAIL/OOS/retest preserved','independent stale report approval','receipt/report/QA gates and no premature shipment','atomic signed QA availability','signed exact shipment once and reverse trace','expiry/frozen/stock/org/permission guards','root lock serializes confirmations/shipments','full production-to-shipment native lifecycle']
matrix('00_FINISHED_GOODS_RTM_V1.0.20.csv',['requirement','test','scenario','owner','evidence'],[[f'FG-{i:02}',f'TC-FG-{i:02}',s,'FinishedGoodsLifecycleIT / affected production QA archive / Chromium','Runtime verification separately recorded'] for i,s in enumerate(scenarios,1)])
matrix('00_FINISHED_GOODS_INTEGRATION_V1.0.20.csv',['producer','consumer','contract','invariant'],[['ProductionQuantity','WMS','actual signed OUTPUT/reversal via port','OUTPUT measurement never physical stock'],['WMS','QMS','confirmed FinishedInboundRequest','independent signature + exact frozen source'],['Production QC','FinishedReport','Sample/TestResult/review/OOS','immutable FAIL; frozen standard; independent review'],['QMS/WMS','QA/eBR','finished receiving/request/sampling/report','8 gates, no QC PASS availability'],['QA','WMS Shipment','effective current signed decision','released available stock only; locks/atomic exact debit'],['WMS/QMS/Production/Release','Trace','scoped source read ports','permission filtered actual-source lineage']])
matrix('00_FINISHED_GOODS_MIGRATION_ALLOCATION_V1.0.20.csv',['version','owner','allocation'],[['V030','WMS/QMS/Release','six normalized tables plus finished report decision FK + 18 permissions'],['V031','WMS/QMS','additional signed/confirmed/approved immutable preservation guards']])
save('00_FINISHED_GOODS_CONSISTENCY_REVIEW.json',{'status':'PENDING_VERIFICATION','approvedDcp':'DCP-FINISHED-GOODS-CHAIN-001','parentBaseline':'v1.0.19','candidateBaseline':'v1.0.20','authoritySwitched':False})
print('Candidate built, parent/pointers unchanged, '+str(len(operations))+' operations')
