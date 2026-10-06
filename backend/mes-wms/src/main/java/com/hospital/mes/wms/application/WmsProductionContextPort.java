package com.hospital.mes.wms.application;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
/** Implemented in boot, using real production and charge query producers. */
public interface WmsProductionContextPort {
 record BatchContext(long mainBatchId,String status,JsonNode snapshot,long versionNo){}
 BatchContext lockBatch(long organizationId,long batchId);
 BigDecimal chargedQuantity(long organizationId,long batchId,long lotId,long unitId);
 /** Same-org read, available to WMS without importing the production module. */
 com.fasterxml.jackson.databind.JsonNode batchFact(long organizationId,long batchId);
}
