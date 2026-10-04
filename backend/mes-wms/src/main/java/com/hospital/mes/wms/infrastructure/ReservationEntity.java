package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.*;
import java.math.BigDecimal;
@TableName("wms_reservation")
public class ReservationEntity extends ScopedEntity {
 private Long mainBatchId;
 public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long value){mainBatchId=value;}
 private Long formulaItemId;
 public Long getFormulaItemId(){return formulaItemId;} public void setFormulaItemId(Long value){formulaItemId=value;}
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private BigDecimal reservedQty;
 public BigDecimal getReservedQty(){return reservedQty;} public void setReservedQty(BigDecimal value){reservedQty=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
}
