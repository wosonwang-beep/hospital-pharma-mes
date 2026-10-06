package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("qms_release_decision")
public class ReleaseDecisionRow extends IncomingRow {
 private String releaseScope;
 public String getReleaseScope(){return releaseScope;} public void setReleaseScope(String value){releaseScope=value;}
 private Long mainBatchId;
 public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long value){mainBatchId=value;}
 private Long finishedLotId;
 public Long getFinishedLotId(){return finishedLotId;} public void setFinishedLotId(Long value){finishedLotId=value;}
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private Long inspectionReportId;
 public Long getInspectionReportId(){return inspectionReportId;} public void setInspectionReportId(Long value){inspectionReportId=value;}
 private Long finishedInspectionReportId;
 public Long getFinishedInspectionReportId(){return finishedInspectionReportId;} public void setFinishedInspectionReportId(Long value){finishedInspectionReportId=value;}
 private String decision;
 public String getDecision(){return decision;} public void setDecision(String value){decision=value;}
 private String releaseBasis;
 public String getReleaseBasis(){return releaseBasis;} public void setReleaseBasis(String value){releaseBasis=value;}
 private String decisionSource;
 public String getDecisionSource(){return decisionSource;} public void setDecisionSource(String value){decisionSource=value;}
 private String reason;
 public String getReason(){return reason;} public void setReason(String value){reason=value;}
 private Long decisionBy;
 public Long getDecisionBy(){return decisionBy;} public void setDecisionBy(Long value){decisionBy=value;}
 private java.time.LocalDateTime decisionAt;
 public java.time.LocalDateTime getDecisionAt(){return decisionAt;} public void setDecisionAt(java.time.LocalDateTime value){decisionAt=value;}
 private Long signatureId;
 public Long getSignatureId(){return signatureId;} public void setSignatureId(Long value){signatureId=value;}
 private String signatureEvidenceJson;
 public String getSignatureEvidenceJson(){return signatureEvidenceJson;} public void setSignatureEvidenceJson(String value){signatureEvidenceJson=value;}
 private Long supersedesDecisionId;
 public Long getSupersedesDecisionId(){return supersedesDecisionId;} public void setSupersedesDecisionId(Long value){supersedesDecisionId=value;}
 private String ruleEvidenceJson;
 public String getRuleEvidenceJson(){return ruleEvidenceJson;} public void setRuleEvidenceJson(String value){ruleEvidenceJson=value;}
 private String evidenceDigest;
 public String getEvidenceDigest(){return evidenceDigest;} public void setEvidenceDigest(String value){evidenceDigest=value;}
}
