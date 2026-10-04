package com.hospital.mes.ebr.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("ebr_signature_rule") public class SignatureRuleEntity extends ScopedEntity {
private Long templateVersionId; public Long getTemplateVersionId(){return templateVersionId;} public void setTemplateVersionId(Long value){templateVersionId=value;}
private String objectScope; public String getObjectScope(){return objectScope;} public void setObjectScope(String value){objectScope=value;}
private String objectCode; public String getObjectCode(){return objectCode;} public void setObjectCode(String value){objectCode=value;}
private String meaning; public String getMeaning(){return meaning;} public void setMeaning(String value){meaning=value;}
private String requiredRole; public String getRequiredRole(){return requiredRole;} public void setRequiredRole(String value){requiredRole=value;}
private Boolean reauthRequired; public Boolean getReauthRequired(){return reauthRequired;} public void setReauthRequired(Boolean value){reauthRequired=value;}
private Integer sequenceNo; public Integer getSequenceNo(){return sequenceNo;} public void setSequenceNo(Integer value){sequenceNo=value;}
private Boolean invalidateOnChange; public Boolean getInvalidateOnChange(){return invalidateOnChange;} public void setInvalidateOnChange(Boolean value){invalidateOnChange=value;}
}
