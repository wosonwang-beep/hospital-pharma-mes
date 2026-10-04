package com.hospital.mes.ebr.application;

import com.hospital.mes.audit.signature.*;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.common.exception.*;
import com.hospital.mes.ebr.domain.EbrCommands.SignatureRule;
import com.hospital.mes.ebr.infrastructure.*;
import com.hospital.mes.masterdata.application.MasterMutation;
import java.util.*;

@org.springframework.stereotype.Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource", name="url")
public class EbrFormSignableProvider implements SignableObjectProvider {
 private final EbrRuntimeStore db;private final EbrRuntimeModel model;
 private final SignatureCanonicalizer canonical;private final EbrRoleEvidence roles;private final SignatureRepository signatures;
 public EbrFormSignableProvider(EbrRuntimeStore db,EbrRuntimeModel model,SignatureCanonicalizer canonical,EbrRoleEvidence roles,SignatureRepository signatures){this.db=db;this.model=model;this.canonical=canonical;this.roles=roles;this.signatures=signatures;}
 public String objectType(){return "EBR_FORM_INSTANCE";}
 public Set<SignatureMeaning> allowedMeanings(){return Set.of(SignatureMeaning.VERIFY,SignatureMeaning.APPROVE);}
 public SignableObject loadForSignature(long org,String id){return model.formSignable(db.store(RuntimeFormEntity.class).get(org,MasterMutation.id(id)));}
 public void validateSignable(SignatureValidationContext context,SignableObject object){
  var actor=context.platform();
  if(!actor.hasPermission("ebr:sign"))throw new PermissionException("PERMISSION_DENIED","ebr:sign required");
  var form=db.store(RuntimeFormEntity.class).get(actor.organizationId(),MasterMutation.id(object.objectId()));
  model.context(form,true);form=db.store(RuntimeFormEntity.class).lock(actor.organizationId(),form.getId());model.active(form);
  if(form.getStatus().equals("DRAFT")||!model.formSignable(form).equals(object))throw new ResourceConflictException("RECORD_CHANGED","Submit current form before signing");
  eligible(actor,context.meaning(),object,form);
 }
 /** Synchronous domain event: an audit failure rolls back the inserted signature and idempotency together. */
 @org.springframework.context.event.EventListener
 public void signatureApplied(SignatureAppliedEvent event){
  var signature=event.signature();if(!signature.objectType().equals(objectType()))return;
  var actor=event.context();var form=db.store(RuntimeFormEntity.class).get(actor.organizationId(),MasterMutation.id(signature.objectId()));
  var object=model.formSignable(form);
  if(!canonical.digest(object).equals(signature.recordDigest()))throw new ResourceConflictException("RECORD_CHANGED","Inserted signature differs from locked form evidence");
  for(var rule:eligible(actor,signature.meaning(),object,form))roles.retain(actor,signature,rule);
 }
 private List<SignatureRule> eligible(CurrentPlatformContext actor,SignatureMeaning meaning,SignableObject object,RuntimeFormEntity form){
  var policies=model.signatureRules(form);
  var matching=policies.stream().filter(rule->rule.meaning().equals(meaning.name())&&actor.roleCodes().contains(rule.requiredRole())).toList();
  if(matching.isEmpty())throw new PermissionException("PERMISSION_DENIED","Frozen signature role required");
  String digest=canonical.digest(object);var existing=signatures.findValid(actor.organizationId(),objectType(),object.objectId());
  var eligible=matching.stream().filter(rule->policies.stream()
   .filter(prior->prior.objectScope().equals(rule.objectScope())&&prior.objectCode().equals(rule.objectCode())&&prior.meaning().equals(rule.meaning())&&prior.sequenceNo()<rule.sequenceNo())
   .allMatch(prior->existing.stream().anyMatch(signature->signature.meaning().name().equals(prior.meaning())&&signature.recordDigest().equals(digest)&&roles.matches(signature,prior)))).toList();
  if(eligible.isEmpty())throw new ComplianceException("EBR_SIGNATURE_SEQUENCE","Earlier frozen signature required");
  return eligible;
 }
}
