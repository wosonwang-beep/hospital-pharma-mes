"""Create/seal only the approved navigation read-contract successor; parent immutable."""
from pathlib import Path
import copy, csv, hashlib, json, shutil, sys
ROOT=Path(__file__).resolve().parents[1]
PARENT=ROOT/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.22'
BASE=ROOT/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.23'
DCP='DCP-PRODUCTION-FINISHED-ENTRY-001'
CONTRACT='00_PRODUCTION_FINISHED_ENTRY_CONTRACT_V1.0.23.md'
REVIEW='00_PRODUCTION_FINISHED_ENTRY_CONSISTENCY_REVIEW.json'
EVIDENCE='docs/acceptance/shared-navigation-2026-10-07/REPORT.md'
def hashes(p):return {f.relative_to(p).as_posix():hashlib.sha256(f.read_bytes()).hexdigest() for f in sorted(p.rglob('*')) if f.is_file()}
def save(name,obj):(BASE/name).write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def read(name):return json.loads((BASE/name).read_text(encoding='utf-8'))
def append(prefix,rows):
 p=next(BASE.glob(prefix+'*.csv'))
 with p.open(encoding='utf-8-sig',newline='') as f:old=list(csv.reader(f))
 assert all(len(r)==len(old[0]) for r in rows),prefix
 with p.open('w',encoding='utf-8',newline='') as f:csv.writer(f).writerows(old+rows)
