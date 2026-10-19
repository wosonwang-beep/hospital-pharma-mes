"""Record the completed visual pass and deliberately incomplete formal G5 evidence."""
from pathlib import Path
import json,re,hashlib

OUT=Path('docs/review/evidence/print-integration-20261015')
manifest=json.loads((OUT/'audit-manifest.json').read_text(encoding='utf-8'))
captures=json.loads((OUT/'run2/capture-journal.json').read_text(encoding='utf-8'))
manifest['screenshots']=[]
def review(status,note):return {'status':status,'note':note}
for c in captures:
    surface,state,width=c['surface'],c['state'],c['viewport']
    if surface=='receipt-detail':
        notes={
          'hierarchy':review('PASS','T3 title, confirmed receipt identity, receipt information, line items and attachments are ordered visibly.'),
          'grid_alignment':review('PASS','Two columns at 1440/1280; one column at 900/375. Labels remain next to controls; no label-above-control split observed.'),
          'spacing_density':review('PASS','Section cards separate header and receipt facts; line item card is nested with consistent row rhythm.'),
          'typography_color':review('PASS','Dark section headings and blue reference links remain readable; disabled controls visually differ from active print action.'),
          'controls_content':review('FAIL' if width==375 else 'PASS','375 receipt/lot identifiers visibly clip in narrow fields and generated-lot link; full-value recovery not demonstrated.' if width==375 else 'Current record, supplier, quantity and unit are visible in readonly controls.'),
          'actions':review('PASS','Print selected record and audit actions remain visible beside or below the identity header; no edit or save action used.'),
          'responsive':review('FAIL' if width==375 else 'PASS','375 long generated-lot identifier extends outside its intended content band although document width equals viewport.' if width==375 else 'Card boundaries fit viewport and complete label/control pairs reflow together.'),
          'interaction_accessibility':review('NOT RUN','Pointer entry/close was exercised; focus order, zoom and keyboard return were not fully exercised.')}
    elif surface=='batch-modal':
        success=state=='success';occluded=width in [1440,1280] and state in ['success','loading']
        notes={
          'hierarchy':review('PASS','Modal title precedes current template, selected count and generate action; success adds download/open actions.'),
          'grid_alignment':review('PASS','Template label and select share a centerline; measured center delta is 0px at this captured viewport.'),
          'spacing_density':review('FAIL' if success else 'PASS','The result surface is dominated by a blank embedded PDF region; valid PDF bytes were rendered separately.' if success else 'Compact one-row desktop controls reflow to a small grouped block at 900/375.'),
          'typography_color':review('PASS','Template label, receipt template name and blue generate action are legible; loading changes button treatment.'),
          'controls_content':review('FAIL' if success else 'PASS','Generated result iframe is visibly blank in the automated browser. Native PDF reader acceptance remains unresolved.' if success else 'Current published receipt template V2 and one selected record are displayed.'),
          'actions':review('FAIL' if occluded else 'PASS','Collapsed WMS navigation flyout visibly overlays the result toolbar, concealing the download action.' if occluded else 'Generate/close controls are visible; at narrow width download/open controls remain grouped after result generation.'),
          'responsive':review('PASS','Modal remains within viewport; complete print-template label/select pair stays horizontal and wraps as a group.'),
          'interaction_accessibility':review('NOT RUN','Actual pointer cancel, two rapid clicks and reopen were exercised; full keyboard/focus/WCAG interaction remains unrun.')}
    else:
        notes={
          'hierarchy':review('PASS','Report facts, results, conclusion, approval and print panel are separated; no-template guidance appears in print section.'),
          'grid_alignment':review('PASS','Report sections align to common card boundaries; narrow report identity and reference values wrap.'),
          'spacing_density':review('PASS','Empty print state contains guidance below controls; report tables remain in bounded sections.'),
          'typography_color':review('PASS','Approved and qualified states use text with green treatment; explanatory text distinguishes QC from QA release.'),
          'controls_content':review('FAIL','Print template selector uses a placeholder without a persistent visible label; missing-template reason is present.'),
          'actions':review('PASS','Generate PDF is disabled with no applicable template; source record and approval data were not modified.'),
          'responsive':review('PASS','375 sections reflow to one column; result table uses a bounded horizontal scroll region.'),
          'interaction_accessibility':review('NOT RUN','No-template disabled state checked; keyboard and assistive-technology checks were not executed.')}
    manifest['screenshots'].append({'surface':surface,'state':state,'viewport':width,'path':'run2/'+c['path'],'original_resolution_reviewed':True,'review':notes})

