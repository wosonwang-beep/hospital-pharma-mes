package com.hospital.mes.wms.domain;
import java.util.List;
/** Synchronous transaction event; no external event can manufacture receiving facts. */
public record ReceiptConfirmed(long organizationId,long receiptId,List<Long> exemptionLotIds,boolean directReceive,String idempotencyKey){
 public ReceiptConfirmed{exemptionLotIds=List.copyOf(exemptionLotIds);}
}
