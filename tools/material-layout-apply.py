from pathlib import Path
r=Path(r'D:\codex\_project\gmp\hospital-pharma-mes\frontend\mes-web\src')
p=r/'master/material.ts'
s=p.read_text(encoding='utf-8')
a=s.index('export const materialDisplaySections = [')
b=s.index('export const materialDisplayFields:',a)
s=s[:a]+"""export const materialDisplaySections = [
 {title:'基本信息',keys:['materialCode','materialName','materialType','specification','packUnitId','packSpec','baseUnitId','storageCondition','shelfLifeDays','requiresIncomingInspection','lotControlled','remark']}
]
"""+s[b:]
p.write_text(s,encoding='utf-8')
p=r/'views/master/MaterialDetailView.vue'
s=p.read_text(encoding='utf-8')
s=s.replace("const visibleSections=computed(()=>materialDisplaySections.filter(s=>!readonly.value||activeTab.value==='basic'||activeTab.value==='quality'&&s.title==='质量 / 管理属性'))","const visibleSections=computed(()=>materialDisplaySections)")
s=s.replace("const visibleSections=computed(()=>materialDisplaySections.filter(s=>!readonly.value||activeTab.value==='basic'||activeTab.value==='quality'&&s.title==='���� / ��������'))","const visibleSections=computed(()=>materialDisplaySections)")
s=s.replace("v-else-if=\"f.key==='remark'\"","v-else-if=\"f.key==='remark'\"")
s=s.replace('<textarea v-else-if="f.key===\'remark\'"','<input v-else-if="f.key===\'remark\'" type="text"')
s=s.replace('($event.target as HTMLTextAreaElement).value','($event.target as HTMLInputElement).value')
s += """
<style>
/* Approved material T2/T3: row-major 3-column fields, labels inline, one-line remark. */
#app .material-detail-page .material-basic-card .master-form {max-width:none;display:block;}
#app .material-detail-page .material-basic-card .material-field-section {display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px 18px;padding:16px 20px;border:0;}
#app .material-detail-page .material-basic-card .material-field-section .material-section-title {grid-column:1/-1;}
#app .material-detail-page .material-basic-card .material-field-section>label {display:grid;grid-template-columns:100px minmax(0,1fr);gap:8px;min-width:0;align-items:center;}
#app .material-detail-page .material-basic-card .material-field-section>label.form-full-row {grid-column:1/-1;}
#app .material-detail-page .material-basic-card .material-field-section>label.form-full-row input {width:100%;min-height:34px;}
@media(max-width:1150px){#app .material-detail-page .material-basic-card .material-field-section{grid-template-columns:repeat(2,minmax(0,1fr));}}
@media(max-width:760px){#app .material-detail-page .material-basic-card .material-field-section{grid-template-columns:1fr;}#app .material-detail-page .material-basic-card .material-field-section>label{grid-template-columns:96px minmax(0,1fr);}}
</style>
"""
p.write_text(s,encoding='utf-8')
print('updated field layout')
