package com.hospital.mes.production.application;
import com.fasterxml.jackson.databind.*;
import com.hospital.mes.production.infrastructure.*;
import org.springframework.transaction.support.TransactionSynchronizationManager;
@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProductionQueryService {
 private final ProductionStore db;private final ObjectMapper json;
 public ProductionQueryService(ProductionStore db,ObjectMapper json){this.db=db;this.json=json;}
 public JsonNode batchFact(long org,long id){var b=db.batches.get(org,id);var views=new ProductionViews(json);var n=views.view(b);var subs=n.putArray("subBatches");db.subs(org,id).forEach(x->subs.add(views.view(x)));var executions=n.putArray("executionUnits");db.executions(org,id).forEach(x->executions.add(views.view(x)));n.set("processSnapshot",b.getProcessSnapshotId()==null?json.nullNode():views.view(db.snapshots.get(org,b.getProcessSnapshotId())));return n;}
 public record BatchContext(long mainBatchId,String status,JsonNode snapshot,long versionNo){}
 public record QualityIdentity(long mainBatchId,long productId,long unitId,Long processSnapshotId,Long finishedLotId,String status,long versionNo){}
 public QualityIdentity qualityIdentity(long org,long id){var row=db.batches.get(org,id);return new QualityIdentity(row.getId(),row.getProductId(),row.getUnitId(),row.getProcessSnapshotId(),row.getFinishedLotId(),row.getStatus(),row.getVersionNo());}
 public String batchNumber(long org,long id){return db.batches.get(org,id).getBatchNo();}
 public record ExecutionContext(long mainBatchId,long executionUnitId,Long subBatchId,String batchStatus,String executionStatus,JsonNode snapshot,long versionNo){}
 public BatchContext batch(long org,long id){var b=db.batches.get(org,id);return context(b);}
 public BatchContext lockBatch(long org,long id){requireTransaction();var b=db.batches.get(org,id);db.orders.lock(org,b.getProductionOrderId());return context(db.batches.lock(org,id));}
 public ExecutionContext execution(long org,long id){var e=db.executions.get(org,id);var b=batch(org,e.getMainBatchId());return new ExecutionContext(b.mainBatchId(),id,e.getSubBatchId(),b.status(),e.getStatus(),b.snapshot(),e.getVersionNo());}
 public ExecutionContext lockExecution(long org,long id){requireTransaction();var found=db.executions.get(org,id);var b=lockBatch(org,found.getMainBatchId());var e=db.executions.lock(org,id);return new ExecutionContext(b.mainBatchId(),id,e.getSubBatchId(),b.status(),e.getStatus(),b.snapshot(),e.getVersionNo());}
 public java.math.BigDecimal executionPlan(long org,long id){var e=db.executions.get(org,id);return e.getSubBatchId()==null?db.batches.get(org,e.getMainBatchId()).getPlannedQty():db.subs.get(org,e.getSubBatchId()).getPlannedQty();}
 public long executionPlanUnit(long org,long id){var e=db.executions.get(org,id);return db.batches.get(org,e.getMainBatchId()).getUnitId();}
 public java.util.List<Long> executionIds(long org,long batchId){db.batches.get(org,batchId);return db.executions(org,batchId).stream().map(ExecutionUnitEntity::getId).toList();}
 private BatchContext context(BatchEntity b){try{return new BatchContext(b.getId(),b.getStatus(),b.getProcessSnapshotId()==null?json.nullNode():json.readTree(db.snapshots.get(b.getOrgId(),b.getProcessSnapshotId()).getSnapshotJson()),b.getVersionNo());}catch(java.io.IOException ex){throw new IllegalStateException("Frozen snapshot corrupt",ex);}}
 private static void requireTransaction(){if(!TransactionSynchronizationManager.isActualTransactionActive())throw new IllegalStateException("Lock contract requires transaction");}
}
