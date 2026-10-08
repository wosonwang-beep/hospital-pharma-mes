package com.hospital.mes.reporting.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("mes_print_binding")
public class PrintBinding extends ScopedEntity {
 private String businessType;
 public String getBusinessType(){return businessType;} public void setBusinessType(String v){businessType=v;}
 private Long templateVersionId;
 public Long getTemplateVersionId(){return templateVersionId;} public void setTemplateVersionId(Long v){templateVersionId=v;}
 private Boolean enabled;
 public Boolean getEnabled(){return enabled;} public void setEnabled(Boolean v){enabled=v;}
 public java.util.List<String> allowedActions(){return java.util.List.of();}
}
