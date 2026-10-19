from pathlib import Path
import hashlib,json,shutil
from datetime import datetime
R=Path(r'D:\codex\_project\gmp\hospital-pharma-mes'); B=R/'backend/mes-wms/src/main/java/com/hospital/mes/wms'; E=R/'docs/review/evidence/RECEIPT_DEVELOPMENT_20261018';E.mkdir(parents=True,exist_ok=True)
def write(p,s):
 p=Path(p);p.parent.mkdir(parents=True,exist_ok=True)
 if p.exists():
  dest=E/'before'/p.relative_to(R);dest.parent.mkdir(parents=True,exist_ok=True)
  if not dest.exists():dest.write_bytes(p.read_bytes())
 p.write_text(s,encoding='utf-8')
def change(p,a,b):
 s=p.read_text(encoding='utf-8');assert s.count(a)==1,(str(p),a[:80],s.count(a));write(p,s.replace(a,b))
# New schema is append-only; no rule is seeded, no fake approval or sample number is enabled.
mig=R/'backend/mes-boot/src/main/resources/db/migration/V065__receipt_source_identity_and_number_reservation.sql'
assert not mig.exists(),'Migration V065 exists; reconcile rather than overwrite'
write(mig,'''-- Receiving SOURCE_A integration. Existing records/values and Flyway history remain intact.
ALTER TABLE wms_material_receipt
 ADD COLUMN input_contract VARCHAR(32) NULL COMMENT 'Receiving contract version; null preserves legacy semantics',
 ADD COLUMN arrival_date DATE NULL COMMENT 'Actual arrival date; not backfilled from creation time',
 ADD COLUMN acceptance_text VARCHAR(1000) NULL COMMENT 'Operator arrival acceptance notes',
 MODIFY transport_check_passed TINYINT(1) NULL COMMENT 'NULL unchecked, 0 failed, 1 passed';
ALTER TABLE wms_material_receipt_item
 MODIFY package_check_passed TINYINT(1) NULL,
 MODIFY seal_check_passed TINYINT(1) NULL,
 MODIFY label_check_passed TINYINT(1) NULL,
 MODIFY damage_check_passed TINYINT(1) NULL,
 MODIFY contamination_check_passed TINYINT(1) NULL;
CREATE TABLE wms_receipt_number_rule (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL,
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL, version_no BIGINT NOT NULL DEFAULT 0,
 prefix VARCHAR(20) NOT NULL, date_pattern VARCHAR(20) NOT NULL,
 sequence_width INT NOT NULL, next_value BIGINT NOT NULL DEFAULT 1,
 timezone_id VARCHAR(64) NOT NULL, approval_reference VARCHAR(200) NULL,
 enabled TINYINT(1) NOT NULL DEFAULT 0,
 UNIQUE KEY uk_receipt_number_rule_org(org_id),
 CHECK(sequence_width BETWEEN 1 AND 18), CHECK(next_value > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Controlled receiving number configuration; no automatically approved rule';
CREATE TABLE wms_receipt_number_reservation (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, org_id BIGINT NOT NULL,
 created_by BIGINT NOT NULL, created_at DATETIME(3) NOT NULL,
 updated_by BIGINT NOT NULL, updated_at DATETIME(3) NOT NULL, version_no BIGINT NOT NULL DEFAULT 0,
 rule_id BIGINT NOT NULL, material_id BIGINT NOT NULL, client_line_id VARCHAR(100) NOT NULL,
 lot_no VARCHAR(100) NOT NULL, approval_reference VARCHAR(200) NOT NULL,
 bound_receipt_item_id BIGINT NULL,
 UNIQUE KEY uk_receipt_reserved_number(org_id,lot_no),
 UNIQUE KEY uk_receipt_reserved_intent(org_id,client_line_id),
 FOREIGN KEY(rule_id) REFERENCES wms_receipt_number_rule(id),
 FOREIGN KEY(material_id) REFERENCES md_material(id),
 FOREIGN KEY(bound_receipt_item_id) REFERENCES wms_material_receipt_item(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Persistent reservation is not stock; never recycled after cancellation';
''')
for cls,table,fields in [('ReceiptNumberRule','wms_receipt_number_rule',[('String','prefix'),('String','datePattern'),('Integer','sequenceWidth'),('Long','nextValue'),('String','timezoneId'),('String','approvalReference'),('Boolean','enabled')]),('ReceiptNumberReservation','wms_receipt_number_reservation',[('Long','ruleId'),('Long','materialId'),('String','clientLineId'),('String','lotNo'),('String','approvalReference'),('Long','boundReceiptItemId')])]:
 body='package com.hospital.mes.wms.infrastructure;\nimport com.baomidou.mybatisplus.annotation.TableName;\nimport com.hospital.mes.masterdata.infrastructure.ScopedEntity;\n@TableName("'+table+'")\npublic class '+cls+' extends ScopedEntity {\n'
 for typ,name in fields:
  upper=name[0].upper()+name[1:];body+=f' private {typ} {name};\n public {typ} get{upper}(){{return {name};}} public void set{upper}({typ} v){{{name}=v;}}\n'
 write(B/'infrastructure'/f'{cls}.java',body+'}\n')
 write(B/'infrastructure'/f'{cls}Mapper.java',f'package com.hospital.mes.wms.infrastructure;\n@org.apache.ibatis.annotations.Mapper\npublic interface {cls}Mapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<{cls}> {{}}\n')