selected={
 'product-style':('No verified match after one ERP retry. General built-in guidance is fallback only; existing Global UI V2 remains authority.',[]),
 'layout':('Overflow Hidden / Table Handling apply to receipt long identifiers and narrow report table.',['Overflow Hidden','Table Handling']),
 'forms':('Persistent form labels and error recovery apply to the print-template select.',['Form Labels','Error Recovery','Error Messages']),
 'tables':('Report result table and missing-template guidance require overflow/empty-state checks.',['Table Handling','Empty States']),
 'modal':('Focus must remain visible; AAA enhanced rules are not used as AA requirements.',['Focus States']),
 'accessibility':('Keyboard focus/focus-not-obscured minimum must be actually exercised; currently NOT RUN.',['Focus States','Focus Not Obscured (Minimum)']),
 'typography':('Corporate Trust is a contextual readability reference; no font changes or external font fetches authorized.',['Corporate Trust']),
 'stack':('Vue forms/v-model guidance fits controlled template selection; no validation library added.',['Use v-model modifiers'])}
for k in manifest['knowledge_queries']:
    original=k.get('original_group',k['group'])
    k['applicability'],k['selected_rules']=selected[original]
    k['original_group']=original
    k['group']={'product-style':'product_style','tables':'tables_lists','modal':'modal_feedback','typography':'typography_color'}.get(original,original)

manifest['interaction_checks']=[
 {'name':'real receipt UI generation','status':'PASS','evidence':'run2/http-journal.json'},
 {'name':'cancel without print at four viewports','status':'PASS','evidence':'run2/cancel-results.json'},
 {'name':'two rapid pointer clicks issue one request','status':'PASS','evidence':'run2/duplicate-click.json'},
 {'name':'reopen clears old result','status':'PASS','evidence':'run2/reopen-results.json'},
 {'name':'report no-template disables generation','status':'PASS','evidence':'run2/no-template-result.json'},
 {'name':'real limited-role403','status':'NOT RUN','evidence':'http/summary.json'},
 {'name':'full keyboard focus recovery and service-error UI recovery','status':'NOT RUN','evidence':'run2/stage.json'}]
manifest['geometry_checks']=[
 {'name':'print template horizontal label/select centerline at four viewports','status':'PASS','evidence':'run2/capture-journal.json'},
 {'name':'document width equals viewport for captured cells','status':'PASS','evidence':'run2/capture-journal.json'},
 {'name':'all ordinary/spanning field anchors and target sizes','status':'NOT RUN','evidence':'run2/capture-journal.json'}]
manifest['adversarial_pass']={'completed':True,'alignment':'Print pair center delta0 does not overrule clipped receipt identifier at375.','whitespace':'Generated embedded PDF region remains white/blank despite valid separately rendered bytes.','hierarchy':'Desktop WMS flyout sits above the result toolbar and obscures download; report no-template reason stays below controls.','action_discoverability':'Print entry visible; native open/print not reattempted after historical repeated failure; no page-entry configuration screen found.','recovery':'Cancel sends0 POST, reopen clears blob; real limited-role and service-error UI recovery remain unrun.'}
manifest['issues']=[
 {'id':'PRINT-UX-001','severity':'HIGH','status':'OPEN','evidence':'run2/batch-modal-success-1440.png','note':'Embedded browser preview blank. File render is valid; current evidence cannot distinguish browser PDF support limitation from application integration defect. Reuse historical stopped native-popup item; no repeated workaround.'},
 {'id':'PRINT-UX-002','severity':'HIGH','status':'OPEN','evidence':'run2/batch-modal-success-1280.png','note':'Navigation flyout overlaps and conceals download toolbar after viewport reflow. Minimum follow-up: inspect shell flyout layering/close behavior with modal; no fix authorized here.'},
 {'id':'PRINT-UX-003','severity':'MEDIUM','status':'OPEN','evidence':'run2/inspection-report-no-template-375.png','note':'Report print-template select has placeholder-only identification; follow-up persistent horizontal label and semantic association.'},
 {'id':'PRINT-UX-004','severity':'MEDIUM','status':'OPEN','evidence':'run2/receipt-detail-default-375.png','note':'Long receipt/lot values clip at375; follow-up full-value wrapping or accessible full-value exposure within existing design.'},
 {'id':'PRINT-CONTRACT-001','severity':'MEDIUM','status':'OPEN','evidence':'http/results.json','note':'Wrong type rejected422 PRINT_STATE_INVALID; script expected400. Original controlled-printing documents422 while newer print-once documents409 for state failure. Preserve raw mismatch; reconcile authoritative error mapping before changing either implementation or test.'}]
