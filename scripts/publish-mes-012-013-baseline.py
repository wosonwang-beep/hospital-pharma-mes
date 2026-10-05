"""Switch reviewed authorized baseline locally; no git publish or runtime completion claim."""
from pathlib import Path
import json,hashlib
root=Path(__file__).resolve().parents[1]
b=root/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16'
review=json.loads((b/'00_MES_012_013_CONSISTENCY_REVIEW.json').read_text(encoding='utf8'))
assert not any(review[k] for k in ['oldReleaseChanged','unresolvedOpenapiRefs','contractErrors'])
review.update(status='REVIEWED / AUTHORITY SWITCH APPROVED',independentReview='mes012_contract_consistency: HIGH corrections closed; final rereview no blocking findings; runtime pending',physicalAllocation='Native DevelopmentHistoryReadIT highest24/success24/fail0; nextV025')
(b/'00_MES_012_013_CONSISTENCY_REVIEW.json').write_text(json.dumps(review,indent=2),encoding='utf8')
p=b/'00_DESIGN_CHANGE_DCP-MES-012-013-CONTRACT-001_APPROVED.md'
s=p.read_text(encoding='utf8').replace('This release remains CANDIDATE until consistency review and authority switch','Consistency review completed on 2026-10-05 with no remaining blocking findings; this cumulative release is authoritative')
p.write_text(s,encoding='utf8')
p=b/'00_MANIFEST_FINAL_FROZEN_V1.0.16.md'
s=p.read_text(encoding='utf8')
if '## Reviewed MES-012/013 cumulative authority' not in s:
    s+='\n\n## Reviewed MES-012/013 cumulative authority\n\nDCP-MES-012-013-CONTRACT-001 approved by the user. Independent consistency review completed before pointer switch. Current complete byte manifest: [00_MES_012_013_MANIFEST.json](00_MES_012_013_MANIFEST.json). Old embedded supplement manifests remain historical; this current manifest governs the complete cumulative directory. Runtime/acceptance evidence is separate.\n'
p.write_text(s,encoding='utf8')
for filename in ['AGENTS.md','docs/PROJECT_BASELINE.md']:
    p=root/filename;s=p.read_text(encoding='utf-8-sig')
    if filename=='AGENTS.md':s=s.replace('FINAL BASELINE COMPLETE v1.0.15','FINAL BASELINE COMPLETE v1.0.16',1)
    else:
        s=s.replace('Authoritative design: `FINAL BASELINE COMPLETE v1.0.15`','Authoritative design: `FINAL BASELINE COMPLETE v1.0.16`')
        s=s.replace('HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.15/','HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/')
        s=s.replace('00_MANIFEST_FINAL_FROZEN_V1.0.15.md','00_MANIFEST_FINAL_FROZEN_V1.0.16.md')
        if 'Current authorized completion: user approved DCP-MES-012-013-CONTRACT-001' not in s:
            s=s.replace('## Authority','## Authority\n\n- Current authorized completion: user approved DCP-MES-012-013-CONTRACT-001 on 2026-10-04; reviewed cumulative v1.0.16 is authoritative after 2026-10-05 consistency review. Production quality, material balance, finished QA/archive and narrow producer/consumer extensions are authorized. v1.0.15 remains immutable historical authority; runtime completion is not inferred. Read [current contract](../releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/00_MES_012_013_COMPLETION_CONTRACT_V1.0.16.md).',1)
    p.write_text(s,encoding='utf8')
p=root/'MES_TASKS.md';s=p.read_text(encoding='utf-8-sig')
lines=[]
for line in s.splitlines():
    if line.startswith('| MES-'):line=line.replace('HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.15/','HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16/')
    if line.startswith('This is the sole task-status index'):line=line.replace('v1.0.15','v1.0.16')
    lines.append(line)
s='\n'.join(lines)+'\n'
if '## Reviewed MES-012/013 contract publication — 2026-10-05' not in s:
    s+='\n## Reviewed MES-012/013 contract publication — 2026-10-05\n\nv1.0.16 is the reviewed authoritative cumulative release under explicit DCP-MES-012-013-CONTRACT-001 approval. OpenAPI references resolve,35 added/typed operation entries checked, scoped independent HIGH findings closed, v1.0.15 remains unchanged. Native physical history24 successes/0 failures, highest24; planned V025 is not yet applied. Design publication does not imply runtime completion. MES-012 IN PROGRESS, MES-013 NOT STARTED until actual producer contracts are available.\n'
p.write_text(s,encoding='utf8')
manifest={str(p.relative_to(b)).replace('\\','/'):hashlib.sha256(p.read_bytes()).hexdigest() for p in b.rglob('*') if p.is_file() and not p.name.endswith('_MANIFEST.json')}
(b/'00_MES_012_013_MANIFEST.json').write_text(json.dumps(manifest,indent=2),encoding='utf8')
print('v1.0.16 authority switched after independent review; runtime acceptance NOT CLAIMED')
