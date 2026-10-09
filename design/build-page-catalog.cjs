const fs=require('fs'),path=require('path'),cp=require('child_process');
const root='D:/codex/_project/gmp/hospital-pharma-mes';
const design=path.join(root,'design');fs.mkdirSync(design,{recursive:true});
const read=p=>fs.readFileSync(path.join(root,p),'utf8');
const env={};
for(const line of read('.env').split(/\r?\n/)){const m=line.match(/^\s*(?:export\s+)?(MES_[A-Z0-9_]+)\s*=\s*(.*?)\s*$/);if(m)env[m[1]]=m[2].replace(/^(['"])(.*)\1$/,'$2');}
const dburl=env.MES_DB_URL||'jdbc:mariadb://localhost:3306/hospital_pharma_mes_dev';
const m=dburl.match(/^jdbc:mariadb:\/\/(localhost|127\.0\.0\.1):(\d+)\/([A-Za-z0-9_]+)/);
if(!m)throw Error('Only local MariaDB is permitted');
const sql="SELECT id,COALESCE(parent_id,0),menu_code,menu_name,COALESCE(route_path,''),sort_no,status FROM sys_menu WHERE org_id=1 AND status='ACTIVE' ORDER BY sort_no,id";
const res=cp.spawnSync('C:/Program Files/MariaDB 13.0/bin/mysql.exe',['-N','-B','--default-character-set=utf8mb4','-h',m[1],'-P',m[2],'-u',env.MES_DB_USERNAME||'mes',m[3],'-e',sql],{encoding:'utf8',timeout:15000,maxBuffer:1024*1024,env:{...process.env,MYSQL_PWD:env.MES_DB_PASSWORD||''}});
if(res.status!==0)throw Error('MariaDB menu query unsuccessful; source not replaced.');
const menus=res.stdout.trim().split(/\r?\n/).filter(Boolean).map(s=>{const x=s.split('\t');return {id:x[0],parentId:x[1]==='0'?null:x[1],code:x[2],title:x[3],path:x[4]||null,sort:Number(x[5])}});
const routes=[];
for(const line of read('frontend/mes-web/src/router/index.ts').split(/\r?\n/)){
 const p=line.match(/\bpath\s*:\s*'([^']+)'/),n=line.match(/\bname\s*:\s*'([^']+)'/);if(!p||!n)continue;
 routes.push({path:p[1]==='/'?'/':'/'+p[1].replace(/^\/+/,''),name:n[1],title:line.match(/\btitle\s*:\s*'([^']+)'/)?.[1]||n[1],mode:line.match(/\bmode\s*:\s*'([^']*)'/)?.[1]||''});
}
for(const line of read('frontend/mes-web/src/master/resources.ts').split(/\r?\n/)){
 const m=line.match(/^\s*\{key:'([^']+)',title:'([^']+)',permission:'([^']+)'/);if(!m)continue;
 for(const [tail,mode] of [['',''],['/create','create'],['/:id','view'],['/:id/edit','edit']])routes.push({path:'/master/'+m[1]+tail,name:'master-'+m[1]+(mode?'-'+mode:''),title:m[2],mode});
}
const unique=[...new Map(routes.map(r=>[r.path,r])).values()];
const safe=s=>String(s).replace(/[\\/:*?"<>|]/g,'-').trim();
const index=new Map(menus.map(x=>[x.id,x]));
function group(menu){let v=menu;const seen=new Set();while(v.parentId&&index.has(v.parentId)&&!seen.has(v.id)){seen.add(v.id);v=index.get(v.parentId)}return v.title;}
function type(r,root){if(r.mode==='create'||r.path.endsWith('/create'))return '新增';if(r.mode==='edit'||r.path.endsWith('/edit'))return '编辑';if(r.mode==='execute'||r.path.endsWith('/execute'))return '执行';if(r.mode==='review'||r.path.endsWith('/review'))return '审核';if(r.mode==='release'||r.path.endsWith('/release'))return '放行';if(r.mode==='designer'||/\/(design|designer)$/.test(r.path))return '设计';if(r.path===root)return '查询';if(r.mode==='view'||r.path.includes(':id'))return '查看';return '功能';}
const assigned=new Set(),features=[];
for(const menu of menus.filter(x=>x.path)){
 const matches=unique.filter(r=>r.path===menu.path||r.path.startsWith(menu.path+'/')).map(r=>{assigned.add(r.path);return {...r,type:type(r,menu.path)}});
 if(!matches.length)matches.push({path:menu.path,name:'MISSING_ROUTE',title:menu.title,mode:'',type:'查询',routeMissing:true});
 const used=new Set();for(const r of matches){let file=safe(menu.title)+'-'+r.type+'.png';if(used.has(file))file=safe(menu.title)+'-'+r.type+'-'+safe(r.name)+'.png';used.add(file);r.designFile=file;}
 const folder=safe(group(menu))+'/'+safe(menu.title);
 fs.mkdirSync(path.join(design,folder),{recursive:true});
 features.push({menu:menu.title,group:group(menu),menuCode:menu.code,path:menu.path,folder,pages:matches});
}
const globals=unique.filter(r=>['/','/login','/change-password'].includes(r.path)).map(r=>({...r,folder:'系统入口/'+safe(r.title),designFile:safe(r.title)+'.png'}));
const others=unique.filter(r=>!assigned.has(r.path)&&!globals.some(g=>g.path===r.path)).map(r=>({...r,folder:'业务上下文页/'+safe(r.title),designFile:safe(r.title)+'-'+type(r,'')+'.png'}));
for(const x of [...globals,...others])fs.mkdirSync(path.join(design,x.folder),{recursive:true});
const result={state:'ALL_PENDING_DESIGN',source:'MariaDB sys_menu + same-main Vue Router',counts:{rootMenus:menus.filter(x=>!x.parentId).length,features:features.length,routes:unique.length,globalPages:globals.length,contextPages:others.length},features,globals,others};
fs.writeFileSync(path.join(design,'MES_UI_PAGE_CATALOG_V1.json'),JSON.stringify(result,null,2));
const out=['# MES 全功能 PC 页面设计清单','','数据库菜单 + 真实前端路由核对；设计图尚未生成的页面不得标记通过。','','菜单数：'+result.counts.features+'；路由数：'+result.counts.routes+'；业务上下文：'+result.counts.contextPages,'','| 一级菜单 | 功能菜单 | 主入口 | 分页面设计图（待生成） |','|---|---|---|---|'];
for(const f of features)out.push('| '+f.group+' | '+f.menu+' | '+f.path+' | '+f.pages.map(p=>p.designFile).join('、')+' |');
out.push('','## 系统入口',...globals.map(p=>'- '+p.folder+'/'+p.designFile+' — '+p.path),'','## 业务上下文',...others.map(p=>'- '+p.folder+'/'+p.designFile+' — '+p.path));
fs.writeFileSync(path.join(design,'MES_UI_PAGE_CATALOG_V1.md'),out.join('\n'));
console.log(JSON.stringify({counts:result.counts,first:features.slice(0,8).map(f=>f.folder),catalog:'design/MES_UI_PAGE_CATALOG_V1.md'}));
