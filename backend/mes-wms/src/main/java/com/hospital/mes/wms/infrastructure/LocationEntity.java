package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.*;
import java.math.BigDecimal;
@TableName("wms_location")
public class LocationEntity extends ScopedEntity {
 private Long warehouseId;
 public Long getWarehouseId(){return warehouseId;} public void setWarehouseId(Long value){warehouseId=value;}
 private String locationCode;
 public String getLocationCode(){return locationCode;} public void setLocationCode(String value){locationCode=value;}
 private String locationName;
 public String getLocationName(){return locationName;} public void setLocationName(String value){locationName=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
}