ID={'type':'string','pattern':'^[1-9][0-9]*$'}
STR={'type':'string'}
ROUTES=[('PE-01','生产执行','/production/execution','mes:execution:view AND mes:operation:view'),('PE-02','物料平衡','/production/balances','balance:view'),('PE-03','成品生产入库（待检）','/finished/receiving','wms:finished-inbound:view'),('PE-04','成品检验','/finished/tests','qms:test:view'),('PE-05','QA批审核','/finished/qa-reviews','qa:batch-review'),('PE-06','成品放行','/finished/releases','qa:release AND qa:batch-review')]
PATHS={'/navigation/executions':'ExecutionEntryPage','/navigation/batches':'BatchEntryPage','/navigation/batches/{id}':'BatchEntry','/quality/finished-tests':'FinishedTestEntryPage','/quality/finished-tests/{id}':'ProductionTest'}
def build():
 if BASE.exists():raise SystemExit('Refuse overwrite')
 parent=hashes(PARENT);shutil.copytree(PARENT,BASE)
 for p in list(BASE.rglob('*')):
  if not p.is_file():continue
  if p.suffix in {'.md','.csv','.json','.yaml','.html','.js','.txt'}:p.write_text(p.read_text(encoding='utf-8-sig').replace('v1.0.22','v1.0.23').replace('V1.0.22','V1.0.23'),encoding='utf-8')
  if '1.0.22' in p.name:p.rename(p.with_name(p.name.replace('1.0.22','1.0.23')))
 save('00_PRODUCTION_FINISHED_ENTRY_PARENT_HASHES.json',parent)
 save('00_PRODUCTION_FINISHED_ENTRY_MIGRATION_HASHES.json',hashes(ROOT/'backend/mes-boot/src/main/resources/db/migration'))
 text=(ROOT/'docs/development'/f'{DCP}-APPROVED.md').read_text(encoding='utf-8')
 text+='\n\n## Current single navigation authority\n\n'+(ROOT/'docs/development/DCP-GLOBAL-NAVIGATION-001-APPROVED.md').read_text(encoding='utf-8')
 (BASE/CONTRACT).write_text(text,encoding='utf-8')
 note=f'\n\n## v1.0.23 approved entry/read supplement\n\n[{CONTRACT}]({CONTRACT}) supersedes inherited navigation/entry scope only. Seven shared domains; production five and finished nine entries; T1 selectors and scoped read models. Prior business facts, tables/PK/FK, states, permissions, commands, QA/signature/trace rules unchanged. No migration; V031 retained. No mandatory weighing. Older copied manifests attest only their historical deltas; current review and SHA256SUMS govern this successor. Task readiness only MES_TASKS.md. PE-01..06 cover Test/RTM.\n'
 for p in BASE.glob('*.md'):
  if p.name[:3] in [f'{i:02d}_' for i in range(1,16)] or p.name=='README.md':p.write_text(p.read_text(encoding='utf-8')+note,encoding='utf-8')
 api=read('08_OPENAPI_FULL_V1.0.23_FROZEN.yaml');api['info']['version']='1.0.23';schemas=api['components']['schemas']
 fields={'ExecutionEntry':{'id':ID,'executionNo':STR,'mainBatchId':ID,'batchNo':STR,'unitType':STR,'status':STR},'BatchEntry':{'id':ID,'mainBatchId':ID,'batchNo':STR,'productId':ID,'plannedQty':STR,'unitId':ID,'status':STR,'finishedLotId':{'type':['string','null'],'pattern':'^[1-9][0-9]*$'},'versionNo':{'type':'integer','minimum':0}},'FinishedTestEntry':{'id':ID,'testCode':STR,'sampleId':ID,'sampleNo':STR,'mainBatchId':ID,'batchNo':STR,'inspectionRequestId':ID,'inspectionRequestNo':STR,'status':STR,'attemptNo':{'type':'integer','minimum':1}}}
 for name,properties in fields.items():
  schemas[name]={'type':'object','additionalProperties':False,'required':list(properties),'properties':properties}
  schemas[name+'Page']={'type':'object','additionalProperties':False,'required':['items','total','page','size'],'properties':{'items':{'type':'array','items':{'$ref':'#/components/schemas/'+name}},'total':{'type':'integer','minimum':0},'page':{'type':'integer','minimum':0},'size':{'type':'integer','minimum':1,'maximum':100}}}
 # Resolve the exact existing production-test detail response instead of duplicating its facts.
 detail=api['paths']['/quality/production-tests/{id}']['get']['responses']['200']['content']['application/json']['schema']
 for path,name in PATHS.items():
  permission='mes:execution:view' if 'executions' in path else 'balance:view | qa:batch-review | (qa:release AND qa:batch-review)' if '/navigation/batches' in path else 'qms:test:view'
  properties=[]
  if '{id}' in path:properties.append({'name':'id','in':'path','required':True,'schema':ID})
  else:
   properties=[{'name':k,'in':'query','required':False,'schema':v} for k,v in {'page':{'type':'integer','minimum':0,'maximum':1000000,'default':0},'size':{'type':'integer','minimum':1,'maximum':100,'default':20},'keyword':{'type':'string','maxLength':200},'status':STR,'mainBatchId':ID}.items()]
   if 'finished-tests' in path:properties.append({'name':'sampleId','in':'query','required':False,'schema':ID})
  if '/navigation/batches' in path:properties.append({'name':'context','in':'query','required':True,'schema':{'type':'string','enum':['balance','qa-review','release']}})
  if name!='ProductionTest':
   schemas['Api'+name]=copy.deepcopy(schemas['ApiFinishedInboundRequestPage'])
   schemas['Api'+name]['properties']['data']={'$ref':'#/components/schemas/'+name}
  response=detail if name=='ProductionTest' else {'$ref':'#/components/schemas/Api'+name}
  api['paths'][path]={'get':{'operationId':'read'+name+('ScopedDetail' if '{id}' in path else ''),'tags':['navigation-entry'],'x-permission':permission,'security':[{'bearerAuth':[]}],'parameters':properties,'responses':{'200':{'description':'Existing scoped facts only; no state writes','content':{'application/json':{'schema':response}}},'400':{'description':'Invalid closed query'},'401':{'description':'Unauthenticated'},'403':{'description':'Forbidden'},'404':{'description':'Scoped record absent'}}}}
 api['paths']['/finished-inbound-requests']['get']['parameters'].append({'name':'receivingOnly','in':'query','required':False,'schema':{'type':'boolean','default':False},'description':'SUBMITTED/CONFIRMED only, filtered before count/pagination; true sorting id ASC/DESC; default unchanged.'})
 save('08_OPENAPI_FULL_V1.0.23_FROZEN.yaml',api)
 append('10_UI_PAGE_ROUTE_MATRIX',[[r,n,'T1',p,r,permission] for r,n,p,permission in ROUTES]+[['PE-04-D','成品检验记录','T3','/finished/tests/:id','PE-04','qms:test:view']])
 append('12_TEST_CASE_CATALOG',[[f'TC-{r}',r,DCP,'Navigation/Production/Finished',n,'NORMAL/NEGATIVE','Real scoped records / Chromium fixtures','Select actual record; verify scoped read and permission',n,EVIDENCE,'Targeted native MariaDB / PC Chromium'] for r,n,_,_ in ROUTES])
 append('12_TEST_COVERAGE_MATRIX',[['Requirement',r,'TC-'+r,'1'] for r,_,_,_ in ROUTES])
 append('13_REQUIREMENT_TRACEABILITY_MATRIX',[[r,n,'Existing tables/PK/FK only','GET navigation/finished-tests; receivingOnly',p,DCP,'TC-'+r,'FROZEN CONTRACT; task readiness only MES_TASKS.md'] for r,n,p,_ in ROUTES])
 append('15_INTEGRATION_CONTRACT_MATRIX',[['Production/QMS/WMS read services','PE-01..06 scoped indexes','Shared navigation -> existing controlled workbenches','Actual execution, batch, finished sampling/request/test, inbound records','Same organization; existing context permissions; READ ONLY','Absent/forbidden source never fabricated','PE-01..06']])
 append('15_TASK_DEPENDENCY_MATRIX',[[DCP,'Approved real execution/balance/finished entrances','MES-008;MES-010;MES-012;MES-013;DCP-GLOBAL-NAVIGATION-001','None','Shared-shell navigation and existing controlled pages',CONTRACT]])
 for prefix in ['15_MIGRATION_DEPENDENCY_MATRIX','15_LOGICAL_TO_PHYSICAL_MIGRATION_LEDGER']:append(prefix,[['LG-PE-NONE','NONE',DCP,'Navigation_read_projection','Existing tables and relationships','Applied V001..V031','NONE; highest successful031 unchanged','NO MIGRATION']])
 save(REVIEW,{'status':'PENDING'});print('v1.0.23 candidate built; parent unchanged')
