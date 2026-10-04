"""One-time, explicitly approved V024 recovery. Never use for another migration."""
from pathlib import Path
import os, subprocess, json, hashlib, zlib, zipfile, tempfile, sys
root = Path(__file__).resolve().parents[1]
evidence = root / 'docs/acceptance/functional-closure/migration-failure'
env = dict(os.environ)
values = {}
for line in (root / '.env').read_text(encoding='utf-8-sig').splitlines():
    if '=' in line and not line.lstrip().startswith('#'):
        k,v = line.split('=',1); values[k.strip()] = v.strip().strip('"\'')
assert values['MES_DB_URL'] == 'jdbc:mariadb://localhost:3306/hospital_pharma_mes_dev'
env['MYSQL_PWD'] = values['MES_DB_PASSWORD']
def query(sql):
    result = subprocess.run([r'C:\Program Files\MariaDB 13.0\bin\mariadb.exe','--host=localhost','--port=3306','--user='+values['MES_DB_USERNAME'],'--database=hospital_pharma_mes_dev','--batch','--raw','--skip-column-names','--execute',sql], env=env, capture_output=True,text=True,check=True)
    return result.stdout.strip()
history_sql = "SELECT JSON_OBJECT('installedRank',installed_rank,'version',version,'description',description,'type',type,'script',script,'checksum',checksum,'installedBy',installed_by,'installedOn',installed_on,'executionTime',execution_time,'success',success) FROM flyway_schema_history ORDER BY installed_rank"
def history(): return [json.loads(line) for line in query(history_sql).splitlines()]
migrations = root / 'backend/mes-boot/src/main/resources/db/migration'
v024 = migrations / 'V024__controlled_inventory_ipc_clearance.sql'
def checksum(path):
    crc = 0
    for line in path.read_text(encoding='utf-8-sig').splitlines(): crc = zlib.crc32(line.encode('utf-8'),crc)
    return crc if crc < 2**31 else crc-2**32
def successful_guard(rows):
    successful = [r for r in rows if r['success']==1]
    assert len(successful)==23 and max(int(r['version']) for r in successful)==23
    for row in successful: assert checksum(migrations / row['script']) == row['checksum'], 'Successful migration checksum mismatch: '+row['version']
    return successful
action = sys.argv[1]
if action in ('prepare','repair','migrate'):
    assert all(int(p.name.split('__',1)[0][1:])<=24 for p in migrations.glob('V*__*.sql')), 'Additional migration scripts exist; this one-time tool is limited to V024'
if action == 'repair':
    assert not (evidence/'v024-repair.log').exists(), 'The approved one-time repair has already been executed'
if action == 'prepare':
    rows=history(); successful_guard(rows)
    assert len(rows)==24 and rows[-1]['version']=='024' and rows[-1]['success']==0
    assert v024.read_bytes()==(evidence/'V024-original-failed.sql').read_bytes()
    assert query("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name IN ('wms_material_lot_inventory_decision','mes_clearance_record','mes_clearance_review','qms_ipc_instance','qms_ipc_result_revision','qms_ipc_review')")=='0'
    column=query("SELECT CONCAT(DATA_TYPE,'|',IS_NULLABLE,'|',COLUMN_DEFAULT) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='proc_operation_def' AND column_name='ipc_definitions_json'")
    assert column in ('longtext|NO|\'[]\'','longtext|NO|(\'[]\')'), column
    ddl=query('SHOW CREATE TABLE proc_operation_def')
    assert 'json_valid(`ipc_definitions_json`)' in ddl.lower()
    (evidence/'flyway-history-recovery-precheck.jsonl').write_text('\n'.join(json.dumps(r) for r in rows)+'\n',encoding='utf-8')
    (evidence/'column-precheck.txt').write_text(column+'\n'+ddl+'\n',encoding='utf-8')
    text=v024.read_text(encoding='utf-8').replace('ALTER TABLE proc_operation_def ADD ipc_definitions_json','ALTER TABLE proc_operation_def ADD COLUMN IF NOT EXISTS ipc_definitions_json').replace('REFERENCES wms_material_lot(id)','REFERENCES md_material_lot(id)')
    v024.write_text(text,encoding='utf-8',newline='\n')
    generator=root/'scripts/generate-functional-closure-storage.py'
    gen=generator.read_text(encoding='utf-8').replace("'materialLotId':'wms_material_lot'","'materialLotId':'md_material_lot'").replace('ALTER TABLE proc_operation_def ADD ipc_definitions_json','ALTER TABLE proc_operation_def ADD COLUMN IF NOT EXISTS ipc_definitions_json')
    generator.write_text(gen,encoding='utf-8',newline='\n')
    print('Precheck PASS: 23 successful checksums match; failed V024 only; six new tables absent; existing column exact. Failed original retained; approved two SQL corrections applied.')
