package com.hospital.mes.production.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.masterdata.application.ScopedStore;
import com.hospital.mes.production.infrastructure.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/** Permission-scoped context selection; never returns generic batch snapshots or performs writes. */
@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
@Transactional(readOnly=true)
public class NavigationQueryService {
 private final ProductionStore db;private final MasterMutation mutations;private final ObjectMapper json;
 public NavigationQueryService(ProductionStore db,MasterMutation mutations,ObjectMapper json){this.db=db;this.mutations=mutations;this.json=json;}
 private long org(String context){return switch(context){case "balance"->mutations.context("balance:view").organizationId();case "qa-review"->mutations.context("qa:batch-review").organizationId();case "release"->{mutations.context("qa:batch-review");yield mutations.context("qa:release").organizationId();}default->throw new IllegalArgumentException("Unknown navigation context");};}
 private void filters(Map<String,String> f,int page,int size,Set<String> names){if(page<0||page>1000000||size<1||size>100)throw new IllegalArgumentException("Invalid pagination");if(!names.containsAll(f.keySet()))throw new IllegalArgumentException("Unknown navigation filter");}
 private void common(QueryWrapper<?> q,Map<String,String> f,String number){if(f.containsKey("keyword")&&!f.get("keyword").isBlank())q.like(number,com.hospital.mes.masterdata.domain.MasterRules.text(f.get("keyword"),200));if(f.containsKey("status")&&!f.get("status").isBlank())q.eq("status",f.get("status"));if(f.containsKey("mainBatchId")&&!f.get("mainBatchId").isBlank())q.eq("main_batch_id",MasterMutation.id(f.get("mainBatchId")));}
 public ScopedStore.PageData<JsonNode> executions(int page,int size,Map<String,String> f){long org=mutations.context("mes:execution:view").organizationId();filters(f,page,size,Set.of("page","size","keyword","status","mainBatchId"));var q=new QueryWrapper<ExecutionUnitEntity>().eq("org_id",org);common(q,f,"execution_no");long count=db.executionMapper.selectCount(q);var items=db.executionMapper.selectList(q.orderByDesc("id").last("LIMIT "+((long)page*size)+","+size)).stream().map(e->{var batch=db.batches.get(org,e.getMainBatchId());var n=json.createObjectNode();n.put("id",e.getId().toString());n.put("executionNo",e.getExecutionNo());n.put("mainBatchId",e.getMainBatchId().toString());n.put("batchNo",batch.getBatchNo());n.put("unitType",e.getUnitType());n.put("status",e.getStatus());return (JsonNode)n;}).toList();return new ScopedStore.PageData<>(items,count,page,size);}
 private QueryWrapper<BatchEntity> scoped(long org,String context){var q=new QueryWrapper<BatchEntity>().eq("org_id",org);if(!context.equals("balance"))q.isNotNull("finished_lot_id").in("status",List.of("PRODUCTION_COMPLETED","PENDING_QA","QA_RELEASED","REJECTED"));return q;}
 private JsonNode fact(BatchEntity b){var n=json.createObjectNode();n.put("id",b.getId().toString());n.put("mainBatchId",b.getId().toString());n.put("batchNo",b.getBatchNo());n.put("productId",b.getProductId().toString());n.put("plannedQty",b.getPlannedQty().setScale(6).toPlainString());n.put("unitId",b.getUnitId().toString());n.put("status",b.getStatus());if(b.getFinishedLotId()==null)n.putNull("finishedLotId");else n.put("finishedLotId",b.getFinishedLotId().toString());n.put("versionNo",b.getVersionNo());return n;}
 public ScopedStore.PageData<JsonNode> batches(String context,int page,int size,Map<String,String> f){long org=org(context);filters(f,page,size,Set.of("context","page","size","keyword","status","mainBatchId"));var q=scoped(org,context);if(f.containsKey("keyword")&&!f.get("keyword").isBlank())q.like("batch_no",com.hospital.mes.masterdata.domain.MasterRules.text(f.get("keyword"),200));if(f.containsKey("status")&&!f.get("status").isBlank())q.eq("status",f.get("status"));if(f.containsKey("mainBatchId")&&!f.get("mainBatchId").isBlank())q.eq("id",MasterMutation.id(f.get("mainBatchId")));long count=db.batchMapper.selectCount(q);var items=db.batchMapper.selectList(q.orderByDesc("id").last("LIMIT "+((long)page*size)+","+size)).stream().map(this::fact).toList();return new ScopedStore.PageData<>(items,count,page,size);}
 public JsonNode batch(String context,String id){long org=org(context);var b=db.batchMapper.selectOne(scoped(org,context).eq("id",MasterMutation.id(id)));if(b==null)throw new NoSuchElementException("Batch not found in navigation scope");return fact(b);}
}