dimensions=['layout','typography','spacing_alignment','forms','tables_lists','query_filters','component_interaction','component_states','wcag22']
manifest['dimension_reviews']=[]
for surface in ['receipt-detail','batch-modal','inspection-report']:
    for dimension in dimensions:
        status='NOT RUN'
        note='Visual screenshots inspected, but full state matrix and dedicated '+dimension+' checks remain incomplete; no complete dimension PASS claimed.'
        if surface=='batch-modal' and dimension in ['layout','component_states']:
            status='FAIL';note='Success result blank and desktop flyout obscures download; required result/focus/error states remain unresolved.'
        if surface=='inspection-report' and dimension=='forms':
            status='FAIL';note='Actual print panel and source confirm template select has no persistent visible label; no-template message and disabled action were verified.'
        if dimension=='query_filters':note='T3/modal surfaces contain no query/reset form. Business-list query and selection lifecycle not tested in this scoped run; remains NOT RUN.'
        manifest['dimension_reviews'].append({'surface':surface,'dimension':dimension,'status':status,'note':note,'evidence':'run2/capture-journal.json'})
manifest['wcag22_checks']=[{'criterion':c,'status':'NOT RUN','note':'Full criterion-specific DOM, keyboard, contrast or resize evidence was not collected; screenshots alone are insufficient.','evidence':'run2/stage.json'} for c in ['1.3.1','1.4.1','1.4.3','1.4.4','1.4.10','1.4.11','2.1.1','2.1.2','2.4.3','2.4.7','2.4.11','2.5.3','2.5.7','2.5.8','3.3.1','3.3.2','3.3.3','4.1.2','4.1.3']]
manifest['verdict']='FAIL'
for shot in manifest['screenshots']:
    # Screenshot schema permits PASS/FAIL/N/A; incomplete screenshot-category
    # assessment is FAIL, while dimension/interaction records retain NOT RUN.
    if shot['review']['interaction_accessibility']['status']=='NOT RUN':
        shot['review']['interaction_accessibility']['status']='FAIL'
(OUT/'audit-manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')

checks=[]
for file,id,name,lot,qty,unit in [
 ('browser-receipt-page-1.txt','22','氯化钾','RM-KCL-20261007223208-02','50000.000','克'),
 ('two-records-page-1.txt','25','纯化水','PW-20261007225254-01','200000.000','毫升'),
 ('two-records-page-2.txt','22','氯化钾','RM-KCL-20261007223208-02','50000.000','克')]:
    text=(OUT/'pdf-render'/file).read_text(encoding='utf-8')
    receipt=json.loads((OUT/'http'/('receipt-'+id+'.json')).read_text(encoding='utf-8'))
    compact=re.sub(r'\s+','',text)
    checks.append({'file':file,'receiptId':id,'receiptNoMatches':receipt['receiptNo'] in text,'titleMatches':'收货记录打印' in text,'materialMatches':name in text,'lotMatches':lot in compact,'quantityMatches':qty in text,'unitMatches':unit in text})
(OUT/'pdf-render/content-checks.json').write_text(json.dumps({'checks':checks,'allChecks':all(all(v for k,v in c.items() if k.endswith('Matches')) for c in checks),'batchOrder':['25','22'],'visualOriginalResolution':'three rendered pages inspected; inherited footer #1#/#1# retained, no template edit'},ensure_ascii=False,indent=2),encoding='utf-8')
print('Saved17 original-resolution screenshot reviews; formal G5 FAIL retained.')
