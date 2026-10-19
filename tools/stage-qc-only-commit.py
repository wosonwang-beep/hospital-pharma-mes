from pathlib import Path
import subprocess
r=Path(r'D:\codex\_project\gmp\hospital-pharma-mes')
def git(*args,input=None):
 p=subprocess.run(['git','-C',str(r),*args],input=input,capture_output=True)
 if p.returncode:raise RuntimeError(p.stderr.decode('utf-8','replace')[:500])
 return p.stdout
for filename in ('MES_TASKS.md','docs/PROJECT_BASELINE.md'):
 current=(r/filename).read_text(encoding='utf-8')
 original=git('show','HEAD:'+filename).decode('utf-8')
 if filename=='MES_TASKS.md':
  assert current.startswith('## QC_OBJECT_TYPE ')
  prefix=current.split('# MES Task Status Index',1)[0]
  assert original.startswith('# MES Task Status Index')
  merged=prefix+original
 else:
  start=current.index('## 2026-10-11 统一质量检验对象固定字典补充')
  end=current.index('## Authority',start)
  section=current[start:end]
  anchor='## Authority'
  assert anchor in original
  merged=original.replace(anchor,section+anchor,1)
 blob=git('hash-object','-w','--stdin',input=merged.encode('utf-8')).decode().strip()
 git('update-index','--add','--cacheinfo','100644',blob,filename)
 print('staged only QC-specific content:',filename)
files=[
'backend/mes-system/src/main/java/com/hospital/mes/system/api/SystemDictionaryController.java',
'backend/mes-boot/src/main/resources/db/migration/V053__qc_inspection_object_type_dictionary.sql',
'backend/mes-boot/src/test/java/com/hospital/mes/iam/integration/QcInspectionObjectDictionaryIT.java',
'docs/development/DCP-QC-OBJECT-TYPE-001-APPROVED-PRINCIPLES.md']
git('add','--',*files)
print(git('diff','--cached','--stat').decode('utf-8','replace'))
