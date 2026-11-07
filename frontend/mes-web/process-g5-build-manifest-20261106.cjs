const fs=require('fs'),crypto=require('crypto'),path=require('path');
const root=path.resolve(__dirname,'../..'),dir=path.join(root,'docs/development/process-form-cpp-ebr/20261106-formal-code/g5-readonly');
const read=(p)=>fs.readFileSync(p),sha=(p)=>crypto.createHash('sha256').update(read(p)).digest('hex');
const input=JSON.parse(read(path.join(dir,'browser-capture.json')));
const design=path.join(root,'docs/development/decisions/PROCESS_FORMAL_DIRECT_IMPLEMENTATION_20261106.md');
const approval=path.join(root,'docs/development/decisions/PROCESS_PC_ONLY_SCOPE_20261104_APPROVED.md');
const files=['frontend/mes-web/src/views/process/ProcessDetailView.vue','frontend/mes-web/src/views/ebr/DefinitionDesigner.vue','frontend/mes-web/src/views/ebr/CppRuleDesigner.vue'];
const sourceHashes=Object.fromEntries(files.map(p=>[p,sha(path.join(root,p))]));
const groups=['product_style','layout','forms_errors','table_actions','modal_feedback','a11y_keyboard','typography_color','vue_stack'];
const dims=['layout','typography','spacing_alignment','forms','tables_lists','query_filters','component_interaction','component_states','wcag22'];
const wcag=['1.3.1','1.4.1','1.4.3','1.4.4','1.4.10','1.4.11','2.1.1','2.1.2','2.4.3','2.4.7','2.4.11','2.5.3','2.5.7','2.5.8','3.3.1','3.3.2','3.3.3','4.1.2','4.1.3'];
const surfaces=[...new Set(input.screens.map(x=>x.surface))];
const shots=input.screens.map(s=>{
 const review={};for(const c of ['hierarchy','grid_alignment','spacing_density','typography_color','controls_content','actions','responsive','interaction_accessibility'])review[c]={status:'NOT RUN',note:'实际正式页面截图已采集，但这一截图的独立原尺寸八类审查及二次对抗复核尚未全部完成；不得替代通过证据。'};
 return {surface:s.surface,state:s.state,viewport:s.width,path:path.join(dir,s.file),sha256:sha(path.join(dir,s.file)),source_hash:sourceHashes[files[0]],review};
});
const d={
 audit_id:'PROCESS-FORMAL-G5-20261106',
 reviewer:{executor:'ChatGPT SELF-REVIEW',skill:'mes-ui-ux-audit',ui_ux_pro_max_path:'C:/Users/Administrator/.agents/skills/ui-ux-pro-max/SKILL.md',ui_ux_pro_max_sha256:sha('C:/Users/Administrator/.agents/skills/ui-ux-pro-max/SKILL.md')},
 source:{url:'http://127.0.0.1:5173/process/packages',source_hash:sourceHashes[files[0]],design_lock:design,dependency_hashes:sourceHashes},
 scope_note:'正式 MES G5 PC_ONLY: 1440/1280/900。375px手机端已依用户2026-11-04批准撤出范围；不是将未审格算PASS。',
 applicability:{mode:'PC_ONLY',approval,approval_sha256:sha(approval)},
 required_viewports:[1440,1280,900],
 expected_surfaces:surfaces.map(surface=>({surface,states:[...new Set(input.screens.filter(s=>s.surface===surface).map(s=>s.state))]})),
 knowledge_queries:groups.map((g,i)=>({group:g,query:['enterprise manufacturing admin','desktop responsive overflow grid','form labels validation error recovery','data table action visibility','modal focus confirmation cancel','focus keyboard wcag 2.2','text contrast hierarchy','form tables vue'][i],domain:g==='vue_stack'?'stack':g==='product_style'?'product':'ux',evidence:path.join(dir,'knowledge',g+'.txt'),exit_code:0})),
 screenshots:shots,
 interaction_checks:[{name:'Live Chrome form, CPP real GET/PUT roundtrip and product picker save/cancel',status:'PASS',evidence:path.join(root,'frontend/mes-web/e2e/process-formal-live.spec.ts')},{name:'Complete permissions, 409/403 and zoom recovery',status:'NOT RUN',evidence:path.join(dir,'browser-capture.json')}],
 geometry_checks:[{name:'Document scrollWidth equals viewport for all 30 PC captures',status:'PASS',evidence:path.join(dir,'browser-capture.json')},{name:'Full label anchor/textarea contrast and 900px sticky footer adversarial pass',status:'NOT RUN',evidence:path.join(dir,'browser-capture.json')}],
 adversarial_pass:{completed:false,alignment:'真实几何已抓取，但30格全量标签锚点二次复核尚未完成。',whitespace:'未完成30格原始截图第二遍。',hierarchy:'部分原图已检查，不能代替全部30格的第二遍。',action_discoverability:'900产品选择弹窗按钮遮挡与反序已发现并修复，余全矩阵未完成。',recovery:'真实Chrome取消、保存与重读通过；全部失败/权限恢复矩阵未完成。'},
 issues:[{id:'G5-MODAL-ORDER-OCCLUSION',severity:'HIGH',status:'RESOLVED',evidence:path.join(dir,'900-product-picker-before.png'),recheck:path.join(dir,'900-product-picker-after.png')},{id:'G5-FULL-VISUAL-AND-A11Y',severity:'HIGH',status:'OPEN',evidence:path.join(dir,'browser-capture.json'),note:'30张截图尚未逐图全维度及完整G5核对，不能关闭。'}],
 dimension_reviews:surfaces.flatMap(surface=>dims.map(dimension=>({surface,dimension,status:'NOT RUN',note:'本轮只完成正式页面采集与部分视觉核对；该表面九维完整人工审查仍需独立完成。',evidence:path.join(dir,'browser-capture.json')}))),
 wcag22_checks:wcag.map(criterion=>({criterion,status:'NOT RUN',note:'仅部分键盘/标签/可视性检查；该WCAG条款未完成全场景核对。',evidence:path.join(dir,'browser-capture.json')})),
 verdict:'IN PROGRESS'};
fs.writeFileSync(path.join(dir,'audit-manifest.json'),JSON.stringify(d,null,2),'utf8');
console.log(JSON.stringify({screens:d.screenshots.length,surfaces:d.expected_surfaces.length,dimensions:d.dimension_reviews.length,wcag:d.wcag22_checks.length,verdict:d.verdict,authChallenges:input.authChallenges.length,otherErrors:input.errors.length,sourceHashes},null,2));