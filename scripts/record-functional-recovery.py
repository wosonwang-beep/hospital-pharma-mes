"""Record the verified, explicitly approved recovery and publish only its bounded contract."""
from pathlib import Path
import csv,hashlib,json,re,tempfile
root=Path(__file__).resolve().parents[1]
b=root/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.15'
old=root/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.14'
e=root/'docs/acceptance/functional-closure'
def read(p):return p.read_text(encoding='utf-8-sig')
def write(p,s):p.write_text(s,encoding='utf-8',newline='\n')
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
# A previous frozen release is checked, never edited.
old_count=0
for line in read(old/'00_SHA256SUMS_V1.0.14.txt').splitlines():
    expected,path=line.split('  ',1);assert digest(old/path)==expected, 'Immutable v1.0.14 mismatch: '+path;old_count+=1
schema=json.loads(read(e/'migration-failure/recovered-schema-verification.json'))
assert len(schema['history'])==24 and all(r['success']==1 for r in schema['history'])
api_path=b/'08_OPENAPI_FULL_V1.0.15_FROZEN.yaml';api=json.loads(read(api_path))
nodes=api['components']['schemas']['TraceNode']['properties']['type']['enum']
if 'INVENTORY_DECISION' not in nodes:nodes.append('INVENTORY_DECISION')
relation=api['components']['schemas']['TraceEdge']['properties']['relation']
if 'enum' in relation and 'INVENTORY_CONTROL' not in relation['enum']:relation['enum'].append('INVENTORY_CONTROL')
write(api_path,json.dumps(api,ensure_ascii=False,indent=2))
refs=[]
def check(node):
    if isinstance(node,dict):
        if '$ref' in node:
            ref=node['$ref'];assert ref.startswith('#/');target=api
            for part in ref[2:].split('/'):target=target[part.replace('~1','/').replace('~0','~')]
            refs.append(ref)
        for v in node.values():check(v)
    elif isinstance(node,list):
        for v in node:check(v)
check(api)
contract=b/'00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md'
s=read(contract)
s=s.replace('本发布为候选文档，尚未切换 authoritative pointer。V024 首次执行发生外键表名错误：实际批次事实源是 `md_material_lot`。第一次失败的迁移文件和原生失败历史保留；数据库恢复须按单独、透明的恢复提案取得例外授权。本契约不授权覆盖已执行迁移或修复历史。','本发布累计继承不可变 v1.0.14。用户另行明确批准 DCP-MIGRATION-V024-RECOVERY-001 的一次性例外：仅修正失败 V024、保留原失败证据、执行一次 repair 并正常迁移。V024 已成功，V001–V023 全部成功历史字段和脚本校验值不变。此例外不扩大为今后改写已执行迁移的授权；本发布经跨文档一致性复核后启用，发布不代表整项 MES 验收。')
if '## 10. 验收后的精确契约说明' not in s:s+='''

## 10. 验收后的精确契约说明

签名中的 BigDecimal 使用精确十进制字符串，禁止二进制浮点；历史签名封套不重新规范化或覆盖。动作 HTTP 的路径/分页参数显式命名，缺少必需头返回 INVALID_REQUEST/400；权限逐条对应正式动作，无角色隐式授权。

生产投料→MaterialLot 的追溯查询从库存决定事实表读取原事实，形成 INVENTORY_DECISION 节点、MATERIAL_LOT→INVENTORY_DECISION 的 INVENTORY_CONTROL 关系，以及决定→SIGNATURE 的既有 SIGNED_EVIDENCE 关系。PK/签名 ID 均为字符串。库存状态独立于 QA 质量状态，图中不维护另一套库存决定。

本轮完成 11 项功能/GxP/HTTP 原生用例、10 项真实竞争实例及 5 项直接回归；3 项 IPC 单元与前端 typecheck 通过。生产冻结 Gate 覆盖 Reserve/ConfirmIssue/Weigh/Charge 两种提交顺序。MariaDB 1020 按既有受控 409 返回，新事务重新读取后必须严格返回实际业务拒绝；拒绝请求无成功幂等/额外账事实。V2 IPC 定义不改变既有 V1 批次。

再认证 port 在原生夹具中使用 mock，签名、审计、SQL、业务动作与锁均真实；不能称为完整登录或真实令牌到期验收。并发试验沿用已批准的独立保留夹具，失败历史不删除；试验物料最终 REJECTED 且库存 BLOCKED/FROZEN，均禁止生产。本轮闭合三个授权缺口，其他正式 RTM 未闭合行不因此豁免。
'''
write(contract,s)
for pattern in ['04_DOMAIN_MODEL*md','05_DATABASE_DESIGN*md','07_STATE_MACHINE*md','08_API_DETAILED*md','09_FUNCTIONAL*md','10_UI_PAGE_DETAILED*md','11_GMP*md','12A_TEST_ACCEPTANCE*md','12_TEST_CASE_DETAILED*md','15_MODULE_DEPENDENCY*md','00_INCOMING_TRACE_STANDARD*md']:
    for p in b.glob(pattern):
        s=read(p)
        if '## Verified functional closure recovery — 2026-10-04' not in s:
            s+='\n\n## Verified functional closure recovery — 2026-10-04\n\nThe approved bounded delta and one-time V024 exception are recorded in [functional closure §§9–10](00_FUNCTIONAL_CLOSURE_CONTRACT_V1.0.15.md). V024 is successful; V001–V023 remain immutable. Trace inventory decisions use INVENTORY_DECISION / INVENTORY_CONTROL with original SIGNED_EVIDENCE, decimal signing values are exact strings, and mandatory missing headers return 400. Scope-level native verification PASS does not waive other formal task RTM or authorize full MES-012/013.\n'
            write(p,s)
