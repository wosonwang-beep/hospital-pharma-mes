package com.hospital.mes.qc.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("qc_specification")
public class SpecificationEntity extends ScopedEntity {
 private Long materialId;
 public Long getMaterialId(){return materialId;} public void setMaterialId(Long value){materialId=value;}
 private String specificationCode;
 public String getSpecificationCode(){return specificationCode;} public void setSpecificationCode(String value){specificationCode=value;}
 private String specificationName;
 public String getSpecificationName(){return specificationName;} public void setSpecificationName(String value){specificationName=value;}
}
