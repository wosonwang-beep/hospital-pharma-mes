package com.hospital.mes.qc.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
/** QC quality standard root row. Basic library record: no approval, no signature, no version. */
@TableName("qc_specification")
public class SpecificationEntity extends ScopedEntity {
 /** Retained primary applicable material so existing references keep resolving; nothing reads it as identity. */
 private Long materialId;
 public Long getMaterialId(){return materialId;} public void setMaterialId(Long value){materialId=value;}
 private String specificationCode;
 public String getSpecificationCode(){return specificationCode;} public void setSpecificationCode(String value){specificationCode=value;}
 private String specificationName;
 public String getSpecificationName(){return specificationName;} public void setSpecificationName(String value){specificationName=value;}
 private String basisType;
 public String getBasisType(){return basisType;} public void setBasisType(String value){basisType=value;}
 private String basisReference;
 public String getBasisReference(){return basisReference;} public void setBasisReference(String value){basisReference=value;}
 private java.time.LocalDate effectiveDate;
 public java.time.LocalDate getEffectiveDate(){return effectiveDate;} public void setEffectiveDate(java.time.LocalDate value){effectiveDate=value;}
 private String remark;
 public String getRemark(){return remark;} public void setRemark(String value){remark=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
}
