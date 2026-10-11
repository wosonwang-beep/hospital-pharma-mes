package com.hospital.mes.reporting.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("mes_print_batch_source_evidence")
public class PrintBatchSourceEvidence {
 @TableId(type=IdType.AUTO) private Long id;
 public Long getId(){return id;} public void setId(Long value){id=value;}
 private Long orgId;
 public Long getOrgId(){return orgId;} public void setOrgId(Long value){orgId=value;}
 private Long batchId;
 public Long getBatchId(){return batchId;} public void setBatchId(Long value){batchId=value;}
 private Long sourceId;
 public Long getSourceId(){return sourceId;} public void setSourceId(Long value){sourceId=value;}
 private Long sourceRevisionId;
 public Long getSourceRevisionId(){return sourceRevisionId;} public void setSourceRevisionId(Long value){sourceRevisionId=value;}
 private String configHash;
 public String getConfigHash(){return configHash;} public void setConfigHash(String value){configHash=value;}
 private String typeMetadataHash;
 public String getTypeMetadataHash(){return typeMetadataHash;} public void setTypeMetadataHash(String value){typeMetadataHash=value;}
 private String catalogVersion;
 public String getCatalogVersion(){return catalogVersion;} public void setCatalogVersion(String value){catalogVersion=value;}
 private Long createdBy;
 public Long getCreatedBy(){return createdBy;} public void setCreatedBy(Long value){createdBy=value;}
 private java.time.LocalDateTime createdAt;
 public java.time.LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(java.time.LocalDateTime value){createdAt=value;}
}
