package com.hospital.mes.ebr.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("ebr_form_def") public class FormEntity extends ScopedEntity {
private Long templateVersionId; public Long getTemplateVersionId(){return templateVersionId;} public void setTemplateVersionId(Long value){templateVersionId=value;}
private Long operationDefId; public Long getOperationDefId(){return operationDefId;} public void setOperationDefId(Long value){operationDefId=value;}
private String formCode; public String getFormCode(){return formCode;} public void setFormCode(String value){formCode=value;}
private String formName; public String getFormName(){return formName;} public void setFormName(String value){formName=value;}
private String schemaVersion; public String getSchemaVersion(){return schemaVersion;} public void setSchemaVersion(String value){schemaVersion=value;}
private String schemaJson; public String getSchemaJson(){return schemaJson;} public void setSchemaJson(String value){schemaJson=value;}
private Integer sequenceNo; public Integer getSequenceNo(){return sequenceNo;} public void setSequenceNo(Integer value){sequenceNo=value;}
private String status; public String getStatus(){return status;} public void setStatus(String value){status=value;}
}
