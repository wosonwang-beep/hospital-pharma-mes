package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.LocalDateTime;
import java.math.BigDecimal;
@TableName(value="qms_ipc_result_revision", excludeProperty={"updatedBy","updatedAt","versionNo"}) public class IpcResultEntity extends ScopedEntity {
 private Long ipcInstanceId;
 public Long getIpcInstanceId(){return ipcInstanceId;} public void setIpcInstanceId(Long value){ipcInstanceId=value;}
 private Integer revisionNo;
 public Integer getRevisionNo(){return revisionNo;} public void setRevisionNo(Integer value){revisionNo=value;}
 private Long previousRevisionId;
 public Long getPreviousRevisionId(){return previousRevisionId;} public void setPreviousRevisionId(Long value){previousRevisionId=value;}
 private BigDecimal resultNumeric;
 public BigDecimal getResultNumeric(){return resultNumeric;} public void setResultNumeric(BigDecimal value){resultNumeric=value;}
 private String resultText;
 public String getResultText(){return resultText;} public void setResultText(String value){resultText=value;}
 private String resultConclusion;
 public String getResultConclusion(){return resultConclusion;} public void setResultConclusion(String value){resultConclusion=value;}
 private String definitionSnapshotJson;
 public String getDefinitionSnapshotJson(){return definitionSnapshotJson;} public void setDefinitionSnapshotJson(String value){definitionSnapshotJson=value;}
 private String reasonForChange;
 public String getReasonForChange(){return reasonForChange;} public void setReasonForChange(String value){reasonForChange=value;}
 private Long recordedBy;
 public Long getRecordedBy(){return recordedBy;} public void setRecordedBy(Long value){recordedBy=value;}
 private LocalDateTime recordedAt;
 public LocalDateTime getRecordedAt(){return recordedAt;} public void setRecordedAt(LocalDateTime value){recordedAt=value;}
 private Long signatureId;
 public Long getSignatureId(){return signatureId;} public void setSignatureId(Long value){signatureId=value;}
 private String signatureEvidenceJson;
 public String getSignatureEvidenceJson(){return signatureEvidenceJson;} public void setSignatureEvidenceJson(String value){signatureEvidenceJson=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
