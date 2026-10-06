"""Reconcile the approved candidate and verify scoped contracts; never edit parents."""
from pathlib import Path
import copy, csv, hashlib, json, re, subprocess, sys

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.20'
PARENT = ROOT / 'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.19'
DRY = '--check' in sys.argv
manifest = BASE / '00_MANIFEST_FINAL_FROZEN_V1.0.20.md'
if manifest.exists() and 'Published cumulative release' in manifest.read_text(encoding='utf-8') and not DRY:
    raise SystemExit('Published release is immutable: use --check for read-only verification')

def read(name):
    return json.loads((BASE / name).read_text(encoding='utf-8'))

def save(name, value):
    if DRY: return
    (BASE / name).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def rows(name):
    with (BASE / name).open(encoding='utf-8-sig', newline='') as f:
        return list(csv.DictReader(f))

def append(name, additions, key):
    with (BASE / name).open(encoding='utf-8-sig', newline='') as f:
        reader = csv.DictReader(f)
        header, current = reader.fieldnames, list(reader)
    identities = {r[key] for r in current}
    if DRY:
        assert all(r[key] in identities for r in additions), name
        return
    for r in additions:
        if r[key] not in identities:
            assert set(r) == set(header), (name, set(r) ^ set(header))
            current.append(r)
            identities.add(r[key])
    if DRY:
        assert all(r[key] in identities for r in additions), name
        return
    with (BASE / name).open('w', encoding='utf-8', newline='') as f:
        w = csv.DictWriter(f, fieldnames=header)
        w.writeheader()
        w.writerows(current)

api = read('08_OPENAPI_FULL_V1.0.20_FROZEN.yaml')
schemas = api['components']['schemas']
# Public nested presentations are permission-filtered; internal source evidence stays complete.
for field in ('samplingRecords', 'reports'):
    if field in schemas['FinishedInspectionRequest']['required']:
        schemas['FinishedInspectionRequest']['required'].remove(field)
schemas['FinishedInspectionRequest']['description'] = 'Sampling/report collections omitted when their dedicated view permission is absent; no fake empty collection. Internal evidence is complete.'
save('08_OPENAPI_FULL_V1.0.20_FROZEN.yaml', api)
ops = read('00_FINISHED_GOODS_OPERATIONS.json')
task = 'DCP-FINISHED-GOODS-CHAIN-001'
append('08_OPENAPI_ENDPOINT_CATALOG_V1.0.20_FROZEN.csv', [dict(zip(
    ['Method','Path','OperationId','Tag','Permission','MES Task','Audit Required'],
    [o['method'],o['path'],api['paths'][o['path']][o['method'].lower()]['operationId'],'finished-goods',o['permission'],task,str(o['method'] == 'POST')])) for o in ops], 'OperationId')
ui = rows('00_FINISHED_GOODS_UI_ROUTE_MATRIX_V1.0.20.csv')
append('10_UI_PAGE_ROUTE_MATRIX_V1.0.20_FROZEN.csv', [dict(zip(
    ['Page ID','Name','Type','Route','Requirement','Permission'],
    ['UI-FG-' + str(i+1).zfill(2), r['route'],r['template'],r['route'],'FG-01..13',r['permission']])) for i,r in enumerate(ui)], 'Page ID')
rtm = rows('00_FINISHED_GOODS_RTM_V1.0.20.csv')
append('12_TEST_CASE_CATALOG_V1.0.20_FROZEN.csv', [dict(zip(
    ['Test Case ID','Requirement ID','MES Task','Module','Title','Type','Preconditions','Steps','Expected Result','Evidence','Automation'],
    [r['test'],r['requirement'],task,'WMS/QMS/QA/Trace',r['scenario'],'NORMAL/NEGATIVE','actual test-owned production and frozen standard','See approved contract FG acceptance scenario',r['scenario'],'docs/acceptance/finished-goods-chain-2026-10-06/REPORT.md','Native MariaDB + targeted Chromium'])) for r in rtm], 'Test Case ID')
