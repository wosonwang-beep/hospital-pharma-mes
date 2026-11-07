const fs=require('fs'),path=require('path')
const root=path.resolve(__dirname,'../..')
const base=path.join(root,'docs/development/process-form-cpp-ebr/20261107-formal-g5')
const file=path.join(base,'source-final/audit-manifest.json')
const m=JSON.parse(fs.readFileSync(file,'utf8'))
const sessionsPath=path.join(base,'role-login-e2e-evidence.json')
const roleEvidence=JSON.parse(fs.readFileSync(sessionsPath,'utf8'))
if(roleEvidence.total!==7||roleEvidence.evidence.length!==7||roleEvidence.evidence.filter(x=>x.server===200).length!==1||roleEvidence.evidence.filter(x=>x.server===403).length!==6)throw Error('Seven real role sessions do not match expected permission matrix')
const role=m.interaction_checks[2]
if(role.status!=='NOT RUN'&&!role.name.startsWith('Seven '))throw Error('Existing gate check changed; refusing to overwrite')
m.interaction_checks[2]={name:'Seven existing business identities independently authenticated: one read-only process viewer and six server 403 denials with UI route guards',status:'PASS',evidence:sessionsPath,note:'7 separate real Chrome login sessions, bearer GET /process-packages/829 and actual UI navigation. Admin duplicate-code HTTP409 and keyboard cases validated separately.'}
const nativeZoom=path.join(base,'native-zoom-probe.json')
const nativeResult=JSON.parse(fs.readFileSync(nativeZoom,'utf8'))
if(nativeResult.zoomChanged)throw Error('Unexpected actual native zoom changed; reassess before updating evidence')
m.interaction_checks[4]={name:'Native browser UI zoom and assistive technology announcements',status:'NOT RUN',evidence:nativeZoom,note:'Headless Control+Equal four times had no change to CSS viewport width/devicePixelRatio/visualScale; cannot count as actual native zoom. Equivalent layout 200% separately passed; human/native-browser validation remains.'}
const geom=m.geometry_checks.find(x=>x.name.includes('Native browser zoom'))
if(geom){geom.status='NOT RUN';geom.evidence=nativeZoom;geom.note='Native desktop browser zoom was not actuated in headless Chrome. Modal focus trap at normal size passed at three PC widths; native zoom-specific geometry not established.'}
const wcagNotes=path.join(base,'WCAG_TARGETED_EVIDENCE.md')
for(const item of m.wcag22_checks){item.evidence=wcagNotes;item.note='Targeted evidence for selected controls exists in WCAG_TARGETED_EVIDENCE.md, but this criterion is not exhaustively checked across all process and EBR states. NOT RUN means no full criterion-level G5 verdict; no claim of WCAG AA certification.'}
m.g5_current_snapshot={role_login_evidence:sessionsPath,native_zoom_limit_evidence:nativeZoom,wcag_targeted_evidence:wcagNotes,actor:'ChatGPT SELF-REVIEW',status:'IN PROGRESS'}
m.verdict='IN PROGRESS'
fs.writeFileSync(file,JSON.stringify(m,null,2))
console.log(JSON.stringify({sourceHash:m.source.source_hash,roleLoginCount:roleEvidence.total,readOnlyUsers:roleEvidence.evidence.filter(x=>x.server===200).length,deniedUsers:roleEvidence.evidence.filter(x=>x.server===403).length,interactions:m.interaction_checks.map((x,i)=>({i,status:x.status})),wcagNotRun:m.wcag22_checks.filter(x=>x.status==='NOT RUN').length,verdict:m.verdict},null,2))