package com.hospital.mes.production.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.*;
@TableName("prd_main_batch") public class BatchEntity extends ScopedEntity {
 private Integer productionSchemaVersion=1;
 @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL) public Integer getProductionSchemaVersion(){return Integer.valueOf(2).equals(productionSchemaVersion)?2:null;} public void setProductionSchemaVersion(Integer v){productionSchemaVersion=v;}
 private LocalDate finishedManufactureDate;
 @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL) public LocalDate getFinishedManufactureDate(){return finishedManufactureDate;} public void setFinishedManufactureDate(LocalDate v){finishedManufactureDate=v;}
 private LocalDate finishedExpiryDate;
 @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL) public LocalDate getFinishedExpiryDate(){return finishedExpiryDate;} public void setFinishedExpiryDate(LocalDate v){finishedExpiryDate=v;}
 private Long finishedBaseUnitId;
 @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL) public Long getFinishedBaseUnitId(){return finishedBaseUnitId;} public void setFinishedBaseUnitId(Long v){finishedBaseUnitId=v;}
 private String finishedProductSnapshotJson;
 @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL) public String getFinishedProductSnapshotJson(){return finishedProductSnapshotJson;} public void setFinishedProductSnapshotJson(String v){finishedProductSnapshotJson=v;}
 private String finishedProductSnapshotDigest;
 @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL) public String getFinishedProductSnapshotDigest(){return finishedProductSnapshotDigest;} public void setFinishedProductSnapshotDigest(String v){finishedProductSnapshotDigest=v;}
 private String finishedQualityStatus;
 @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL) public String getFinishedQualityStatus(){return finishedQualityStatus;} public void setFinishedQualityStatus(String v){finishedQualityStatus=v;}
 private String finishedInventoryStatus;
 @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL) public String getFinishedInventoryStatus(){return finishedInventoryStatus;} public void setFinishedInventoryStatus(String v){finishedInventoryStatus=v;}


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
