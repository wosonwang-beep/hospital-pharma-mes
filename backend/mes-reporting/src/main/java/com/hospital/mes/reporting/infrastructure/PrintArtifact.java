package com.hospital.mes.reporting.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("mes_print_artifact")
public class PrintArtifact extends ScopedEntity {
 private String businessType;
 public String getBusinessType(){return businessType;} public void setBusinessType(String v){businessType=v;}
 private String businessId;
 public String getBusinessId(){return businessId;} public void setBusinessId(String v){businessId=v;}
 private String businessVersion;
 public String getBusinessVersion(){return businessVersion;} public void setBusinessVersion(String v){businessVersion=v;}
 private String reportNo;
 public String getReportNo(){return reportNo;} public void setReportNo(String v){reportNo=v;}
 private Long templateVersionId;
 public Long getTemplateVersionId(){return templateVersionId;} public void setTemplateVersionId(Long v){templateVersionId=v;}
 private Long templateRevision;
 public Long getTemplateRevision(){return templateRevision;} public void setTemplateRevision(Long v){templateRevision=v;}
 private Boolean formal;
 public Boolean getFormal(){return formal;} public void setFormal(Boolean v){formal=v;}
 private String snapshotJson;
 public String getSnapshotJson(){return snapshotJson;} public void setSnapshotJson(String v){snapshotJson=v;}
 private String snapshotHash;
 public String getSnapshotHash(){return snapshotHash;} public void setSnapshotHash(String v){snapshotHash=v;}
 private String pdfHash;
 public String getPdfHash(){return pdfHash;} public void setPdfHash(String v){pdfHash=v;}
 private byte[] docx;
 public byte[] getDocx(){return docx;} public void setDocx(byte[] v){docx=v;}
 private byte[] pdf;
 public byte[] getPdf(){return pdf;} public void setPdf(byte[] v){pdf=v;}
 private String formalKey;
 public String getFormalKey(){return formalKey;} public void setFormalKey(String v){formalKey=v;}
 public java.util.List<String> allowedActions(){return java.util.List.of();}
}
