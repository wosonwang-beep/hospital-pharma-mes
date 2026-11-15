package com.hospital.mes.qc.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
/** Applicability row: one QC quality standard applies to one material or one product. */
@TableName("qc_specification_scope")
public class SpecificationScopeEntity extends ScopedEntity {
 private Long specificationId;
 public Long getSpecificationId(){return specificationId;} public void setSpecificationId(Long value){specificationId=value;}
 private String subjectType;
 public String getSubjectType(){return subjectType;} public void setSubjectType(String value){subjectType=value;}
 private Long subjectId;
 public Long getSubjectId(){return subjectId;} public void setSubjectId(Long value){subjectId=value;}
}
