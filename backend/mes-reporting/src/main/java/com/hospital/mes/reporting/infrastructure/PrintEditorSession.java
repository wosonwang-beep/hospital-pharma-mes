package com.hospital.mes.reporting.infrastructure;
import com.baomidou.mybatisplus.annotation.*;
@TableName("mes_print_editor_session") public class PrintEditorSession {
 @TableId(type=IdType.INPUT)
 private String id; public String getId(){return id;} public void setId(String v){id=v;}
 private Long orgId; public Long getOrgId(){return orgId;} public void setOrgId(Long v){orgId=v;}
 private Long actorId; public Long getActorId(){return actorId;} public void setActorId(Long v){actorId=v;}
 private String loginSessionId; public String getLoginSessionId(){return loginSessionId;} public void setLoginSessionId(String v){loginSessionId=v;}
 private Long sourceTemplateVersionId; public Long getSourceTemplateVersionId(){return sourceTemplateVersionId;} public void setSourceTemplateVersionId(Long v){sourceTemplateVersionId=v;}
 private Long savedTemplateVersionId; public Long getSavedTemplateVersionId(){return savedTemplateVersionId;} public void setSavedTemplateVersionId(Long v){savedTemplateVersionId=v;}
 private String lastContentHash; public String getLastContentHash(){return lastContentHash;} public void setLastContentHash(String v){lastContentHash=v;}
 private java.time.LocalDateTime expiresAt; public java.time.LocalDateTime getExpiresAt(){return expiresAt;} public void setExpiresAt(java.time.LocalDateTime v){expiresAt=v;}
 private Long versionNo; public Long getVersionNo(){return versionNo;} public void setVersionNo(Long v){versionNo=v;}
}
