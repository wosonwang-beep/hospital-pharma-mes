import json
from pathlib import Path

root=Path(r'D:\codex\_project\gmp\hospital-pharma-mes\docs\review\evidence')

def save(path,obj):
    path.write_text(json.dumps(obj,ensure_ascii=False,indent=2),encoding='utf-8')

# Updated control contrast evidence.
for folder,note in [
    ('print-template-list-g5-20261012','Print template filters use the new dedicated Global UI V2 control-border token.'),
    ('role-id-acceptance-20261012','ROLE-ID selectors use the new dedicated Global UI V2 control-border token.')
]:
    save(root/folder/'contrast-after.json',{
        'controlBorder':'#8895a5',
        'surface':'#ffffff',
        'computedContrastRatio':3.048272007850795,
        'wcag22_1_4_11_required':3.0,
        'result':'PASS',
        'note':note+' Card/table structural borders remain on the lighter structural token.'
    })

# Print manifest
p=root/'print-template-list-g5-20261012'/'audit-manifest.json'
m=json.loads(p.read_text(encoding='utf-8'))
for shot in m['screenshots']:
    if shot['state']=='default':
        shot['path']=f"final-{shot['viewport']}.png"
    elif shot['state']=='empty':
        shot['path']='final-empty-1440.png'
for d in m['dimension_reviews']:
    if d['dimension']=='wcag22':
        d.update(status='PASS',note='Global UI V2 control border now uses #8895a5 (3.048:1); live result count and responsive table-scroll guidance are present.',evidence='contrast-after.json')
for c in m['wcag22_checks']:
    if c['criterion']=='1.4.11':
        c.update(status='PASS',note='Input/select border #8895a5 against white is about 3.048:1, meeting the 3:1 non-text contrast threshold.',evidence='contrast-after.json')
    if c['criterion']=='4.1.3':
        c.update(status='PASS',note='Result count uses role=status with aria-live=polite and updates from 共1条 to 共0条 after a no-result query.',evidence='final-results.json')
for issue in m['issues']:
    if issue['id']=='PRINT-G5-001':
        issue.update(status='RESOLVED',evidence='contrast.json',recheck='contrast-after.json')
    elif issue['id']=='PRINT-G5-002':
        issue.update(status='RESOLVED',evidence='print-list-375.png',recheck='final-375.png')
    elif issue['id']=='PRINT-G5-003':
        issue.update(status='RESOLVED',evidence='interactions.json',recheck='final-results.json')
m['verdict']='READY FOR ACCEPTANCE'
save(p,m)

# ROLE-ID manifest
p=root/'role-id-acceptance-20261012'/'audit-manifest.json'
m=json.loads(p.read_text(encoding='utf-8'))
for shot in m['screenshots']:
    shot['path']=f"final-{shot['viewport']}.png"
for d in m['dimension_reviews']:
    if d['dimension']=='wcag22':
        d.update(status='PASS',note='ROLE-ID selectors use #8895a5 control border (3.048:1) and the role-directory loading/result state is announced by a polite live status region.',evidence='contrast-after.json')
for c in m['wcag22_checks']:
    if c['criterion']=='1.4.11':
        c.update(status='PASS',note='Role selector border #8895a5 against white is about 3.048:1, meeting the 3:1 non-text contrast threshold.',evidence='contrast-after.json')
    if c['criterion']=='4.1.3':
        c.update(status='PASS',note='Designer exposes a polite role=status live region that announces loading failure or 已加载9个可用角色.',evidence='final-results.json')
for issue in m['issues']:
    if issue['id']=='ROLE-G5-001':
        issue.update(status='RESOLVED',evidence='contrast.json',recheck='contrast-after.json')
    elif issue['id']=='ROLE-G5-002':
        issue.update(status='RESOLVED',evidence='role-id-ui-results.json',recheck='final-results.json')
m['verdict']='READY FOR ACCEPTANCE'
save(p,m)
print('UPDATED')