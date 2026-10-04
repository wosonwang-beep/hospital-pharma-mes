package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.*;
import java.math.BigDecimal;
@TableName("wms_container")
public class ContainerEntity extends ScopedEntity {
 private String containerCode;
 public String getContainerCode(){return containerCode;} public void setContainerCode(String value){containerCode=value;}
 private String containerType;
 public String getContainerType(){return containerType;} public void setContainerType(String value){containerType=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
}
