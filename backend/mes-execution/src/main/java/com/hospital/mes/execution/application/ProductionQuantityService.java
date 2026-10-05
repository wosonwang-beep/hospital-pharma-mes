package com.hospital.mes.execution.application;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.mes.execution.infrastructure.*;
import com.hospital.mes.production.application.*;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.audit.signature.*;
import org.springframework.transaction.annotation.*;
import org.springframework.beans.factory.ObjectProvider;
import java.math.BigDecimal;
import java.util.*;
import static com.hospital.mes.production.application.ProductionInput.*;
import static com.hospital.mes.production.domain.ProductionRules.*;
@org.springframework.stereotype.Service @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProductionQuantityService {
 private final ExecutionStore store;private final ProductionQueryService batches;private final ProductionService batchOwner;private final ExecutionQueryService operations;
 private final MasterMutation mutations;private final MasterQueryService units;private final SignedRecordSupport signing;private final ObjectMapper json;private final ObjectProvider<ExecutionOutputInventoryPort> inventory;
 public ProductionQuantityService(ExecutionStore store,ProductionQueryService batches,ProductionService batchOwner,ExecutionQueryService operations,MasterMutation mutations,MasterQueryService units,SignedRecordSupport signing,ObjectMapper json,ObjectProvider<ExecutionOutputInventoryPort> inventory){this.store=store;this.batches=batches;this.batchOwner=batchOwner;this.operations=operations;this.mutations=mutations;this.units=units;this.signing=signing;this.json=json;this.inventory=inventory;}
 public List<JsonNode> facts(long org,long batch){batches.batch(org,batch);return rows(org,batch).stream().map(x->(JsonNode)view(x)).toList();}
 public JsonNode lotFact(long org,long lot){return inventory.getObject().lot(org,lot);}
 @Transactional(propagation=Propagation.MANDATORY) public void synchronizeLogistics(com.hospital.mes.audit.application.CurrentPlatformContext context,long batch){
  long org=context.organizationId(),actor=context.actorId();
  var root=batches.lockBatch(org,batch);
  for(var fact:inventory.getObject().confirmedLogistics(org,batch)){
   String source=fact.type().equals("ISSUE")?"MaterialIssueItem":"IssueReturnItem";
   var existing=store.quantityEventMapper.selectOne(new QueryWrapper<QuantityEventEntity>().eq("org_id",org).eq("event_type",fact.type()).eq("source_type",source).eq("source_ref",Long.toString(fact.id())));
   if(existing!=null){gate(existing.getMainBatchId()==batch&&existing.getMaterialLotId()==fact.materialLotId()&&existing.getUnitId()==fact.unitId()&&existing.getAmount().compareTo(fact.amount())==0,"QUANTITY_SOURCE_CHANGED","Confirmed source evidence changed");continue;}
   gate(!Set.of("QA_RELEASED","REJECTED").contains(root.status()),"QUANTITY_BATCH_NOT_MUTABLE","Final QA history cannot receive late source facts");
   var row=new QuantityEventEntity();row.setMainBatchId(batch);row.setEventType(fact.type());row.setSourceType(source);row.setSourceRef(Long.toString(fact.id()));row.setMaterialLotId(fact.materialLotId());row.setAmount(fact.amount());row.setUnitId(fact.unitId());row.setOccurredAt(fact.occurredAt());store.quantityEvents.insert(row,org,actor);mutations.auditSnapshot(context,"QUANTITY_SOURCE_SYNCHRONIZED","QuantityEvent",row.getId(),null,view(row),"Projected actual confirmed "+source,"quantity-sync:"+source+":"+fact.id());
  }
 }
 private List<QuantityEventEntity> rows(long org,long batch){return store.quantityEventMapper.selectList(new QueryWrapper<QuantityEventEntity>().eq("org_id",org).eq("main_batch_id",batch).orderByAsc("id"));}
 @Transactional(readOnly=true) public ScopedStore.PageData<JsonNode> list(String batch,int page,int size){var c=mutations.context("balance:view");if(page<0||page>1000000||size<1||size>100)throw new IllegalArgumentException("Invalid pagination");var all=facts(c.organizationId(),MasterMutation.id(batch));return new ScopedStore.PageData<>(all.stream().skip((long)page*size).limit(size).toList(),all.size(),page,size);}
 @Transactional public JsonNode record(String target,JsonNode body,String header,String key){
  fields(body,"versionNo","eventType","operationExecutionId","materialLotId","amount","unitId","sourceRef","reason","signature","lotNo","locationId","productionDate","expiryDate");
  var c=mutations.context("mes:quantity:record");long expected=expected(body,header);String type=text(body,"eventType",30),why=text(body,"reason",1000);BigDecimal amount=amount(body);
  if(!Set.of("OUTPUT","SAMPLE","LOSS","SCRAP","WIP").contains(type)||amount.signum()<0||type.equals("OUTPUT")&&amount.signum()==0)throw new IllegalArgumentException("Controlled production observation required");
  return mutations.execute(c,"PRODUCTION_QUANTITY:"+target,key,Map.of("batch",target,"body",body),()->{
   long batch=MasterMutation.id(target);var root=batches.lockBatch(c.organizationId(),batch);version(root.versionNo(),expected);gate(root.status().equals("IN_PROGRESS"),"QUANTITY_BATCH_NOT_MUTABLE","Batch must be in progress");
   gate(root.snapshot().hasNonNull("qualityPlan"),"QUALITY_PLAN_REQUIRED","Actual frozen quality plan required");var row=new QuantityEventEntity();row.setMainBatchId(batch);row.setEventType(type);row.setAmount(amount);row.setUnitId(id(body,"unitId"));units.unit(c.organizationId(),row.getUnitId());
   row.setSourceType("PRODUCTION_OBSERVATION");row.setSourceRef(text(body,"sourceRef",100));row.setReason(why);row.setOccurredAt(now());
   Long op=optionalId(body,"operationExecutionId");if(op!=null){var actual=operations.lockOperation(c.organizationId(),op);gate(actual.mainBatchId()==batch,"QUANTITY_OPERATION_MISMATCH","Operation must belong to batch");row.setOperationExecutionId(op);row.setExecutionUnitId(actual.executionUnitId());}
   Long lot=optionalId(body,"materialLotId");
   if(type.equals("OUTPUT")){
    var identity=batches.qualityIdentity(c.organizationId(),batch);gate(lot==null||Objects.equals(lot,identity.finishedLotId()),"FINISHED_LOT_IDENTITY_CONFLICT","Cannot associate an unrelated output lot");
    var command=new ExecutionOutputInventoryPort.Output(batch,identity.finishedLotId(),id(root.snapshot().path("qualityPlan"),"finishedMaterialId"),amount,row.getUnitId(),text(body,"lotNo",80),id(body,"locationId"),date(body,"productionDate"),date(body,"expiryDate"),row.getSourceRef(),why);
    lot=inventory.getObject().receiveOutput(c,command,key);row.setMaterialLotId(lot);
   }else if(lot!=null){var actual=inventory.getObject().lot(c.organizationId(),lot);boolean related=Objects.equals(lot,batches.qualityIdentity(c.organizationId(),batch).finishedLotId())||operations.chargesForBatch(c.organizationId(),batch).stream().anyMatch(x->x.path("materialLotId").asLong()==actual.path("id").asLong());gate(related,"QUANTITY_LOT_MISMATCH","Lot must be a consumed input or the batch output");row.setMaterialLotId(lot);}
   var evidence=signing.sign(c,"PRODUCTION_QUANTITY",batch+":"+expected,expected,SignatureMeaning.VERIFY,row,body.get("signature"),key,List.of("MainBatch:"+batch));row.setSignatureId(evidence.id());row.setSignatureEvidenceJson(evidence.envelope());
   store.quantityEvents.insert(row,c.organizationId(),c.actorId());batchOwner.quantityEvidenceRecorded(c,batch,expected,type.equals("OUTPUT")?lot:null,why,key);mutations.auditSnapshot(c,"PRODUCTION_QUANTITY","QuantityEvent",row.getId(),null,view(row),why,key);return row;
  },x->view((QuantityEventEntity)x),201);
 }
 @Transactional public JsonNode reverse(String target,JsonNode body,String header,String key){
  fields(body,"versionNo","reason","signature");var c=mutations.context("mes:quantity:reverse");long expected=expected(body,header);String why=text(body,"reason",1000);
  return mutations.execute(c,"PRODUCTION_QUANTITY_REVERSE:"+target,key,Map.of("event",target,"body",body),()->{
   var original=store.quantityEvents.get(c.organizationId(),MasterMutation.id(target));var root=batches.lockBatch(c.organizationId(),original.getMainBatchId());version(root.versionNo(),expected);gate(root.status().equals("IN_PROGRESS"),"QUANTITY_BATCH_NOT_MUTABLE","Batch must be in progress");
   gate(original.getReversalOfId()==null&&original.getSourceType().equals("PRODUCTION_OBSERVATION"),"QUANTITY_SOURCE_OWNER_REQUIRED","Charge/logistics/reversal corrections belong to their source owner");
   gate(rows(c.organizationId(),original.getMainBatchId()).stream().noneMatch(x->Objects.equals(x.getReversalOfId(),original.getId())),"QUANTITY_ALREADY_REVERSED","Original observation already reversed");
   var row=new QuantityEventEntity();row.setMainBatchId(original.getMainBatchId());row.setExecutionUnitId(original.getExecutionUnitId());row.setOperationExecutionId(original.getOperationExecutionId());row.setMaterialLotId(original.getMaterialLotId());row.setUnitId(original.getUnitId());row.setAmount(original.getAmount().negate());row.setReversalOfId(original.getId());row.setEventType("REVERSAL");row.setSourceType("PRODUCTION_REVERSAL");row.setSourceRef(original.getId().toString());row.setOccurredAt(now());row.setReason(why);
   if(original.getEventType().equals("OUTPUT"))inventory.getObject().reverseOutput(c,original.getMaterialLotId(),original.getSourceRef(),original.getAmount(),original.getUnitId(),row.getSourceRef(),why,key);
   var evidence=signing.sign(c,"PRODUCTION_QUANTITY",original.getMainBatchId()+":"+expected,expected,SignatureMeaning.VERIFY,row,body.get("signature"),key,List.of("QuantityEvent:"+target));row.setSignatureId(evidence.id());row.setSignatureEvidenceJson(evidence.envelope());
   store.quantityEvents.insert(row,c.organizationId(),c.actorId());batchOwner.quantityEvidenceRecorded(c,row.getMainBatchId(),expected,null,why,key);mutations.auditSnapshot(c,"PRODUCTION_QUANTITY_REVERSE","QuantityEvent",row.getId(),null,view(row),why,key);return row;
  },x->view((QuantityEventEntity)x),200);
 }
 private static long expected(JsonNode body,String header){if(header==null||!header.matches("\"(?:0|[1-9][0-9]*)\""))throw new IllegalArgumentException("Exact quoted If-Match required");return SignedRecordSupport.expected(body,header);}
 private static BigDecimal amount(JsonNode body){String raw=text(body,"amount",50);if(!raw.matches("(?:0|[1-9][0-9]{0,11})(?:\\.[0-9]{1,6})?"))throw new IllegalArgumentException("Exact nonnegative DECIMAL(18,6) required");return new BigDecimal(raw);}
 public ObjectNode view(QuantityEventEntity row){var n=SignedRecordSupport.publicView(row,json);n.remove("signatureEvidenceJson");n.putArray("allowedActions");return n;}
 public List<String> envelopes(long org,String id){String[] root=id.split(":");if(root.length!=2)throw new IllegalArgumentException("Quantity signature identity invalid");return rows(org,MasterMutation.id(root[0])).stream().map(QuantityEventEntity::getSignatureEvidenceJson).filter(Objects::nonNull).toList();}
}
