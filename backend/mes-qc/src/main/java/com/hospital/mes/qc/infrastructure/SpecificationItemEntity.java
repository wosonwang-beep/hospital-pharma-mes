package com.hospital.mes.qc.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("qc_specification_item")
public class SpecificationItemEntity extends ScopedEntity {
 private Long specificationVersionId;
 public Long getSpecificationVersionId(){return specificationVersionId;} public void setSpecificationVersionId(Long value){specificationVersionId=value;}
 private String itemCode;
 public String getItemCode(){return itemCode;} public void setItemCode(String value){itemCode=value;}
 private String itemName;
 public String getItemName(){return itemName;} public void setItemName(String value){itemName=value;}
 private Boolean required;
 public Boolean getRequired(){return required;} public void setRequired(Boolean value){required=value;}
 private String resultType;
 public String getResultType(){return resultType;} public void setResultType(String value){resultType=value;}
 private java.math.BigDecimal lowerLimit;
 public java.math.BigDecimal getLowerLimit(){return lowerLimit;} public void setLowerLimit(java.math.BigDecimal value){lowerLimit=value;}
 private java.math.BigDecimal upperLimit;
 public java.math.BigDecimal getUpperLimit(){return upperLimit;} public void setUpperLimit(java.math.BigDecimal value){upperLimit=value;}
 private Long unitId;
 public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
 private String textAcceptanceCriteria;
 public String getTextAcceptanceCriteria(){return textAcceptanceCriteria;} public void setTextAcceptanceCriteria(String value){textAcceptanceCriteria=value;}
 private String methodCode;
 public String getMethodCode(){return methodCode;} public void setMethodCode(String value){methodCode=value;}
 private String methodVersion;
 public String getMethodVersion(){return methodVersion;} public void setMethodVersion(String value){methodVersion=value;}
 private Boolean active;
 public Boolean getActive(){return active;} public void setActive(Boolean value){active=value;}
}
