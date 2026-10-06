"""Bounded approved successor; never modifies the parent release or migration SQL."""
from pathlib import Path
import copy, csv, hashlib, json, re, shutil, sys

ROOT=Path(__file__).resolve().parents[1]
PARENT=ROOT/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.21'
BASE=ROOT/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.22'
DCP='DCP-TRACE-FINISHED-INVENTORY-001'
CONTRACT='00_TRACE_FINISHED_INVENTORY_CONTRACT_V1.0.22.md'
REVIEW='00_TRACE_FINISHED_INVENTORY_CONSISTENCY_REVIEW.json'
MANIFEST='00_TRACE_FINISHED_INVENTORY_MANIFEST.json'
EVIDENCE='docs/acceptance/trace-finished-inventory-2026-10-06/REPORT.md'
NODES=['SUPPLIER','MATERIAL_REQUEST','MATERIAL_ISSUE','MATERIAL_ISSUE_ITEM','ISSUE_RETURN','FINISHED_INBOUND_REQUEST','FINISHED_INSPECTION_REQUEST','FINISHED_SAMPLING_RECORD','FINISHED_REPORT','FINISHED_REPORT_REVIEW','PRODUCTION_TEST','RESULT_REVIEW','FINISHED_SHIPMENT','INVENTORY_LEDGER']
CONTEXT=['mainBatchId','batchNo','productId','productCode','productName','productSpecification']

def hashes(folder,excludes=()):
 return {p.relative_to(folder).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(folder.rglob('*')) if p.is_file() and p.name not in excludes}
def save(name,obj): (BASE/name).write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def read(name): return json.loads((BASE/name).read_text(encoding='utf-8'))
def append(prefix,rows):
 p=next(BASE.glob(prefix+'*'))
 with p.open(encoding='utf-8-sig',newline='') as f: source=list(csv.reader(f))
 assert all(len(r)==len(source[0]) for r in rows),(prefix,len(source[0]))
 with p.open('w',encoding='utf-8',newline='') as f: csv.writer(f).writerows(source+rows)

def build():
 if BASE.exists(): raise SystemExit('Refuse overwrite of successor')
 parent=hashes(PARENT);shutil.copytree(PARENT,BASE)
 for p in list(BASE.rglob('*')):
  if not p.is_file(): continue
  if p.suffix in {'.md','.csv','.yaml','.json','.html','.js','.mjs','.txt'}:
   p.write_text(p.read_text(encoding='utf-8-sig').replace('v1.0.21','v1.0.22').replace('V1.0.21','V1.0.22'),encoding='utf-8')
  if '1.0.21' in p.name: p.rename(p.with_name(p.name.replace('1.0.21','1.0.22')))
 save('00_TRACE_FINISHED_INVENTORY_PARENT_HASHES.json',parent)
 text=(ROOT/'docs/development'/f'{DCP}-APPROVED.md').read_text(encoding='utf-8')
 (BASE/CONTRACT).write_text(text,encoding='utf-8')
 note=f'\n\n## Approved read closure — v1.0.22\n\n[{CONTRACT}]({CONTRACT}) controls only charge-based forward/WMS/source trace and finished inventory projection/menu. Existing DB/state/signature/permission/stock/QA/weighing rules unchanged; migration NONE. Copied older manifests/reviews retain prior-delta attestation only; current read-closure manifest and SHA256SUMS govern this successor. TI-01..05 cover Test/RTM. Status is only MES_TASKS.md.\n'
 for p in BASE.glob('*.md'):
  if re.match(r'^(?:0[1-9]|1[0-5])_',p.name) or p.name=='README.md': p.write_text(p.read_text(encoding='utf-8')+note,encoding='utf-8')
 for task in ('008','011','013'):
  for p in (BASE/'tasks').glob('MES-'+task+'*.md'): p.write_text(p.read_text(encoding='utf-8')+note.replace(f']({CONTRACT})',f'](../{CONTRACT})'),encoding='utf-8')
 api=read('08_OPENAPI_FULL_V1.0.22_FROZEN.yaml');api['info']['version']='1.0.22'
 enum=api['components']['schemas']['TraceNode']['properties']['type']['enum']
 for node in NODES:
  if node not in enum: enum.append(node)
 row=api['components']['schemas']['WmsInventoryLot']
 for field in CONTEXT:
  row['properties'][field]={'type':['string','null'],'description':'Same-org actual finished batch/product context; current master labels, null for raw lots.'}
  if field.endswith('Id'):row['properties'][field]['pattern']='^[1-9][0-9]*$'
  row['required'].append(field)
 params=api['paths']['/wms/inventory']['get']['parameters']
 for field in ('finishedOnly','productId','mainBatchId'):
  params.append({'name':field,'in':'query','required':False,'schema':{'type':'boolean','default':False} if field=='finishedOnly' else {'type':'string','pattern':'^[1-9][0-9]*$'}})
 save('08_OPENAPI_FULL_V1.0.22_FROZEN.yaml',api)
 append('10_UI_PAGE_ROUTE_MATRIX', [['UI-TI-01','Finished inventory','T1','/finished/inventory','TI-03;TI-05','wms:inventory:view'],['UI-TI-02','Charge-based complete trace','T4','/trace','TI-01;TI-02;TI-05','trace:view; source links require existing source view rights']])
 scenarios=[('TI-01','Actual charge forward finished shipment and reverse trace'),('TI-02','Actual WMS/source nodes and permission-isolated parent links'),('TI-03','Finished inventory server filters exact stock and scoped context'),('TI-04','Closed read contract and affected direct regressions'),('TI-05','PC T1 inventory and T4 trace source navigation')]
 append('12_TEST_CASE_CATALOG',[[f'TC-{r}',r,DCP,'Trace/WMS',title,'NORMAL/NEGATIVE','Actual scoped facts; rollback fixtures','Approved bounded read closure',title,EVIDENCE,'Native MariaDB / targeted PC Chromium'] for r,title in scenarios])
 append('12_TEST_COVERAGE_MATRIX',[['Requirement',r,'TC-'+r,'1'] for r,_ in scenarios])
 append('13_REQUIREMENT_TRACEABILITY_MATRIX',[[r,title,'Existing facts only; no schema change','GET /trace; GET /wms/inventory','Trace T4; finished inventory T1',DCP,'TC-'+r,'FROZEN CONTRACT; readiness only MES_TASKS.md'] for r,title in scenarios])
 append('15_INTEGRATION_CONTRACT_MATRIX',[['Production/WMS source queries','TI: existing fact projection','boot query ports -> Trace/WMS reads','Actual charge/Genealogy/batch/lot/product; no inferred assignment','Same organization; existing category/source permissions','No writes; absent evidence never fabricated','TI-01..05']])
 append('15_TASK_DEPENDENCY_MATRIX',[[DCP,'Forward trace and finished inventory read closure','MES-008;MES-011;MES-013;DCP-FINISHED-GOODS-CHAIN-001','None','Trace/WMS finished inventory',CONTRACT]])
 for prefix in ('15_MIGRATION_DEPENDENCY_MATRIX','15_LOGICAL_TO_PHYSICAL_MIGRATION_LEDGER'):
  append(prefix,[['LG-TI-NONE','NONE',DCP,'trace_finished_inventory_reads','Existing tables/PK/FK; no physical change','Applied V001..V031','NONE; highest successful031 unchanged','NO MIGRATION']])
 (BASE/'00_MANIFEST_FINAL_FROZEN_V1.0.22.md').write_text('# FINAL BASELINE COMPLETE v1.0.22\n\nCandidate pending cross-document review.\n'+note,encoding='utf-8')
 save(REVIEW,{'status':'PENDING','approvedDcp':DCP});print('Candidate built; parent preserved:',len(parent))

