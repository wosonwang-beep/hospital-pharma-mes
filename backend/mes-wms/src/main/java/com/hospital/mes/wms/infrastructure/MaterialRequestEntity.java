package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("wms_material_request") public class MaterialRequestEntity extends ScopedEntity {
 private String requestNo;
 public String getRequestNo(){return requestNo;} public void setRequestNo(String value){requestNo=value;}
 private Long mainBatchId;
 public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long value){mainBatchId=value;}
 private Long processSnapshotId;
 public Long getProcessSnapshotId(){return processSnapshotId;} public void setProcessSnapshotId(Long value){processSnapshotId=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private Long submittedBy;
 public Long getSubmittedBy(){return submittedBy;} public void setSubmittedBy(Long value){submittedBy=value;}
 private java.time.LocalDateTime submittedAt;
 public java.time.LocalDateTime getSubmittedAt(){return submittedAt;} public void setSubmittedAt(java.time.LocalDateTime value){submittedAt=value;}
 private Long cancelledBy;
 public Long getCancelledBy(){return cancelledBy;} public void setCancelledBy(Long value){cancelledBy=value;}
 private java.time.LocalDateTime cancelledAt;
 public java.time.LocalDateTime getCancelledAt(){return cancelledAt;} public void setCancelledAt(java.time.LocalDateTime value){cancelledAt=value;}
 private String cancellationReason;
 public String getCancellationReason(){return cancellationReason;} public void setCancellationReason(String value){cancellationReason=value;}
}
