package com.hospital.mes.masterdata.application;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.audit.application.CurrentPlatformContext;
import com.hospital.mes.audit.signature.*;
import com.hospital.mes.common.exception.*;
import java.util.*;
/** Business command adapter to the existing platform signing service. No new signature storage. */
@org.springframework.stereotype.Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class SignedRecordSupport {
 public record Evidence(long id,String envelope){}
 private record Intent(CurrentPlatformContext context,SignatureMeaning meaning,SignableObject object){}
 private static final ThreadLocal<Intent> ACTIVE=new ThreadLocal<>();
 private final SignatureApplicationService signatures;private final SignatureTransactionService checks;private final ObjectMapper json;private final MasterMutation mutations;
 public SignedRecordSupport(SignatureApplicationService signatures,SignatureTransactionService checks,ObjectMapper json,MasterMutation mutations){this.signatures=signatures;this.checks=checks;this.json=json;this.mutations=mutations;}
 public Evidence sign(CurrentPlatformContext c,String type,String target,long version,SignatureMeaning meaning,Object record,JsonNode signature,String key,List<String> refs){
  if(!c.hasPermission("ebr:sign"))throw new PermissionException("PERMISSION_DENIED","ebr:sign required");
  if(signature==null||!signature.isObject()||signature.size()!=1||!signature.path("reauthToken").isTextual()||signature.path("reauthToken").asText().isBlank())throw new IllegalArgumentException("Signature reauthentication required");
  ObjectNode canonical=json.valueToTree(record);canonical.remove(List.of("signatureId","signatureEvidenceJson","allowedActions"));canonical.put("meaning",meaning.name());
  // The platform signs exact decimal strings. valueToTree may otherwise create DoubleNode.
  var bean=new org.springframework.beans.BeanWrapperImpl(record);for(var property:bean.getPropertyDescriptors()){String name=property.getName();if(canonical.hasNonNull(name)&&bean.getPropertyValue(name) instanceof java.math.BigDecimal decimal)canonical.put(name,decimal.toPlainString());}
  var object=new SignableObject(type,target,version,canonical,refs);
  if(ACTIVE.get()!=null)throw new IllegalStateException("Nested signing intent");ACTIVE.set(new Intent(c,meaning,object));
  try{var result=signatures.sign(new SignCommand(c,type,target,meaning,version,signature.path("reauthToken").asText(),mutations.digest(Map.of("key",key,"type",type,"target",target)),null));return new Evidence(Long.parseLong(result.id()),json.writeValueAsString(object));}
  catch(java.io.IOException ex){throw new IllegalStateException(ex);}finally{ACTIVE.remove();}
 }
 public boolean valid(long org,Long id){return id!=null&&checks.verify(org,id);}
 /** Detached API projection only. Never normalize a canonical signing envelope. */
 public static ObjectNode publicView(com.hospital.mes.masterdata.infrastructure.ScopedEntity record,ObjectMapper mapper){var n=(ObjectNode)mapper.valueToTree(record);var bean=new org.springframework.beans.BeanWrapperImpl(record);for(var descriptor:bean.getPropertyDescriptors()){String name=descriptor.getName();if(!n.hasNonNull(name))continue;Object value=bean.getPropertyValue(name);if(value instanceof Long&&(name.equals("id")||name.endsWith("Id")||name.endsWith("By")))n.put(name,value.toString());else if(value instanceof java.math.BigDecimal decimal)n.put(name,decimal.toPlainString());else if(value instanceof java.time.LocalDateTime at)n.put(name,at.toInstant(java.time.ZoneOffset.UTC).toString());}n.remove("signatureEvidenceJson");return n;}
 public static SignableObjectProvider provider(String type,java.util.function.BiFunction<Long,String,List<String>> envelopes){return new SignableObjectProvider(){
  public String objectType(){return type;}public Set<SignatureMeaning> allowedMeanings(){return Set.of(SignatureMeaning.VERIFY,SignatureMeaning.APPROVE,SignatureMeaning.REJECT);}
  public SignableObject loadForSignature(long org,String id){var i=ACTIVE.get();if(i!=null&&i.context.organizationId()==org&&i.object.objectType().equals(type)&&i.object.objectId().equals(id))return i.object;
   for(String envelope:envelopes.apply(org,id)){if(envelope==null)continue;try{var object=new ObjectMapper().findAndRegisterModules().readValue(envelope,SignableObject.class);if(object.objectType().equals(type)&&object.objectId().equals(id))return object;}catch(java.io.IOException ex){throw new IllegalStateException("Invalid signature evidence",ex);}}throw new NoSuchElementException("Signature object not found");}
  public void validateSignable(SignatureValidationContext c,SignableObject object){var i=ACTIVE.get();if(i==null||i.context.organizationId()!=c.platform().organizationId()||i.context.actorId()!=c.platform().actorId()||i.meaning!=c.meaning()||!i.object.equals(object)||!c.platform().hasPermission("ebr:sign"))throw new ComplianceException("SIGNATURE_INTENT_REQUIRED","Use the controlled business command");}
 };}
 public static long expected(JsonNode body,String header){if(header==null||header.isBlank())throw new IllegalArgumentException("If-Match required");if(!body.path("versionNo").isIntegralNumber())throw new IllegalArgumentException("Integer versionNo required");return MasterMutation.version(header,body.get("versionNo").longValue());}
}
