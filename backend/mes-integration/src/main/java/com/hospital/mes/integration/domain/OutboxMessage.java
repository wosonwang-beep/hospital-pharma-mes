package com.hospital.mes.integration.domain;

import com.hospital.mes.common.exception.StateTransitionException;
import com.hospital.mes.integration.policy.IntegrationRetryPolicy;
import java.time.Instant;

public record OutboxMessage(long id,long organizationId,long actorId,String messageId,String targetSystem,String eventType,
    String aggregateType,String aggregateId,String payloadJson,OutboxStatus status,int retryCount,Instant nextRetryAt,
    Instant publishedAt,String lastErrorCode,String lastErrorMessage,Instant createdAt,long versionNo,Instant claimedAt){
    public static OutboxMessage pending(long org,long actor,String messageId,String target,String eventType,String aggregateType,String aggregateId,String payload,Instant now){
        return new OutboxMessage(0,org,actor,messageId,target,eventType,aggregateType,aggregateId,payload,OutboxStatus.PENDING,0,null,null,null,null,now,0,null);}
    public OutboxMessage claim(Instant now){if(status!=OutboxStatus.PENDING&&!(status==OutboxStatus.RETRY_WAIT&&(nextRetryAt==null||!now.isBefore(nextRetryAt))))invalid();return copy(OutboxStatus.DISPATCHING,retryCount,nextRetryAt,publishedAt,lastErrorCode,lastErrorMessage,versionNo+1,now);}
    public OutboxMessage published(Instant now){if(status!=OutboxStatus.DISPATCHING)invalid();return copy(OutboxStatus.PUBLISHED,retryCount,null,now,lastErrorCode,lastErrorMessage,versionNo+1,null);}
    public OutboxMessage fail(String code,String message,Instant now,IntegrationRetryPolicy policy){if(status!=OutboxStatus.DISPATCHING)invalid();int count=retryCount+1;
        if(count>=policy.maximumAttempts())return copy(OutboxStatus.DEAD_LETTER,count,null,publishedAt,code,message,versionNo+1,null);
        return copy(OutboxStatus.RETRY_WAIT,count,now.plus(policy.delayAfterFailure(count)),publishedAt,code,message,versionNo+1,null);}
    public OutboxMessage recoverAbandoned(Instant now, IntegrationRetryPolicy policy) {
        if (status != OutboxStatus.DISPATCHING || claimedAt == null
            || now.isBefore(claimedAt.plus(policy.claimTimeout()))) {
            invalid();
        }
        return fail("CLAIM_ABANDONED", "Dispatch claim exceeded the controlled timeout", now, policy);
    }
    public OutboxMessage manualRetry(){if(status!=OutboxStatus.RETRY_WAIT&&status!=OutboxStatus.DEAD_LETTER)invalid();return copy(OutboxStatus.PENDING,retryCount,null,publishedAt,lastErrorCode,lastErrorMessage,versionNo+1,null);}
    private OutboxMessage copy(OutboxStatus s,int retry,Instant next,Instant published,String code,String error,long version,Instant claimed){return new OutboxMessage(id,organizationId,actorId,messageId,targetSystem,eventType,aggregateType,aggregateId,payloadJson,s,retry,next,published,code,error,createdAt,version,claimed);}
    private static void invalid(){throw new StateTransitionException("INTEGRATION_STATE_INVALID","Integration outbox transition is not allowed");}
}