def seal():
 if (BASE/'00_PRODUCTION_FINISHED_ENTRY_MANIFEST.json').exists():raise SystemExit('Already sealed')
 api=read('08_OPENAPI_FULL_V1.0.23_FROZEN.yaml');old=json.loads((PARENT/'08_OPENAPI_FULL_V1.0.22_FROZEN.yaml').read_text(encoding='utf-8'))
 inherited={k:v for k,v in api['paths'].items() if k not in PATHS};inherited['/finished-inbound-requests']['get']['parameters']=old['paths']['/finished-inbound-requests']['get']['parameters']
 refs=[]
 def visit(v):
  if isinstance(v,dict):
   if '$ref' in v:refs.append(v['$ref'])
   for x in v.values():visit(x)
  elif isinstance(v,list):
   for x in v:visit(x)
 visit(api)
 checks={'parent_immutable':hashes(PARENT)==read('00_PRODUCTION_FINISHED_ENTRY_PARENT_HASHES.json'),'migration_immutable':hashes(ROOT/'backend/mes-boot/src/main/resources/db/migration')==read('00_PRODUCTION_FINISHED_ENTRY_MIGRATION_HASHES.json'),'inherited_endpoints_unchanged_except_approved_query':inherited==old['paths'],'inherited_schemas_unchanged':all(api['components']['schemas'][k]==v for k,v in old['components']['schemas'].items()),'only_five_new_reads':set(api['paths'])-set(old['paths'])==set(PATHS),'refs_resolve':all(r.split('/')[-1] in api['components'].get(r.split('/')[2],{}) for r in refs),'routes_rtm_tests_reconciled':all(r in next(BASE.glob('10_UI_PAGE_ROUTE*.csv')).read_text(encoding='utf-8') and r in next(BASE.glob('13_REQUIREMENT*.csv')).read_text(encoding='utf-8') and 'TC-'+r in next(BASE.glob('12_TEST_CASE*.csv')).read_text(encoding='utf-8') for r,_,_,_ in ROUTES)}
 report={'status':'PASS' if all(checks.values()) else 'FAIL','approvedDcp':DCP,'checks':checks,'parentFileCount':len(hashes(PARENT))};save(REVIEW,report);print(json.dumps(report))
 if not all(checks.values()):raise SystemExit('Stop: consistency failed')
 (BASE/'00_MANIFEST_FINAL_FROZEN_V1.0.23.md').write_text(f'# FINAL BASELINE COMPLETE v1.0.23\n\nAUTHORITATIVE — user-approved {DCP}; cross-document consistency PASS.\n\nBounded shared navigation, entry routes, and read projections only. v1.0.22 immutable; inherited commands, states, permissions, signatures, DB/migrations, QA gates, actual-charge trace and no-mandatory-weighing rules unchanged. UI V2/T1–T6 retained. Current checks: {REVIEW}. Historical copied reviews/manifests only attest previous deltas. Runtime readiness only MES_TASKS.md.\n',encoding='utf-8')
 save('00_PRODUCTION_FINISHED_ENTRY_MANIFEST.json',{k:v for k,v in hashes(BASE).items() if k!='SHA256SUMS.txt'})
 (BASE/'SHA256SUMS.txt').write_text(''.join(f'{h}  {p}\n' for p,h in hashes(BASE).items() if p!='SHA256SUMS.txt'),encoding='utf-8')
if __name__=='__main__':{'build':build,'seal':seal}[sys.argv[1]]()
