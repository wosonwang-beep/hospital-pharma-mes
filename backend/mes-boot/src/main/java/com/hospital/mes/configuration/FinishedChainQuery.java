package com.hospital.mes.configuration;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.mes.audit.application.CurrentPlatformContextResolver;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.production.application.*;
import com.hospital.mes.production.infrastructure.*;
import com.hospital.mes.execution.application.ExecutionQueryService;
import com.hospital.mes.execution.application.ProductionQuantityService;
import com.hospital.mes.qms.application.*;
import com.hospital.mes.release.application.FinishedDecisionQuery;
import com.hospital.mes.wms.application.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@org.springframework.stereotype.Service @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class FinishedChainQuery {
 private final WmsQueryService inventory;private final FinishedGoodsService goods;private final FinishedInspectionService inspection;private final ProductionQualityService quality;private final ProductionQueryService production;private final ProductionQuantityService quantities;private final ProductionStore store;private final FinishedDecisionQuery decisions;private final ExecutionQueryService execution;private final CurrentPlatformContextResolver contexts;private final ObjectMapper json;private final MasterMutation mutations;
 public FinishedChainQuery(WmsQueryService inventory,FinishedGoodsService goods,FinishedInspectionService inspection,ProductionQualityService quality,ProductionQueryService production,ProductionQuantityService quantities,ProductionStore store,FinishedDecisionQuery decisions,ExecutionQueryService execution,CurrentPlatformContextResolver contexts,ObjectMapper json,MasterMutation mutations){this.inventory=inventory;this.goods=goods;this.inspection=inspection;this.quality=quality;this.production=production;this.quantities=quantities;this.store=store;this.decisions=decisions;this.execution=execution;this.contexts=contexts;this.json=json;this.mutations=mutations;}
 @Transactional(readOnly=true) public JsonNode get(String id){var c=mutations.context("wms:inventory:view");var chain=forLot(c.organizationId(),MasterMutation.id(id));if(chain==null)throw new NoSuchElementException("Finished material lot not found");return chain;}
 public Long batchForLot(long org,long lot){var b=store.batchMapper.selectOne(new QueryWrapper<BatchEntity>().eq("org_id",org).eq("finished_lot_id",lot));return b==null?null:b.getId();}
 @Transactional(readOnly=true) public JsonNode forLot(long org,long lot){var id=batchForLot(org,lot);if(id==null)return null;var c=contexts.current();if(c.organizationId()!=org)throw new NoSuchElementException("Scoped finished lot not found");var n=json.createObjectNode();var access=n.putObject("access");n.put("mainBatchId",id+"");n.set("lot",inventory.materialLot(org,lot));add(n,access,"production",c.hasPermission("production:batch:view"),()->{var p=json.createObjectNode();p.set("batch",production.batchFact(org,id));p.set("outputFacts",json.valueToTree(quantities.facts(org,id)));p.set("inputCharges",json.valueToTree(execution.chargesForBatch(org,id)));p.set("genealogy",json.valueToTree(execution.genealogyForBatch(org,id)));return p;});add(n,access,"receiving",c.hasPermission("wms:finished-inbound:view"),()->json.valueToTree(goods.inboundFacts(org,lot)));boolean qc=c.hasPermission("qms:finished-request:view")&&c.hasPermission("qms:finished-sampling:view")&&c.hasPermission("qms:finished-report:view");add(n,access,"finishedQuality",qc,()->inspection.sourceEvidence(org,id));add(n,access,"rawQuality",c.hasPermission("qms:test:view")&&c.hasPermission("qms:sample:view"),()->quality.sourceEvidence(org,id));add(n,access,"decisions",c.hasPermission("qa:batch-review"),()->json.valueToTree(decisions.sourceDecisionEvidence(org,id)));add(n,access,"shipments",c.hasPermission("wms:finished-shipment:view"),()->json.valueToTree(goods.shipmentFacts(org,lot)));add(n,access,"ledger",c.hasPermission("wms:inventory:view"),()->json.valueToTree(inventory.ledger(org,lot)));return n;}
 private void add(ObjectNode n,ObjectNode access,String key,boolean allowed,java.util.function.Supplier<JsonNode> fact){access.put(key,allowed?"AVAILABLE":"NOT_AUTHORIZED");if(allowed)n.set(key,fact.get());}
}
