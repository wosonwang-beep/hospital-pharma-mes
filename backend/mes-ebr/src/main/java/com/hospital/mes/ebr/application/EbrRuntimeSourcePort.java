package com.hospital.mes.ebr.application;
import com.fasterxml.jackson.databind.JsonNode;
/** An actual instrument/system producer must verify its source record before runtime ingestion. */
public interface EbrRuntimeSourcePort {
 String sourceType();
 void requireEvidence(long organizationId,long operationId,String sourceReference,String fieldCode,JsonNode rawValue,String unitId);
}
