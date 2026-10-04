package com.hospital.mes.execution.application;
import com.fasterxml.jackson.databind.JsonNode;
/** Owner API contract; QMS implements it, Execution has no QMS dependency. */
public interface ExecutionQualityPort {
 void initialize(long org,long actor,long operationId,JsonNode frozenDefinition);
 void requireComplete(long org,long operationId,JsonNode frozenDefinition);
 JsonNode instances(long org,long operationId);
}