for p in b.glob('*.csv'):
    rows=[row for row in csv.reader(read(p).splitlines()) if row]
    if 'MIGRATION_DEPENDENCY' in p.name:
        for row in rows[1:]:
            if row and row[0]=='LG-FUNCTIONAL-CLOSURE':row[-1]='SUCCESS V024; explicit one-time failed recovery; V001-V023 unchanged; original failure retained'
    if 'REQUIREMENT_TRACEABILITY' in p.name:
        for row in rows[1:]:
            if row and row[0] in ('WMS-ELG-001','QMS-IPC-001','MES-OP-001-CLEARANCE'):row[-1]='TARGETED NATIVE GATE PASS; full-task RTM review remains separate'
    assert all(len(r)==len(rows[0]) for r in rows),p.name+' width mismatch'
    with p.open('w',encoding='utf-8',newline='') as f:csv.writer(f,lineterminator='\n').writerows(rows)
rtm=b/'00_FUNCTIONAL_CLOSURE_RTM_V1.0.15.csv'
s=read(rtm).replace('qms_ipc_instance/results/review','qms_ipc_instance;qms_ipc_result_revision;qms_ipc_review').replace('mes_clearance_record/review','mes_clearance_record;mes_clearance_review');write(rtm,s)
consistency={'state':'PUBLISHED bounded approved contract','approval':'2026-10-04 user 批准方案; separate explicit V024 recovery approval','immutablePriorRelease':{'version':'1.0.14','verifiedHashes':old_count},'openapiReferences':{'result':'PASS','count':len(refs)},'csvWidths':'PASS','migration':'24 successful; V024 recovered once; old successful history unchanged','databaseDomainStateApiUiPermissionAuditSignatureTestRtmIntegration':'PASS for approved inventory/IPC/clearance scope; six physical owner tables; exact single facts; frozen IPC and controlled Gates','trace':'INVENTORY_DECISION/INVENTORY_CONTROL/SIGNED_EVIDENCE, original identities','nativeAcceptance':'26 distinct scoped native scenarios PASS; other full-task RTM unwaived','review':'Independent increment review: HIGH=0 CRITICAL=0; MEDIUM one-time-tool target guard fixed','limitation':'mocked reauthentication port and prior mocked browser API; not full login/token-expiry browser E2E','taskScope':'MES-012 IPC stage only; no CAPA/balance/MES-013 expansion'}
write(b/'00_FUNCTIONAL_CLOSURE_CONSISTENCY_REVIEW.json',json.dumps(consistency,ensure_ascii=False,indent=2))
sha_file=b/'00_SHA256SUMS_V1.0.15.txt';manifest_file=b/'00_FUNCTIONAL_CLOSURE_MANIFEST.json'
write(sha_file,'\n'.join(digest(p)+'  '+p.relative_to(b).as_posix() for p in sorted(b.rglob('*')) if p.is_file() and p not in (sha_file,manifest_file))+'\n')
manifest={p.relative_to(b).as_posix():digest(p) for p in sorted(b.rglob('*')) if p.is_file() and p!=manifest_file};write(manifest_file,json.dumps(manifest,indent=2))
assert all(digest(b/path)==sha for path,sha in json.loads(read(manifest_file)).items())
# All consistency checks above precede the authority switch.
project=root/'docs/PROJECT_BASELINE.md';s=read(project)
s=s.replace('v1.0.15 is a candidate, not the switched authoritative release; V024 first migration failure and pending transparent recovery are recorded in [functional closure evidence](acceptance/functional-closure/IMPLEMENTATION-2026-10-04.md). This approval does not authorize executed-migration repair or rewriting history.','v1.0.15 is now the authoritative cumulative release after cross-document consistency review. The user separately approved the one-time failed V024 recovery; migration and scoped native verification passed. Original failure evidence and bounded approval are retained in [recovery/acceptance evidence](acceptance/functional-closure/RECOVERY-2026-10-04.md); no general authorization to rewrite migration history is implied.')
s=s.replace('- Authoritative design: `FINAL BASELINE COMPLETE v1.0.14`.','- Authoritative design: `FINAL BASELINE COMPLETE v1.0.15`.').replace('- Release directory: [`releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.14/`](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.14/).','- Release directory: [`releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.15/`](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.15/).').replace('- Frozen manifest: [`00_MANIFEST_FINAL_FROZEN_V1.0.14.md`](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.14/00_MANIFEST_FINAL_FROZEN_V1.0.14.md).','- Frozen manifest: [`00_MANIFEST_FINAL_FROZEN_V1.0.15.md`](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.15/00_MANIFEST_FINAL_FROZEN_V1.0.15.md).')
write(project,s)
agents=root/'AGENTS.md';s=read(agents).replace('`FINAL BASELINE COMPLETE v1.0.14` remains authoritative','`FINAL BASELINE COMPLETE v1.0.15` remains authoritative',1);write(agents,s)
tasks=root/'MES_TASKS.md';s=read(tasks);parts=s.split('## MES-003–MES-005 pre-implementation review',1);head=parts[0].replace('v1.0.14','v1.0.15')
head=re.sub(r'Latest authorized increment.*?\n\n','Latest verified increment (2026-10-04): [approved V024 recovery and scoped functional closure](docs/acceptance/functional-closure/RECOVERY-2026-10-04.md). V024/24 migration validation PASS; 26 distinct native cases plus 3 IPC units PASS. Inventory freeze/unfreeze, production IPC and controlled clearance produced contracts are verified and available. MES-008/008A/010/011 remain IN PROGRESS pending the separate full-task RTM closure; MES-012 remains only its approved IPC stage. No human acceptance inferred. v1.0.15 is the reviewed authoritative design release.\n\n',head,count=1,flags=re.S)
write(tasks,head+('## MES-003–MES-005 pre-implementation review'+parts[1] if len(parts)>1 else ''))
print(f'Consistency PASS: {len(refs)} OpenAPI refs; CSV widths; {old_count} immutable v1.0.14 hashes; {len(manifest)} v1.0.15 hashes. Authority switched after checks; task acceptance unchanged.')
