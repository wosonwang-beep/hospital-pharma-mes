package com.hospital.mes.wms.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.time.*;
import java.math.BigDecimal;
@TableName("wms_material_issue")
public class MaterialIssueEntity extends ScopedEntity {
 private Long materialRequestId;
 public Long getMaterialRequestId(){return materialRequestId;} public void setMaterialRequestId(Long value){materialRequestId=value;}
 private Long mainBatchId;
 public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long value){mainBatchId=value;}
 private String issueNo;
 public String getIssueNo(){return issueNo;} public void setIssueNo(String value){issueNo=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private LocalDateTime issuedAt;
 public LocalDateTime getIssuedAt(){return issuedAt;} public void setIssuedAt(LocalDateTime value){issuedAt=value;}
}
