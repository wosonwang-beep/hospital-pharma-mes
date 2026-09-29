package com.hospital.mes.integration.application;

import com.fasterxml.jackson.databind.ObjectMapper;import com.hospital.mes.audit.application.*;import com.hospital.mes.audit.domain.*;import com.hospital.mes.audit.idempotency.*;import com.hospital.mes.common.exception.*;import com.hospital.mes.integration.api.*;import com.hospital.mes.integration.domain.*;
import java.nio.charset.StandardCharsets;import java.security.MessageDigest;import java.time.Clock;import java.util.HexFormat;import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;

@Service @ConditionalOnProperty(prefix="spring.datasource",name="url")
public class IntegrationRetryApplicationService{
 private final IntegrationMessageStore store;private final PlatformIdempotencyService idempotency;private final AuditApplicationService audit;private final ObjectMapper json;private final Clock clock;
 public IntegrationRetryApplicationService(IntegrationMessageStore s,PlatformIdempotencyService i,AuditApplicationService a,ObjectMapper j,Clock c){store=s;idempotency=i;audit=a;json=j;clock=c;}
 @Transactional public IntegrationMessageResponse manualRetry(CurrentPlatformContext context,String messageRef,String reason,long expectedVersion,String key){
  if(reason==null||reason.isBlank()||reason.length()>1000)throw new ComplianceException("RETRY_REASON_REQUIRED","Retry reason is required");
  IntegrationMessageRef ref=IntegrationMessageRef.parse(messageRef);var decision=idempotency.begin(new IdempotencyCommand(context.organizationId(),context.actorId(),"INTEGRATION_MANUAL_RETRY",key,messageRef+"|"+expectedVersion+"|"+reason));
  if(decision.type()==IdempotencyDecisionType.REPLAY)return decode(decision.responseJson());if(decision.type()==IdempotencyDecisionType.CONFLICT)throw new ResourceConflictException("IDEMPOTENCY_KEY_REUSED","Idempotency key was reused");if(decision.type()==IdempotencyDecisionType.IN_PROGRESS_CONFLICT)throw new ResourceConflictException("IDEMPOTENCY_IN_PROGRESS","Operation is in progress");
  IntegrationMessageResponse response;String oldState,newState;
  if(ref.direction()==IntegrationDirection.INBOX){InboxMessage old=store.findInbox(context.organizationId(),ref.id());if(old.versionNo()!=expectedVersion)changed();InboxMessage next=old.manualRetry();if(!store.saveInbox(next,old.versionNo()))changed();response=view(next);oldState=old.status()+"|"+old.versionNo();newState=next.status()+"|"+next.versionNo();}
  else{OutboxMessage old=store.findOutbox(context.organizationId(),ref.id());if(old.versionNo()!=expectedVersion)changed();OutboxMessage next=old.manualRetry();if(!store.saveOutbox(next,old.versionNo()))changed();response=view(next);oldState=old.status()+"|"+old.versionNo();newState=next.status()+"|"+next.versionNo();}
  var now=clock.instant();audit.append(new AuditCommand(context.organizationId(),context.actorId(),context.roleSnapshot(),"INTEGRATION_MESSAGE_RETRY_REQUESTED","INTEGRATION_MESSAGE",messageRef,sha(oldState),sha(newState),reason,null,now,context.requestId()==null?java.util.UUID.randomUUID().toString():context.requestId(),context.requestId(),AuditSource.API,key));
  String body=encode(response);idempotency.complete(decision.handle(),200,body,"INTEGRATION_MESSAGE",messageRef);return response;
 }
 private static void changed(){throw new ResourceConflictException("INTEGRATION_MESSAGE_CHANGED","Integration message changed");}
 private String encode(IntegrationMessageResponse r){try{return json.writeValueAsString(r);}catch(Exception e){throw new IllegalStateException(e);}}
 private IntegrationMessageResponse decode(String s){try{return json.readValue(s,IntegrationMessageResponse.class);}catch(Exception e){throw new IllegalStateException("Invalid replay",e);}}
 private static IntegrationMessageResponse view(InboxMessage m){return new IntegrationMessageResponse("INBOX:"+m.id(),IntegrationDirection.INBOX,m.messageId(),m.sourceSystem(),null,null,null,null,m.status().name(),m.retryCount(),m.nextRetryAt(),m.lastErrorCode(),m.lastErrorMessage(),m.receivedAt(),m.processedAt(),m.versionNo());}
 private static IntegrationMessageResponse view(OutboxMessage m){return new IntegrationMessageResponse("OUTBOX:"+m.id(),IntegrationDirection.OUTBOX,m.messageId(),null,m.targetSystem(),m.eventType(),m.aggregateType(),m.aggregateId(),m.status().name(),m.retryCount(),m.nextRetryAt(),m.lastErrorCode(),m.lastErrorMessage(),m.createdAt(),m.publishedAt(),m.versionNo());}
 private static String sha(String s){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
