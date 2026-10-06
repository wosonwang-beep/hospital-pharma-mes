"""Bounded approved v1.0.18 consistency gate; never changes authority pointers."""
from pathlib import Path
import hashlib,json,subprocess
ROOT=Path(__file__).resolve().parents[1]
OLD=ROOT/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.17'
NEW=ROOT/'releases/HOSPITAL_PHARMA_MES_V2_FINAL_BASELINE_COMPLETE_v1.0.18'
errors=[]
hashes=json.loads((NEW/'00_MATERIAL_STORAGE_SOURCE_PARENT_HASHES.json').read_text(encoding='utf-8'))
for name,digest in hashes.items():
 if hashlib.sha256((OLD/name).read_bytes()).hexdigest()!=digest:errors.append('Parent modified: '+name)
old=json.loads((OLD/'08_OPENAPI_FULL_V1.0.17_FROZEN.yaml').read_text(encoding='utf-8'))
new=json.loads((NEW/'08_OPENAPI_FULL_V1.0.18_FROZEN.yaml').read_text(encoding='utf-8'))
normalized=json.loads(json.dumps(old,ensure_ascii=False).replace('v1.0.17','v1.0.18').replace('V1.0.17','V1.0.18'))
if new['paths']!=normalized['paths']:errors.append('Unexpected public path/query/permission change')
schemas=json.loads(json.dumps(new['components']['schemas']))
for name in ['MaterialCreateRequest','MaterialUpdateRequest','MaterialResponse']:
 field=schemas[name]['properties'].pop('storageCondition',None)
 if field!={'type':['string','null'],'maxLength':500}:errors.append(name+' storage schema')
item=schemas['MaterialSupplierAssignmentRequest']['properties']['suppliers']['items']
if item['properties'].pop('manufacturerName',None)!={'type':['string','null'],'maxLength':200}:errors.append('Assignment manufacturer schema')
if schemas['MaterialSupplierResponse']['properties'].pop('manufacturerName',None)!={'type':['string','null'],'maxLength':200}:errors.append('Relationship read schema')
source=schemas.pop('ReceiptSourceSnapshot',{})
if set(source.get('properties',{}))!=set(['relationshipId','materialId','supplierId','supplierCode','supplierName','manufacturerName']):errors.append('Source shape')
for name in ['ReceiptItem','MaterialLot']:
 if schemas[name]['properties'].pop('sourceSnapshot',None)!={'$ref':'#/components/schemas/ReceiptSourceSnapshot','readOnly':True}:errors.append(name+' source read schema')
if schemas!=normalized['components']['schemas']:errors.append('Unexpected schema/state delta')
for prefix in ['10_UI_PAGE_ROUTE_MATRIX','15_TASK_DEPENDENCY_MATRIX']:
 previous=(OLD/(prefix+'_V1.0.17_FROZEN.csv')).read_text(encoding='utf-8-sig').replace('v1.0.17','v1.0.18').replace('V1.0.17','V1.0.18')
 if (NEW/(prefix+'_V1.0.18_FROZEN.csv')).read_text(encoding='utf-8-sig')!=previous:errors.append(prefix+' changed')
for filename in ['12_TEST_CASE_CATALOG','13_REQUIREMENT_TRACEABILITY_MATRIX']:
 text=(NEW/(filename+'_V1.0.18_FROZEN.csv')).read_text(encoding='utf-8-sig')
 for test in ['TC-MAT-STORAGE-001','TC-MAT-SOURCE-001','TC-RCV-SOURCE-001','TC-UI-MAT-CLEAN-001']:
  if test not in text:errors.append(filename+' missing '+test)
for name in ['004','005','008']:
 if '00_MATERIAL_STORAGE_SOURCE_CONTRACT_V1.0.18.md' not in (NEW/'tasks'/('MES-'+name+'-R2.md')).read_text(encoding='utf-8'):errors.append('Task maintenance mapping '+name)
changed=subprocess.check_output(['git','diff','--name-only','HEAD','--','backend/mes-boot/src/main/resources/db/migration'],cwd=ROOT,text=True).strip()
allowed_migration='backend/mes-boot/src/main/resources/db/migration/V028__material_storage_supplier_source.sql'
if any(path!=allowed_migration for path in changed.splitlines()):errors.append('Executed migration modified')
# V028 may be staged as a new file; never allow replacing an existing HEAD migration.
existing=subprocess.run(['git','cat-file','-e','HEAD:'+allowed_migration],cwd=ROOT,capture_output=True)
if existing.returncode==0 and allowed_migration in changed.splitlines():errors.append('Executed V028 modified')
migration=ROOT/'backend/mes-boot/src/main/resources/db/migration/V028__material_storage_supplier_source.sql'
text=migration.read_text(encoding='utf-8')
if any(x not in text for x in ['md_material_supplier','manufacturer_name','wms_material_receipt_item','source_snapshot_json','JSON_VALID']):errors.append('V028 schema mismatch')
if any(x in text.upper() for x in ['DROP ','DELETE ','UPDATE ']):errors.append('Destructive/backfill migration')
# Public receipt commands must remain closed against client-supplied source facts.
for name in ['ReceiptItemInput','ReceiptCreate','ReceiptUpdate']:
 if 'sourceSnapshot' in new['components']['schemas'][name].get('properties',{}):errors.append('Client-writable source '+name)
contract=(NEW/'00_MATERIAL_STORAGE_SOURCE_CONTRACT_V1.0.18.md').read_text(encoding='utf-8')
for phrase in ['APPROVED','V028','CONFIRM','sourceSnapshot','QUARANTINE','optimistic','Permission','Audit']:
 if phrase.lower() not in contract.lower():errors.append('Missing contract '+phrase)
report={'status':'PASS' if not errors else 'FAIL','approvedDcp':'DCP-MATERIAL-STORAGE-SOURCE-001','parentBaseline':'v1.0.17','candidateBaseline':'v1.0.18','historicalFilesVerified':len(hashes),'physicalMigration':'V028','newPublicPaths':0,'querySortPaginationChanged':False,'newPermissions':0,'businessStateEnumsChanged':False,'signatureRuleChanged':False,'sourceFactOwner':'ReceiptItem frozen source via existing MaterialLot.receiptItemId','uiAuthority':'Global UI V2 / T1-T6 unchanged','errors':errors}
(NEW/'00_MATERIAL_STORAGE_SOURCE_CONSISTENCY_REVIEW.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
if errors:print(json.dumps(report,ensure_ascii=False));raise SystemExit(1)
# Refresh cumulative candidate manifests only; immutable parent is untouched.
for path in NEW.glob('*MANIFEST*.json'):
 if path.name=='00_MATERIAL_STORAGE_SOURCE_MANIFEST.json':continue
 data=json.loads(path.read_text(encoding='utf-8'))
 if isinstance(data,dict) and data and all(isinstance(v,str) and len(v)==64 for v in data.values()):
  data={name:hashlib.sha256((NEW/name).read_bytes()).hexdigest() for name in data if (NEW/name).is_file()};path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
manifest={str(p.relative_to(NEW)).replace('\\','/'):hashlib.sha256(p.read_bytes()).hexdigest() for p in NEW.rglob('*') if p.is_file() and p.name!='00_MATERIAL_STORAGE_SOURCE_MANIFEST.json'}
(NEW/'00_MATERIAL_STORAGE_SOURCE_MANIFEST.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'status':'PASS','historicalFilesVerified':len(hashes),'manifestFiles':len(manifest),'errors':errors}))