def check():
 api=read('08_OPENAPI_FULL_V1.0.22_FROZEN.yaml');old=json.loads((PARENT/'08_OPENAPI_FULL_V1.0.21_FROZEN.yaml').read_text(encoding='utf-8'))
 unchanged=copy.deepcopy(api['paths']);unchanged['/wms/inventory']['get']['parameters']=old['paths']['/wms/inventory']['get']['parameters']
 refs=[]
 def visit(v):
  if isinstance(v,dict):
   if '$ref' in v:refs.append(v['$ref'])
   for x in v.values():visit(x)
  elif isinstance(v,list):
   for x in v:visit(x)
 visit(api)
 checks={'parent_release_immutable':read('00_TRACE_FINISHED_INVENTORY_PARENT_HASHES.json')==hashes(PARENT),
 'only_inventory_query_parameters_changed':unchanged==old['paths'],
 'only_two_read_schemas_changed':set(api['components']['schemas'])==set(old['components']['schemas']) and all(v==api['components']['schemas'][k] for k,v in old['components']['schemas'].items() if k not in ('TraceNode','WmsInventoryLot')),
 'all_existing_and_finished_trace_node_types_typed':set(NODES)<=set(api['components']['schemas']['TraceNode']['properties']['type']['enum']),
 'nullable_context_closed':set(CONTEXT)<=set(api['components']['schemas']['WmsInventoryLot']['required']) and api['components']['schemas']['WmsInventoryLot']['additionalProperties'] is False,
 'local_openapi_refs_resolve':all(r.startswith('#/components/') and r.split('/')[-1] in api['components'][r.split('/')[2]] for r in refs),
 'test_rtm_integration_migration_dependency_reconciled':all('TI-0'+str(i) in next(BASE.glob('13_REQUIREMENT*')).read_text(encoding='utf-8') for i in range(1,6)) and DCP in next(BASE.glob('15_TASK_DEPENDENCY*')).read_text(encoding='utf-8') and 'LG-TI-NONE' in next(BASE.glob('15_MIGRATION_DEPENDENCY*')).read_text(encoding='utf-8'),
 'schema_migrations_unchanged':hashes(ROOT/'backend/mes-boot/src/main/resources/db/migration')==read('00_TRACE_MIGRATION_HASHES.json')}
 report={'status':'PASS' if all(checks.values()) else 'FAIL','approvedDcp':DCP,'checks':checks,'parentFileCount':len(hashes(PARENT))}
 print(json.dumps(report,ensure_ascii=False));return report

