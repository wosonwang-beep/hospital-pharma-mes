from pathlib import Path
r=Path(r'D:\codex\_project\gmp\hospital-pharma-mes')
cmd=r/'backend/mes-masterdata/src/main/java/com/hospital/mes/masterdata/application/MaterialCommands.java'
s=cmd.read_text(encoding='utf-8')
s=s.replace('String storageCondition){public Create','String storageCondition,Integer shelfLifeDays){public Create')
s=s.replace('requiresIncomingInspection,null);}@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown', 'requiresIncomingInspection,null,null);}@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown',1)
s=s.replace('String storageCondition){public Update','String storageCondition,Integer shelfLifeDays){public Update')
s=s.replace('requiresIncomingInspection,null);}@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown', 'requiresIncomingInspection,null,null);}@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown',1)
# Preserve old canonical constructor signatures for existing direct Java callers.
s=s.replace('public Create(String materialCode,', 'public Create(String materialCode,',1)
pos=s.index('}@com.fasterxml.jackson.annotation.JsonAnySetter')
s=s[:pos]+'''public Create(String materialCode,String materialName,String materialType,String specification,String gradePurity,String appearance,String baseUnitId,String packSpec,String packUnitId,String manufacturerName,Boolean lotControlled,String effectiveFrom,String effectiveTo,String remark,Boolean requiresIncomingInspection,String storageCondition){this(materialCode,materialName,materialType,specification,gradePurity,appearance,baseUnitId,packSpec,packUnitId,manufacturerName,lotControlled,effectiveFrom,effectiveTo,remark,requiresIncomingInspection,storageCondition,null);}'''+s[pos:]
pos=s.index('}@com.fasterxml.jackson.annotation.JsonAnySetter',s.index('public record Update'))
s=s[:pos]+'''public Update(Long versionNo,String reason,String materialName,String materialType,String specification,String gradePurity,String appearance,String baseUnitId,String packSpec,String packUnitId,String manufacturerName,Boolean lotControlled,String effectiveFrom,String effectiveTo,String remark,Boolean requiresIncomingInspection,String storageCondition){this(versionNo,reason,materialName,materialType,specification,gradePurity,appearance,baseUnitId,packSpec,packUnitId,manufacturerName,lotControlled,effectiveFrom,effectiveTo,remark,requiresIncomingInspection,storageCondition,null);}'''+s[pos:]
cmd.write_text(s,encoding='utf-8')
svc=r/'backend/mes-masterdata/src/main/java/com/hospital/mes/masterdata/application/MaterialService.java'
s=svc.read_text(encoding='utf-8')
s=s.replace('"requiresIncomingInspection","storageCondition"));','"requiresIncomingInspection","storageCondition","shelfLifeDays"));')
s=s.replace('r.requiresIncomingInspection(),r.storageCondition()),c.organizationId()', 'r.requiresIncomingInspection(),r.storageCondition(),r.shelfLifeDays()),c.organizationId()')
s=s.replace('"storageCondition","status"));','"storageCondition","shelfLifeDays","status"));')
s=s.replace('e.setStorageCondition(MasterRules.optional(r.storageCondition(),500));','e.setStorageCondition(MasterRules.optional(r.storageCondition(),500));\n  if(r.shelfLifeDays()!=null && (r.shelfLifeDays()<1 || r.shelfLifeDays()>36500))throw new IllegalArgumentException("Effective shelf life must be 1..36500 days");\n  e.setShelfLifeDays(r.shelfLifeDays());')
svc.write_text(s,encoding='utf-8')
w=r/'backend/mes-wms/src/main/java/com/hospital/mes/wms/application/WmsService.java'
s=w.read_text(encoding='utf-8')
needle='e.setManufactureDate(date(r.manufactureDate()));e.setExpiryDate(date(r.expiryDate()));'
replacement='''e.setManufactureDate(date(r.manufactureDate()));
    var materialSnapshotForDates=materials.requireUsable(c.organizationId(),e.getMaterialId(),Instant.now());
    var shelfLife=materialSnapshotForDates.path("shelfLifeDays");
    LocalDate computedExpiry=e.getManufactureDate()!=null&&shelfLife.canConvertToInt()&&shelfLife.asInt()>0?e.getManufactureDate().plusDays(shelfLife.asInt()):null;
    LocalDate suppliedExpiry=date(r.expiryDate());
    if(computedExpiry!=null&&suppliedExpiry!=null&&!computedExpiry.equals(suppliedExpiry))throw new ComplianceException("RECEIPT_EXPIRY_MISMATCH","Receipt expiry does not match material shelf life");
    e.setExpiryDate(computedExpiry!=null?computedExpiry:suppliedExpiry);'''
assert needle in s
s=s.replace(needle,replacement,1)
w.write_text(s,encoding='utf-8')
print('UPDATED',cmd,svc,w,sep='\n')
