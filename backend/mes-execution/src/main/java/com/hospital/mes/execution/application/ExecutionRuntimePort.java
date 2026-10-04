package com.hospital.mes.execution.application;
import com.fasterxml.jackson.databind.JsonNode;
/** Boot routes these calls to the actual eBR runtime; it must never return placeholder success. */
public interface ExecutionRuntimePort {
 void initializeOperation(long org,long actor,long batchId,long executionId,long operationId,long operationDefId,JsonNode snapshot);
 void requireOperationComplete(long org,long operationId);
 void requireOperationReview(long org,long operationId);
}
