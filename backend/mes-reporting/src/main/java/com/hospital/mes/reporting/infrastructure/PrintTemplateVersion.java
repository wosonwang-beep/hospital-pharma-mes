package com.hospital.mes.reporting.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
import com.hospital.mes.masterdata.infrastructure.ScopedEntity;
@TableName("mes_print_template_version")
public class PrintTemplateVersion extends ScopedEntity {
 private String templateCode;
 public String getTemplateCode(){return templateCode;} public void setTemplateCode(String v){templateCode=v;}
 private String templateName;
 public String getTemplateName(){return templateName;} public void setTemplateName(String v){templateName=v;}
 private Long templateRevision;
 public Long getTemplateRevision(){return templateRevision;} public void setTemplateRevision(Long v){templateRevision=v;}
 private String businessType;
 private String printType;
 public String getPrintType(){return printType;} public void setPrintType(String value){printType=value;}
 public String getBusinessType(){return businessType;} public void setBusinessType(String v){businessType=v;}
 private String status;
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 private String contentHash;
 public String getContentHash(){return contentHash;} public void setContentHash(String v){contentHash=v;}
 private byte[] docx;
 public byte[] getDocx(){return docx;} public void setDocx(byte[] v){docx=v;}
 private byte[] previewPdf;
 public byte[] getPreviewPdf(){return previewPdf;} public void setPreviewPdf(byte[] v){previewPdf=v;}
 private String previewHash;
 public String getPreviewHash(){return previewHash;} public void setPreviewHash(String v){previewHash=v;}
 private java.time.LocalDateTime publishedAt;
 public java.time.LocalDateTime getPublishedAt(){return publishedAt;} public void setPublishedAt(java.time.LocalDateTime v){publishedAt=v;}
 public java.util.List<String> allowedActions(){return java.util.List.of();}
}
