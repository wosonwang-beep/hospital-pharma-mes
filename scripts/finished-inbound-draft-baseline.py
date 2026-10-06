"""Build/check the bounded approved v21 successor without modifying frozen v20."""
from pathlib import Path
import copy, csv, hashlib, json, re, shutil, subprocess, sys

ROOT = Path(__file__).resolve().parents[1]
PARENT = ROOT / 'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.20'
BASE = ROOT / 'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.21'
DCP = 'DCP-FINISHED-INBOUND-INSPECTION-DRAFT-001'
CONTRACT = '00_FINISHED_INBOUND_DRAFT_CONTRACT_V1.0.21.md'
REVIEW = '00_FINISHED_INBOUND_DRAFT_CONSISTENCY_REVIEW.json'
MANIFEST = '00_FINISHED_INBOUND_DRAFT_MANIFEST.json'
EVIDENCE = 'docs/acceptance/finished-inbound-inspection-draft-2026-10-06/REPORT.md'

def hashes(folder, excludes=()):
    return {p.relative_to(folder).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest()
            for p in sorted(folder.rglob('*')) if p.is_file() and p.name not in excludes}

def read(name):
    return json.loads((BASE / name).read_text(encoding='utf-8'))

def save(name, value):
    (BASE / name).write_text(json.dumps(value, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')

def append(name, values):
    with (BASE/name).open(encoding='utf-8-sig', newline='') as f:
        reader = csv.DictReader(f)
        header, rows = reader.fieldnames, list(reader)
    assert all(len(v) == len(header) for v in values), name
    with (BASE/name).open('w', encoding='utf-8', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(header)
        writer.writerows([[r[k] for k in header] for r in rows]+values)

scenarios = [
    ('FD-01', 'Unchecked create preserves old command; no child or stock'),
    ('FD-02', 'Atomic linked DRAFT; actual frozen plan; idempotent replay'),
    ('FD-03', 'Warehouse confirmation gates submit/accept/sample/QA; normal chain afterwards'),
    ('FD-04', 'Permission/invalid input/child audit failure roll back pair and replay; lost permission blocks replay'),
    ('FD-05', 'Cancelled inbound retains child draft but cannot progress'),
    ('FD-06', 'PC T2 option and exact payload; T3 actual child link and execution prerequisite'),
]

def build():
    if BASE.exists():
        raise SystemExit('Successor exists: refuse overwrite')
    parent_hashes = hashes(PARENT)
    shutil.copytree(PARENT, BASE)
    for p in list(BASE.rglob('*')):
        if not p.is_file(): continue
        if p.suffix in {'.md', '.csv', '.yaml', '.json', '.html', '.js', '.mjs', '.txt'}:
            p.write_text(p.read_text(encoding='utf-8-sig').replace('v1.0.20', 'v1.0.21').replace('V1.0.20', 'V1.0.21'), encoding='utf-8')
        if '1.0.20' in p.name:
            p.rename(p.with_name(p.name.replace('1.0.20', '1.0.21')))
    save('00_FINISHED_INBOUND_DRAFT_PARENT_HASHES.json', parent_hashes)
    contract = (ROOT/'docs/development'/f'{DCP}-APPROVED.md').read_text(encoding='utf-8')
    (BASE/CONTRACT).write_text(contract, encoding='utf-8')
    (BASE/f'00_DESIGN_CHANGE_{DCP}_APPROVED.md').write_text(contract, encoding='utf-8')
    note = ('\n\n## Approved optional inbound inspection draft — v1.0.21\n\n'
            f'[{CONTRACT}]({CONTRACT}) is the controlling bounded supplement: optional atomic linked inspection DRAFT during inbound creation; warehouse confirmation remains mandatory before execution. Existing tables/states/signatures/permissions/routes and 201 response unchanged. Only FinishedInboundCreate gains conditional optional command fields. No migration (native highest31); no physical stock from drafts. Existing UI T2/T3 only, no additional finished-image layout/menu scope. Test/RTM FD-01..06 and synchronous domain-event integration below are cumulative. Inherited reviews/manifests attest prior deltas, not current cumulative bytes. Current draft manifest/consistency review and SHA256SUMS govern this release. Readiness and human acceptance are only in MES_TASKS.md.\n')
    for p in BASE.glob('*.md'):
        if re.match(r'^(?:0[1-9]|1[0-5])_', p.name) or p.name == 'README.md':
            p.write_text(p.read_text(encoding='utf-8')+note, encoding='utf-8')
    for task in ('008', '008A', '009', '010', '012', '013'):
        for p in (BASE/'tasks').glob('MES-'+task+'*.md'):
            p.write_text(p.read_text(encoding='utf-8')+note.replace(f']({CONTRACT})', f'](../{CONTRACT})'), encoding='utf-8')
    api = read('08_OPENAPI_FULL_V1.0.21_FROZEN.yaml')
    api['info']['version'] = '1.0.21'
    original = copy.deepcopy(api['components']['schemas']['FinishedInboundCreate'])
    unchecked = copy.deepcopy(original)
    unchecked['properties']['createInspectionDraft'] = {'type': 'boolean', 'enum': [False]}
    checked = copy.deepcopy(original)
    checked['properties'].update(createInspectionDraft={'type': 'boolean', 'enum': [True]},
        inspectionRequestNo={'type': 'string', 'minLength': 1, 'maxLength': 80, 'pattern': r'(?s).*\S.*'})
    checked['required'] += ['createInspectionDraft', 'inspectionRequestNo']
    api['components']['schemas']['FinishedInboundCreate'] = {
        'description': 'Omitted/false: original create. True: requires actual inspectionRequestNo and BOTH existing create permissions, including replay. Synchronous atomic DRAFT pair, unchanged 201 inbound response. No receiving/QC acceptance/stock effect; warehouse confirmation still gates inspection submit/accept/sample.',
        'oneOf': [unchecked, checked]}
    save('08_OPENAPI_FULL_V1.0.21_FROZEN.yaml', api)
    append('10_UI_PAGE_ROUTE_MATRIX_V1.0.21_FROZEN.csv', [
        ['UI-FD-01', 'Inbound optional inspection draft control', 'T2', '/finished/inbound/create', 'FD-01;FD-02;FD-06', 'wms:finished-inbound:create; optional qms:finished-request:create'],
        ['UI-FD-02', 'Inbound linked actual inspection draft', 'T3', '/finished/inbound/:id', 'FD-03;FD-05;FD-06', 'wms:finished-inbound:view; optional qms:finished-request:view']])
    append('12_TEST_CASE_CATALOG_V1.0.21_FROZEN.csv', [
        [f'TC-{req}', req, DCP, 'WMS/QMS', title, 'NORMAL/NEGATIVE', 'Actual completed batch and frozen plan', 'Approved DCP acceptance case '+req, title, EVIDENCE, 'Native MariaDB / targeted PC Chromium'] for req, title in scenarios])
    append('12_TEST_COVERAGE_MATRIX_V1.0.21.csv', [['Requirement', req, 'TC-'+req, '1'] for req, _ in scenarios])
    append('13_REQUIREMENT_TRACEABILITY_MATRIX_V1.0.21_FROZEN.csv', [
        [req, title, 'Existing wms_finished_inbound_request/qms_finished_inspection_request; no schema delta', 'FinishedInboundCreate; unchanged existing endpoints', 'Existing inbound T2/T3; unchanged request T3', DCP, 'TC-'+req, 'FROZEN CONTRACT; readiness only MES_TASKS.md'] for req, title in scenarios])
    append('15_INTEGRATION_CONTRACT_MATRIX_V1.0.21_FROZEN.csv', [
        ['WMS inbound create', 'FD: FinishedInboundInspectionDraftRequested', 'boot synchronous listener -> QMS early draft', 'Existing scoped inbound and actual frozen quality plan', 'Original actor/org; MANDATORY same transaction, existing per-operation idempotency', 'Any permission/child/audit failure rolls back both records and keys', 'FD-01..06; direct confirmed receiving/full finished chain']])
    append('15_TASK_DEPENDENCY_MATRIX_V1.0.21_FROZEN.csv', [
        [DCP, 'Optional atomic finished inspection draft', 'DCP-FINISHED-GOODS-CHAIN-001;MES-001;MES-008A;MES-009;MES-010;MES-012;MES-013', 'None', 'WMS/QMS/QA/eBR/Trace existing consumers', CONTRACT]])
    for name in ['15_MIGRATION_DEPENDENCY_MATRIX_V1.0.21_FROZEN.csv', '15_LOGICAL_TO_PHYSICAL_MIGRATION_LEDGER_V1.0.21.csv']:
        append(name, [['LG-FD-NONE', 'NONE', DCP, 'optional_finished_inspection_draft', 'Existing tables/PK/FK/indexes; no physical schema change', 'Applied V030/V031', 'NONE; highest successful31 unchanged', 'NO MIGRATION']])
    (BASE/'00_MANIFEST_FINAL_FROZEN_V1.0.21.md').write_text(
        '# FINAL BASELINE COMPLETE v1.0.21\n\nCandidate — not yet published.\n\n'+note, encoding='utf-8')
    save(REVIEW, {'status': 'PENDING', 'approvedDcp': DCP})
    print('Candidate built; '+str(len(parent_hashes))+' frozen v20 parent files untouched.')

def checks():
    api = read('08_OPENAPI_FULL_V1.0.21_FROZEN.yaml')
    old = json.loads((PARENT/'08_OPENAPI_FULL_V1.0.20_FROZEN.yaml').read_text(encoding='utf-8'))
    schema = api['components']['schemas']['FinishedInboundCreate']
    parent_hashes = read('00_FINISHED_INBOUND_DRAFT_PARENT_HASHES.json')
    branches = schema.get('oneOf', [])
    c = {}
    c['all_138_frozen_parent_files_unchanged'] = parent_hashes == hashes(PARENT) and len(parent_hashes) == 138
    c['all_existing_endpoints_permissions_201_read_models_unchanged'] = api['paths'] == old['paths'] and all(
        s == api['components']['schemas'][n] for n, s in old['components']['schemas'].items() if n != 'FinishedInboundCreate')
    c['only_one_existing_command_schema_extended'] = set(api['components']['schemas']) == set(old['components']['schemas'])
    c['optional_closed_boolean_and_required_child_number'] = len(branches) == 2 and all(b['additionalProperties'] is False for b in branches) and branches[0]['properties']['createInspectionDraft']['enum'] == [False] and 'inspectionRequestNo' not in branches[0]['properties'] and branches[1]['properties']['createInspectionDraft']['enum'] == [True] and set(branches[1]['required']) == {'requestNo','mainBatchId','reason','createInspectionDraft','inspectionRequestNo'}
    refs = []
    def collect(v):
        if isinstance(v, dict):
            if '$ref' in v: refs.append(v['$ref'])
            for value in v.values(): collect(value)
        elif isinstance(v, list):
            for value in v: collect(value)
    collect(api)
    c['openapi_refs_resolve'] = all(r.startswith('#/components/') and r.split('/')[-1] in api['components'][r.split('/')[2]] for r in refs)
    def source(p): return (ROOT/p).read_text(encoding='utf-8')
    wms = source('backend/mes-wms/src/main/java/com/hospital/mes/wms/application/FinishedGoodsService.java')
    qms = source('backend/mes-qms/src/main/java/com/hospital/mes/qms/application/FinishedInspectionService.java')
    listener = source('backend/mes-boot/src/main/java/com/hospital/mes/configuration/FinishedInboundInspectionDraftListener.java')
    ui = source('frontend/mes-web/src/views/finished/FinishedDetailView.vue')
    c['synchronous_scoped_mandatory_bridge'] = all(s in listener for s in ['@EventListener', 'Propagation.MANDATORY', 'event.organizationId()', 'event.actorId()', 'quality.createInboundDraft']) and '@Async' not in listener and 'events.publishEvent(' in wms
    c['permission_before_idempotent_replay'] = wms.index('if(draft)mutations.context("qms:finished-request:create")') < wms.index('return mutations.execute(c,"FINISHED_INBOUND_CREATE"')
    c['confirmed_execution_gate_and_draft_actions'] = 'warehouse.confirmed(c.organizationId(),batch)' in qms and 'Propagation.MANDATORY' in qms and '"CONFIRMED".equals(warehouse.inbound(row.getOrgId(),row.getInboundRequestId()).path("status").asText())' in qms
    c['existing_T2_T3_permission_scoped_controls'] = all(s in ui for s in ['inboundCreateBody', "auth.can('qms:finished-request:create')", "auth.can('qms:finished-request:view')", '同时生成成品请验草稿', 'r.inboundRequestId', 'url:definitions.requests!.api', "router.push('/finished/requests/'+linkedInspection.id)"])
    migrations = ROOT/'backend/mes-boot/src/main/resources/db/migration'
    database = (PARENT/'00_FINISHED_GOODS_DATABASE_V1.0.20.md').read_text(encoding='utf-8')
    c['executed_V030_V031_exact_frozen_sql_no_new_migration'] = all((migrations/v).read_text(encoding='utf-8') in database for v in ['V030__finished_goods_controlled_chain.sql','V031__finished_fact_preservation.sql']) and max(int(p.name[1:4]) for p in migrations.glob('V[0-9][0-9][0-9]__*.sql')) == 31
    c['tracked_executed_migrations_not_modified'] = not subprocess.check_output(['git','diff','--name-only','--',str(migrations)],cwd=ROOT,text=True).strip()
    c['FD_01_to_06_test_RTM_integration_dependency_migration_reconciled'] = all(req in source(str((BASE/'13_REQUIREMENT_TRACEABILITY_MATRIX_V1.0.21_FROZEN.csv').relative_to(ROOT))) for req,_ in scenarios) and DCP in (BASE/'15_TASK_DEPENDENCY_MATRIX_V1.0.21_FROZEN.csv').read_text(encoding='utf-8') and 'LG-FD-NONE' in (BASE/'15_MIGRATION_DEPENDENCY_MATRIX_V1.0.21_FROZEN.csv').read_text(encoding='utf-8') and 'FinishedInboundInspectionDraftRequested' in (BASE/'15_INTEGRATION_CONTRACT_MATRIX_V1.0.21_FROZEN.csv').read_text(encoding='utf-8')
    files = ['AGENTS.md','MES_TASKS.md','docs/PROJECT_BASELINE.md','docs/ui/MES_GLOBAL_UI_DESIGN_SYSTEM_V2.md','docs/ui/MES_PAGE_TEMPLATE_STANDARD_V2.md']
    flags = [('Current business authority v1.0.21' if p == 'MES_TASKS.md' else 'FINAL BASELINE COMPLETE v1.0.21') in '\n'.join(source(p).splitlines()[:13]) for p in files]
    c['current_pointers_all_parent_or_all_successor'] = all(flags) or not any(flags)
    review = dict(status='PASS' if all(c.values()) else 'FAIL', approvedDcp=DCP, parentBaseline='v1.0.20',candidateBaseline='v1.0.21',parentFilesVerified=len(parent_hashes),authoritySwitched=all(flags),newEndpoints=0,newPermissions=0,newMigrations=0,checks=c,
                  notes=['Cumulative successor: prior finished-goods delta inherited.','Runtime readiness and human acceptance only MES_TASKS.md.','No auto submit/QC/QA/stock; immutable sources/signatures/OOS remain unchanged.'])
    print(json.dumps(review, ensure_ascii=True, indent=2))
    if not all(c.values()): raise SystemExit(1)
    return review

def seal():
    if (BASE/MANIFEST).exists(): raise SystemExit('Published successor immutable')
    review = checks()
    if not review['authoritySwitched']: raise SystemExit('All current pointers required after candidate PASS')
    save(REVIEW, review)
    (BASE/'00_MANIFEST_FINAL_FROZEN_V1.0.21.md').write_text(
        '# FINAL BASELINE COMPLETE v1.0.21\n\nPublished cumulative release — AUTHORITATIVE, 2026-10-06.\n\n'
        f'User-approved {DCP} follows immutable v1.0.20. [Controlling supplement]({CONTRACT}), [consistency PASS]({REVIEW}), [current hashes]({MANIFEST}). All138 parent files unchanged; inherited v20 finished-goods/WMS/source contracts remain authoritative except the explicitly approved optional atomic draft creation.\n\n'
        'Only FinishedInboundCreate conditional optional fields and existing T2/T3 option/link change. No new endpoint/table/state/permission/route/signature/migration. Warehouse confirmation still gates submission/acceptance/sampling. QC PASS never releases stock. QA and FINAL PDF order unchanged. Native highest successful31 unchanged.\n\n'
        'Current manifest and SHA256SUMS govern cumulative bytes; copied older reviews/manifests are historical attestations. Global UI V2/T1–T6 unchanged. Unapproved workbench gaps/UI V3 excluded. Runtime readiness/human acceptance only MES_TASKS.md. Evidence: '+EVIDENCE+'. No full regression, automatic acceptance, commit or push inferred.\n', encoding='utf-8')
    current = hashes(BASE, (MANIFEST, 'SHA256SUMS.txt'))
    save(MANIFEST, dict(baseline='FINAL BASELINE COMPLETE v1.0.21',status='AUTHORITATIVE',approvedDcp=DCP,parent='v1.0.20',files=current))
    sums = hashes(BASE, ('SHA256SUMS.txt',))
    (BASE/'SHA256SUMS.txt').write_text(''.join(f'{h}  {p}\n' for p,h in sums.items()), encoding='utf-8')
    print('Sealed successor; frozen parent unchanged.')

if __name__ == '__main__':
    if '--build' in sys.argv: build()
    elif '--seal' in sys.argv: seal()
    elif '--check' in sys.argv:
        checks()
        if (BASE/MANIFEST).exists():
            assert read(MANIFEST)['files'] == hashes(BASE, (MANIFEST,'SHA256SUMS.txt'))
            for line in (BASE/'SHA256SUMS.txt').read_text(encoding='utf-8').splitlines():
                digest, name = line.split('  ', 1)
                assert hashlib.sha256((BASE/name).read_bytes()).hexdigest() == digest, name
            print('Current manifest/SHA256SUMS PASS')
    else: raise SystemExit('Use --build, --check or --seal')
