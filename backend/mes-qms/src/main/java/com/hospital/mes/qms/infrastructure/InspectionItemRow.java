package com.hospital.mes.qms.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("qms_inspection_item")
public class InspectionItemRow extends IncomingRow {
 private Long inspectionTaskId;
 public Long getInspectionTaskId(){return inspectionTaskId;} public void setInspectionTaskId(Long value){inspectionTaskId=value;}
 private Long qcSpecificationItemId;
 public Long getQcSpecificationItemId(){return qcSpecificationItemId;} public void setQcSpecificationItemId(Long value){qcSpecificationItemId=value;}
 private String itemCode;
 public String getItemCode(){return itemCode;} public void setItemCode(String value){itemCode=value;}
 private String itemName;
 public String getItemName(){return itemName;} public void setItemName(String value){itemName=value;}
 @TableField("`required`") private Boolean required;
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
}
