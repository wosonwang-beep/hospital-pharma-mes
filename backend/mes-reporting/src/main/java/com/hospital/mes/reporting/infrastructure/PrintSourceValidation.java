package com.hospital.mes.reporting.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("mes_print_source_validation")
public class PrintSourceValidation extends ScopedEntity {
 private Long sourceRevisionId;
 public Long getSourceRevisionId(){return sourceRevisionId;} public void setSourceRevisionId(Long value){sourceRevisionId=value;}
 private Long sourceVersionNo;
 public Long getSourceVersionNo(){return sourceVersionNo;} public void setSourceVersionNo(Long value){sourceVersionNo=value;}
 private String configHash;
 public String getConfigHash(){return configHash;} public void setConfigHash(String value){configHash=value;}
 private String bindingSetHash;
 public String getBindingSetHash(){return bindingSetHash;} public void setBindingSetHash(String value){bindingSetHash=value;}
 private String publicationEvidenceHash;
 public String getPublicationEvidenceHash(){return publicationEvidenceHash;} public void setPublicationEvidenceHash(String value){publicationEvidenceHash=value;}
 private String catalogVersion;
 public String getCatalogVersion(){return catalogVersion;} public void setCatalogVersion(String value){catalogVersion=value;}
 private String typeMetadataSnapshotsJson;
 public String getTypeMetadataSnapshotsJson(){return typeMetadataSnapshotsJson;} public void setTypeMetadataSnapshotsJson(String value){typeMetadataSnapshotsJson=value;}
 private String typeResultsJson;
 public String getTypeResultsJson(){return typeResultsJson;} public void setTypeResultsJson(String value){typeResultsJson=value;}
 private String result;
 public String getResult(){return result;} public void setResult(String value){result=value;}
 private Long validatedBy;
 public Long getValidatedBy(){return validatedBy;} public void setValidatedBy(Long value){validatedBy=value;}
 private java.time.LocalDateTime validatedAt;
 public java.time.LocalDateTime getValidatedAt(){return validatedAt;} public void setValidatedAt(java.time.LocalDateTime value){validatedAt=value;}
 private java.time.LocalDateTime expiresAt;
 public java.time.LocalDateTime getExpiresAt(){return expiresAt;} public void setExpiresAt(java.time.LocalDateTime value){expiresAt=value;}
 private java.time.LocalDateTime invalidatedAt;
 public java.time.LocalDateTime getInvalidatedAt(){return invalidatedAt;} public void setInvalidatedAt(java.time.LocalDateTime value){invalidatedAt=value;}
}
