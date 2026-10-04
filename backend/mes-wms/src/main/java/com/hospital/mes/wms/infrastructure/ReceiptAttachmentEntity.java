package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("wms_receipt_attachment")
public class ReceiptAttachmentEntity {
 @TableId(type=IdType.AUTO)
 private Long id;
 public Long getId(){return id;} public void setId(Long v){id=v;}
 private Long orgId;
 public Long getOrgId(){return orgId;} public void setOrgId(Long v){orgId=v;}
 private Long receiptId;
 public Long getReceiptId(){return receiptId;} public void setReceiptId(Long v){receiptId=v;}
 private Long attachmentId;
 public Long getAttachmentId(){return attachmentId;} public void setAttachmentId(Long v){attachmentId=v;}
 private String purpose;
 public String getPurpose(){return purpose;} public void setPurpose(String v){purpose=v;}
 private String reason;
 public String getReason(){return reason;} public void setReason(String v){reason=v;}
 private Long linkedBy;
 public Long getLinkedBy(){return linkedBy;} public void setLinkedBy(Long v){linkedBy=v;}
 private java.time.LocalDateTime linkedAt;
 public java.time.LocalDateTime getLinkedAt(){return linkedAt;} public void setLinkedAt(java.time.LocalDateTime v){linkedAt=v;}
 private Long recordVersion;
 public Long getRecordVersion(){return recordVersion;} public void setRecordVersion(Long v){recordVersion=v;}
}