elif action in ('repair','migrate'):
    before=history(); old_success=successful_guard(before)
    if action=='repair': assert len(before)==24 and before[-1]['success']==0 and before[-1]['version']=='024'
    else: assert len(before)==23
    libdir=Path(tempfile.gettempdir())/'mes-v024-approved-recovery-libs'; libdir.mkdir(exist_ok=True)
    with zipfile.ZipFile(root/'backend/mes-boot/target/mes-boot-0.1.0-SNAPSHOT.jar') as jar:
        for name in jar.namelist():
            if name.startswith('BOOT-INF/lib/') and name.endswith('.jar'): (libdir/Path(name).name).write_bytes(jar.read(name))
    source=libdir/'ApprovedRecovery.java'
    source.write_text('''import java.nio.file.*; import java.util.*; import org.flywaydb.core.Flyway;
class ApprovedRecovery { public static void main(String[] args) throws Exception {
 Map<String,String> env=new HashMap<>(); for(String line:Files.readAllLines(Path.of(".env"))){if(line.contains("=")&&!line.strip().startsWith("#")){var p=line.split("=",2);env.put(p[0].strip(),p[1].strip().replaceAll("^[\\\"\\\']|[\\\"\\\']$",""));}}
 if(!"jdbc:mariadb://localhost:3306/hospital_pharma_mes_dev".equals(env.get("MES_DB_URL"))) throw new IllegalStateException("Wrong target");
 Flyway f=Flyway.configure().dataSource(env.get("MES_DB_URL"),env.get("MES_DB_USERNAME"),env.get("MES_DB_PASSWORD")).locations("filesystem:backend/mes-boot/src/main/resources/db/migration").target("024").cleanDisabled(true).load();
 if(args[0].equals("repair")){var r=f.repair();if(!r.migrationsAligned.isEmpty()||!r.migrationsDeleted.isEmpty()||r.migrationsRemoved.size()!=1)throw new IllegalStateException("Unexpected repair scope");System.out.println("REPAIR: exactly one failed migration removed; no successful alignment or deletion");}
 else {var r=f.migrate(); if(r.migrationsExecuted!=1)throw new IllegalStateException("Expected exactly V024");f.validate();System.out.println("MIGRATE/VALIDATE PASS: V024");}
 }}''',encoding='utf-8')
    result=subprocess.run(['java','--class-path',str(libdir/'*'),str(source),action],cwd=root,capture_output=True,text=True)
    (evidence/('v024-'+action+'.log')).write_text(result.stdout+'\n'+result.stderr,encoding='utf-8')
    after=history()
    assert [r for r in after if r['success']==1 and int(r['version'])<=23]==old_success, 'Successful history changed!'
    (evidence/('flyway-history-after-'+action+'.jsonl')).write_text('\n'.join(json.dumps(r) for r in after)+'\n',encoding='utf-8')
    print(result.stdout[-3000:]); print(result.stderr[-1500:]); assert result.returncode==0
    if action=='repair': assert len(after)==23
    else: assert len(after)==24 and after[-1]['version']=='024' and after[-1]['success']==1
    print('V001–V023 full successful history unchanged.')
elif action == 'verify':
    rows=history();successful_guard([r for r in rows if int(r['version'])<=23])
    assert len(rows)==24 and all(r['success']==1 for r in rows)
    assert rows[-1]['version']=='024' and checksum(v024)==rows[-1]['checksum']
    tables=['wms_material_lot_inventory_decision','mes_clearance_record','mes_clearance_review','qms_ipc_instance','qms_ipc_result_revision','qms_ipc_review']
    assert query("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name IN ("+','.join("'"+t+"'" for t in tables)+")")=='6'
    data={'history':rows,'correctedV024Sha256':hashlib.sha256(v024.read_bytes()).hexdigest(),'originalFailedV024Sha256':hashlib.sha256((evidence/'V024-original-failed.sql').read_bytes()).hexdigest(),'schemas':{t:query('SHOW CREATE TABLE '+t) for t in tables},'triggers':query("SELECT CONCAT(event_object_table,'|',event_manipulation,'|',action_statement) FROM information_schema.triggers WHERE trigger_schema=DATABASE() AND event_object_table IN ("+','.join("'"+t+"'" for t in tables)+") ORDER BY event_object_table,event_manipulation")}
    assert len(data['triggers'].splitlines())==11
    (evidence/'recovered-schema-verification.json').write_text(json.dumps(data,indent=2),encoding='utf-8')
    # Read only the exact inventory decisions generated by this recovery's retained race trials.
    own=query("SELECT JSON_OBJECT('lotId',l.id,'createdBy',l.created_by,'qualityStatus',l.quality_status,'inventoryStatus',l.inventory_status,'reason',d.reason,'decidedAt',d.decided_at) FROM wms_material_lot_inventory_decision d JOIN md_material_lot l ON l.id=d.material_lot_id WHERE d.reason IN ('Retained real freeze race','IT_RACE freeze all final gates') ORDER BY d.id")
    retained=[json.loads(line) for line in own.splitlines()]
    assert all(r['qualityStatus']=='REJECTED' and r['inventoryStatus'] in ('BLOCKED','FROZEN') for r in retained)
    (evidence/'retained-freeze-trials.json').write_text(json.dumps(retained,indent=2),encoding='utf-8')
    print('Post-recovery PASS: 24 successful migrations; all checksums match; six tables/11 immutable guards; retained race lots all unavailable. No reset/delete.')
else: raise ValueError(action)
