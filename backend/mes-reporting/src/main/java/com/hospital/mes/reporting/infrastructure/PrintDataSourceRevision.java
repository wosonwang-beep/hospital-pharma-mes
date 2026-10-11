package com.hospital.mes.reporting.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("mes_print_data_source_revision")
public class PrintDataSourceRevision extends ScopedEntity {
 private Long sourceId;
 public Long getSourceId(){return sourceId;} public void setSourceId(Long value){sourceId=value;}
 private Long revisionNo;
 public Long getRevisionNo(){return revisionNo;} public void setRevisionNo(Long value){revisionNo=value;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String value){status=value;}
 private Integer schemaVersion;
 public Integer getSchemaVersion(){return schemaVersion;} public void setSchemaVersion(Integer value){schemaVersion=value;}
 private String mainCatalogId;
 public String getMainCatalogId(){return mainCatalogId;} public void setMainCatalogId(String value){mainCatalogId=value;}
 private String parameterDeclarationsJson;
 public String getParameterDeclarationsJson(){return parameterDeclarationsJson;} public void setParameterDeclarationsJson(String value){parameterDeclarationsJson=value;}
 private String configJson;
 public String getConfigJson(){return configJson;} public void setConfigJson(String value){configJson=value;}
 private String configHash;
 public String getConfigHash(){return configHash;} public void setConfigHash(String value){configHash=value;}
 private String publishedBusinessTypesJson;
 public String getPublishedBusinessTypesJson(){return publishedBusinessTypesJson;} public void setPublishedBusinessTypesJson(String value){publishedBusinessTypesJson=value;}
 private String typeMetadataSnapshotsJson;
 public String getTypeMetadataSnapshotsJson(){return typeMetadataSnapshotsJson;} public void setTypeMetadataSnapshotsJson(String value){typeMetadataSnapshotsJson=value;}
 private String publishedBindingSetHash;
 public String getPublishedBindingSetHash(){return publishedBindingSetHash;} public void setPublishedBindingSetHash(String value){publishedBindingSetHash=value;}
 private String publicationEvidenceHash;
 public String getPublicationEvidenceHash(){return publicationEvidenceHash;} public void setPublicationEvidenceHash(String value){publicationEvidenceHash=value;}
 private String publishedCatalogVersion;
 public String getPublishedCatalogVersion(){return publishedCatalogVersion;} public void setPublishedCatalogVersion(String value){publishedCatalogVersion=value;}
 private Long publishedBy;
 public Long getPublishedBy(){return publishedBy;} public void setPublishedBy(Long value){publishedBy=value;}
 private java.time.LocalDateTime publishedAt;
 public java.time.LocalDateTime getPublishedAt(){return publishedAt;} public void setPublishedAt(java.time.LocalDateTime value){publishedAt=value;}
}
