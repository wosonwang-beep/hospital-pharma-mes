package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.*;
import java.math.BigDecimal;
@TableName("wms_warehouse")
public class WarehouseEntity extends ScopedEntity {
 private String warehouseCode;
 public String getWarehouseCode(){return warehouseCode;} public void setWarehouseCode(String value){warehouseCode=value;}
 private String warehouseName;
 public String getWarehouseName(){return warehouseName;} public void setWarehouseName(String value){warehouseName=value;}
 private String warehouseType;
 public String getWarehouseType(){return warehouseType;} public void setWarehouseType(String value){warehouseType=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
}
