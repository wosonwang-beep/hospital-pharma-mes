package com.hospital.mes.reporting.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("mes_print_data_source")
public class PrintDataSource extends ScopedEntity {
 private String sourceCode;
 public String getSourceCode(){return sourceCode;} public void setSourceCode(String value){sourceCode=value;}
 private String sourceName;
 public String getSourceName(){return sourceName;} public void setSourceName(String value){sourceName=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private Long draftRevisionId;
 public Long getDraftRevisionId(){return draftRevisionId;} public void setDraftRevisionId(Long value){draftRevisionId=value;}
 private Long publishedRevisionId;
 public Long getPublishedRevisionId(){return publishedRevisionId;} public void setPublishedRevisionId(Long value){publishedRevisionId=value;}
 private java.time.LocalDateTime deletedAt;
 public java.time.LocalDateTime getDeletedAt(){return deletedAt;} public void setDeletedAt(java.time.LocalDateTime value){deletedAt=value;}
 private Long deletedBy;
 public Long getDeletedBy(){return deletedBy;} public void setDeletedBy(Long value){deletedBy=value;}
}
