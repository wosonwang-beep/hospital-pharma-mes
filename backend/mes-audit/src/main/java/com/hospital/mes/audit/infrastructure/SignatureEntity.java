package com.hospital.mes.audit.infrastructure;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("gxp_signature")
public class SignatureEntity {
    @TableId(type=IdType.AUTO) private Long id;
    private Long orgId,createdBy,updatedBy,versionNo,signerId,revokedSignatureId;
    private LocalDateTime createdAt,updatedAt,signedAt,invalidatedAt;
    private String meaning,objectType,objectId,recordDigest,status,invalidationReason,authContextJson;
    public Long getId(){return id;}public void setId(Long v){id=v;}public Long getOrgId(){return orgId;}public void setOrgId(Long v){orgId=v;}
    public Long getCreatedBy(){return createdBy;}public void setCreatedBy(Long v){createdBy=v;}public LocalDateTime getCreatedAt(){return createdAt;}public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public Long getUpdatedBy(){return updatedBy;}public void setUpdatedBy(Long v){updatedBy=v;}public LocalDateTime getUpdatedAt(){return updatedAt;}public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
    public Long getVersionNo(){return versionNo;}public void setVersionNo(Long v){versionNo=v;}public Long getSignerId(){return signerId;}public void setSignerId(Long v){signerId=v;}
    public String getMeaning(){return meaning;}public void setMeaning(String v){meaning=v;}public String getObjectType(){return objectType;}public void setObjectType(String v){objectType=v;}
    public String getObjectId(){return objectId;}public void setObjectId(String v){objectId=v;}public String getRecordDigest(){return recordDigest;}public void setRecordDigest(String v){recordDigest=v;}
    public LocalDateTime getSignedAt(){return signedAt;}public void setSignedAt(LocalDateTime v){signedAt=v;}public String getStatus(){return status;}public void setStatus(String v){status=v;}
    public LocalDateTime getInvalidatedAt(){return invalidatedAt;}public void setInvalidatedAt(LocalDateTime v){invalidatedAt=v;}public String getInvalidationReason(){return invalidationReason;}public void setInvalidationReason(String v){invalidationReason=v;}
    public String getAuthContextJson(){return authContextJson;}public void setAuthContextJson(String v){authContextJson=v;}public Long getRevokedSignatureId(){return revokedSignatureId;}public void setRevokedSignatureId(Long v){revokedSignatureId=v;}
}
