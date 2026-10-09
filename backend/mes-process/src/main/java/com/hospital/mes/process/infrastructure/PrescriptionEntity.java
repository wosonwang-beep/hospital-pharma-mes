package com.hospital.mes.process.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@TableName("prd_prescription")
public class PrescriptionEntity extends ScopedEntity {
 private String prescriptionCode; private String prescriptionName; private Long productId; private Long processPackageId;
 private BigDecimal batchBasisQty; private Long unitId; private String status; private LocalDateTime effectiveFrom; private LocalDateTime effectiveTo;
 public String getPrescriptionCode(){return prescriptionCode;} public void setPrescriptionCode(String v){prescriptionCode=v;}
 public String getPrescriptionName(){return prescriptionName;} public void setPrescriptionName(String v){prescriptionName=v;}
 public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;}
 public Long getProcessPackageId(){return processPackageId;} public void setProcessPackageId(Long v){processPackageId=v;}
 public BigDecimal getBatchBasisQty(){return batchBasisQty;} public void setBatchBasisQty(BigDecimal v){batchBasisQty=v;}
 public Long getUnitId(){return unitId;} public void setUnitId(Long v){unitId=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public LocalDateTime getEffectiveFrom(){return effectiveFrom;} public void setEffectiveFrom(LocalDateTime v){effectiveFrom=v;}
 public LocalDateTime getEffectiveTo(){return effectiveTo;} public void setEffectiveTo(LocalDateTime v){effectiveTo=v;}
 @Override public java.util.List<String> allowedActions(){
  if("DRAFT".equals(status))return java.util.List.of("EDIT","ACTIVATE");
  if("ACTIVE".equals(status))return java.util.List.of("DEACTIVATE");
  return java.util.List.of();
 }
}