def publish():
 if (BASE/MANIFEST).exists():raise SystemExit('Sealed release is immutable')
 review=check()
 if review['status']!='PASS':raise SystemExit('Consistency failed')
 save(REVIEW,review)
 (BASE/'00_MANIFEST_FINAL_FROZEN_V1.0.22.md').write_text(f'# FINAL BASELINE COMPLETE v1.0.22\n\nAUTHORITATIVE — approved {DCP}, cross-document consistency PASS.\n\nCharge-centered forward trace and finished inventory read/menu closure only. Parent v1.0.21 immutable; no schema/migration/state/permission/signature/QA/weighing rule change. Current hashes: {MANIFEST}; current review: {REVIEW}. Older copied manifests/reviews are historical attestations. UI V2/T1–T6 unchanged. Runtime readiness/human acceptance only MES_TASKS.md.\n',encoding='utf-8')
 for file in ('AGENTS.md','docs/ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md','docs/ui/MES_PAGE_TEMPLATE_STANDARD_V2.md'):
  p=ROOT/file;s=p.read_text(encoding='utf-8');s=s.replace('v1.0.21','v1.0.22')
  if file=='AGENTS.md':
   start=s.index('Current business authority:');end=s.index(' Current UI authority:',start)
   s=s[:start]+f'Current business authority: `FINAL BASELINE COMPLETE v1.0.22` — `AUTHORITATIVE`, following user-approved {DCP} and cross-document consistency PASS. Only actual-charge forward/WMS/source trace and finished inventory read/menu are extended. All prior finished/WMS/source/QA/GxP rules remain inherited; no mandatory weighing introduced. Parent v1.0.21 remains immutable; runtime status only MES_TASKS.md.'+s[end:]
  p.write_text(s,encoding='utf-8')
 p=ROOT/'docs/PROJECT_BASELINE.md';s=p.read_text(encoding='utf-8').replace('### Current approved optional inspection draft authority','### Retained v1.0.21 optional inspection draft authority')
 note=f'### Current approved trace / finished inventory authority — 2026-10-06\n\nCurrent business authority: `FINAL BASELINE COMPLETE v1.0.22` — `AUTHORITATIVE`, approved {DCP} and cross-document consistency PASS. [Contract](../releases/{BASE.name}/{CONTRACT}), [manifest](../releases/{BASE.name}/00_MANIFEST_FINAL_FROZEN_V1.0.22.md), [review](../releases/{BASE.name}/{REVIEW}). Actual-charge forward/WMS/source trace; server-filtered finished inventory and T1 entry only. No database/migration/QA/signature/stock/weighing change. Parent v1.0.21 immutable; UI V2/T1–T6 retained; runtime readiness only MES_TASKS.md.\n\n'
 s=s.replace('## Authority\n','## Authority\n\n'+note,1);p.write_text(s,encoding='utf-8')
 p=ROOT/'MES_TASKS.md';s=p.read_text(encoding='utf-8');s=s.replace('# MES Task Status Index\n','# MES Task Status Index\n\n## Approved trace / finished inventory closure — 2026-10-06\n\n`IN PROGRESS` — user approved audit gaps1/4 only: “授权修改批准，投料记录是关键，不是称量。” Current authority FINAL BASELINE COMPLETE v1.0.22 / approved DCP-TRACE-FINISHED-INVENTORY-001 / consistency PASS. [Contract](docs/development/DCP-TRACE-FINISHED-INVENTORY-001-APPROVED.md), [ledger](docs/development/TRACE-FINISHED-INVENTORY-IMPLEMENTATION.md). Actual-charge forward trace/WMS sources and finished inventory read/T1 menu; no mandatory weighing, new DB/migration, QA/stock/signature rule. Historical acceptance unchanged; no automatic acceptance/commit/push. Earlier dated pointers below retain their historical scope.\n',1);p.write_text(s,encoding='utf-8')
 current=hashes(BASE,(MANIFEST,'SHA256SUMS'))
 save(MANIFEST,{'baseline':'v1.0.22','approvedDcp':DCP,'files':current})
 (BASE/'SHA256SUMS').write_text(''.join(f'{v}  {k}\n' for k,v in hashes(BASE,('SHA256SUMS',)).items()),encoding='utf-8')
 print('Authority published after consistency PASS; runtime status IN PROGRESS')

if __name__=='__main__':
 mode=sys.argv[1]
 if mode=='build':
  build();save('00_TRACE_MIGRATION_HASHES.json',hashes(ROOT/'backend/mes-boot/src/main/resources/db/migration'))
 elif mode=='check':
  r=check();raise SystemExit(0 if r['status']=='PASS' else 1)
 elif mode=='publish':publish()
 else:raise SystemExit('Unknown mode')
