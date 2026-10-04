package com.hospital.mes.ebr.application;

import com.hospital.mes.audit.application.*;
import com.hospital.mes.audit.domain.*;
import com.hospital.mes.audit.signature.*;
import com.hospital.mes.ebr.domain.EbrCommands.SignatureRule;
import java.util.UUID;

/** Immutable role-at-signing evidence is bound to the exact inserted signature, never request identity. */
@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource", name="url")
public class EbrRoleEvidence {
 private final AuditApplicationService audit;
 private final AuditQueryService query;
 public EbrRoleEvidence(AuditApplicationService audit, AuditQueryService query) {this.audit=audit;this.query=query;}
 public void retain(CurrentPlatformContext context, SignatureRecord signature, SignatureRule rule) {
  if(signature.organizationId()!=context.organizationId()||signature.signerId()!=context.actorId())throw new IllegalArgumentException("Signature actor binding mismatch");
  audit.append(new AuditCommand(context.organizationId(),context.actorId(),rule.requiredRole(),
   "EBR_FORM_SIGNATURE_ROLE","EBR_SIGNATURE_POLICY",Long.toString(signature.id()),null,
   signature.recordDigest(),policy(rule),null,signature.signedAt(),UUID.randomUUID().toString(),
   context.requestId(),AuditSource.API,null));
 }
 private static String policy(SignatureRule rule) {return rule.meaning()+"|"+rule.objectScope()+"|"+rule.objectCode()+"|"+rule.sequenceNo();}
 public boolean matches(SignatureRecord signature, SignatureRule rule) {
  for(int page=0;;page++) {
   var result=query.query(signature.organizationId(),new AuditEventQuery(signature.signerId(),
    "EBR_FORM_SIGNATURE_ROLE","EBR_SIGNATURE_POLICY",Long.toString(signature.id()),AuditSource.API,null,null,null,null,page,200));
   if(result.items().stream().anyMatch(event->event.objectType().equals("EBR_SIGNATURE_POLICY")
    &&event.objectId().equals(Long.toString(signature.id()))&&rule.requiredRole().equals(event.actorRole())
    &&policy(rule).equals(event.reason())&&signature.recordDigest().equals(event.newValueDigest())))return true;
   if((page+1L)*200>=result.total())return false;
  }
 }
}
