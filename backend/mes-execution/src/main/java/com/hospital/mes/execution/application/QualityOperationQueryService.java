package com.hospital.mes.execution.application;
import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.mes.execution.infrastructure.*;
import com.hospital.mes.production.application.ProductionQueryService;
import com.hospital.mes.common.exception.ComplianceException;
@org.springframework.stereotype.Service @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class QualityOperationQueryService {
 public record LockedOperation(OperationEntity operation,JsonNode definition){}
 private final ExecutionStore db;private final ProductionQueryService production;
 public QualityOperationQueryService(ExecutionStore db,ProductionQueryService production){this.db=db;this.production=production;}
 public LockedOperation lock(long org,long operation){var found=db.operations.get(org,operation);var context=production.lockExecution(org,found.getExecutionUnitId());var current=db.operations.lock(org,operation);return new LockedOperation(current,definition(context.snapshot(),current.getOperationDefId()));}
 public JsonNode definition(JsonNode snapshot,long definition){for(var d:snapshot.path("process").path("route").path("operations"))if(d.path("operationDefId").asText().equals(Long.toString(definition)))return d;throw new ComplianceException("PRECONDITION_FAILED","Operation missing from frozen snapshot");}
 public OperationEntity get(long org,long id){return db.operations.get(org,id);}
}
