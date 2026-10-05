"""Validate the candidate delta and refresh its manifest; never switches authority."""
from pathlib import Path
import hashlib, json, subprocess
root=Path(__file__).resolve().parents[1]
b=root/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.16'
api=json.loads((b/'08_OPENAPI_FULL_V1.0.16_FROZEN.yaml').read_text(encoding='utf-8'))
def refs(x):
    if isinstance(x,dict):
        if '$ref' in x:yield x['$ref']
        for v in x.values():yield from refs(v)
    elif isinstance(x,list):
        for v in x:yield from refs(v)
missing=[]
for r in refs(api):
    if r.startswith('#/'):
        n=api
        try:
            for part in r[2:].split('/'):n=n[part.replace('~1','/').replace('~0','~')]
        except KeyError:missing.append(r)
old='releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.15'
changed=subprocess.check_output(['git','diff','--name-only','HEAD','--',old],cwd=root,text=True).splitlines()
operations=json.loads((b/'00_MES_012_013_OPERATIONS.json').read_text(encoding='utf-8'))
errors=[]
for item in operations:
    op=api['paths'][item['path']][item['method']]
    if op['x-permission']!=item['permission']:errors.append(item['path']+' permission')
    if item['request']:
        schema=api['components']['schemas'][item['request']]
        if schema.get('additionalProperties') is not False:errors.append(item['request']+' open properties')
        if not set(schema['required'])<=set(schema['properties']):errors.append(item['request']+' invalid required')
    if item['method']!='get' and not op.get('x-audit-required'):errors.append(item['path']+' missing audit')
review_path=b/'00_MES_012_013_CONSISTENCY_REVIEW.json'
previous=json.loads(review_path.read_text(encoding='utf-8')) if review_path.exists() else {}
result={**previous,'status':previous.get('status','CANDIDATE / NOT SWITCHED'),'oldReleaseChanged':changed,'unresolvedOpenapiRefs':sorted(set(missing)),'contractErrors':errors,'operationsChecked':len(operations),'runtimeAcceptance':'NOT CLAIMED'}
(b/'00_MES_012_013_CONSISTENCY_REVIEW.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
manifest={str(p.relative_to(b)).replace('\\','/'):hashlib.sha256(p.read_bytes()).hexdigest() for p in b.rglob('*') if p.is_file() and not p.name.endswith('_MANIFEST.json')}
(b/'00_MES_012_013_MANIFEST.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
print(json.dumps(result))
if missing or changed or errors:raise SystemExit(1)
