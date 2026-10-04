package com.hospital.mes.ebr.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("ebr_template_version") public class TemplateEntity extends ScopedEntity {
private Long packageVersionId; public Long getPackageVersionId(){return packageVersionId;} public void setPackageVersionId(Long value){packageVersionId=value;}
private String templateCode; public String getTemplateCode(){return templateCode;} public void setTemplateCode(String value){templateCode=value;}
@TableField("version")
private Integer businessVersion; public Integer getBusinessVersion(){return businessVersion;} public void setBusinessVersion(Integer value){businessVersion=value;}
private String status; public String getStatus(){return status;} public void setStatus(String value){status=value;}
private String contentHash; public String getContentHash(){return contentHash;} public void setContentHash(String value){contentHash=value;}
private java.time.LocalDateTime effectiveFrom; public java.time.LocalDateTime getEffectiveFrom(){return effectiveFrom;} public void setEffectiveFrom(java.time.LocalDateTime value){effectiveFrom=value;}
private Long approvedBy; public Long getApprovedBy(){return approvedBy;} public void setApprovedBy(Long value){approvedBy=value;}
private java.time.LocalDateTime approvedAt; public java.time.LocalDateTime getApprovedAt(){return approvedAt;} public void setApprovedAt(java.time.LocalDateTime value){approvedAt=value;}
@Override public java.util.List<String> allowedActions(){return switch(status){case "DRAFT"->java.util.List.of("EDIT","LINT","SIMULATE","SUBMIT");case "SUBMITTED"->java.util.List.of("APPROVE","NEW_VERSION");case "APPROVED"->java.util.List.of("PUBLISH","NEW_VERSION");default->java.util.List.of("NEW_VERSION");};}
}
