package com.hospital.mes.ebr.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("ebr_field_def") public class FieldEntity extends ScopedEntity {
private Long formDefId; public Long getFormDefId(){return formDefId;} public void setFormDefId(Long value){formDefId=value;}
private Long groupDefId; public Long getGroupDefId(){return groupDefId;} public void setGroupDefId(Long value){groupDefId=value;}
private String fieldCode; public String getFieldCode(){return fieldCode;} public void setFieldCode(String value){fieldCode=value;}
private String label; public String getLabel(){return label;} public void setLabel(String value){label=value;}
private String fieldType; public String getFieldType(){return fieldType;} public void setFieldType(String value){fieldType=value;}
private String sourceType; public String getSourceType(){return sourceType;} public void setSourceType(String value){sourceType=value;}
private String dataType; public String getDataType(){return dataType;} public void setDataType(String value){dataType=value;}
private Long unitId; public Long getUnitId(){return unitId;} public void setUnitId(Long value){unitId=value;}
private Integer precisionScale; public Integer getPrecisionScale(){return precisionScale;} public void setPrecisionScale(Integer value){precisionScale=value;}
private Boolean requiredFlag; public Boolean getRequiredFlag(){return requiredFlag;} public void setRequiredFlag(Boolean value){requiredFlag=value;}
private Boolean readonlyFlag; public Boolean getReadonlyFlag(){return readonlyFlag;} public void setReadonlyFlag(Boolean value){readonlyFlag=value;}
private String defaultExpr; public String getDefaultExpr(){return defaultExpr;} public void setDefaultExpr(String value){defaultExpr=value;}
private String placeholder; public String getPlaceholder(){return placeholder;} public void setPlaceholder(String value){placeholder=value;}
private String helpText; public String getHelpText(){return helpText;} public void setHelpText(String value){helpText=value;}
private Integer sequenceNo; public Integer getSequenceNo(){return sequenceNo;} public void setSequenceNo(Integer value){sequenceNo=value;}
private String validationJson; public String getValidationJson(){return validationJson;} public void setValidationJson(String value){validationJson=value;}
}
