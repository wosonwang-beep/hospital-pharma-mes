package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("qms_test_result_revision")
public class TestResultRevisionRow extends IncomingRow {
 private Long sampleId;
 public Long getSampleId(){return sampleId;} public void setSampleId(Long value){sampleId=value;}
 private Long inspectionItemId;
 public Long getInspectionItemId(){return inspectionItemId;} public void setInspectionItemId(Long value){inspectionItemId=value;}
 private Long testExecutionId;
 public Long getTestExecutionId(){return testExecutionId;} public void setTestExecutionId(Long value){testExecutionId=value;}
 private String testCode;
 public String getTestCode(){return testCode;} public void setTestCode(String value){testCode=value;}
 private Integer revisionNo;
 public Integer getRevisionNo(){return revisionNo;} public void setRevisionNo(Integer value){revisionNo=value;}
 private Long previousRevisionId;
 public Long getPreviousRevisionId(){return previousRevisionId;} public void setPreviousRevisionId(Long value){previousRevisionId=value;}
 private java.math.BigDecimal resultNumeric;
 public java.math.BigDecimal getResultNumeric(){return resultNumeric;} public void setResultNumeric(java.math.BigDecimal value){resultNumeric=value;}
 private String resultText;
 public String getResultText(){return resultText;} public void setResultText(String value){resultText=value;}
 private Long resultUnitId;
 public Long getResultUnitId(){return resultUnitId;} public void setResultUnitId(Long value){resultUnitId=value;}
 private String resultConclusion;
 public String getResultConclusion(){return resultConclusion;} public void setResultConclusion(String value){resultConclusion=value;}
 private String reasonForChange;
 public String getReasonForChange(){return reasonForChange;} public void setReasonForChange(String value){reasonForChange=value;}
 private Long recordedBy;
 public Long getRecordedBy(){return recordedBy;} public void setRecordedBy(Long value){recordedBy=value;}
 private java.time.LocalDateTime recordedAt;
 public java.time.LocalDateTime getRecordedAt(){return recordedAt;} public void setRecordedAt(java.time.LocalDateTime value){recordedAt=value;}
 private Long signatureId;
 public Long getSignatureId(){return signatureId;} public void setSignatureId(Long value){signatureId=value;}
 private String signatureEvidenceJson;
 public String getSignatureEvidenceJson(){return signatureEvidenceJson;} public void setSignatureEvidenceJson(String value){signatureEvidenceJson=value;}
}
