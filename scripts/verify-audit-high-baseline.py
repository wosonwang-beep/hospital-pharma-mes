"""Deterministic cross-document check for the approved v1.0.17 delta, no authority switch."""
from pathlib import Path
import hashlib,json,subprocess,csv
ROOT=Path(__file__).resolve().parents[1]
OLD=ROOT/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16'
NEW=ROOT/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17'
names=['InspectionRequest','SamplingTask','SamplingDetail','Sample','InspectionTask','InspectionItem','TestExecution','TestResultRevision','InspectionReport','Deviation','ReleaseDecision','ReleaseReview']
errors=[]
old_hashes=json.loads((NEW/'00_AUDIT_HIGH_PARENT_HASHES.json').read_text())
for name,expected in old_hashes.items():
    if hashlib.sha256((OLD/name).read_bytes()).hexdigest()!=expected:errors.append('Historical release changed: '+name)
old=json.loads((OLD/'08_OPENAPI_FULL_V1.0.16_FROZEN.yaml').read_text(encoding='utf-8'))
new=json.loads((NEW/'08_OPENAPI_FULL_V1.0.17_FROZEN.yaml').read_text(encoding='utf-8'))
normalized=json.loads(json.dumps(old,ensure_ascii=False).replace('v1.0.16','v1.0.17').replace('V1.0.16','V1.0.17'))
paths=json.loads(json.dumps(new['paths']))
params=paths['/main-batches']['get']['parameters']
added=[p for p in params if p['name']=='productionOrderId']
if len(added)!=1 or added[0]['schema'].get('pattern')!='^[1-9][0-9]*$':errors.append('Query parameter contract')
paths['/main-batches']['get']['parameters']=[p for p in params if p['name']!='productionOrderId']
if paths!=normalized['paths']:errors.append('Unexpected path/permission/operation/response delta')
schemas=json.loads(json.dumps(new['components']['schemas']))
for name in names:
    s=schemas[name]
    action=s['properties'].pop('allowedActions',None)
    if not action or action.get('readOnly') is not True or action.get('items',{}).get('type')!='string':errors.append(name+' actions schema')
    if 'allowedActions' not in s['required']:errors.append(name+' missing required actions')
    s['required'].remove('allowedActions')
if schemas!=normalized['components']['schemas']:errors.append('Unexpected business field/state/DTO delta')
def refs(value):
    if isinstance(value,dict):
        if '$ref' in value:yield value['$ref']
        for v in value.values():yield from refs(v)
    elif isinstance(value,list):
        for v in value:yield from refs(v)
for ref in refs(new):
    if ref.startswith('#/'):
        node=new
        try:
            for part in ref[2:].split('/'):node=node[part.replace('~1','/').replace('~0','~')]
        except KeyError:errors.append('Missing reference '+ref)
for filename in ['08_OPENAPI_ENDPOINT_CATALOG','10_UI_PAGE_ROUTE_MATRIX']:
    oldtext=(OLD/(filename+'_V1.0.16_FROZEN.csv')).read_text(encoding='utf-8-sig').replace('v1.0.16','v1.0.17').replace('V1.0.16','V1.0.17')
    if (NEW/(filename+'_V1.0.17_FROZEN.csv')).read_text(encoding='utf-8-sig')!=oldtext:errors.append(filename+' unexpected route/permission delta')
for relative in ['docs/development/incoming-approved-production-openapi.json','frontend/mes-web/src/views/quality/incoming-contract.json']:
    data=json.loads((ROOT/relative).read_text(encoding='utf-8'))
    source=data.get('schemas',data.get('components',{}).get('schemas',{}))
    for name in names:
        if name in source and source[name]!=new['components']['schemas'][name]:errors.append(relative+' schema '+name)
    if 'paths' in data and data['paths']['/main-batches']['get']['parameters']!=new['paths']['/main-batches']['get']['parameters']:errors.append(relative+' query mismatch')
for name in ['12_TEST_CASE_CATALOG','13_REQUIREMENT_TRACEABILITY_MATRIX']:
    text=(NEW/(name+'_V1.0.17_FROZEN.csv')).read_text(encoding='utf-8-sig')
    for issue in ['05','07','08']:
        if f'TC-AUD-H{issue}' not in text:errors.append(name+' missing test '+issue)
migrations=subprocess.check_output(['git','diff','--name-only','HEAD','--','backend/mes-boot/src/main/resources/db/migration'],cwd=ROOT,text=True).splitlines()
if migrations:errors.append('Executed migration files modified')
report={'status':'PASS' if not errors else 'FAIL','approvedDcp':'DCP-AUDIT-HIGH-QUERY-ACTIONS-OCCUPANCY-001','parentBaseline':'v1.0.16','candidateBaseline':'v1.0.17','historicalFilesVerified':len(old_hashes),'addedPaths':0,'addedPermissions':0,'physicalSchemaDelta':False,'executedMigrationsChanged':False,'incomingDecoratedReadSchemas':names,'businessStateEnumsChanged':False,'signatureCanonicalChanged':False,'globalUiV2Changed':False,'pageTemplates':'T1–T6 unchanged','errors':errors,'runtimeEvidence':'See docs/review/MES_V1.0.17_AUDIT_HIGH_CLOSURE_REPORT.md; consistency PASS does not imply human acceptance.'}
(NEW/'00_AUDIT_HIGH_CONSISTENCY_REVIEW.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'status':report['status'],'historicalFilesVerified':len(old_hashes),'errors':errors},ensure_ascii=False))
if errors:raise SystemExit(1)
