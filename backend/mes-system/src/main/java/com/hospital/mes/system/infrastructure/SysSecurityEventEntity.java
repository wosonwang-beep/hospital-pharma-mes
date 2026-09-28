package com.hospital.mes.system.infrastructure;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("sys_security_event")
public class SysSecurityEventEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String eventType;
    private String outcome;
    private Long actorUserId;
    private Long targetUserId;
    private Long targetRoleId;
    private Long targetPermissionId;
    private String traceId;
    private String requestContext;
    private LocalDateTime occurredAt;

    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public String getEventType() { return eventType; }
    public void setEventType(String value) { eventType = value; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String value) { outcome = value; }
    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long value) { actorUserId = value; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long value) { targetUserId = value; }
    public Long getTargetRoleId() { return targetRoleId; }
    public void setTargetRoleId(Long value) { targetRoleId = value; }
    public Long getTargetPermissionId() { return targetPermissionId; }
    public void setTargetPermissionId(Long value) { targetPermissionId = value; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String value) { traceId = value; }
    public String getRequestContext() { return requestContext; }
    public void setRequestContext(String value) { requestContext = value; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime value) { occurredAt = value; }
}
