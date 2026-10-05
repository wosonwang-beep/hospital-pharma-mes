"""Build the explicitly approved audit delta candidate; never switch authority here."""
from pathlib import Path
import csv, hashlib, json, shutil

ROOT = Path(__file__).resolve().parents[1]
OLD = ROOT / 'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16'
NEW = ROOT / 'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17'
if NEW.exists():
    raise SystemExit('Candidate already exists; never overwrite a release')
hashes = {str(p.relative_to(OLD)): hashlib.sha256(p.read_bytes()).hexdigest() for p in OLD.rglob('*') if p.is_file()}
shutil.copytree(OLD, NEW)
for p in list(NEW.rglob('*')):
    if p.is_file() and p.suffix in {'.md','.csv','.yaml','.json','.html','.js','.mjs','.txt'}:
        p.write_text(p.read_text(encoding='utf-8-sig').replace('v1.0.16','v1.0.17').replace('V1.0.16','V1.0.17'),encoding='utf-8')
        if '1.0.16' in p.name:
            p.rename(p.with_name(p.name.replace('1.0.16','1.0.17')))
DCP = 'DCP-AUDIT-HIGH-QUERY-ACTIONS-OCCUPANCY-001'
approval = (ROOT/'docs/development'/f'{DCP}-PROPOSED.md').read_text(encoding='utf-8')
(NEW/f'00_DESIGN_CHANGE_{DCP}_APPROVED.md').write_text(approval,encoding='utf-8')
delta = '''# Approved audit HIGH closure — v1.0.17

Status: CANDIDATE until consistency review PASS. User explicitly approved DCP-AUDIT-HIGH-QUERY-ACTIONS-OCCUPANCY-001 on 2026-10-05. Cumulative inheritance from immutable FINAL BASELINE COMPLETE v1.0.16. MES-012/013 completion remains accepted; historical approvals are retained.

## Database / migration
No table, column, FK, index or physical migration change. md_equipment is the existing identity mutex. mes_equipment_usage links equipment_id to operation_execution_id; mes_equipment_run links equipment_usage_id to immutable start/end segment history. Executed Flyway files remain unchanged. Native localhost:3306/hospital_pharma_mes_dev is retained.

## Query API / UI-BAT-Q
GET /main-batches adds the already implemented optional query productionOrderId, positive ID string. Existing filters, pagination and response remain unchanged. T1 order/batch query restores product/status/date/order/keyword/page from route query. No new route or permission.

## Domain / state / equipment integration
START/RESUME lock existing equipment identities in ascending ID order in the same transaction, before material gates. Current locking reads of usages and runs reject any existing RUNNING segment with EQUIPMENT_OCCUPIED. PAUSE/COMPLETE acquire the same mutex before closing segments. Locks and inserts/updates/audits commit atomically. No status API, global equipment status rewrite, history replacement or new state. MariaDB snapshot conflict is surfaced as CONCURRENT_MODIFICATION; it fails closed and a fresh command must encounter the occupancy gate. Existing production order/batch/operation lock hierarchy and qualifiers remain.

## Incoming read DTO / API / UI
Required read-only allowedActions string[] is added to InspectionRequest, SamplingTask, SamplingDetail, Sample, InspectionTask, InspectionItem, TestExecution, TestResultRevision, InspectionReport, Deviation (incoming branch), ReleaseDecision and ReleaseReview. Empty/missing actions fail closed in consumers. Action names are existing command suffixes: submit, accept, approve-plan, assign, start, details, complete, label, receive, retain, dispose, executions, approved-retests, results, revisions, submit-review, review, approve, update, investigate, decide, close, release-decisions.

Domain IncomingActionPolicy shares state candidates with command validation. Application narrows by authenticated assignee, configured qualification, independent reviewer, completed/resealed sampling, complete test evidence, exact confirmed results, report digest and signed retest quota. DTO availability is advisory at read time: request-specific inputs and authoritative current evidence are validated again by the existing command transaction. ReleaseReview offers the existing decision command only in supported lot transition states; eligibleForRelease still governs RELEASED, not reject/disposition. No generic availability implies release eligibility.

Frontend intersects server actions with the existing operation permission. Nested inspection item/execution/revision controls use their own source actions. Existing T1–T6 visual language/layout remains; no T7 or Phase 2B.

## Permission / GxP / source of truth
No permission, electronic-signature target/meaning, qualification rule, release gate or allowed command transition is added or weakened. Dynamic actions never enter signature canonical evidence or audit fact snapshots. Read DTO decoration is not persisted. Original FAIL corrections remain prohibited; retest creates new execution/result facts. Incoming Sample/TestResult/Report/Release and WMS lot source-of-truth ownership is unchanged.

## Required tests / RTM / dependency
AUD-H05 -> TC-AUD-H05: native existing productionOrderId filtering plus browser query restoration/reset/pagination.
AUD-H07 -> TC-AUD-H07: native request/sample/item/execution/revision/report/release actions, actor independence and signature integrity; browser missing actions and permission fail closed.
AUD-H08 -> TC-AUD-H08: native different-order concurrency has at most one RUNNING owner; fresh retry blocked; PAUSE frees; occupied RESUME blocked; COMPLETE frees; ended history retained.
Existing MES-008/008A/010/011 producer-consumer boundaries stay accepted. EquipmentQueryService exposes an internal transaction mutex, not a public API. Incoming consumers use decorated existing DTOs, not a second fact source. MEDIUM-01..04 remains outside this change. Runtime verification and human acceptance are separate from baseline publication.
'''
(NEW/'00_AUDIT_HIGH_CONTRACT_V1.0.17.md').write_text(delta,encoding='utf-8')
note='\n\n## Approved audit HIGH delta — v1.0.17\n\n[00_AUDIT_HIGH_CONTRACT_V1.0.17.md](00_AUDIT_HIGH_CONTRACT_V1.0.17.md) governs only productionOrderId query publication, incoming read-only allowedActions and exclusive active equipment occupancy. All other inherited contracts remain unchanged. No schema/migration, new permission/route/state or signature/release-rule change.\n'
for prefix in ['01_','04_','05_','07_','08_API_','09_','10_UI_PAGE_','11_','12A_','12_TEST_CASE_','15_MODULE_']:
    for p in NEW.glob(prefix+'*.md'):
        p.write_text(p.read_text(encoding='utf-8')+note,encoding='utf-8')
