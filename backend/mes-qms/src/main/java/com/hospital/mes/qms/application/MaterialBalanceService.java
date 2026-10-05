package com.hospital.mes.qms.application;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.hospital.mes.qms.infrastructure.*;
import com.hospital.mes.qms.domain.BalanceFormula;
import com.hospital.mes.execution.application.ProductionQuantityService;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.production.application.*;
import com.hospital.mes.audit.application.*;
import com.hospital.mes.audit.idempotency.*;
import com.hospital.mes.audit.signature.*;
import com.hospital.mes.common.exception.*;
import org.springframework.transaction.annotation.*;
import java.math.BigDecimal;
import java.util.*;
import static com.hospital.mes.production.application.ProductionInput.*;
import static com.hospital.mes.qms.domain.IncomingRules.gate;
import static com.hospital.mes.qms.infrastructure.IncomingStore.now;

/** Append-only calculations and separately signed exceptions over actual source evidence. */
@org.springframework.stereotype.Service @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class MaterialBalanceService {
 private static final String RULE="mes_balance_rule",RESULT="mes_balance_result",INV="mes_balance_investigation";
 private final IncomingStore store;private final ProductionQueryService batches;private final ProductionQuantityService quantities;private final MasterQueryService units;
 private final MasterMutation mutations;private final PlatformIdempotencyService keys;private final SignedRecordSupport signing;private final CurrentPlatformContextResolver contexts;private final ObjectMapper json;
 public MaterialBalanceService(IncomingStore store,ProductionQueryService batches,ProductionQuantityService quantities,MasterQueryService units,MasterMutation mutations,PlatformIdempotencyService keys,SignedRecordSupport signing,CurrentPlatformContextResolver contexts,ObjectMapper json){this.store=store;this.batches=batches;this.quantities=quantities;this.units=units;this.mutations=mutations;this.keys=keys;this.signing=signing;this.contexts=contexts;this.json=json;}
 private record Inputs(Map<String,BigDecimal> totals,ObjectNode snapshot,String digest){}
 @Transactional(readOnly=true) public JsonNode get(String target){var c=mutations.context("balance:view");long batch=MasterMutation.id(target);batches.batch(c.organizationId(),batch);return view(c.organizationId(),batch);}
 @Transactional public JsonNode recalculate(String target,JsonNode body,String header,String key){
  fields(body,"versionNo","reason");var c=mutations.context("balance:view");long expected=ProductionQualityPlanService.expected(body,header);String why=text(body,"reason",1000);
  return command(c,"BALANCE_RECALCULATE:"+target,key,Map.of("batch",target,"body",body),()->{
   long batch=MasterMutation.id(target);var root=batches.lockBatch(c.organizationId(),batch);com.hospital.mes.production.domain.ProductionRules.version(root.versionNo(),expected);mutable(root.status());requirePlan(root.snapshot());
   var rules=rules(c.organizationId(),batch);gate(!rules.isEmpty(),"BALANCE_RULE_MISSING","No actual frozen rules initialized");quantities.synchronizeLogistics(c,batch);
   for(var rule:rules){var input=inputs(rule);var result=BalanceFormula.calculate(parse(rule.getFormulaExpr()),input.totals(),rule.getToleranceLow(),rule.getToleranceHigh());
    var previous=results(rule);var row=new BalanceResultRow();row.setMainBatchId(batch);row.setBalanceRuleId(rule.getId());row.setCalculationVersion(previous.isEmpty()?1:previous.getLast().getCalculationVersion()+1);
    row.setExpectedValue(result.expected());row.setActualValue(result.actual());row.setDifferenceValue(result.difference());row.setDifferencePct(result.differencePct());row.setStatus(result.status());row.setCalculatedAt(now());row.setCalculatedBy(c.actorId());row.setInputSnapshotJson(input.snapshot().toString());row.setInputDigest(input.digest());row.setRuleSnapshotJson(definition(rule).toString());
    store.insert(RESULT,row,c.organizationId(),c.actorId());mutations.auditSnapshot(c,"BALANCE_CALCULATED","BalanceResult",row.getId(),null,store.view(row),why,key);
   }
   return view(c.organizationId(),batch);
  });
 }
 @Transactional public JsonNode createInvestigation(String target,JsonNode body,String header,String key){
  fields(body,"versionNo","reason","investigationText");var c=mutations.context("balance:investigate");long expected=ProductionQualityPlanService.expected(body,header);String why=text(body,"reason",1000),summary=text(body,"investigationText",1000);
  return command(c,"BALANCE_INVESTIGATION_CREATE:"+target,key,Map.of("result",target,"body",body),()->{
   var result=(BalanceResultRow)store.get(RESULT,c.organizationId(),MasterMutation.id(target));var root=batches.lockBatch(c.organizationId(),result.getMainBatchId());com.hospital.mes.production.domain.ProductionRules.version(root.versionNo(),expected);mutable(root.status());currentResult(result);
   gate(result.getStatus().equals("FAIL"),"BALANCE_INVESTIGATION_NOT_REQUIRED","Only an actual failed result may have an exception");gate(store.rows(INV,c.organizationId(),"balanceResultId",result.getId()).isEmpty(),"BALANCE_INVESTIGATION_EXISTS","Result already has an investigation");
   var row=new BalanceInvestigationRow();row.setBalanceResultId(result.getId());row.setStatus("OPEN");row.setInvestigationText(summary);row.setInputDigest(result.getInputDigest());store.insert(INV,row,c.organizationId(),c.actorId());audit(c,"BALANCE_INVESTIGATION_CREATE",row,null,why,key);return investigationView(row);
  });
 }
 @Transactional public JsonNode investigate(String target,JsonNode body,String header,String key){
  fields(body,"versionNo","reason","investigationText");var c=mutations.context("balance:investigate");long expected=ProductionQualityPlanService.expected(body,header);String why=text(body,"reason",1000),summary=text(body,"investigationText",1000);
  return command(c,"BALANCE_INVESTIGATE:"+target,key,Map.of("investigation",target,"body",body),()->{
   var row=lockInvestigation(c.organizationId(),target);com.hospital.mes.qms.domain.IncomingRules.version(row.getVersionNo(),expected);gate(row.getStatus().equals("OPEN"),"BALANCE_INVESTIGATION_STATE","Investigation must be open");
   var before=store.view(row);row.setStatus("INVESTIGATING");row.setInvestigationText(summary);row.setInvestigatedBy(c.actorId());store.update(INV,row,expected,c.actorId(),"status","investigationText","investigatedBy");audit(c,"BALANCE_INVESTIGATE",row,before,why,key);return investigationView(row);
  });
 }
 @Transactional public JsonNode approve(String target,JsonNode body,String header,String key){
  fields(body,"versionNo","reason","decision","signature");var c=mutations.context("balance:approve");long expected=ProductionQualityPlanService.expected(body,header);String why=text(body,"reason",1000),decision=text(body,"decision",30);
  if(!Set.of("ACCEPT_EXCEPTION","REQUIRE_RECALCULATION").contains(decision))throw new IllegalArgumentException("Invalid investigation decision");
  return command(c,"BALANCE_INVESTIGATION_APPROVE:"+target,key,Map.of("investigation",target,"body",body),()->{
   var row=lockInvestigation(c.organizationId(),target);com.hospital.mes.qms.domain.IncomingRules.version(row.getVersionNo(),expected);gate(row.getStatus().equals("INVESTIGATING"),"BALANCE_INVESTIGATION_STATE","Investigation evidence required");
   var original=(BalanceResultRow)store.get(RESULT,c.organizationId(),row.getBalanceResultId());
   gate(!Objects.equals(c.actorId(),row.getInvestigatedBy())&&!Objects.equals(c.actorId(),row.getCreatedBy())&&!Objects.equals(c.actorId(),original.getCalculatedBy()),"INDEPENDENT_REVIEW_REQUIRED","Calculator/investigator cannot approve an exception");
   currentResult(original);gate(row.getInputDigest().equals(original.getInputDigest()),"BALANCE_RESULT_STALE","Investigation must bind the exact original input digest");
   var before=store.view(row);row.setDecision(decision);row.setStatus(decision.equals("ACCEPT_EXCEPTION")?"APPROVED_EXCEPTION":"RECALCULATE_REQUIRED");row.setApprovedBy(c.actorId());row.setApprovedAt(now());
   var evidence=signing.sign(c,"BALANCE_INVESTIGATION",target,expected,SignatureMeaning.APPROVE,row,body.get("signature"),key,List.of("BalanceResult:"+row.getBalanceResultId(),"InputDigest:"+row.getInputDigest()));row.setSignatureId(evidence.id());row.setSignatureEvidenceJson(evidence.envelope());
   store.update(INV,row,expected,c.actorId(),"decision","status","approvedBy","approvedAt","signatureId","signatureEvidenceJson");audit(c,"BALANCE_INVESTIGATION_APPROVE",row,before,why,key);return investigationView(row);
  });
 }
 private BalanceInvestigationRow lockInvestigation(long org,String target){
  var found=(BalanceInvestigationRow)store.get(INV,org,MasterMutation.id(target));var result=(BalanceResultRow)store.get(RESULT,org,found.getBalanceResultId());mutable(batches.lockBatch(org,result.getMainBatchId()).status());return (BalanceInvestigationRow)store.lock(INV,org,found.getId());
 }
 @Transactional(propagation=Propagation.MANDATORY) public void requireCheckpoint(long org,long batch,String checkpoint,Long operation){
  if(!Set.of("OPERATION_COMPLETE","BATCH_COMPLETE","QA_RELEASE").contains(checkpoint))throw new IllegalArgumentException("Invalid checkpoint");var root=batches.lockBatch(org,batch);requirePlan(root.snapshot());
  var c=contexts.current();gate(c.organizationId()==org,"BALANCE_ORGANIZATION_MISMATCH","Cross-organization gate is forbidden");quantities.synchronizeLogistics(c,batch);
  var all=rules(org,batch);gate(!all.isEmpty(),"BALANCE_RULE_MISSING","Actual frozen balance rules required");
  for(var rule:all){boolean due=checkpoint.equals("QA_RELEASE")||checkpoint.equals("BATCH_COMPLETE")&&!rule.getCheckPoint().equals("QA_RELEASE")||checkpoint.equals("OPERATION_COMPLETE")&&rule.getCheckPoint().equals(checkpoint)&&Objects.equals(rule.getOperationExecutionId(),operation);if(!due)continue;
   var history=results(rule);gate(!history.isEmpty(),"BALANCE_RESULT_MISSING","Required calculation missing");var latest=history.getLast();currentResult(latest);
   if(latest.getStatus().equals("FAIL")){var investigation=investigation(latest);gate(investigation!=null&&investigation.getStatus().equals("APPROVED_EXCEPTION")&&investigation.getInputDigest().equals(latest.getInputDigest())&&signing.valid(org,investigation.getSignatureId()),"BALANCE_FAILED","Failed balance requires exact independent signed exception");}
  }
 }
 private void currentResult(BalanceResultRow result){var rule=(BalanceRuleRow)store.get(RULE,result.getOrgId(),result.getBalanceRuleId());var history=results(rule);gate(!history.isEmpty()&&history.getLast().getId().equals(result.getId()),"BALANCE_RESULT_STALE","Result is no longer the latest calculation");gate(result.getInputDigest().equals(inputs(rule).digest()),"BALANCE_RESULT_STALE","Actual inputs/conversions changed since calculation");}
 private Inputs inputs(BalanceRuleRow rule){
  List<JsonNode> actual=quantities.facts(rule.getOrgId(),rule.getMainBatchId());Map<Long,JsonNode> byId=new HashMap<>();actual.forEach(x->byId.put(x.path("id").asLong(),x));
  var selected=new ArrayList<JsonNode>();var materialByEvent=new HashMap<Long,Long>();var typeByEvent=new HashMap<Long,String>();
  for(JsonNode event:actual){if(rule.getOperationExecutionId()!=null&&event.path("operationExecutionId").asLong()!=rule.getOperationExecutionId())continue;
   Long material=event.hasNonNull("materialLotId")?quantities.lotFact(rule.getOrgId(),event.path("materialLotId").asLong()).path("materialId").asLong():null;
   if(rule.getMaterialId()!=null&&!rule.getMaterialId().equals(material))continue;selected.add(event);if(material!=null)materialByEvent.put(event.path("id").asLong(),material);
  }
  gate(selected.size()<=1000,"BALANCE_EVIDENCE_LIMIT","Balance input evidence exceeds frozen bounded read model");var snapshot=json.createObjectNode();var events=snapshot.putArray("events");var conversions=snapshot.putArray("conversions");Map<String,BigDecimal> totals=new HashMap<>();
  for(JsonNode event:selected){long eventId=event.path("id").asLong(),from=event.path("unitId").asLong();String type=event.path("eventType").asText();
   if(type.equals("REVERSAL")){var original=byId.get(event.path("reversalOfId").asLong());gate(original!=null&&original.path("mainBatchId").asLong()==rule.getMainBatchId()&&original.path("unitId").asLong()==from&&Objects.equals(original.get("materialLotId"),event.get("materialLotId")),"BALANCE_REVERSAL_INVALID","Exact original source identity required");type=original.path("eventType").asText();gate(new BigDecimal(original.path("amount").asText()).negate().compareTo(new BigDecimal(event.path("amount").asText()))==0,"BALANCE_REVERSAL_INVALID","Reversal amount must exactly negate original");}
   if(type.equals("CHARGE_REVERSE")){
    var candidates=actual.stream().filter(x->x.path("eventType").asText().equals("CHARGE")&&x.path("sourceType").asText().equals("MaterialCharge")&&x.path("sourceRef").asText().equals(event.path("sourceRef").asText())).toList();
    gate(candidates.size()==1&&event.path("sourceType").asText().equals("MaterialCharge"),"BALANCE_REVERSAL_INVALID","Exact actual charge predecessor required");var original=candidates.getFirst();
    gate(original.path("unitId").asLong()==from&&Objects.equals(original.get("materialLotId"),event.get("materialLotId"))&&Objects.equals(original.get("operationExecutionId"),event.get("operationExecutionId"))&&new BigDecimal(original.path("amount").asText()).negate().compareTo(new BigDecimal(event.path("amount").asText()))==0,"BALANCE_REVERSAL_INVALID","Charge reversal must preserve exact original identity/amount");type="CHARGE";
   }
   gate(Set.of("CHARGE","ISSUE","RETURN","OUTPUT","SAMPLE","LOSS","SCRAP","WIP").contains(type),"BALANCE_INPUT_INVALID","Unrecognized actual quantity source");
   if(event.hasNonNull("signatureId"))gate(signing.valid(rule.getOrgId(),event.path("signatureId").asLong()),"QUANTITY_SIGNATURE_INVALID","Actual observation signature must remain valid");
   typeByEvent.put(eventId,type);events.add(event);
  }
  var formula=parse(rule.getFormulaExpr());var referenced=BalanceFormula.sources(formula);var globalSources=new HashSet<String>();
  for(var group:BalanceFormula.sourceGroups(formula)){var materials=new HashSet<Long>();for(var event:selected)if(group.contains(typeByEvent.get(event.path("id").asLong()))&&materialByEvent.containsKey(event.path("id").asLong()))materials.add(materialByEvent.get(event.path("id").asLong()));if(materials.size()>1)globalSources.addAll(group);}
  for(JsonNode event:selected){long eventId=event.path("id").asLong(),from=event.path("unitId").asLong();String type=typeByEvent.get(eventId);if(!referenced.contains(type))continue;
   Long material=globalSources.contains(type)?null:materialByEvent.get(eventId);BigDecimal amount=new BigDecimal(event.path("amount").asText());var proof=units.conversionEvidence(rule.getOrgId(),from,rule.getUnitId(),material,amount);var conversion=proof.conversion();BigDecimal converted=amount.multiply(new BigDecimal(conversion.factor()));
   gate(converted.compareTo(new BigDecimal(conversion.convertedValue()))==0,"QUANTITY_PRECISION_LOSS","Every referenced input unit conversion must be exact");totals.merge(type,converted,BigDecimal::add);
   var item=conversions.addObject().put("eventId",eventId+"").put("sourceUnitId",from+"").put("targetUnitId",rule.getUnitId()+"").put("sourceAmount",amount.toPlainString()).put("convertedAmount",converted.toPlainString()).put("factor",conversion.factor());if(material==null)item.putNull("materialId");else item.put("materialId",material+"");if(proof.conversionId()==null)item.putNull("conversionId");else item.put("conversionId",proof.conversionId()+"");
  }
  return new Inputs(Map.copyOf(totals),snapshot,mutations.digest(snapshot));
 }
 private ObjectNode definition(BalanceRuleRow rule){var frozen=batches.batch(rule.getOrgId(),rule.getMainBatchId()).snapshot().path("qualityPlan");for(var definition:frozen.path("balanceRules"))if(definition.path("balanceCode").asText().equals(rule.getBalanceCode()))return (ObjectNode)definition.deepCopy();throw new ComplianceException("BALANCE_RULE_MISSING","Rule has no actual frozen plan source");}
 private List<BalanceRuleRow> rules(long org,long batch){return store.rows(RULE,org,"mainBatchId",batch).stream().map(x->(BalanceRuleRow)x).toList();}
 private List<BalanceResultRow> results(BalanceRuleRow rule){return store.rows(RESULT,rule.getOrgId(),"balanceRuleId",rule.getId()).stream().map(x->(BalanceResultRow)x).sorted(Comparator.comparingInt(BalanceResultRow::getCalculationVersion)).toList();}
 private BalanceInvestigationRow investigation(BalanceResultRow result){var found=store.rows(INV,result.getOrgId(),"balanceResultId",result.getId());return found.isEmpty()?null:(BalanceInvestigationRow)found.getFirst();}
 private ObjectNode investigationView(BalanceInvestigationRow row){var n=store.view(row);n.remove(List.of("signatureEvidence","signatureEvidenceJson"));var actions=n.putArray("allowedActions");if(row.getStatus().equals("OPEN"))actions.add("INVESTIGATE");if(row.getStatus().equals("INVESTIGATING"))actions.add("APPROVE");return n;}
 private ObjectNode resultView(BalanceResultRow row){var n=store.view(row);var investigation=investigation(row);n.set("investigation",investigation==null?json.nullNode():investigationView(investigation));return n;}
 private ObjectNode view(long org,long batch){var root=batches.batch(org,batch);var n=json.createObjectNode().put("mainBatchId",Long.toString(batch));var list=n.putArray("results");var blocks=n.putArray("blockingCodes");if(!root.snapshot().hasNonNull("qualityPlan"))blocks.add("QUALITY_PLAN_REQUIRED");for(var rule:rules(org,batch)){var history=results(rule);history.forEach(x->list.add(resultView(x)));if(history.isEmpty())blocks.add("BALANCE_RESULT_MISSING");else try{currentResult(history.getLast());var latest=history.getLast();if(latest.getStatus().equals("FAIL")){var investigation=investigation(latest);if(investigation==null||!investigation.getStatus().equals("APPROVED_EXCEPTION")||!signing.valid(org,investigation.getSignatureId()))blocks.add("BALANCE_FAILED");}}catch(ComplianceException e){blocks.add(e.code());}}
  var actions=n.putArray("allowedActions");if(Set.of("IN_PROGRESS","PRODUCTION_COMPLETED","PENDING_QA").contains(root.status())&&root.snapshot().hasNonNull("qualityPlan"))actions.add("RECALCULATE");return n;
 }
 private static void requirePlan(JsonNode snapshot){gate(snapshot.hasNonNull("qualityPlan")&&snapshot.path("qualityPlan").path("status").asText().equals("APPROVED"),"QUALITY_PLAN_REQUIRED","Actual frozen approved quality plan required");}
 private static void mutable(String status){gate(Set.of("IN_PROGRESS","PRODUCTION_COMPLETED","PENDING_QA").contains(status),"BALANCE_BATCH_NOT_MUTABLE","Final QA/draft batches cannot acquire new balance facts");}
 private void audit(CurrentPlatformContext c,String action,BalanceInvestigationRow row,JsonNode before,String reason,String key){mutations.auditSnapshot(c,action,"BalanceInvestigation",row.getId(),before,store.view(row),reason,key);}
 private JsonNode parse(String raw){try{return json.readTree(raw);}catch(java.io.IOException e){throw new IllegalStateException("Corrupt balance evidence",e);}}
 private JsonNode command(CurrentPlatformContext c,String action,String key,Object request,java.util.function.Supplier<JsonNode> work){
  String canonical;try{canonical=new org.erdtman.jcs.JsonCanonicalizer(json.writeValueAsBytes(request)).getEncodedString();}catch(java.io.IOException e){throw new IllegalArgumentException(e);}var d=keys.begin(new IdempotencyCommand(c.organizationId(),c.actorId(),action,key,canonical));if(d.type()==IdempotencyDecisionType.REPLAY)return parse(d.responseJson());if(d.type()!=IdempotencyDecisionType.OWNER)throw new ResourceConflictException("IDEMPOTENCY_CONFLICT","Command key already in use");JsonNode response;try{response=work.get();}catch(RuntimeException e){throw ScopedStore.translateConcurrency(e);}keys.complete(d.handle(),200,response.toString(),"MaterialBalance",response.path("id").asText(response.path("mainBatchId").asText()));return parse(response.toString());
 }
 public List<String> envelopes(long org,String id){var row=(BalanceInvestigationRow)store.get(INV,org,MasterMutation.id(id));return row.getSignatureEvidenceJson()==null?List.of():List.of(row.getSignatureEvidenceJson());}
 /** Narrow downstream query; original immutable calculations and exception signatures are retained. */
 @Transactional(readOnly=true) public JsonNode evidence(long org,long batch){batches.batch(org,batch);var a=json.createArrayNode();for(var rule:rules(org,batch))for(var result:results(rule))a.add(resultView(result));return a;}
 @Transactional(readOnly=true) public JsonNode sourceEvidence(long org,long batch){batches.batch(org,batch);var a=json.createArrayNode();for(var rule:rules(org,batch)){var n=store.view(rule);var h=n.putArray("results");for(var result:results(rule)){var r=store.view(result);var investigation=investigation(result);r.set("investigation",investigation==null?json.nullNode():store.view(investigation));h.add(r);}a.add(n);}return a;}

}
