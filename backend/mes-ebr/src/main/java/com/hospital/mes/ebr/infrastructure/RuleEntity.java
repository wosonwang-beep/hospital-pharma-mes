package com.hospital.mes.ebr.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("ebr_rule_def") public class RuleEntity extends ScopedEntity {
private Long templateVersionId; public Long getTemplateVersionId(){return templateVersionId;} public void setTemplateVersionId(Long value){templateVersionId=value;}
private Long formDefId; public Long getFormDefId(){return formDefId;} public void setFormDefId(Long value){formDefId=value;}
private Long fieldDefId; public Long getFieldDefId(){return fieldDefId;} public void setFieldDefId(Long value){fieldDefId=value;}
private String ruleCode; public String getRuleCode(){return ruleCode;} public void setRuleCode(String value){ruleCode=value;}
private String ruleType; public String getRuleType(){return ruleType;} public void setRuleType(String value){ruleType=value;}
private String triggerPoint; public String getTriggerPoint(){return triggerPoint;} public void setTriggerPoint(String value){triggerPoint=value;}
private String expression; public String getExpression(){return expression;} public void setExpression(String value){expression=value;}
private String severity; public String getSeverity(){return severity;} public void setSeverity(String value){severity=value;}
private String errorCode; public String getErrorCode(){return errorCode;} public void setErrorCode(String value){errorCode=value;}
private String messageTemplate; public String getMessageTemplate(){return messageTemplate;} public void setMessageTemplate(String value){messageTemplate=value;}
private Boolean deviationTrigger; public Boolean getDeviationTrigger(){return deviationTrigger;} public void setDeviationTrigger(Boolean value){deviationTrigger=value;}
private Boolean activeFlag; public Boolean getActiveFlag(){return activeFlag;} public void setActiveFlag(Boolean value){activeFlag=value;}
}
