package com.hospital.mes.ebr.infrastructure;
@com.baomidou.mybatisplus.annotation.TableName("ebr_rule_execution")
public class RuntimeRuleEntity extends com.hospital.mes.masterdata.infrastructure.ScopedEntity {
 private Long formInstanceId; public Long getFormInstanceId(){return formInstanceId;} public void setFormInstanceId(Long v){formInstanceId=v;}
 private String ruleCode; public String getRuleCode(){return ruleCode;} public void setRuleCode(String v){ruleCode=v;}
 private String ruleVersion; public String getRuleVersion(){return ruleVersion;} public void setRuleVersion(String v){ruleVersion=v;}
 private String triggerPoint; public String getTriggerPoint(){return triggerPoint;} public void setTriggerPoint(String v){triggerPoint=v;}
 private Boolean passed; public Boolean getPassed(){return passed;} public void setPassed(Boolean v){passed=v;}
 private String severity; public String getSeverity(){return severity;} public void setSeverity(String v){severity=v;}
 private String inputSnapshotJson; public String getInputSnapshotJson(){return inputSnapshotJson;} public void setInputSnapshotJson(String v){inputSnapshotJson=v;}
 private String outputJson; public String getOutputJson(){return outputJson;} public void setOutputJson(String v){outputJson=v;}
 private String engineVersion; public String getEngineVersion(){return engineVersion;} public void setEngineVersion(String v){engineVersion=v;}
 private java.time.LocalDateTime executedAt; public java.time.LocalDateTime getExecutedAt(){return executedAt;} public void setExecutedAt(java.time.LocalDateTime v){executedAt=v;}
}
