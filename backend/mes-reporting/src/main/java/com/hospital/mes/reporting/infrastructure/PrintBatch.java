package com.hospital.mes.reporting.infrastructure;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("mes_print_batch")
public class PrintBatch extends ScopedEntity {
 private String businessType,printType,recordIdsJson,sourceArtifactIdsJson,snapshotJson,snapshotHash,pdfHash;
 private Long templateVersionId;private Boolean formal;private byte[] pdf;
 public String getBusinessType(){return businessType;} public void setBusinessType(String v){businessType=v;}
 public String getPrintType(){return printType;} public void setPrintType(String v){printType=v;}
 public String getRecordIdsJson(){return recordIdsJson;} public void setRecordIdsJson(String v){recordIdsJson=v;}
 public String getSourceArtifactIdsJson(){return sourceArtifactIdsJson;} public void setSourceArtifactIdsJson(String v){sourceArtifactIdsJson=v;}
 public String getSnapshotJson(){return snapshotJson;} public void setSnapshotJson(String v){snapshotJson=v;}
 public String getSnapshotHash(){return snapshotHash;} public void setSnapshotHash(String v){snapshotHash=v;}
 public String getPdfHash(){return pdfHash;} public void setPdfHash(String v){pdfHash=v;}
 public Long getTemplateVersionId(){return templateVersionId;} public void setTemplateVersionId(Long v){templateVersionId=v;}
 public Boolean getFormal(){return formal;} public void setFormal(Boolean v){formal=v;}
 public byte[] getPdf(){return pdf;} public void setPdf(byte[] v){pdf=v;}
 public java.util.List<String> allowedActions(){return java.util.List.of();}
}
