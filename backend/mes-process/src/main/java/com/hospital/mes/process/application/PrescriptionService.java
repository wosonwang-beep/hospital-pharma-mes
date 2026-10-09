package com.hospital.mes.process.application;
import com.fasterxml.jackson.databind.*;import com.fasterxml.jackson.databind.node.*;
import com.hospital.mes.process.infrastructure.*;import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.common.exception.*;import org.springframework.transaction.annotation.Transactional;
import java.util.*;import java.time.*;import static com.hospital.mes.process.domain.ProcessCommands.*;
import static com.hospital.mes.process.domain.ProcessRules.*;
@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class PrescriptionService {
 private final PrescriptionStore db;private final ProcessStore processDb;private final ProcessQueryService process;
 private final ProcessDefinitions definitions;private final MasterMutation mutations;private final ObjectMapper json;
 public PrescriptionService(PrescriptionStore db,ProcessStore processDb,ProcessQueryService process,ProcessDefinitions definitions,MasterMutation mutations,ObjectMapper json){
  this.db=db;this.processDb=processDb;this.process=process;this.definitions=definitions;this.mutations=mutations;this.json=json;
 }
 public ScopedStore.PageData<JsonNode> list(int page,int size,String keyword,Map<String,String> filters){
  var c=mutations.context("production:prescription:view");var rows=db.page(c.organizationId(),page,size,keyword,filters);
  return new ScopedStore.PageData<>(rows.items().stream().map(this::view).map(n->(JsonNode)n).toList(),rows.total(),page,size);
 }
 public JsonNode get(String id){var c=mutations.context("production:prescription:view");return view(db.prescriptions().get(c.organizationId(),MasterMutation.id(id)));}
 @Transactional public JsonNode create(PrescriptionCreate r,String key){
  var c=mutations.context("production:prescription:create");validate(c.organizationId(),r.prescriptionCode(),r.prescriptionName(),r.productId(),r.processPackageId(),r.batchBasisQty(),r.unitId(),r.items());
  return mutations.execute(c,"ProductionPrescription:CREATE",key,r,()->{
   if(db.prescriptions().count(c.organizationId(),"prescription_code",r.prescriptionCode(),null)>0)throw new ResourceConflictException("DUPLICATE_CODE","prescriptionCode already exists");
   var row=new PrescriptionEntity();row.setPrescriptionCode(text(r.prescriptionCode(),64,"prescriptionCode"));row.setPrescriptionName(text(r.prescriptionName(),200,"prescriptionName"));
   row.setProductId(MasterMutation.id(r.productId()));row.setProcessPackageId(MasterMutation.id(r.processPackageId()));row.setBatchBasisQty(decimal(r.batchBasisQty(),18,true,"batchBasisQty"));row.setUnitId(MasterMutation.id(r.unitId()));row.setStatus("DRAFT");
   db.prescriptions().insert(row,c.organizationId(),c.actorId());replaceItems(c.organizationId(),c.actorId(),row.getId(),r.items());
   var after=view(row);mutations.auditSnapshot(c,"ProductionPrescription:CREATE","ProductionPrescription",row.getId(),null,after,"创建生产处方",key);return row;
  },e->view((PrescriptionEntity)e),201);
 }
 @Transactional public JsonNode update(String id,PrescriptionUpdate r,String header,String key){
  var c=mutations.context("production:prescription:update");long expected=MasterMutation.version(header,r.versionNo());long target=MasterMutation.id(id);
  return mutations.execute(c,"ProductionPrescription:UPDATE:"+id,key,Map.of("id",id,"version",expected,"body",r),()->{
   var row=db.prescriptions().lock(c.organizationId(),target);if(!"DRAFT".equals(row.getStatus()))throw new ComplianceException("PRESCRIPTION_NOT_DRAFT","Only draft prescription can be edited");
   validate(c.organizationId(),row.getPrescriptionCode(),r.prescriptionName(),row.getProductId().toString(),r.processPackageId(),r.batchBasisQty(),r.unitId(),r.items());
   var before=view(row);row.setPrescriptionName(text(r.prescriptionName(),200,"prescriptionName"));row.setProcessPackageId(MasterMutation.id(r.processPackageId()));row.setBatchBasisQty(decimal(r.batchBasisQty(),18,true,"batchBasisQty"));row.setUnitId(MasterMutation.id(r.unitId()));
   db.prescriptions().update(row,expected,c.actorId(),List.of("prescriptionName","processPackageId","batchBasisQty","unitId"));replaceItems(c.organizationId(),c.actorId(),row.getId(),r.items());
   var after=view(row);mutations.auditSnapshot(c,"ProductionPrescription:UPDATE","ProductionPrescription",row.getId(),before,after,reason(r.reason()),key);return row;
  },e->view((PrescriptionEntity)e),200);
 }
 @Transactional public JsonNode activate(String id,PrescriptionAction r,String header,String key){
  var c=mutations.context("production:prescription:activate");long expected=MasterMutation.version(header,r.versionNo());long target=MasterMutation.id(id);
  return mutations.execute(c,"ProductionPrescription:ACTIVATE:"+id,key,Map.of("id",id,"version",expected,"body",r),()->{
   var row=db.prescriptions().lock(c.organizationId(),target);if(!"DRAFT".equals(row.getStatus()))throw new ComplianceException("PRESCRIPTION_STATE_INVALID","Draft prescription required");
   var items=commands(c.organizationId(),row.getId());validate(c.organizationId(),row.getPrescriptionCode(),row.getPrescriptionName(),row.getProductId().toString(),row.getProcessPackageId().toString(),plain(row.getBatchBasisQty()),row.getUnitId().toString(),items);
   if(db.activeCount(c.organizationId(),row.getProductId(),row.getId())>0)throw new ResourceConflictException("ACTIVE_PRESCRIPTION_EXISTS","Product already has an active prescription");
   var before=view(row);row.setStatus("ACTIVE");row.setEffectiveFrom(now());row.setEffectiveTo(null);db.prescriptions().update(row,expected,c.actorId(),List.of("status","effectiveFrom","effectiveTo"));
   var after=view(row);mutations.auditSnapshot(c,"ProductionPrescription:ACTIVATE","ProductionPrescription",row.getId(),before,after,reason(r.reason()),key);return row;
  },e->view((PrescriptionEntity)e),200);
 }
 @Transactional public JsonNode deactivate(String id,PrescriptionAction r,String header,String key){
  var c=mutations.context("production:prescription:activate");long expected=MasterMutation.version(header,r.versionNo());long target=MasterMutation.id(id);
  return mutations.execute(c,"ProductionPrescription:DEACTIVATE:"+id,key,Map.of("id",id,"version",expected,"body",r),()->{
   var row=db.prescriptions().lock(c.organizationId(),target);if(!"ACTIVE".equals(row.getStatus()))throw new ComplianceException("PRESCRIPTION_STATE_INVALID","Active prescription required");
   var before=view(row);row.setStatus("INACTIVE");row.setEffectiveTo(now());db.prescriptions().update(row,expected,c.actorId(),List.of("status","effectiveTo"));
   var after=view(row);mutations.auditSnapshot(c,"ProductionPrescription:DEACTIVATE","ProductionPrescription",row.getId(),before,after,reason(r.reason()),key);return row;
  },e->view((PrescriptionEntity)e),200);
 }
 @Transactional(readOnly=true) public JsonNode requireActive(long org,long productId){
  var row=db.active(org,productId);if(row==null)throw new ComplianceException("ACTIVE_PRESCRIPTION_REQUIRED","Product has no active production prescription");
  return snapshot(org,row);
 }
 @Transactional(readOnly=true) public JsonNode requireActive(long org,long productId,long prescriptionId){
  var row=db.prescriptions().get(org,prescriptionId);if(!row.getProductId().equals(productId)||!"ACTIVE".equals(row.getStatus()))throw new ComplianceException("PRESCRIPTION_NOT_ACTIVE","Selected production prescription is not active for this product");
  return snapshot(org,row);
 }
 private void validate(long org,String code,String name,String productId,String packageId,String basis,String unitId,List<PrescriptionItem> items){
  text(code,64,"prescriptionCode");text(name,200,"prescriptionName");long product=MasterMutation.id(productId),pkg=MasterMutation.id(packageId);process.requireProduct(org,product);
  var processPackage=processDb.packages().get(org,pkg);if(!"ACTIVE".equals(processPackage.getStatus()))throw new ComplianceException("PROCESS_NOT_ACTIVE","Active production process required");
  process.requireCurrent(org,pkg);
  if(items==null||items.isEmpty())throw new ComplianceException("PRESCRIPTION_ITEMS_REQUIRED","At least one prescription material is required");
  definitions.validateFormula(org,new FormulaSave(null,null,code,basis,unitId,items.stream().map(i->new FormulaLine(i.lineNo(),i.materialId(),i.requiredQty(),i.unitId(),i.overagePct(),i.critical())).toList()));
 }
 private void replaceItems(long org,long actor,long prescriptionId,List<PrescriptionItem> lines){
  db.deleteItems(org,prescriptionId);
  for(var line:lines){var item=new PrescriptionItemEntity();item.setPrescriptionId(prescriptionId);item.setLineNo(line.lineNo());item.setMaterialId(MasterMutation.id(line.materialId()));item.setRequiredQty(decimal(line.requiredQty(),18,true,"requiredQty"));item.setUnitId(MasterMutation.id(line.unitId()));item.setOveragePct(nullableDecimal(line.overagePct(),9,"overagePct"));item.setCritical(Boolean.TRUE.equals(line.critical()));db.items().insert(item,org,actor);}
 }
 private List<PrescriptionItem> commands(long org,long id){return db.items(org,id).stream().map(i->new PrescriptionItem(i.getLineNo(),i.getMaterialId().toString(),plain(i.getRequiredQty()),i.getUnitId().toString(),plain(i.getOveragePct()),i.getCritical())).toList();}
 private ObjectNode view(PrescriptionEntity row){
  var n=(ObjectNode)mutations.view(row);for(String key:List.of("productId","processPackageId","unitId"))if(n.hasNonNull(key))n.put(key,n.path(key).asText());
  n.put("batchBasisQty",plain(row.getBatchBasisQty()));n.put("productName",process.productName(row.getOrgId(),row.getProductId()));n.put("processCode",processDb.packages().get(row.getOrgId(),row.getProcessPackageId()).getPackageCode());
  var out=n.putArray("items");for(var i:db.items(row.getOrgId(),row.getId())){var x=out.addObject();x.put("id",i.getId().toString()).put("lineNo",i.getLineNo()).put("materialId",i.getMaterialId().toString()).put("requiredQty",plain(i.getRequiredQty())).put("unitId",i.getUnitId().toString());if(i.getOveragePct()==null)x.putNull("overagePct");else x.put("overagePct",plain(i.getOveragePct()));x.put("critical",Boolean.TRUE.equals(i.getCritical()));}
  return n;
 }
 private ObjectNode snapshot(long org,PrescriptionEntity row){
  var n=json.createObjectNode();n.put("prescriptionId",row.getId().toString()).put("prescriptionCode",row.getPrescriptionCode()).put("prescriptionName",row.getPrescriptionName()).put("productId",row.getProductId().toString()).put("processPackageId",row.getProcessPackageId().toString()).put("batchBasisQty",plain(row.getBatchBasisQty())).put("unitId",row.getUnitId().toString());
  var a=n.putArray("items");for(var i:db.items(org,row.getId())){var x=a.addObject();x.put("formulaItemId",i.getId().toString()).put("lineNo",i.getLineNo()).put("materialId",i.getMaterialId().toString()).put("requiredQty",plain(i.getRequiredQty())).put("unitId",i.getUnitId().toString());if(i.getOveragePct()==null)x.putNull("overagePct");else x.put("overagePct",plain(i.getOveragePct()));x.put("critical",Boolean.TRUE.equals(i.getCritical()));}
  n.put("contentHash",mutations.digest(n));return n;
 }
 private static LocalDateTime now(){return LocalDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);}
 private static String reason(String value){if(value==null||value.isBlank()||value.length()>1000)throw new IllegalArgumentException("Reason required");return value.strip();}
 private static String plain(java.math.BigDecimal v){return v==null?null:v.stripTrailingZeros().toPlainString();}
}