const fs=require('fs'),path=require('path'),cp=require('child_process');
const root='D:/codex/_project/gmp/hospital-pharma-mes';
const env={};
for(const line of fs.readFileSync(path.join(root,'.env'),'utf8').split(/\r?\n/)){let m=line.match(/^\s*(?:export\s+)?(MES_[A-Z0-9_]+)\s*=\s*(.*?)\s*$/);if(m)env[m[1]]=m[2].replace(/^(['"])(.*)\1$/,'$2');}
const url=env.MES_DB_URL||'jdbc:mariadb://localhost:3306/hospital_pharma_mes_dev';
const m=url.match(/^jdbc:mariadb:\/\/(localhost|127\.0\.0\.1):(\d+)\/([A-Za-z0-9_]+)/);
if(!m)throw Error('Cannot audit an unexpected database URL');
function sql(q){const v=cp.spawnSync('C:/Program Files/MariaDB 13.0/bin/mysql.exe',['-N','-B','--default-character-set=utf8mb4','-h',m[1],'-P',m[2],'-u',env.MES_DB_USERNAME||'mes','-D',m[3],'-e',q],{env:{...process.env,MYSQL_PWD:env.MES_DB_PASSWORD||''},encoding:'utf8',timeout:14000,maxBuffer:1024*1024});if(v.status!==0)throw Error('Database read-only query error');return v.stdout.trim().split(/\r?\n/).filter(Boolean)}
const tables=sql("SELECT table_name FROM information_schema.tables WHERE table_schema=database() AND (table_name LIKE '%dict%' OR table_name LIKE '%department%' OR table_name LIKE '%dept%' OR table_name LIKE '%organization%' OR table_name LIKE 'sys_%') ORDER BY table_name");
console.log('DB_TABLES '+JSON.stringify(tables));
const matchedTables=tables.filter(s=>/dict|department|dept|organization/i.test(s));
for(const t of ['sys_user','sys_role','sys_menu','sys_permission'])console.log('COLUMNS '+t+' '+JSON.stringify(sql("SELECT column_name,data_type FROM information_schema.columns WHERE table_schema=database() AND table_name='"+t+"' ORDER BY ordinal_position")));
for(const t of matchedTables)console.log('COLUMNS '+t+' '+JSON.stringify(sql("SELECT column_name,data_type FROM information_schema.columns WHERE table_schema=database() AND table_name='"+t+"' ORDER BY ordinal_position")));
const terms=/data.dict|dictionary|dict_type|dict_item|sys_dict|字典|部门|department|enumOptions|choices:|statusLabel/ig;
const outputs=[];
function walk(sub,exts,max=90){const full=path.join(root,sub);if(!fs.existsSync(full))return;let count=0;const todo=[full];while(todo.length){const cur=todo.pop();for(const ent of fs.readdirSync(cur,{withFileTypes:true})){if(['target','node_modules','.git','dist','releases','.codex'].includes(ent.name))continue;const p=path.join(cur,ent.name);if(ent.isDirectory()){todo.push(p);continue}if(!exts.includes(path.extname(ent.name)))continue;const content=fs.readFileSync(p,'utf8');const hits=content.split(/\r?\n/).map((l,i)=>({l,i})).filter(x=>{terms.lastIndex=0;return terms.test(x.l)});if(hits.length){outputs.push({path:path.relative(root,p).replace(/\\/g,'/'),count:hits.length,lines:hits.slice(0,3).map(x=>({line:x.i+1,text:x.l.slice(0,140)}))});count+=hits.length;if(outputs.length>=max)return}}}}
walk('backend/mes-system/src/main',['.java']);
walk('backend/mes-boot/src/main/resources/db/migration',['.sql'],80);
walk('frontend/mes-web/src',['.ts','.vue'],140);
console.log('SOURCE_MATCHES '+JSON.stringify(outputs.slice(0,90)));console.log('MATCHED_FILES '+outputs.length);
