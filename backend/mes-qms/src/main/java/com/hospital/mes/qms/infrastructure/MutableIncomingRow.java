package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
public abstract class MutableIncomingRow extends IncomingRow {
 private Long updatedBy;
 public Long getUpdatedBy(){return updatedBy;} public void setUpdatedBy(Long value){updatedBy=value;}
 private java.time.LocalDateTime updatedAt;
 public java.time.LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(java.time.LocalDateTime value){updatedAt=value;}
 @Version private Long versionNo;
 public Long getVersionNo(){return versionNo;} public void setVersionNo(Long value){versionNo=value;}
}
