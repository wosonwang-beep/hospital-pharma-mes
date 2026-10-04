package com.hospital.mes.production.application;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.mes.masterdata.application.MasterMutation;
import com.hospital.mes.common.exception.ComplianceException;
import org.springframework.boot.context.properties.bind.*;
import org.springframework.core.env.Environment;
import java.math.BigDecimal;
import java.util.*;

/** Controlled deployment producer. Released batches consume its immutable snapshot only. */
@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class ProductionMaterialPolicyService {
 private final Environment environment;private final MasterMutation mutations;private final ObjectMapper json;
 public ProductionMaterialPolicyService(Environment environment,MasterMutation mutations,ObjectMapper json){this.environment=environment;this.mutations=mutations;this.json=json;}
 public record Mapping(String organizationId,String materialId,Boolean required,String precision,String tolerancePct,String policyVersion){}
 public ObjectNode requirePolicy(long organizationId,long materialId,long baseUnitId){
  try{
   var mappings=Binder.get(environment).bind("mes.production.material-weighing-policies",Bindable.listOf(Mapping.class)).orElse(List.of());var identities=new HashSet<String>();Mapping selected=null;
   for(var m:mappings){positiveId(m.organizationId());positiveId(m.materialId());if(!identities.add(m.organizationId()+":"+m.materialId()))throw new IllegalArgumentException("Duplicate deployment identity");validate(m);if(m.organizationId().equals(Long.toString(organizationId))&&m.materialId().equals(Long.toString(materialId)))selected=m;}
   if(selected==null)throw new IllegalArgumentException("No exact deployment binding");var out=json.createObjectNode();out.put("organizationId",Long.toString(organizationId));out.put("materialId",Long.toString(materialId));out.put("baseUnitId",Long.toString(baseUnitId));out.put("required",selected.required());out.put("precision",decimal(selected.precision(),18,true));out.put("tolerancePct",decimal(selected.tolerancePct(),9,false));out.put("policyVersion",selected.policyVersion());out.put("configurationHash",mutations.digest(out));return out;
  }catch(RuntimeException ex){throw failure(materialId,"Invalid or missing deployment weighing policy");}
 }
 public JsonNode requireFrozen(JsonNode policy,long organizationId,long materialId,long baseUnitId){
  try{
   if(!policy.isObject()||policy.size()!=8||!policy.path("required").isBoolean())throw new IllegalArgumentException();
   for(String name:List.of("organizationId","materialId","baseUnitId","precision","tolerancePct","policyVersion","configurationHash"))if(!policy.path(name).isTextual())throw new IllegalArgumentException();
   if(!policy.path("organizationId").asText().equals(Long.toString(organizationId))||!policy.path("materialId").asText().equals(Long.toString(materialId))||!policy.path("baseUnitId").asText().equals(Long.toString(baseUnitId)))throw new IllegalArgumentException();
   validate(new Mapping(policy.path("organizationId").asText(),policy.path("materialId").asText(),policy.path("required").booleanValue(),policy.path("precision").asText(),policy.path("tolerancePct").asText(),policy.path("policyVersion").asText()));
   var payload=(ObjectNode)policy.deepCopy();String expected=payload.remove("configurationHash").asText();if(!mutations.digest(payload).equals(expected))throw new IllegalArgumentException();return policy.deepCopy();
  }catch(RuntimeException ex){throw failure(materialId,"Frozen weighing policy missing, malformed or tampered");}
 }
 private static void validate(Mapping m){if(m.required()==null||m.policyVersion()==null||m.policyVersion().isBlank()||m.policyVersion().length()>80)throw new IllegalArgumentException();decimal(m.precision(),18,true);decimal(m.tolerancePct(),9,false);}
 private static void positiveId(String value){if(value==null||!value.matches("[1-9][0-9]*")||Long.parseLong(value)<=0)throw new IllegalArgumentException();}
 private static String decimal(String value,int precision,boolean positive){if(value==null||!value.matches("[0-9]+(\\.[0-9]{1,6})?"))throw new IllegalArgumentException();var n=new BigDecimal(value);if(n.precision()-n.scale()>precision-6||n.signum()<0||positive&&n.signum()==0)throw new IllegalArgumentException();return n.stripTrailingZeros().toPlainString();}
 private static ComplianceException failure(long materialId,String message){return new ComplianceException("WEIGHING_POLICY_REQUIRED",message+" for material "+materialId);}
}
