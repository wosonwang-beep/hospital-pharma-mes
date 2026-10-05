package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("qms_deviation")
public class DeviationRow extends MutableIncomingRow {
 private Long productionTestInstanceId;
 @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
 public Long getProductionTestInstanceId(){return productionTestInstanceId;} public void setProductionTestInstanceId(Long value){productionTestInstanceId=value;}
 private String deviationNo;
 public String getDeviationNo(){return deviationNo;} public void setDeviationNo(String value){deviationNo=value;}
 private Long mainBatchId;
 public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long value){mainBatchId=value;}
 private Long operationExecutionId;
 public Long getOperationExecutionId(){return operationExecutionId;} public void setOperationExecutionId(Long value){operationExecutionId=value;}
 private String severity;
 public String getSeverity(){return severity;} public void setSeverity(String value){severity=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private String description;
 public String getDescription(){return description;} public void setDescription(String value){description=value;}
 private String disposition;
 public String getDisposition(){return disposition;} public void setDisposition(String value){disposition=value;}
 private String investigationScope;
 public String getInvestigationScope(){return investigationScope;} public void setInvestigationScope(String value){investigationScope=value;}
 private String investigationKind;
 public String getInvestigationKind(){return investigationKind;} public void setInvestigationKind(String value){investigationKind=value;}
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private Long testExecutionId;
 public Long getTestExecutionId(){return testExecutionId;} public void setTestExecutionId(Long value){testExecutionId=value;}
 private Long originalResultRevisionId;
 public Long getOriginalResultRevisionId(){return originalResultRevisionId;} public void setOriginalResultRevisionId(Long value){originalResultRevisionId=value;}
 private String investigationSummary;
 public String getInvestigationSummary(){return investigationSummary;} public void setInvestigationSummary(String value){investigationSummary=value;}
 private String decisionCode;
 public String getDecisionCode(){return decisionCode;} public void setDecisionCode(String value){decisionCode=value;}
 private Long decisionSignatureId;
 public Long getDecisionSignatureId(){return decisionSignatureId;} public void setDecisionSignatureId(Long value){decisionSignatureId=value;}
 private String decisionSignatureEvidenceJson;
 public String getDecisionSignatureEvidenceJson(){return decisionSignatureEvidenceJson;} public void setDecisionSignatureEvidenceJson(String value){decisionSignatureEvidenceJson=value;}
 private Long decidedBy;
 public Long getDecidedBy(){return decidedBy;} public void setDecidedBy(Long value){decidedBy=value;}
 private java.time.LocalDateTime decidedAt;
 public java.time.LocalDateTime getDecidedAt(){return decidedAt;} public void setDecidedAt(java.time.LocalDateTime value){decidedAt=value;}
 private Integer authorizedRetestCount;
 public Integer getAuthorizedRetestCount(){return authorizedRetestCount;} public void setAuthorizedRetestCount(Integer value){authorizedRetestCount=value;}
 private Long selectedResultRevisionId;
 public Long getSelectedResultRevisionId(){return selectedResultRevisionId;} public void setSelectedResultRevisionId(Long value){selectedResultRevisionId=value;}
 private String originalResultDisposition;
 public String getOriginalResultDisposition(){return originalResultDisposition;} public void setOriginalResultDisposition(String value){originalResultDisposition=value;}
 private String closeReason;
 public String getCloseReason(){return closeReason;} public void setCloseReason(String value){closeReason=value;}
 private Long closeSignatureId;
 public Long getCloseSignatureId(){return closeSignatureId;} public void setCloseSignatureId(Long value){closeSignatureId=value;}
 private String closeSignatureEvidenceJson;
 public String getCloseSignatureEvidenceJson(){return closeSignatureEvidenceJson;} public void setCloseSignatureEvidenceJson(String value){closeSignatureEvidenceJson=value;}
 private Long closedBy;
 public Long getClosedBy(){return closedBy;} public void setClosedBy(Long value){closedBy=value;}
 private java.time.LocalDateTime closedAt;
 public java.time.LocalDateTime getClosedAt(){return closedAt;} public void setClosedAt(java.time.LocalDateTime value){closedAt=value;}
}
