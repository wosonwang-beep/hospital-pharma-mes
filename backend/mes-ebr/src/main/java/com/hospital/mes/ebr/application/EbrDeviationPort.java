package com.hospital.mes.ebr.application;
import com.fasterxml.jackson.databind.JsonNode;
/** A real controlled investigation producer; absence is fail-closed. */
public interface EbrDeviationPort {
 void record(long org,long actor,long batchId,long operationId,long formId,String ruleCode,JsonNode input,String reason,String idempotencyKey);
}
