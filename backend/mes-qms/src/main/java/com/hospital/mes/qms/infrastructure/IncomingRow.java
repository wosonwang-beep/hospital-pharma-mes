package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
public abstract class IncomingRow {
 @TableId(type=IdType.AUTO) private Long id;
 public Long getId(){return id;} public void setId(Long value){id=value;}
 private Long orgId;
 public Long getOrgId(){return orgId;} public void setOrgId(Long value){orgId=value;}
 private Long createdBy;
 public Long getCreatedBy(){return createdBy;} public void setCreatedBy(Long value){createdBy=value;}
 private java.time.LocalDateTime createdAt;
 public java.time.LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(java.time.LocalDateTime value){createdAt=value;}
}
