package com.hospital.mes.qc.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("qc_specification_version")
public class SpecificationVersionEntity extends ScopedEntity {
 private Long specificationId;
 public Long getSpecificationId(){return specificationId;} public void setSpecificationId(Long value){specificationId=value;}
 private Integer versionNoBusiness;
 public Integer getVersionNoBusiness(){return versionNoBusiness;} public void setVersionNoBusiness(Integer value){versionNoBusiness=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private String contentHash;
 public String getContentHash(){return contentHash;} public void setContentHash(String value){contentHash=value;}
 private Long approvedBy;
 public Long getApprovedBy(){return approvedBy;} public void setApprovedBy(Long value){approvedBy=value;}
 private java.time.LocalDateTime approvedAt;
 public java.time.LocalDateTime getApprovedAt(){return approvedAt;} public void setApprovedAt(java.time.LocalDateTime value){approvedAt=value;}
 private Long approvalSignatureId;
 public Long getApprovalSignatureId(){return approvalSignatureId;} public void setApprovalSignatureId(Long value){approvalSignatureId=value;}
 private String approvalReason;
 public String getApprovalReason(){return approvalReason;} public void setApprovalReason(String value){approvalReason=value;}
 private Long retiredBy;
 public Long getRetiredBy(){return retiredBy;} public void setRetiredBy(Long value){retiredBy=value;}
 private java.time.LocalDateTime retiredAt;
 public java.time.LocalDateTime getRetiredAt(){return retiredAt;} public void setRetiredAt(java.time.LocalDateTime value){retiredAt=value;}
 private Long retirementSignatureId;
 public Long getRetirementSignatureId(){return retirementSignatureId;} public void setRetirementSignatureId(Long value){retirementSignatureId=value;}
 private String retirementReason;
 public String getRetirementReason(){return retirementReason;} public void setRetirementReason(String value){retirementReason=value;}
}