write(B/'domain/ReceiptNumberingPolicy.java','''package com.hospital.mes.wms.domain;
import java.time.*;
import java.time.format.DateTimeFormatter;
public final class ReceiptNumberingPolicy {
 private ReceiptNumberingPolicy() {}
 public static String format(String prefix,String pattern,int width,long sequence,ZoneId zone,Instant now){
  if(prefix==null||!prefix.matches("[A-Za-z0-9-]{1,20}")||!java.util.Set.of("yyyyMMdd","yyMMdd").contains(pattern)||width<1||width>18||sequence<1)throw new IllegalArgumentException("Invalid approved numbering configuration");
  String digits=Long.toString(sequence);if(digits.length()>width)throw new IllegalStateException("Approved number sequence exhausted; review numbering rule");
  return prefix+"-"+DateTimeFormatter.ofPattern(pattern).withZone(zone).format(now)+"-"+"0".repeat(width-digits.length())+digits;
 }
}
''')
write(B/'application/ReceiptNumberingService.java','''package com.hospital.mes.wms.application;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.common.exception.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.masterdata.domain.MasterRules;
import com.hospital.mes.wms.infrastructure.*;
import com.hospital.mes.wms.domain.ReceiptNumberingPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.time.*;
import java.util.*;
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ReceiptNumberingService {
 private final ReceiptNumberRuleMapper rules; private final ReceiptNumberReservationMapper reservations;
 private final MasterMutation mutations; private final MaterialQueryService materials; private final MaterialLotMapper lots;
 private final ScopedStore<ReceiptNumberRule> ruleStore;private final ScopedStore<ReceiptNumberReservation> reservationStore;
 public ReceiptNumberingService(ReceiptNumberRuleMapper rules,ReceiptNumberReservationMapper reservations,MasterMutation mutations,MaterialQueryService materials,MaterialLotMapper lots){
  this.rules=rules;this.reservations=reservations;this.mutations=mutations;this.materials=materials;this.lots=lots;
  ruleStore=new ScopedStore<>(rules,List.of(),Map.of());reservationStore=new ScopedStore<>(reservations,List.of(),Map.of());
 }
 public record Reserve(String materialId,String clientLineId){@com.fasterxml.jackson.annotation.JsonAnySetter public void unknown(String name,Object v){throw new IllegalArgumentException("Unknown field: "+name);}}
 @Transactional public JsonNode reserve(Reserve input,String key){
  var c=mutations.context("wms:receipt:create");long material=MasterMutation.id(input.materialId());String line=MasterRules.text(input.clientLineId(),100);
  materials.requireUsable(c.organizationId(),material,Instant.now());
  return mutations.execute(c,"ReceiptNumber:RESERVE",key,input,()->{
   // Serialize by organization rule. No browser counters and no implicit approval.
   var rule=rules.selectOne(new QueryWrapper<ReceiptNumberRule>().eq("org_id",c.organizationId()).last("FOR UPDATE"));
   if(rule==null||!Boolean.TRUE.equals(rule.getEnabled())||rule.getApprovalReference()==null||rule.getApprovalReference().isBlank())throw new ComplianceException("RECEIPT_NUMBER_RULE_NOT_APPROVED","尚未配置获批准的内部物料批号规则，不能生成正式批号");
   var old=reservations.selectOne(new QueryWrapper<ReceiptNumberReservation>().eq("org_id",c.organizationId()).eq("client_line_id",line));
   if(old!=null){if(!old.getMaterialId().equals(material)||!old.getCreatedBy().equals(c.actorId()))throw new ResourceConflictException("RECEIPT_NUMBER_INTENT_CONFLICT","编号申请与原物料或操作者不一致");return old;}
   long seq=rule.getNextValue();String lot=ReceiptNumberingPolicy.format(rule.getPrefix(),rule.getDatePattern(),rule.getSequenceWidth(),seq,ZoneId.of(rule.getTimezoneId()),Instant.now());
   if(lots.selectCount(new QueryWrapper<MaterialLotEntity>().eq("org_id",c.organizationId()).eq("lot_no",lot))>0)throw new ResourceConflictException("RECEIPT_NUMBER_COLLISION","编号与历史物料批号冲突，需检查批准规则");
   rule.setNextValue(Math.addExact(seq,1));ruleStore.update(rule,rule.getVersionNo(),c.actorId(),List.of("nextValue"));
   var r=new ReceiptNumberReservation();r.setRuleId(rule.getId());r.setMaterialId(material);r.setClientLineId(line);r.setLotNo(lot);r.setApprovalReference(rule.getApprovalReference());reservationStore.insert(r,c.organizationId(),c.actorId());
   mutations.auditSnapshot(c,"ReceiptNumber:RESERVE","ReceiptNumberReservation",r.getId(),null,mutations.view(r),"依据批准规则 "+rule.getApprovalReference(),key);return r;
  });
 }
 // Called in the same transaction as receipt item persistence; another receipt may never steal a number.
 public void bind(long org,long actor,ReceiptItemEntity item){
  var r=reservations.selectOne(new QueryWrapper<ReceiptNumberReservation>().eq("org_id",org).eq("lot_no",item.getLotNo()).last("FOR UPDATE"));
  if(r==null||!r.getMaterialId().equals(item.getMaterialId()))throw new ComplianceException("RECEIPT_NUMBER_NOT_RESERVED","内部批号未由当前组织为该物料分配");
  if(r.getBoundReceiptItemId()!=null){if(!r.getBoundReceiptItemId().equals(item.getId()))throw new ResourceConflictException("RECEIPT_NUMBER_ALREADY_USED","内部批号已绑定其他收货明细");return;}
  if(!r.getCreatedBy().equals(actor))throw new PermissionException("RECEIPT_NUMBER_OWNER_MISMATCH","不能使用其他操作者未绑定的编号申请");
  r.setBoundReceiptItemId(item.getId());reservationStore.update(r,r.getVersionNo(),actor,List.of("boundReceiptItemId"));
 }
}
''')
# Optional new-contract fields with old constructor source compatibility.
p=B/'domain/WmsCommands.java';s=p.read_text(encoding='utf-8')
a='public record ReceiptCreate(String receiptNo,String supplierId,String purchaseOrderNo,String deliveryNoteNo,String warehouseId,Boolean transportCheckPassed,List<ReceiptItemInput> items) {'
b='public record ReceiptCreate(String receiptNo,String supplierId,String purchaseOrderNo,String deliveryNoteNo,String warehouseId,Boolean transportCheckPassed,List<ReceiptItemInput> items,String inputContract,String arrivalDate,String acceptanceText) {public ReceiptCreate(String receiptNo,String supplierId,String purchaseOrderNo,String deliveryNoteNo,String warehouseId,Boolean transportCheckPassed,List<ReceiptItemInput> items){this(receiptNo,supplierId,purchaseOrderNo,deliveryNoteNo,warehouseId,transportCheckPassed,items,null,null,null);}'
assert a in s;s=s.replace(a,b)
a='public record ReceiptUpdate(String receiptNo,String supplierId,String purchaseOrderNo,String deliveryNoteNo,String warehouseId,Boolean transportCheckPassed,List<ReceiptItemInput> items,Long versionNo,String reason) {'
b='public record ReceiptUpdate(String receiptNo,String supplierId,String purchaseOrderNo,String deliveryNoteNo,String warehouseId,Boolean transportCheckPassed,List<ReceiptItemInput> items,Long versionNo,String reason,String inputContract,String arrivalDate,String acceptanceText) {public ReceiptUpdate(String receiptNo,String supplierId,String purchaseOrderNo,String deliveryNoteNo,String warehouseId,Boolean transportCheckPassed,List<ReceiptItemInput> items,Long versionNo,String reason){this(receiptNo,supplierId,purchaseOrderNo,deliveryNoteNo,warehouseId,transportCheckPassed,items,versionNo,reason,null,null,null);}'
assert a in s;write(p,s.replace(a,b))
p=B/'infrastructure/ReceiptEntity.java';s=p.read_text(encoding='utf-8');idx=s.rfind('}')
for typ,name in [('String','inputContract'),('LocalDate','arrivalDate'),('String','acceptanceText')]:
 upper=name[0].upper()+name[1:];s=s[:idx]+f' private {typ} {name};\n public {typ} get{upper}(){{return {name};}} public void set{upper}({typ} v){{{name}=v;}}\n'+s[idx:];idx=s.rfind('}')
