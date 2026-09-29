package com.hospital.mes.audit.infrastructure;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("platform_idempotency_record")
public class IdempotencyEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private Long orgId; private Long createdBy; private LocalDateTime createdAt;
    private Long updatedBy; private LocalDateTime updatedAt; private Long versionNo;
    private Long actorId; private String operationCode; private String idempotencyKey;
    private String requestDigest; private String state; private Integer httpStatus;
    private String responseJson; private String resourceType; private String resourceId;
    private LocalDateTime expiresAt;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getOrgId(){return orgId;} public void setOrgId(Long v){orgId=v;}
    public Long getCreatedBy(){return createdBy;} public void setCreatedBy(Long v){createdBy=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public Long getUpdatedBy(){return updatedBy;} public void setUpdatedBy(Long v){updatedBy=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
    public Long getVersionNo(){return versionNo;} public void setVersionNo(Long v){versionNo=v;}
    public Long getActorId(){return actorId;} public void setActorId(Long v){actorId=v;}
    public String getOperationCode(){return operationCode;} public void setOperationCode(String v){operationCode=v;}
    public String getIdempotencyKey(){return idempotencyKey;} public void setIdempotencyKey(String v){idempotencyKey=v;}
    public String getRequestDigest(){return requestDigest;} public void setRequestDigest(String v){requestDigest=v;}
    public String getState(){return state;} public void setState(String v){state=v;}
    public Integer getHttpStatus(){return httpStatus;} public void setHttpStatus(Integer v){httpStatus=v;}
    public String getResponseJson(){return responseJson;} public void setResponseJson(String v){responseJson=v;}
    public String getResourceType(){return resourceType;} public void setResourceType(String v){resourceType=v;}
    public String getResourceId(){return resourceId;} public void setResourceId(String v){resourceId=v;}
    public LocalDateTime getExpiresAt(){return expiresAt;} public void setExpiresAt(LocalDateTime v){expiresAt=v;}
}
