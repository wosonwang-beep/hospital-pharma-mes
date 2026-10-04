package com.hospital.mes.ebr.application;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
/** Implemented in mes-boot against actual production/execution query producers. */
public interface EbrExecutionPort {
 record OperationContext(long mainBatchId,long executionUnitId,long operationId,long operationDefId,String operationStatus,String batchStatus,JsonNode snapshot){}
 OperationContext operation(long org,long operationId);
 OperationContext lockOperation(long org,long operationId);
 List<Long> operationIds(long org,long executionId);
 List<Long> batchOperationIds(long org,long batchId);
}
