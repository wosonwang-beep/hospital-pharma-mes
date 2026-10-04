package com.hospital.mes.execution.application;
import com.fasterxml.jackson.databind.*;
import com.hospital.mes.audit.signature.*;
import com.hospital.mes.execution.infrastructure.ExecutionStore;
import com.hospital.mes.common.exception.*;
import java.util.*;
@org.springframework.stereotype.Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class WeighingSignableProvider implements SignableObjectProvider {
 private final ExecutionStore db;private final ObjectMapper json;private final SignatureRepository signatures;private final SignatureCanonicalizer canonicalizer;private final ThreadLocal<Intent> pending=new ThreadLocal<>();
 private record Intent(long organizationId,long actorId,SignableObject object){}
 public WeighingSignableProvider(ExecutionStore db,ObjectMapper json,SignatureRepository signatures,SignatureCanonicalizer canonicalizer){this.db=db;this.json=json;this.signatures=signatures;this.canonicalizer=canonicalizer;}
 public String objectType(){return "WEIGHING_VERIFICATION";} public Set<SignatureMeaning> allowedMeanings(){return Set.of(SignatureMeaning.VERIFY);}
 public <T> T withIntent(long org,long actor,SignableObject object,java.util.function.Supplier<T> action){if(pending.get()!=null)throw new IllegalStateException("Nested signing intent forbidden");pending.set(new Intent(org,actor,object));try{return action.get();}finally{pending.remove();}}
 public SignableObject loadForSignature(long org,String objectId){var intent=pending.get();if(intent!=null&&intent.organizationId()==org&&intent.object().objectId().equals(objectId))return intent.object();var parts=objectId.split(":");if(parts.length!=3||!parts[1].equals("VERIFY"))throw new IllegalArgumentException("Invalid weighing verification identity");long id=com.hospital.mes.masterdata.application.MasterMutation.id(parts[0]);var row=db.weighings.get(org,id);try{for(var e:json.readTree(row.getSignatureEvidenceJson()))if(e.path("objectType").asText().equals(objectType())&&e.path("objectId").asText().equals(objectId)){var evidence=new ArrayList<String>();e.path("evidenceIds").forEach(v->evidence.add(v.asText()));var object=new SignableObject(objectType(),objectId,e.path("recordVersion").longValue(),e.path("canonicalRecord").deepCopy(),evidence);var signature=signatures.find(org,Long.parseLong(e.path("signatureId").asText()));if(!signature.objectType().equals(objectType())||!signature.objectId().equals(objectId)||!signature.recordDigest().equals(canonicalizer.digest(object)))throw new ComplianceException("SIGNATURE_EVIDENCE_INVALID","Signing envelope differs from linked signature");return object;}}catch(java.io.IOException ex){throw new IllegalStateException(ex);}throw new NoSuchElementException("Signing envelope not found");}
 public void validateSignable(SignatureValidationContext context,SignableObject object){var p=context.platform();var intent=pending.get();if(intent==null||intent.organizationId()!=p.organizationId()||intent.actorId()!=p.actorId()||!intent.object().objectId().equals(object.objectId()))throw new ComplianceException("SIGNING_INTENT_REQUIRED","Use the controlled weighing verification command");if(!p.hasPermission("mes:weigh:verify")||!p.hasPermission("ebr:sign"))throw new PermissionException("PERMISSION_DENIED","Verification and signing permissions required");}
}