append('12_TEST_COVERAGE_MATRIX_V1.0.20.csv', [dict(zip(['Dimension','ID','Test Cases','Count'],['Requirement',r['requirement'],r['test'],'1'])) for r in rtm], 'ID')
append('13_REQUIREMENT_TRACEABILITY_MATRIX_V1.0.20_FROZEN.csv', [dict(zip(
    ['Requirement ID','Title','Database','API','UI','Task','Test','Status'],
    [r['requirement'],r['scenario'],'00_FINISHED_GOODS_DATABASE_V1.0.20.md','00_FINISHED_GOODS_OPERATIONS.json','00_FINISHED_GOODS_UI_ROUTE_MATRIX_V1.0.20.csv',task,r['test'],'FROZEN CONTRACT; runtime readiness in MES_TASKS.md'])) for r in rtm], 'Requirement ID')
integration = rows('00_FINISHED_GOODS_INTEGRATION_V1.0.20.csv')
append('15_INTEGRATION_CONTRACT_MATRIX_V1.0.20_FROZEN.csv', [dict(zip(
    ['Producer','Contract','Consumer','Source of Truth','Transaction/Event','Failure Behavior','Regression Scope'],
    [r['producer'],'FG: '+r['contract'],r['consumer'],r['contract'],'Root locks/current reads; signed transaction',r['invariant'],'FG-01..13 + direct QA/QC/archive regressions'])) for r in integration], 'Contract')
append('15_TASK_DEPENDENCY_MATRIX_V1.0.20_FROZEN.csv', [dict(zip(
    ['Task','Capability','Hard Dependency','Soft Dependency','Produces Contract For','Primary Contract'],
    [task,'Approved finished-goods chain maintenance','MES-008A;MES-009;MES-010;MES-011;MES-012;MES-013','None','WMS/QMS/QA/eBR/Trace','00_FINISHED_GOODS_CONTRACT_V1.0.20.md']))], 'Task')
for name in ['15_MIGRATION_DEPENDENCY_MATRIX_V1.0.20_FROZEN.csv','15_LOGICAL_TO_PHYSICAL_MIGRATION_LEDGER_V1.0.20.csv']:
    append(name, [dict(zip(
        ['Logical Migration Group','Original Section 15 Label','Task','Name','Tables/Capability','Depends On','Physical Mapping','Status'],
        ['LG-FG-'+v,v,task,n,'00_FINISHED_GOODS_DATABASE_V1.0.20.md',dependency,v+'__'+n+'.sql','APPLIED; append-only'])) for v,n,dependency in [('V030','finished_goods_controlled_chain','native successful V029'),('V031','finished_fact_preservation','native successful V030')]], 'Logical Migration Group')

checks = {}
parent_hashes = read('00_FINISHED_GOODS_PARENT_HASHES.json')
checks['parent_frozen_bytes_unchanged'] = all(hashlib.sha256((PARENT/p).read_bytes()).hexdigest() == h for p,h in parent_hashes.items())
old_api = json.loads((PARENT/'08_OPENAPI_FULL_V1.0.19_FROZEN.yaml').read_text(encoding='utf-8'))
checks['inherited_endpoints_unchanged'] = all(api['paths'][p] == v for p,v in old_api['paths'].items())
allowed_schema_delta = {'FinishedDecision','BatchEbrReadModel'}
checks['inherited_schemas_unchanged_except_approved_evidence'] = all(schemas[n] == v for n,v in old_api['components']['schemas'].items() if n not in allowed_schema_delta)
refs = []
def collect(value):
    if isinstance(value, dict):
        if '$ref' in value: refs.append(value['$ref'])
        for v in value.values(): collect(v)
    elif isinstance(value, list):
        for v in value: collect(v)
