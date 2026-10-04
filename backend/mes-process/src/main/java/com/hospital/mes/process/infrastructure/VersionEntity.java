package com.hospital.mes.process.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("proc_package_version")
public class VersionEntity extends ScopedEntity {
 private Long packageId;
 public Long getPackageId(){return packageId;} public void setPackageId(Long v){packageId=v;}
 @TableField("version")
 private Integer businessVersion;
 public Integer getBusinessVersion(){return businessVersion;} public void setBusinessVersion(Integer v){businessVersion=v;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 private String contentHash;
 public String getContentHash(){return contentHash;} public void setContentHash(String v){contentHash=v;}
 private java.time.LocalDateTime effectiveFrom;
 public java.time.LocalDateTime getEffectiveFrom(){return effectiveFrom;} public void setEffectiveFrom(java.time.LocalDateTime v){effectiveFrom=v;}
 @Override public java.util.List<String> allowedActions(){return switch(status){case "DRAFT"->java.util.List.of("EDIT","LINT","SUBMIT");case "SUBMITTED"->java.util.List.of("APPROVE");case "APPROVED"->java.util.List.of("PUBLISH");default->java.util.List.of();};}
}
