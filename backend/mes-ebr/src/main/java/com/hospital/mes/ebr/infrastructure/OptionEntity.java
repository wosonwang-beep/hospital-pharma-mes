package com.hospital.mes.ebr.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("ebr_option_def") public class OptionEntity extends ScopedEntity {
private Long fieldDefId; public Long getFieldDefId(){return fieldDefId;} public void setFieldDefId(Long value){fieldDefId=value;}
private String optionCode; public String getOptionCode(){return optionCode;} public void setOptionCode(String value){optionCode=value;}
private String optionLabel; public String getOptionLabel(){return optionLabel;} public void setOptionLabel(String value){optionLabel=value;}
private String optionValue; public String getOptionValue(){return optionValue;} public void setOptionValue(String value){optionValue=value;}
private Integer sequenceNo; public Integer getSequenceNo(){return sequenceNo;} public void setSequenceNo(Integer value){sequenceNo=value;}
private Boolean activeFlag; public Boolean getActiveFlag(){return activeFlag;} public void setActiveFlag(Boolean value){activeFlag=value;}
}
