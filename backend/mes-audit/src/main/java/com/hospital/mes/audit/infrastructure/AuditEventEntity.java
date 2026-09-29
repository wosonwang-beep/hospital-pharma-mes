package com.hospital.mes.audit.infrastructure;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("gxp_audit_event")
public class AuditEventEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private Long orgId;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long actorId;
    private String actorRole;
    private String action;
    private String objectType;
    private String objectId;
    private String oldValueDigest;
    private String newValueDigest;
    private String reason;
    private String clientInfo;
    private LocalDateTime occurredAt;
    private String transactionId;
    private String requestId;
    private String source;
    private String idempotencyKey;

    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public Long getOrgId() { return orgId; } public void setOrgId(Long v) { orgId = v; }
    public Long getCreatedBy() { return createdBy; } public void setCreatedBy(Long v) { createdBy = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
    public Long getActorId() { return actorId; } public void setActorId(Long v) { actorId = v; }
    public String getActorRole() { return actorRole; } public void setActorRole(String v) { actorRole = v; }
    public String getAction() { return action; } public void setAction(String v) { action = v; }
    public String getObjectType() { return objectType; } public void setObjectType(String v) { objectType = v; }
    public String getObjectId() { return objectId; } public void setObjectId(String v) { objectId = v; }
    public String getOldValueDigest() { return oldValueDigest; } public void setOldValueDigest(String v) { oldValueDigest = v; }
    public String getNewValueDigest() { return newValueDigest; } public void setNewValueDigest(String v) { newValueDigest = v; }
    public String getReason() { return reason; } public void setReason(String v) { reason = v; }
    public String getClientInfo() { return clientInfo; } public void setClientInfo(String v) { clientInfo = v; }
    public LocalDateTime getOccurredAt() { return occurredAt; } public void setOccurredAt(LocalDateTime v) { occurredAt = v; }
    public String getTransactionId() { return transactionId; } public void setTransactionId(String v) { transactionId = v; }
    public String getRequestId() { return requestId; } public void setRequestId(String v) { requestId = v; }
    public String getSource() { return source; } public void setSource(String v) { source = v; }
    public String getIdempotencyKey() { return idempotencyKey; } public void setIdempotencyKey(String v) { idempotencyKey = v; }
}
