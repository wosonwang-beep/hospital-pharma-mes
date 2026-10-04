package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("qms_test_execution")
public class TestExecutionRow extends IncomingRow {
 private Long inspectionItemId;
 public Long getInspectionItemId(){return inspectionItemId;} public void setInspectionItemId(Long value){inspectionItemId=value;}
 private Integer attemptNo;
 public Integer getAttemptNo(){return attemptNo;} public void setAttemptNo(Integer value){attemptNo=value;}
 private Long originalExecutionId;
 public Long getOriginalExecutionId(){return originalExecutionId;} public void setOriginalExecutionId(Long value){originalExecutionId=value;}
 private Long approvedInvestigationId;
 public Long getApprovedInvestigationId(){return approvedInvestigationId;} public void setApprovedInvestigationId(Long value){approvedInvestigationId=value;}
 private Long instrumentId;
 public Long getInstrumentId(){return instrumentId;} public void setInstrumentId(Long value){instrumentId=value;}
 private String rawDataJson;
 public String getRawDataJson(){return rawDataJson;} public void setRawDataJson(String value){rawDataJson=value;}
 private String observation;
 public String getObservation(){return observation;} public void setObservation(String value){observation=value;}
 private String calculationInputJson;
 public String getCalculationInputJson(){return calculationInputJson;} public void setCalculationInputJson(String value){calculationInputJson=value;}
 private java.time.LocalDateTime startedAt;
 public java.time.LocalDateTime getStartedAt(){return startedAt;} public void setStartedAt(java.time.LocalDateTime value){startedAt=value;}
 private java.time.LocalDateTime completedAt;
 public java.time.LocalDateTime getCompletedAt(){return completedAt;} public void setCompletedAt(java.time.LocalDateTime value){completedAt=value;}
 private Long performedBy;
 public Long getPerformedBy(){return performedBy;} public void setPerformedBy(Long value){performedBy=value;}
}
