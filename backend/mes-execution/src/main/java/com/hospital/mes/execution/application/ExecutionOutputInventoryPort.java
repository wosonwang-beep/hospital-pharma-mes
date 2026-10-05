package com.hospital.mes.execution.application;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDate;
/** Boot delegates physical output to WMS. Execution cannot author inventory rows. */
public interface ExecutionOutputInventoryPort {
 record Output(long batchId,Long finishedLotId,long materialId,BigDecimal amount,long unitId,String lotNo,long locationId,LocalDate productionDate,LocalDate expiryDate,String sourceRef,String reason){}
 long receiveOutput(CurrentPlatformContext context,Output output,String key);
 void reverseOutput(CurrentPlatformContext context,long materialLotId,String originalSourceRef,BigDecimal amount,long unitId,String sourceRef,String reason,String key);
 JsonNode lot(long organizationId,long materialLotId);
 record LogisticsQuantity(long id,String type,long materialLotId,BigDecimal amount,long unitId,java.time.LocalDateTime occurredAt){}
 java.util.List<LogisticsQuantity> confirmedLogistics(long organizationId,long mainBatchId);
}