write(p,s)
p=B/'application/WmsService.java';s=p.read_text(encoding='utf-8')
def rs(a,b):
 global s
 assert s.count(a)==1,(a[:90],s.count(a));s=s.replace(a,b)
rs(' private final WmsStore db;', ' @org.springframework.beans.factory.annotation.Autowired private ReceiptNumberingService numbering;\n private static final String SOURCE_A="RECEIPT_SOURCE_A_V1";\n private boolean sourceA(ReceiptEntity r){return SOURCE_A.equals(r.getInputContract());}\n private final WmsStore db;')
rs('new ReceiptCreate(r.receiptNo(),r.supplierId(),r.purchaseOrderNo(),r.deliveryNoteNo(),r.warehouseId(),r.transportCheckPassed(),r.items())','new ReceiptCreate(r.receiptNo(),r.supplierId(),r.purchaseOrderNo(),r.deliveryNoteNo(),r.warehouseId(),r.transportCheckPassed(),r.items(),r.inputContract()==null?e.getInputContract():r.inputContract(),r.arrivalDate()==null&&e.getArrivalDate()!=null?e.getArrivalDate().toString():r.arrivalDate(),r.acceptanceText()==null?e.getAcceptanceText():r.acceptanceText())')
rs('List.of("receiptNo","supplierId","purchaseOrderNo","deliveryNoteNo","warehouseId","transportCheckPassed")','List.of("receiptNo","supplierId","purchaseOrderNo","deliveryNoteNo","warehouseId","transportCheckPassed","inputContract","arrivalDate","acceptanceText")')
rs(' private void applyReceipt(ReceiptEntity e,ReceiptCreate r,CurrentPlatformContext c){',''' private void applyReceipt(ReceiptEntity e,ReceiptCreate r,CurrentPlatformContext c){
  if(r.inputContract()!=null&&!SOURCE_A.equals(r.inputContract()))throw new IllegalArgumentException("Unsupported receipt input contract");
  if(e.getInputContract()!=null&&!e.getInputContract().equals(r.inputContract()))throw new IllegalArgumentException("Cannot downgrade receipt input contract");
  e.setInputContract(r.inputContract());e.setArrivalDate(date(r.arrivalDate()));e.setAcceptanceText(MasterRules.optional(r.acceptanceText(),1000));
  if(sourceA(e)&&(e.getArrivalDate()==null||e.getArrivalDate().isAfter(LocalDate.now(ZoneOffset.UTC))))throw new IllegalArgumentException("请核对实际到货日期");''')
