package com.hospital.mes.production.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.*;
@TableName("prd_production_order") public class OrderEntity extends ScopedEntity {
 private String orderNo;
 public String getOrderNo(){return orderNo;} public void setOrderNo(String value){orderNo=value;}
 private Long productId;
 public Long getProductId(){return productId;} public void setProductId(Long value){productId=value;}
 private BigDecimal plannedQty;
 public BigDecimal getPlannedQty(){return plannedQty;} public void setPlannedQty(BigDecimal value){plannedQty=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private LocalDate plannedDate;
 public LocalDate getPlannedDate(){return plannedDate;} public void setPlannedDate(LocalDate value){plannedDate=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 @Override public java.util.List<String> allowedActions(){return java.util.List.of();}
}
