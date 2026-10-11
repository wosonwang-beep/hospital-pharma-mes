package com.hospital.mes.reporting.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("mes_print_data_source_type_binding")
public class PrintDataSourceTypeBinding extends ScopedEntity {
 private String businessType;
 public String getBusinessType(){return businessType;} public void setBusinessType(String value){businessType=value;}
 private Long sourceId;
 public Long getSourceId(){return sourceId;} public void setSourceId(Long value){sourceId=value;}
}
