package com.hospital.mes.production.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.*;
@TableName("prd_main_batch") public class BatchEntity extends ScopedEntity {
 private Long finishedLotId;
 public Long getFinishedLotId(){return finishedLotId;} public void setFinishedLotId(Long value){finishedLotId=value;}
 private String batchNo;
 public String getBatchNo(){return batchNo;} public void setBatchNo(String value){batchNo=value;}
 private Long productionOrderId;
 public Long getProductionOrderId(){return productionOrderId;} public void setProductionOrderId(Long value){productionOrderId=value;}
 private Long productId;
 public Long getProductId(){return productId;} public void setProductId(Long value){productId=value;}
 private Long processSnapshotId;
 public Long getProcessSnapshotId(){return processSnapshotId;} public void setProcessSnapshotId(Long value){processSnapshotId=value;}
 private BigDecimal plannedQty;
 public BigDecimal getPlannedQty(){return plannedQty;} public void setPlannedQty(BigDecimal value){plannedQty=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private LocalDate plannedDate;
 public LocalDate getPlannedDate(){return plannedDate;} public void setPlannedDate(LocalDate value){plannedDate=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private LocalDateTime releasedAt;
 public LocalDateTime getReleasedAt(){return releasedAt;} public void setReleasedAt(LocalDateTime value){releasedAt=value;}
 private LocalDateTime startedAt;
 public LocalDateTime getStartedAt(){return startedAt;} public void setStartedAt(LocalDateTime value){startedAt=value;}
 private LocalDateTime completedAt;
 public LocalDateTime getCompletedAt(){return completedAt;} public void setCompletedAt(LocalDateTime value){completedAt=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
