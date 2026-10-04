package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.*;
import java.math.BigDecimal;
@TableName("wms_inventory_ledger")
public class LedgerEntity extends ScopedEntity {
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private Long locationId;
 public Long getLocationId(){return locationId;} public void setLocationId(Long value){locationId=value;}
 private Long containerId;
 public Long getContainerId(){return containerId;} public void setContainerId(Long value){containerId=value;}
 private String eventType;
 public String getEventType(){return eventType;} public void setEventType(String value){eventType=value;}
 private BigDecimal deltaQty;
 public BigDecimal getDeltaQty(){return deltaQty;} public void setDeltaQty(BigDecimal value){deltaQty=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private String sourceType;
 public String getSourceType(){return sourceType;} public void setSourceType(String value){sourceType=value;}
 private String sourceRef;
 public String getSourceRef(){return sourceRef;} public void setSourceRef(String value){sourceRef=value;}
 private String idempotencyKey;
 public String getIdempotencyKey(){return idempotencyKey;} public void setIdempotencyKey(String value){idempotencyKey=value;}
 private LocalDateTime occurredAt;
 public LocalDateTime getOccurredAt(){return occurredAt;} public void setOccurredAt(LocalDateTime value){occurredAt=value;}
}
