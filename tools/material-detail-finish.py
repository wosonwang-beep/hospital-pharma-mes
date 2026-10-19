from pathlib import Path
p=Path(r'D:\codex\_project\gmp\hospital-pharma-mes\frontend\mes-web\src\views\master\MaterialDetailView.vue')
s=p.read_text(encoding='utf-8')
a=s.index('<a-tabs v-if="readonly"')
b=s.index('<a-card v-if="!readonly||activeTab',a)
s=s[:a]+s[b:]
s=s.replace('<a-card v-if="!readonly||activeTab===\'basic\'||activeTab===\'quality\'"','<a-card')
s=s.replace(" :class=\"{'material-quality-only':readonly&&activeTab==='quality'}\"",'')
s=s.replace('v-if="!readonly||activeTab===\'basic\'"','')
s=s.replace('v-if="!readonly||activeTab===\'suppliers\'"','')
a=s.find('<a-card v-if="readonly&&activeTab===\'usage\'"')
if a!=-1:
 b=s.index('<a-card title=',a)
 s=s[:a]+s[b:]
s=s.replace("const activeTab=ref('basic')","")
s=s.replace('<p v-if="create" class="muted">','<a-card v-if="create" title="状态" class="form-section material-audit-card"><a-tag>启用（保存后）</a-tag></a-card><p v-if="create" class="muted">')
p.write_text(s,encoding='utf-8')
print('tabs removed and material details unified')
