package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("qms_inspection_report_item")
public class InspectionReportItemRow extends IncomingRow {
 private Long inspectionReportId;
 public Long getInspectionReportId(){return inspectionReportId;} public void setInspectionReportId(Long value){inspectionReportId=value;}
 private Long qcSpecificationItemId;
 public Long getQcSpecificationItemId(){return qcSpecificationItemId;} public void setQcSpecificationItemId(Long value){qcSpecificationItemId=value;}
 private Long inspectionItemId;
 public Long getInspectionItemId(){return inspectionItemId;} public void setInspectionItemId(Long value){inspectionItemId=value;}
 private Long resultRevisionId;
 public Long getResultRevisionId(){return resultRevisionId;} public void setResultRevisionId(Long value){resultRevisionId=value;}
 private Long originalResultRevisionId;
 public Long getOriginalResultRevisionId(){return originalResultRevisionId;} public void setOriginalResultRevisionId(Long value){originalResultRevisionId=value;}
 private Long investigationId;
 public Long getInvestigationId(){return investigationId;} public void setInvestigationId(Long value){investigationId=value;}
}
