package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("wms_issue_return_item") public class IssueReturnEntity {
 @TableId(type=IdType.AUTO)
 private Long id;public Long getId(){return id;}public void setId(Long value){id=value;}
 private Long orgId;public Long getOrgId(){return orgId;}public void setOrgId(Long value){orgId=value;}
 private Long issueId;public Long getIssueId(){return issueId;}public void setIssueId(Long value){issueId=value;}
 private Long issueItemId;public Long getIssueItemId(){return issueItemId;}public void setIssueItemId(Long value){issueItemId=value;}
 @TableField("returned_qty")
 private java.math.BigDecimal quantity;public java.math.BigDecimal getQuantity(){return quantity;}public void setQuantity(java.math.BigDecimal value){quantity=value;}
 private Long unitId;public Long getUnitId(){return unitId;}public void setUnitId(Long value){unitId=value;}
 private String reason;public String getReason(){return reason;}public void setReason(String value){reason=value;}
 private Long returnedBy;public Long getReturnedBy(){return returnedBy;}public void setReturnedBy(Long value){returnedBy=value;}
 private java.time.LocalDateTime returnedAt;public java.time.LocalDateTime getReturnedAt(){return returnedAt;}public void setReturnedAt(java.time.LocalDateTime value){returnedAt=value;}
 private Long recordVersion;public Long getRecordVersion(){return recordVersion;}public void setRecordVersion(Long value){recordVersion=value;}
}
