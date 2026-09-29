package com.hospital.mes.integration.domain;

import com.hospital.mes.common.exception.StateTransitionException;
import com.hospital.mes.integration.policy.IntegrationRetryPolicy;
import java.time.Instant;

public record InboxMessage(long id,long organizationId,long actorId,String sourceSystem,String messageId,
    String payloadJson,InboxStatus status,Instant receivedAt,Instant processedAt,int retryCount,
    Instant nextRetryAt,String lastErrorCode,String lastErrorMessage,long versionNo,Instant claimedAt){
    public static InboxMessage received(long org,long actor,String source,String messageId,String payload,Instant now){
        return new InboxMessage(0,org,actor,source,messageId,payload,InboxStatus.RECEIVED,now,null,0,null,null,null,0,null);}
    public InboxMessage claim(Instant now){
        if(status!=InboxStatus.RECEIVED&&!(status==InboxStatus.RETRY_WAIT&&(nextRetryAt==null||!now.isBefore(nextRetryAt))))invalid();
        return copy(InboxStatus.PROCESSING,processedAt,retryCount,nextRetryAt,lastErrorCode,lastErrorMessage,versionNo+1,now);}
    public InboxMessage processed(Instant now){if(status!=InboxStatus.PROCESSING)invalid();return copy(InboxStatus.PROCESSED,now,retryCount,null,lastErrorCode,lastErrorMessage,versionNo+1,null);}
    public InboxMessage fail(String code,String message,Instant now,IntegrationRetryPolicy policy){
        if(status!=InboxStatus.PROCESSING)invalid();int count=retryCount+1;
        if(count>=policy.maximumAttempts())return copy(InboxStatus.DEAD_LETTER,processedAt,count,null,code,message,versionNo+1,null);
        return copy(InboxStatus.RETRY_WAIT,processedAt,count,now.plus(policy.delayAfterFailure(count)),code,message,versionNo+1,null);}
    public InboxMessage manualRetry(){if(status!=InboxStatus.RETRY_WAIT&&status!=InboxStatus.DEAD_LETTER)invalid();return copy(InboxStatus.RECEIVED,processedAt,retryCount,null,lastErrorCode,lastErrorMessage,versionNo+1,null);}
    private InboxMessage copy(InboxStatus s,Instant processed,int retry,Instant next,String code,String error,long version,Instant claimed){return new InboxMessage(id,organizationId,actorId,sourceSystem,messageId,payloadJson,s,receivedAt,processed,retry,next,code,error,version,claimed);}
    private static void invalid(){throw new StateTransitionException("INTEGRATION_STATE_INVALID","Integration inbox transition is not allowed");}
}
