package com.hospital.mes.qms.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.idempotency.*;
import com.hospital.mes.audit.signature.SignatureMeaning;
import com.hospital.mes.common.exception.ResourceConflictException;
import com.hospital.mes.execution.application.ExecutionQueryService;
import com.hospital.mes.masterdata.application.*;
import com.hospital.mes.production.application.*;
import com.hospital.mes.production.domain.ProductionEvents;
import com.hospital.mes.qc.application.QcSpecificationQueryService;
import com.hospital.mes.qms.domain.BalanceFormula;
import com.hospital.mes.qms.domain.IpcRules;
import com.hospital.mes.qms.infrastructure.*;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.*;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import java.util.*;
import static com.hospital.mes.production.application.ProductionInput.*;
import static com.hospital.mes.qms.domain.IncomingRules.gate;
import static com.hospital.mes.qms.infrastructure.IncomingStore.now;

/** Owns the batch quality plan. ProcessSnapshot is the immutable consumer of approved evidence. */
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProductionQualityPlanService implements ProductionQualityPlanPort {
    private static final String PLAN="qms_production_plan", RULE="mes_balance_rule";
    private final IncomingStore store;
    private final ProductionPlanRowMapper plans;
    private final ProductionQueryService production;
    private final ExecutionQueryService execution;
    private final MasterQueryService units;
    private final MaterialQueryService materials;
    private final QcSpecificationQueryService specs;
    private final MasterMutation mutations;
    private final PlatformIdempotencyService keys;
    private final SignedRecordSupport signing;
    private final ObjectMapper json;
    public ProductionQualityPlanService(IncomingStore store,ProductionPlanRowMapper plans,ProductionQueryService production,
            ExecutionQueryService execution,MasterQueryService units,MaterialQueryService materials,QcSpecificationQueryService specs,
            MasterMutation mutations,PlatformIdempotencyService keys,SignedRecordSupport signing,ObjectMapper json) {
        this.store=store;this.plans=plans;this.production=production;this.execution=execution;this.units=units;this.materials=materials;
        this.specs=specs;this.mutations=mutations;this.keys=keys;this.signing=signing;this.json=json;
    }
    @Transactional(readOnly=true)
    public JsonNode get(String id) { var c=mutations.context("qms:plan:view");return view((ProductionPlanRow)store.get(PLAN,c.organizationId(),MasterMutation.id(id))); }
    @Transactional(readOnly=true)
    public ScopedStore.PageData<JsonNode> list(int page,int size,Map<String,String> filters) {
        var c=mutations.context("qms:plan:view");if(page<0||page>1000000||size<1||size>100)throw new IllegalArgumentException("Invalid pagination");
        var query=new QueryWrapper<ProductionPlanRow>().eq("org_id",c.organizationId());
        String batch=filters.get("mainBatchId"),status=filters.get("status");
        if(batch!=null&&!batch.isBlank())query.eq("main_batch_id",MasterMutation.id(batch));
        if(status!=null&&!status.isBlank()){if(!Set.of("DRAFT","APPROVED").contains(status))throw new IllegalArgumentException("Invalid status filter");query.eq("status",status);}
        var result=plans.selectPage(new Page<>(page+1,size),query.orderByDesc("id"));
        return new ScopedStore.PageData<>(result.getRecords().stream().map(this::view).map(x->(JsonNode)x).toList(),result.getTotal(),page,size);
    }
    @Transactional
    public JsonNode create(JsonNode body,String key) {
        fields(body,"mainBatchId","finishedMaterialId","qcSpecificationVersionId","balanceRules","reason");
        var c=mutations.context("qms:plan:create");String reason=text(body,"reason",1000);
        return mutate(c,"PRODUCTION_PLAN_CREATE",key,body,201,()->{
            long batch=id(body,"mainBatchId");draftBatch(c.organizationId(),batch);
            gate(store.rows(PLAN,c.organizationId(),"mainBatchId",batch).isEmpty(),"QUALITY_PLAN_ALREADY_EXISTS","Batch already has a quality plan");
            var row=new ProductionPlanRow();row.setMainBatchId(batch);apply(c,row,body);row.setStatus("DRAFT");
            store.insert(PLAN,row,c.organizationId(),c.actorId());audit(c,"PRODUCTION_PLAN_CREATE",row,null,reason,key);return row;
        });
    }
    @Transactional
    public JsonNode update(String target,JsonNode body,String header,String key) {
        fields(body,"mainBatchId","finishedMaterialId","qcSpecificationVersionId","balanceRules","reason","versionNo");
        var c=mutations.context("qms:plan:update");long expected=expected(body,header);String reason=text(body,"reason",1000);
        return mutate(c,"PRODUCTION_PLAN_UPDATE:"+target,key,Map.of("target",target,"body",body),200,()->{
            var found=(ProductionPlanRow)store.get(PLAN,c.organizationId(),MasterMutation.id(target));draftBatch(c.organizationId(),found.getMainBatchId());
            var row=(ProductionPlanRow)store.lock(PLAN,c.organizationId(),found.getId());
            com.hospital.mes.qms.domain.IncomingRules.version(row.getVersionNo(),expected);
            gate(row.getStatus().equals("DRAFT"),"QUALITY_PLAN_FROZEN","Approved plan cannot be edited");
            gate(id(body,"mainBatchId")==row.getMainBatchId(),"QUALITY_PLAN_BATCH_IMMUTABLE","A plan cannot be reassigned to another batch");
            var before=store.view(row);apply(c,row,body);
            store.update(PLAN,row,expected,c.actorId(),"finishedMaterialId","qcSpecificationVersionId","planJson");
            audit(c,"PRODUCTION_PLAN_UPDATE",row,before,reason,key);return row;
        });
    }
    @Transactional
    public JsonNode approve(String target,JsonNode body,String header,String key) {
        fields(body,"versionNo","reason","signature");var c=mutations.context("qms:plan:approve");long expected=expected(body,header);String reason=text(body,"reason",1000);
        return mutate(c,"PRODUCTION_PLAN_APPROVE:"+target,key,Map.of("target",target,"body",body),200,()->{
            var found=(ProductionPlanRow)store.get(PLAN,c.organizationId(),MasterMutation.id(target));draftBatch(c.organizationId(),found.getMainBatchId());
            var row=(ProductionPlanRow)store.lock(PLAN,c.organizationId(),found.getId());com.hospital.mes.qms.domain.IncomingRules.version(row.getVersionNo(),expected);
            gate(row.getStatus().equals("DRAFT"),"QUALITY_PLAN_FROZEN","Approved plan cannot be approved again");
            gate(!Objects.equals(c.actorId(),row.getCreatedBy())&&!Objects.equals(c.actorId(),row.getUpdatedBy()),"INDEPENDENT_REVIEW_REQUIRED","Plan author/editor cannot approve it");
            finishedMaterial(c.organizationId(),row.getFinishedMaterialId());
            var spec=specs.requireSelectable(c.organizationId(),row.getFinishedMaterialId(),row.getQcSpecificationVersionId());
            gate(signing.valid(c.organizationId(),spec.approvalSignatureId()),"QUALITY_STANDARD_SIGNATURE_INVALID","Quality standard approval signature invalid");
            var snapshot=(ObjectNode)json.valueToTree(spec);
            for(int i=0;i<spec.items().size();i++){
                var item=spec.items().get(i);var node=(ObjectNode)snapshot.path("items").get(i);
                if(item.lowerLimit()!=null)node.put("lowerLimit",item.lowerLimit().toPlainString());
                if(item.upperLimit()!=null)node.put("upperLimit",item.upperLimit().toPlainString());
            }
            var before=store.view(row);row.setSpecificationSnapshotJson(snapshot.toString());
            var frozen=json.createObjectNode().put("mainBatchId",row.getMainBatchId().toString()).put("finishedMaterialId",row.getFinishedMaterialId().toString())
                    .put("qcSpecificationVersionId",row.getQcSpecificationVersionId().toString());
            frozen.set("balanceRules",parse(row.getPlanJson()));frozen.set("specification",snapshot);
            row.setContentHash(mutations.digest(frozen));row.setStatus("APPROVED");row.setApprovedBy(c.actorId());row.setApprovedAt(now());
            var evidence=signing.sign(c,"PRODUCTION_QUALITY_PLAN",target,expected,SignatureMeaning.APPROVE,row,body.get("signature"),key,List.of("MainBatch:"+row.getMainBatchId(),"QcSpecificationVersion:"+row.getQcSpecificationVersionId()));
            row.setSignatureId(evidence.id());row.setSignatureEvidenceJson(evidence.envelope());
            store.update(PLAN,row,expected,c.actorId(),"specificationSnapshotJson","contentHash","status","approvedBy","approvedAt","signatureId","signatureEvidenceJson");
            audit(c,"PRODUCTION_PLAN_APPROVE",row,before,reason,key);return row;
        });
    }
    private void apply(CurrentPlatformContext c,ProductionPlanRow row,JsonNode body) {
        long material=id(body,"finishedMaterialId"),spec=id(body,"qcSpecificationVersionId");finishedMaterial(c.organizationId(),material);
        specs.requireSelectable(c.organizationId(),material,spec);
        JsonNode rules=body.get("balanceRules");if(rules==null||!rules.isArray()||rules.isEmpty()||rules.size()>1000)throw new IllegalArgumentException("Nonempty bounded balanceRules required");
        Set<String> codes=new HashSet<>();boolean completion=false;
        for(JsonNode rule:rules){
            fields(rule,"balanceCode","basis","operationCode","materialId","unitId","formulaExpr","toleranceLow","toleranceHigh","checkPoint");
            if(!codes.add(text(rule,"balanceCode",80)))throw new IllegalArgumentException("Duplicate balanceCode");
            String basis=text(rule,"basis",30),checkpoint=text(rule,"checkPoint",30);
            if(!Set.of("BATCH","OPERATION","PACKAGING").contains(basis)||!Set.of("OPERATION_COMPLETE","BATCH_COMPLETE","QA_RELEASE").contains(checkpoint))throw new IllegalArgumentException("Invalid balance basis/checkpoint");
            if(basis.equals("OPERATION")||checkpoint.equals("OPERATION_COMPLETE"))text(rule,"operationCode",80);
            else if(rule.hasNonNull("operationCode"))text(rule,"operationCode",80);
            units.unit(c.organizationId(),id(rule,"unitId"));
            if(rule.hasNonNull("materialId"))materials.requireUsable(c.organizationId(),id(rule,"materialId"),java.time.Instant.now());
            BalanceFormula.validate(rule.get("formulaExpr"));
            if(IpcRules.numeric(text(rule,"toleranceLow",50)).compareTo(IpcRules.numeric(text(rule,"toleranceHigh",50)))>0)throw new IllegalArgumentException("Invalid tolerances");
            completion|=checkpoint.equals("BATCH_COMPLETE");
        }
        if(!completion)throw new IllegalArgumentException("At least one BATCH_COMPLETE rule required");
        row.setFinishedMaterialId(material);row.setQcSpecificationVersionId(spec);row.setPlanJson(rules.toString());
    }
    private void finishedMaterial(long org,long material) {
        var snapshot=materials.requireUsable(org,material,java.time.Instant.now());
        gate(snapshot.path("materialType").asText().equals("FINISHED"),"QUALITY_PLAN_FINISHED_MATERIAL_REQUIRED","Plan requires a usable finished material");
    }
    private void draftBatch(long org,long id) { gate(production.lockBatch(org,id).status().equals("DRAFT"),"QUALITY_PLAN_FROZEN","Plan may change only while the batch is draft"); }
    public static long expected(JsonNode body,String header) {
        if(header==null||!header.matches("\"(?:0|[1-9][0-9]*)\""))throw new IllegalArgumentException("Exact quoted If-Match required");
        return SignedRecordSupport.expected(body,header);
    }
    @Override @Transactional(propagation=Propagation.MANDATORY)
    public JsonNode frozenPlan(long org,long batch,JsonNode route) {
        var rows=store.lockedRows(PLAN,org,"mainBatchId",batch);if(rows.isEmpty())return null;
        var row=(ProductionPlanRow)rows.getFirst();gate(row.getStatus().equals("APPROVED"),"QUALITY_PLAN_NOT_APPROVED","Draft quality plan cannot be dispatched");
        gate(signing.valid(org,row.getSignatureId()),"QUALITY_PLAN_SIGNATURE_INVALID","Approved plan signature invalid");
        Set<String> codes=new HashSet<>();route.path("operations").forEach(x->codes.add(x.path("operationCode").asText()));
        for(JsonNode rule:parse(row.getPlanJson()))if(rule.hasNonNull("operationCode"))gate(codes.contains(rule.path("operationCode").asText()),"QUALITY_PLAN_OPERATION_MISMATCH","Balance operation is absent from the dispatched route");
        ObjectNode result=view(row);result.set("specification",parse(row.getSpecificationSnapshotJson()));result.remove("allowedActions");return result;
    }
    @EventListener @Order(100) @Transactional(propagation=Propagation.MANDATORY)
    public void initializeRules(ProductionEvents.BatchReleased event) {
        long org=event.organizationId(),batch=event.mainBatchId();var context=production.lockBatch(org,batch);JsonNode plan=context.snapshot().path("qualityPlan");if(plan.isMissingNode())return;
        gate(store.rows(RULE,org,"mainBatchId",batch).isEmpty(),"BALANCE_RULE_ALREADY_INITIALIZED","Rules cannot be duplicated");
        Long snapshotId=production.qualityIdentity(org,batch).processSnapshotId();
        for(JsonNode definition:plan.path("balanceRules")){
            List<Long> operations=new ArrayList<>();
            if(definition.hasNonNull("operationCode"))for(long op:execution.batchOperationIds(org,batch)){
                var actual=execution.operation(org,op);
                for(var frozen:context.snapshot().path("process").path("route").path("operations"))
                    if(frozen.path("operationDefId").asLong()==actual.operationDefId()&&frozen.path("operationCode").asText().equals(definition.path("operationCode").asText()))operations.add(op);
            }
            else operations.add(null);
            gate(!operations.isEmpty(),"QUALITY_PLAN_OPERATION_MISMATCH","Required actual operation missing");
            for(Long operation:operations){
                var row=new BalanceRuleRow();row.setProcessSnapshotId(snapshotId);row.setMainBatchId(batch);row.setBalanceCode(text(definition,"balanceCode",80));row.setBasis(text(definition,"basis",30));
                row.setOperationExecutionId(operation);row.setMaterialId(optionalId(definition,"materialId"));row.setUnitId(id(definition,"unitId"));row.setFormulaExpr(definition.path("formulaExpr").toString());
                row.setToleranceLow(IpcRules.numeric(text(definition,"toleranceLow",50)));row.setToleranceHigh(IpcRules.numeric(text(definition,"toleranceHigh",50)));row.setRuleVersion(1);row.setCheckPoint(text(definition,"checkPoint",30));
                store.insert(RULE,row,org,event.actorId());
            }
        }
    }
    /** Downstream archive keeps the actual plan approval envelope, not a second authored plan. */
    @Transactional(readOnly=true) public JsonNode sourceEvidence(long org,long batch){production.batch(org,batch);var rows=json.createArrayNode();for(var row:store.rows(PLAN,org,"mainBatchId",batch))rows.add(store.view(row));return rows;}
    public JsonNode parse(String raw) {try{return json.readTree(raw);}catch(java.io.IOException e){throw new IllegalStateException("Corrupt frozen quality evidence",e);}}
    public ObjectNode view(ProductionPlanRow row) {
        ObjectNode node=store.view(row);node.retain(List.of("id","orgId","createdBy","createdAt","updatedBy","updatedAt","versionNo","mainBatchId","finishedMaterialId","qcSpecificationVersionId","status","contentHash","approvedBy","approvedAt","signatureId"));node.set("balanceRules",parse(row.getPlanJson()));
        var actions=node.putArray("allowedActions");if(row.getStatus().equals("DRAFT")&&production.batch(row.getOrgId(),row.getMainBatchId()).status().equals("DRAFT")){actions.add("EDIT");actions.add("APPROVE");}
        return node;
    }
    private void audit(CurrentPlatformContext c,String action,ProductionPlanRow row,JsonNode before,String reason,String key) {
        mutations.auditSnapshot(c,action,"ProductionQualityPlan",row.getId(),before,store.view(row),reason,key);
    }
    private JsonNode mutate(CurrentPlatformContext c,String operation,String key,Object request,int status,java.util.function.Supplier<ProductionPlanRow> work) {
        String canonical;try{canonical=new org.erdtman.jcs.JsonCanonicalizer(json.writeValueAsBytes(request)).getEncodedString();}catch(java.io.IOException e){throw new IllegalArgumentException("Invalid command",e);}
        var decision=keys.begin(new IdempotencyCommand(c.organizationId(),c.actorId(),operation,key,canonical));
        if(decision.type()==IdempotencyDecisionType.REPLAY)return parse(decision.responseJson());
        if(decision.type()!=IdempotencyDecisionType.OWNER)throw new ResourceConflictException("IDEMPOTENCY_CONFLICT","Command key already in use");
        ProductionPlanRow row;try{row=work.get();}catch(RuntimeException e){throw ScopedStore.translateConcurrency(e);}
        JsonNode response=view(row);keys.complete(decision.handle(),status,response.toString(),"ProductionQualityPlan",row.getId().toString());return parse(response.toString());
    }
    public List<String> envelopes(long org,String id) {
        var row=(ProductionPlanRow)store.get(PLAN,org,MasterMutation.id(id));return row.getSignatureEvidenceJson()==null?List.of():List.of(row.getSignatureEvidenceJson());
    }
}
