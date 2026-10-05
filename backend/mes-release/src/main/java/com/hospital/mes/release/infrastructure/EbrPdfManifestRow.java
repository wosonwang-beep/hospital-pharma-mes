package com.hospital.mes.release.infrastructure;
@com.baomidou.mybatisplus.annotation.TableName("ebr_pdf_manifest")
public class EbrPdfManifestRow extends com.hospital.mes.qms.infrastructure.IncomingRow {
 private Long mainBatchId; public Long getMainBatchId(){return mainBatchId;} public void setMainBatchId(Long v){mainBatchId=v;}
 private Integer generationVersion; public Integer getGenerationVersion(){return generationVersion;} public void setGenerationVersion(Integer v){generationVersion=v;}
 private String definitionHash; public String getDefinitionHash(){return definitionHash;} public void setDefinitionHash(String v){definitionHash=v;}
 private String recordDigest; public String getRecordDigest(){return recordDigest;} public void setRecordDigest(String v){recordDigest=v;}
 private String fileHash; public String getFileHash(){return fileHash;} public void setFileHash(String v){fileHash=v;}
 private Long fileId; public Long getFileId(){return fileId;} public void setFileId(Long v){fileId=v;}
 private Long generatedBy; public Long getGeneratedBy(){return generatedBy;} public void setGeneratedBy(Long v){generatedBy=v;}
 private java.time.LocalDateTime generatedAt; public java.time.LocalDateTime getGeneratedAt(){return generatedAt;} public void setGeneratedAt(java.time.LocalDateTime v){generatedAt=v;}
 private String archiveKind; public String getArchiveKind(){return archiveKind;} public void setArchiveKind(String v){archiveKind=v;}
 private Long releaseDecisionId; public Long getReleaseDecisionId(){return releaseDecisionId;} public void setReleaseDecisionId(Long v){releaseDecisionId=v;}
}