collect(api)
checks['openapi_refs_resolve'] = all(r.startswith('#/components/') and r.split('/')[-1] in api['components'][r.split('/')[2]] for r in refs)
security = (ROOT/'backend/mes-security/src/main/java/com/hospital/mes/security/SecurityConfiguration.java').read_text(encoding='utf-8')
controllers = '\n'.join((ROOT/p).read_text(encoding='utf-8') for p in ['backend/mes-wms/src/main/java/com/hospital/mes/wms/api/FinishedGoodsController.java','backend/mes-qms/src/main/java/com/hospital/mes/qms/api/FinishedInspectionController.java','backend/mes-boot/src/main/java/com/hospital/mes/configuration/FinishedChainController.java'])
checks['24_operations_match_catalog_security'] = len(ops) == 24 and all(o['permission'] in security and api['paths'][o['path']][o['method'].lower()]['x-permission'] == o['permission'] for o in ops)
checks['closed_command_schemas'] = all(schemas[o['request']].get('additionalProperties') is False for o in ops if o.get('request'))
checks['controller_source_exists'] = all(n in controllers for n in ['finished-inbound-requests','finished-shipments','finished-inspection-requests','finished-sampling-records','finished-inspection-reports','finished-material-lots'])
sql = (ROOT/'backend/mes-boot/src/main/resources/db/migration/V030__finished_goods_controlled_chain.sql').read_text(encoding='utf-8')
new_perms = {o['permission'] for o in ops if 'finished-' in o['permission']}
checks['18_new_permissions_seeded'] = len(new_perms) == 18 and all(p in sql for p in new_perms)
checks['six_normalized_tables'] = len(re.findall(r'CREATE TABLE ',sql)) == 6
checks['migration_documents_exact'] = all((ROOT/'backend/mes-boot/src/main/resources/db/migration'/v).read_text(encoding='utf-8') in (BASE/'00_FINISHED_GOODS_DATABASE_V1.0.20.md').read_text(encoding='utf-8') for v in ['V030__finished_goods_controlled_chain.sql','V031__finished_fact_preservation.sql'])
changed_migrations = subprocess.check_output(['git','diff','--name-only','--','backend/mes-boot/src/main/resources/db/migration'],cwd=ROOT,text=True).splitlines()
checks['no_executed_migration_modified'] = not changed_migrations
router = (ROOT/'frontend/mes-web/src/router/index.ts').read_text(encoding='utf-8')
checks['13_new_ui_routes_and_T1_T6_only'] = all(r['route'][1:] in router and r['template'] in ['T1','T2','T3','T4','T6'] for r in ui if r['route'].startswith('/finished/'))
checks['13_requirements_tests_rtm'] = len(rtm) == 13 and {r['requirement'] for r in rtm} == {f'FG-{i:02}' for i in range(1,14)}
authority_files = ['AGENTS.md','MES_TASKS.md','docs/PROJECT_BASELINE.md','docs/ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md','docs/ui/MES_PAGE_TEMPLATE_STANDARD_V2.md']
authority_flags = ['v1.0.20' in '\n'.join((ROOT/p).read_text(encoding='utf-8').splitlines()[:13]) for p in authority_files]
checks['authority_pointers_consistent'] = all(authority_flags) or not any(authority_flags)
review = dict(status='PASS' if all(checks.values()) else 'FAIL', approvedDcp=task, parentBaseline='v1.0.19',candidateBaseline='v1.0.20', authoritySwitched=all(authority_flags),parentFilesVerified=len(parent_hashes),operations=len(ops),newPermissions=len(new_perms),checks=checks,notes=['Current review applies to approved finished delta. Historical inherited reviews remain prior-delta evidence.','Runtime readiness/explicit human acceptance only MES_TASKS.md.','QA original failures require signed investigation; public nested reads honor dedicated permissions.'])
save('00_FINISHED_GOODS_CONSISTENCY_REVIEW.json',review)
print(json.dumps(review, ensure_ascii=True, indent=2))
if not all(checks.values()): raise SystemExit(1)
