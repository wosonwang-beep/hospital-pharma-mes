package com.hospital.mes.integration.api;
import com.hospital.mes.integration.domain.IntegrationDirection;import java.time.Instant;
public record IntegrationMessageResponse(String messageRef,IntegrationDirection direction,String messageId,String sourceSystem,
 String targetSystem,String eventType,String aggregateType,String aggregateId,String status,int retryCount,Instant nextRetryAt,
 String lastErrorCode,String lastErrorMessage,Instant occurredAt,Instant completedAt,long versionNo){}
