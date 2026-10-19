package com.hospital.mes.wms.infrastructure;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Append-only business fact. No public business-field setters or update commands. */
@TableName("wms_finished_inventory_ledger")
public class FinishedInventoryLedgerEntity extends ScopedEntity {
 private Long mainBatchId,productId,locationId,unitId,inboundRequestId,shipmentId;
 private String eventType,idempotencyKey;
 private BigDecimal deltaQty;
 private LocalDateTime occurredAt;
 public Long getMainBatchId(){return mainBatchId;} public Long getProductId(){return productId;}
 public Long getLocationId(){return locationId;} public Long getUnitId(){return unitId;}
 public Long getInboundRequestId(){return inboundRequestId;} public Long getShipmentId(){return shipmentId;}
 public String getEventType(){return eventType;} public String getIdempotencyKey(){return idempotencyKey;}
 public BigDecimal getDeltaQty(){return deltaQty;} public LocalDateTime getOccurredAt(){return occurredAt;}
 public static FinishedInventoryLedgerEntity receipt(long batch,long product,long location,long unit,BigDecimal amount,long source,String key,LocalDateTime at){return fact(batch,product,location,unit,amount,"FINISHED_RECEIPT",source,null,key,at);}
 public static FinishedInventoryLedgerEntity shipment(long batch,long product,long location,long unit,BigDecimal amount,long source,String key,LocalDateTime at){return fact(batch,product,location,unit,amount.negate(),"FINISHED_SHIPMENT",null,source,key,at);}
 private static FinishedInventoryLedgerEntity fact(long batch,long product,long location,long unit,BigDecimal delta,String event,Long receipt,Long shipment,String key,LocalDateTime at){
  if(delta==null||delta.signum()==0||batch<=0||product<=0||location<=0||unit<=0||key==null||at==null)throw new IllegalArgumentException("Complete finished stock fact required");
  if(receipt!=null&&delta.signum()<0||shipment!=null&&delta.signum()>0)throw new IllegalArgumentException("Stock source direction mismatch");
  delta=delta.setScale(6,java.math.RoundingMode.UNNECESSARY);if(delta.precision()-delta.scale()>12)throw new IllegalArgumentException("Bounded stock quantity required");
  var row=new FinishedInventoryLedgerEntity();row.mainBatchId=batch;row.productId=product;row.locationId=location;row.unitId=unit;row.deltaQty=delta;row.eventType=event;row.inboundRequestId=receipt;row.shipmentId=shipment;row.idempotencyKey=key;row.occurredAt=at;return row;
 }
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
