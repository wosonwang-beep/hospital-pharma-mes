package com.hospital.mes.production.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.*;
@TableName("prd_sub_batch") public class SubBatchEntity extends ScopedEntity {
 private Long mainBatchId;
 public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long value){mainBatchId=value;}
 private String subBatchNo;
 public String getSubBatchNo(){return subBatchNo;} public void setSubBatchNo(String value){subBatchNo=value;}
 private Integer sequenceNo;
 public Integer getSequenceNo(){return sequenceNo;} public void setSequenceNo(Integer value){sequenceNo=value;}
 private BigDecimal plannedQty;
 public BigDecimal getPlannedQty(){return plannedQty;} public void setPlannedQty(BigDecimal value){plannedQty=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
