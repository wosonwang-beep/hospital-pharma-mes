package com.hospital.mes.audit.attachment.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("gxp_attachment")
public class AttachmentEntity {
 @TableId(type=IdType.AUTO)
 private Long id;
 public Long getId(){return id;} public void setId(Long value){id=value;}
 private Long orgId;
 public Long getOrgId(){return orgId;} public void setOrgId(Long value){orgId=value;}
 private Long uploadedBy;
 public Long getUploadedBy(){return uploadedBy;} public void setUploadedBy(Long value){uploadedBy=value;}
 private java.time.LocalDateTime uploadedAt;
 public java.time.LocalDateTime getUploadedAt(){return uploadedAt;} public void setUploadedAt(java.time.LocalDateTime value){uploadedAt=value;}
 private String fileName;
 public String getFileName(){return fileName;} public void setFileName(String value){fileName=value;}
 private String mediaType;
 public String getMediaType(){return mediaType;} public void setMediaType(String value){mediaType=value;}
 private Long byteLength;
 public Long getByteLength(){return byteLength;} public void setByteLength(Long value){byteLength=value;}
 private String sha256;
 public String getSha256(){return sha256;} public void setSha256(String value){sha256=value;}
 @TableField(select=false)
 private byte[] content;
 public byte[] getContent(){return content;} public void setContent(byte[] value){content=value;}
 private Long recordVersion;
 public Long getRecordVersion(){return recordVersion;} public void setRecordVersion(Long value){recordVersion=value;}
 private String retentionStatus;
 public String getRetentionStatus(){return retentionStatus;} public void setRetentionStatus(String value){retentionStatus=value;}
}
