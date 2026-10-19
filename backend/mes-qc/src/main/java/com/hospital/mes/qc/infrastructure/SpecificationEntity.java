package com.hospital.mes.qc.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("qc_specification")
public class SpecificationEntity extends ScopedEntity {
 private String subjectType = "MATERIAL";
 public String getSubjectType(){return subjectType;} public void setSubjectType(String v){subjectType=v;}
 private Long productId;
 public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;}

 private Long materialId;
 public Long getMaterialId(){return materialId;} public void setMaterialId(Long value){materialId=value;}
 private String specificationCode;
 public String getSpecificationCode(){return specificationCode;} public void setSpecificationCode(String value){specificationCode=value;}
 private String specificationName;
 public String getSpecificationName(){return specificationName;} public void setSpecificationName(String value){specificationName=value;}
}