for task in ['008','008A','010','011']:
    for p in (NEW/'tasks').glob(f'MES-{task}*.md'):
        p.write_text(p.read_text(encoding='utf-8')+note.replace('](00_','](../00_'),encoding='utf-8')
api_path=NEW/'08_OPENAPI_FULL_V1.0.17_FROZEN.yaml'
api=json.loads(api_path.read_text(encoding='utf-8'));api['info']['version']='1.0.17'
api['paths']['/main-batches']['get']['parameters'].append({'name':'productionOrderId','in':'query','required':False,'description':'Existing production-order identity filter; approved audit HIGH-05 publication','schema':{'type':'string','pattern':'^[1-9][0-9]*$'}})
names=['InspectionRequest','SamplingTask','SamplingDetail','Sample','InspectionTask','InspectionItem','TestExecution','TestResultRevision','InspectionReport','Deviation','ReleaseDecision','ReleaseReview']
actions={'type':'array','readOnly':True,'items':{'type':'string'},'description':'Server-derived existing command availability; intersect with existing permission and revalidate on command. Missing/empty fail closed.'}
for name in names:
    schema=api['components']['schemas'][name];schema['properties']['allowedActions']=actions.copy()
    if 'allowedActions' not in schema.setdefault('required',[]):schema['required'].append('allowedActions')
api_path.write_text(json.dumps(api,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
for relative in ['docs/development/incoming-approved-production-openapi.json','frontend/mes-web/src/views/quality/incoming-contract.json']:
    p=ROOT/relative;data=json.loads(p.read_text(encoding='utf-8-sig'))
    if 'paths' in data:
        data['paths']['/main-batches']['get']['parameters']=api['paths']['/main-batches']['get']['parameters']
        for name in names:
            if name in data['components']['schemas']:data['components']['schemas'][name]=api['components']['schemas'][name]
    else:
        for name in names:
            if name in data['schemas']:data['schemas'][name]=api['components']['schemas'][name]
    p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
test_path=NEW/'12_TEST_CASE_CATALOG_V1.0.17_FROZEN.csv'
with test_path.open('a',encoding='utf-8',newline='') as f:
    w=csv.writer(f)
    for issue,title in [('05','Existing production order query'),('07','Server incoming action controls'),('08','Exclusive equipment occupancy')]:
        w.writerow([f'TC-AUD-H{issue}',f'AUD-H{issue}','Audit HIGH closure','Production/QMS/Execution',title,'Targeted integration/browser','Unique rollback or retained native fixtures','See approved audit contract','Fail closed; unchanged regulated facts','Scoped validation report','Automated'])
rtm=NEW/'13_REQUIREMENT_TRACEABILITY_MATRIX_V1.0.17_FROZEN.csv'
with rtm.open('a',encoding='utf-8',newline='') as f:
    w=csv.writer(f)
    for issue,title,db,endpoint,ui in [('05','Production order filter','No delta','GET /main-batches','UI-BAT-Q/T1'),('07','Incoming server actions','No delta','Existing incoming read DTOs','Existing incoming T1-T6'),('08','Exclusive active equipment','Existing equipment/usage/run','Existing operation commands','Existing operation execution')]:
        w.writerow([f'AUD-H{issue}',title,db,endpoint,ui,'Approved audit closure',f'TC-AUD-H{issue}','CANDIDATE'])
(NEW/'00_AUDIT_HIGH_PARENT_HASHES.json').write_text(json.dumps(hashes,indent=2)+'\n',encoding='utf-8')
print('v1.0.17 candidate created; authority remains v1.0.16 until review PASS')
