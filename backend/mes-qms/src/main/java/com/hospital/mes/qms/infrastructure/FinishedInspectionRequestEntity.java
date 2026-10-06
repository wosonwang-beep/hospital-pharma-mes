package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("qms_finished_inspection_request") public class FinishedInspectionRequestEntity extends ScopedEntity {
 private String inspectionRequestNo;
 public String getInspectionRequestNo(){return inspectionRequestNo;} public void setInspectionRequestNo(String value){inspectionRequestNo=value;}
 private Long mainBatchId;
 public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long value){mainBatchId=value;}
 private Long materialLotId;
 public Long getMaterialLotId(){return materialLotId;} public void setMaterialLotId(Long value){materialLotId=value;}
 private Long inboundRequestId;
 public Long getInboundRequestId(){return inboundRequestId;} public void setInboundRequestId(Long value){inboundRequestId=value;}
 private Long qcSpecificationVersionId;
 public Long getQcSpecificationVersionId(){return qcSpecificationVersionId;} public void setQcSpecificationVersionId(Long value){qcSpecificationVersionId=value;}
 private String specificationSnapshotJson;
 public String getSpecificationSnapshotJson(){return specificationSnapshotJson;} public void setSpecificationSnapshotJson(String value){specificationSnapshotJson=value;}
 private String specificationDigest;
 public String getSpecificationDigest(){return specificationDigest;} public void setSpecificationDigest(String value){specificationDigest=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private Long submittedBy;
 public Long getSubmittedBy(){return submittedBy;} public void setSubmittedBy(Long value){submittedBy=value;}
 private java.time.LocalDateTime submittedAt;
 public java.time.LocalDateTime getSubmittedAt(){return submittedAt;} public void setSubmittedAt(java.time.LocalDateTime value){submittedAt=value;}
 private Long acceptedBy;
 public Long getAcceptedBy(){return acceptedBy;} public void setAcceptedBy(Long value){acceptedBy=value;}
 private java.time.LocalDateTime acceptedAt;
 public java.time.LocalDateTime getAcceptedAt(){return acceptedAt;} public void setAcceptedAt(java.time.LocalDateTime value){acceptedAt=value;}
 private String reason;
 public String getReason(){return reason;} public void setReason(String value){reason=value;}
}
