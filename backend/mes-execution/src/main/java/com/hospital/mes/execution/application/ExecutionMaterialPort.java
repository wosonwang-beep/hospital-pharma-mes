package com.hospital.mes.execution.application;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
public interface ExecutionMaterialPort {
 void requireOperationMaterials(long org,long mainBatchId);
 JsonNode lot(long org,long materialLotId);
 void requireEligible(long org,long mainBatchId,long materialLotId,long materialId,Instant at);
}
