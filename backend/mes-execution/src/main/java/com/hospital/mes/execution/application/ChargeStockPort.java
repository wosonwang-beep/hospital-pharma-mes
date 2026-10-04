package com.hospital.mes.execution.application;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import java.math.BigDecimal;
public interface ChargeStockPort {
 void lockLot(long organizationId,long lotId);
 void consume(CurrentPlatformContext context,long mainBatchId,long lotId,long chargeId,BigDecimal quantity,long unitId,String key);
 void reverse(CurrentPlatformContext context,long mainBatchId,long lotId,long chargeId,String key);
}
