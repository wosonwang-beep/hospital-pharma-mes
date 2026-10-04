package com.hospital.mes.ebr.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("ebr_review_rule") public class ReviewRuleEntity extends ScopedEntity {
private Long templateVersionId; public Long getTemplateVersionId(){return templateVersionId;} public void setTemplateVersionId(Long value){templateVersionId=value;}
private String objectScope; public String getObjectScope(){return objectScope;} public void setObjectScope(String value){objectScope=value;}
private String objectCode; public String getObjectCode(){return objectCode;} public void setObjectCode(String value){objectCode=value;}
private String reviewType; public String getReviewType(){return reviewType;} public void setReviewType(String value){reviewType=value;}
private String requiredRole; public String getRequiredRole(){return requiredRole;} public void setRequiredRole(String value){requiredRole=value;}
private Boolean independentUserRequired; public Boolean getIndependentUserRequired(){return independentUserRequired;} public void setIndependentUserRequired(Boolean value){independentUserRequired=value;}
private Integer sequenceNo; public Integer getSequenceNo(){return sequenceNo;} public void setSequenceNo(Integer value){sequenceNo=value;}
}