rs('WmsRules.check(r.transportCheckPassed());','if(!sourceA(e))WmsRules.check(r.transportCheckPassed());')
rs('e.setSupplierLotNo(MasterRules.text(r.supplierLotNo(),100));e.setManufacturerLotNo(MasterRules.optional(r.manufacturerLotNo(),100));','''if(sourceA(receipt)&&i<old.size()&&(!Objects.equals(e.getLotNo(),r.lotNo())||!Objects.equals(e.getMaterialId(),id(r.materialId()))))throw new IllegalArgumentException("已保存收货明细的物料与内部批号不能替换");
   e.setSupplierLotNo(sourceA(receipt)?MasterRules.optional(r.supplierLotNo(),100):MasterRules.text(r.supplierLotNo(),100));e.setManufacturerLotNo(sourceA(receipt)?MasterRules.text(r.manufacturerLotNo(),100):MasterRules.optional(r.manufacturerLotNo(),100));''')
rs('for(Boolean check:Arrays.asList(r.packageCheckPassed(),r.sealCheckPassed(),r.labelCheckPassed(),r.damageCheckPassed(),r.contaminationCheckPassed()))WmsRules.check(check);','if(!sourceA(receipt))for(Boolean check:Arrays.asList(r.packageCheckPassed(),r.sealCheckPassed(),r.labelCheckPassed(),r.damageCheckPassed(),r.contaminationCheckPassed()))WmsRules.check(check);')
rs('suppliers.requireApproved(c.organizationId(),e.getMaterialId(),receipt.getSupplierId(),Instant.now());','var approvedSource=suppliers.requireApproved(c.organizationId(),e.getMaterialId(),receipt.getSupplierId(),Instant.now());if(sourceA(receipt)&&approvedSource.path("manufacturerName").asText("").isBlank())throw new ComplianceException("MANUFACTURER_NOT_CONFIGURED","批准供货来源缺少生产企业");')
a='"requiresIncomingInspectionSnapshot"));\n  }\n }\n private void confirm';b='"requiresIncomingInspectionSnapshot"));\n   if(sourceA(receipt))numbering.bind(c.organizationId(),c.actorId(),e);\n  }\n }\n private void confirm';rs(a,b)
# Return live lot status in the same receipt response; keep historical manufacturer from snapshot.
rs('if(lot!=null)n.put("materialLotId",lot.getId().toString());a.add(n);','if(lot!=null){n.put("materialLotId",lot.getId().toString());n.put("qualityStatus",lot.getQualityStatus());n.put("inventoryStatus",lot.getInventoryStatus());}a.add(n);')
write(p,s)
# Minimal scoped context read: existing services enforce material/source validity, no new side effects.
write(B/'api/ReceiptSourceController.java','''package com.hospital.mes.wms.api;
import com.hospital.mes.wms.application.ReceiptNumberingService;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.common.api.ApiResponse;
import com.hospital.mes.common.trace.TraceIdProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@RestController @RequestMapping("/api/v1/wms/receipts") @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ReceiptSourceController {
 private final MasterMutation auth;private final MaterialQueryService materials;private final ApprovedSupplierQueryService sources;private final ReceiptNumberingService numbers;private final TraceIdProvider traces;private final ObjectMapper json;
 public ReceiptSourceController(MasterMutation auth,MaterialQueryService materials,ApprovedSupplierQueryService sources,ReceiptNumberingService numbers,TraceIdProvider traces,ObjectMapper json){this.auth=auth;this.materials=materials;this.sources=sources;this.numbers=numbers;this.traces=traces;this.json=json;}
 @GetMapping("/source-context") public ApiResponse<?> source(@RequestParam String materialId,@RequestParam String supplierId){var c=auth.context("wms:receipt:view");long mid=MasterMutation.id(materialId),sid=MasterMutation.id(supplierId);var out=json.createObjectNode();out.set("material",materials.requireUsable(c.organizationId(),mid,java.time.Instant.now()));out.set("source",sources.requireApproved(c.organizationId(),mid,sid,java.time.Instant.now()));return ApiResponse.success(out,traces.currentTraceId());}
 @PostMapping("/number-reservations") public ApiResponse<?> reserve(@RequestBody ReceiptNumberingService.Reserve body,@RequestHeader("Idempotency-Key")String key){return ApiResponse.success(numbers.reserve(body,key),traces.currentTraceId());}
}
''')
(E/'backend-start.json').write_text(json.dumps({'at':datetime.now().astimezone().isoformat(),'migration':mig.name,'rule_seeded':False,'scope':'SOURCE_A development, legacy constructors retained'},indent=2),encoding='utf-8')
print('BACKEND_SOURCE_WRITTEN_NO_DATABASE_COMMANDS')
