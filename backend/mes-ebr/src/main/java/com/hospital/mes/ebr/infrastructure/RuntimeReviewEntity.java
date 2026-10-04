package com.hospital.mes.ebr.infrastructure;
@com.baomidou.mybatisplus.annotation.TableName("ebr_review_record")
public class RuntimeReviewEntity extends com.hospital.mes.masterdata.infrastructure.ScopedEntity {
 private String objectType; public String getObjectType(){return objectType;} public void setObjectType(String v){objectType=v;}
 private Long objectId; public Long getObjectId(){return objectId;} public void setObjectId(Long v){objectId=v;}
 private String reviewType; public String getReviewType(){return reviewType;} public void setReviewType(String v){reviewType=v;}
 private Long reviewerId; public Long getReviewerId(){return reviewerId;} public void setReviewerId(Long v){reviewerId=v;}
 private String roleSnapshot; public String getRoleSnapshot(){return roleSnapshot;} public void setRoleSnapshot(String v){roleSnapshot=v;}
 private String decision; public String getDecision(){return decision;} public void setDecision(String v){decision=v;}
 private String comment; public String getComment(){return comment;} public void setComment(String v){comment=v;}
 private java.time.LocalDateTime reviewedAt; public java.time.LocalDateTime getReviewedAt(){return reviewedAt;} public void setReviewedAt(java.time.LocalDateTime v){reviewedAt=v;}
 private Long signatureId; public Long getSignatureId(){return signatureId;} public void setSignatureId(Long v){signatureId=v;}
 private java.time.LocalDateTime invalidatedAt; public java.time.LocalDateTime getInvalidatedAt(){return invalidatedAt;} public void setInvalidatedAt(java.time.LocalDateTime v){invalidatedAt=v;}
 private String invalidationReason; public String getInvalidationReason(){return invalidationReason;} public void setInvalidationReason(String v){invalidationReason=v;}
 private Integer formRevision; public Integer getFormRevision(){return formRevision;} public void setFormRevision(Integer v){formRevision=v;}
 private Long reviewRuleId; public Long getReviewRuleId(){return reviewRuleId;} public void setReviewRuleId(Long v){reviewRuleId=v;}
 private String reason; public String getReason(){return reason;} public void setReason(String v){reason=v;}
 private String signatureEvidenceJson; public String getSignatureEvidenceJson(){return signatureEvidenceJson;} public void setSignatureEvidenceJson(String v){signatureEvidenceJson=v;}
}
