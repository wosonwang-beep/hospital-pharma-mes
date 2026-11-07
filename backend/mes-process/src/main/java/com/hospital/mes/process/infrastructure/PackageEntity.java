package com.hospital.mes.process.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("proc_package")
public class PackageEntity extends ScopedEntity {
 private Long productId;
 public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;}
 private String packageCode;
 public String getPackageCode(){return packageCode;} public void setPackageCode(String v){packageCode=v;}
 private String status;
 private String packageName;
 public String getPackageName(){return packageName;} public void setPackageName(String v){packageName=v;}
 private String description;
 public String getDescription(){return description;} public void setDescription(String v){description=v;}
 private String processVersion;
 public String getProcessVersion(){return processVersion;} public void setProcessVersion(String v){processVersion=v;}
 private java.time.LocalDate effectiveApprovedDate;
 public java.time.LocalDate getEffectiveApprovedDate(){return effectiveApprovedDate;} public void setEffectiveApprovedDate(java.time.LocalDate v){effectiveApprovedDate=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 @Override public java.util.List<String> allowedActions(){return status.equals("ACTIVE")?java.util.List.of("DISABLE"):java.util.List.of();}
}
