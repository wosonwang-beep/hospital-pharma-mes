package com.hospital.mes.qc.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
/** Editable revision of a QC quality standard basic record (NOT a regulated version). */
@TableName("qc_specification_revision")
public class SpecificationRevisionEntity extends ScopedEntity {
 private Long specificationId;
 public Long getSpecificationId(){return specificationId;} public void setSpecificationId(Long value){specificationId=value;}
 private Integer revisionNo;
 public Integer getRevisionNo(){return revisionNo;} public void setRevisionNo(Integer value){revisionNo=value;}
 private String revisionType;
 public String getRevisionType(){return revisionType;} public void setRevisionType(String value){revisionType=value;}
 private String revisionReason;
 public String getRevisionReason(){return revisionReason;} public void setRevisionReason(String value){revisionReason=value;}
 private String beforeJson;
 public String getBeforeJson(){return beforeJson;} public void setBeforeJson(String value){beforeJson=value;}
 private String afterJson;
 public String getAfterJson(){return afterJson;} public void setAfterJson(String value){afterJson=value;}
}
