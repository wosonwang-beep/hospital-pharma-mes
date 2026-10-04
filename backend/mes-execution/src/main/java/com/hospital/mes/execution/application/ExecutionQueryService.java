package com.hospital.mes.execution.application;
import com.fasterxml.jackson.databind.JsonNode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.mes.execution.infrastructure.*;
import com.hospital.mes.production.application.*;
import com.hospital.mes.masterdata.application.MasterQueryService;
import java.util.*;
import java.math.BigDecimal;
@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ExecutionQueryService {
 private final ExecutionStore db;private final ProductionQueryService production;private final com.fasterxml.jackson.databind.ObjectMapper json;private final MasterQueryService units;private final ProductionViews views;
 public ExecutionQueryService(ExecutionStore db,ProductionQueryService production,com.fasterxml.jackson.databind.ObjectMapper json,MasterQueryService units,ProductionViews views){this.db=db;this.production=production;this.json=json;this.units=units;this.views=views;}
 public record OperationContext(long mainBatchId,long executionUnitId,long operationId,long operationDefId,String operationStatus,String batchStatus,JsonNode snapshot){}
 public OperationContext operation(long org,long id){return context(org,db.operations.get(org,id),false);}
 public OperationContext lockOperation(long org,long id){var found=db.operations.get(org,id);production.lockExecution(org,found.getExecutionUnitId());return context(org,db.operations.lock(org,id),true);}
 private OperationContext context(long org,OperationEntity row,boolean locked){var parent=production.execution(org,row.getExecutionUnitId());return new OperationContext(parent.mainBatchId(),row.getExecutionUnitId(),row.getId(),row.getOperationDefId(),row.getStatus(),parent.batchStatus(),parent.snapshot());}
 public List<Long> operationIds(long org,long execution){production.execution(org,execution);return db.operationMapper.selectList(new QueryWrapper<OperationEntity>().eq("org_id",org).eq("execution_unit_id",execution).orderByAsc("operation_seq","id")).stream().map(OperationEntity::getId).toList();}
 public List<Long> batchOperationIds(long org,long batch){production.batch(org,batch);return production.executionIds(org,batch).stream().flatMap(x->operationIds(org,x).stream()).toList();}
 /** Current actual consumption only; reversal remains visible in evidence but contributes zero entitlement usage. */
 public BigDecimal chargedQuantity(long org,long batch,long lot,long unit){production.batch(org,batch);var ids=production.executionIds(org,batch);if(ids.isEmpty())return BigDecimal.ZERO;var rows=db.chargeMapper.selectList(new QueryWrapper<ChargeEntity>().eq("org_id",org).in("execution_unit_id",ids).eq("material_lot_id",lot).eq("status","CONFIRMED"));BigDecimal result=BigDecimal.ZERO;for(var row:rows)result=result.add(ProductionInput.exact(units,org,row.getUnitId(),unit,material(org,row),row.getChargedQty()));return result;}
 private long material(long org,ChargeEntity row){var snapshot=production.execution(org,row.getExecutionUnitId()).snapshot();if(row.getWeighingRecordId()!=null){long item=db.weighings.get(org,row.getWeighingRecordId()).getBomItemId();for(var f:snapshot.path("process").path("formula").path("items"))if(f.path("formulaItemId").asText().equals(Long.toString(item)))return f.path("materialId").asLong();} // For same-unit conversions material lookup is unnecessary; differing-unit direct charge resolves via immutable gate source below.
  try{var evidence=json.readTree(row.getGateEvidenceJson());for(var e:evidence)if(e.hasNonNull("materialId"))return Long.parseLong(e.get("materialId").asText());}catch(java.io.IOException ex){throw new IllegalStateException(ex);}throw new IllegalStateException("Charge material evidence missing");}
 public List<JsonNode> chargesForBatch(long org,long batch){production.batch(org,batch);var ids=production.executionIds(org,batch);if(ids.isEmpty())return List.of();return db.chargeMapper.selectList(new QueryWrapper<ChargeEntity>().eq("org_id",org).in("execution_unit_id",ids).orderByAsc("id")).stream().map(x->(JsonNode)views.view(x)).toList();}
 public List<JsonNode> chargesForLot(long org,long lot){return db.chargeMapper.selectList(new QueryWrapper<ChargeEntity>().eq("org_id",org).eq("material_lot_id",lot).orderByAsc("id")).stream().map(x->(JsonNode)views.view(x)).toList();}
 public JsonNode weighing(long org,long id){var out=views.view(db.weighings.get(org,id));out.set("formulaItemId",out.remove("bomItemId"));return out;}
 public JsonNode operationFact(long org,long id){return views.view(db.operations.get(org,id));}
 public List<JsonNode> genealogyForBatch(long org,long batch){production.batch(org,batch);return db.genealogyMapper.selectList(new QueryWrapper<GenealogyEntity>().eq("org_id",org).eq("main_batch_id",batch).orderByAsc("id")).stream().map(x->(JsonNode)views.view(x)).toList();}
}
